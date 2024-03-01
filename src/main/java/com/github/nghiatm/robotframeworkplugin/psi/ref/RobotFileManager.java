package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotProjectData;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ProjectScope;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.stubs.PyModuleNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This handles finding Robot files or python classes/files.
 *
 * @author mrubino
 * @since 2014-06-28
 */
public class RobotFileManager {
//    put CACHE into RobotProjectData, avoid project disposed exception when switch between project
//    private static final Map<String, PsiElement> FILE_CACHE = new HashMap<String, PsiElement>();
//    private static final MultiMap<PsiElement, String> FILE_NAMES = MultiMap.createSet();

    private RobotFileManager() {
    }

    @Nullable
    private static synchronized PsiElement getFromCache(@NotNull String value, @NotNull Project project) {
        return RobotProjectData.getInstance(project).getFromCache(value);
    }

    private static synchronized void addToCache(@Nullable PsiElement element, @NotNull String value) {
        if (element != null && !element.getProject().isDisposed()) {
            RobotProjectData.getInstance(element.getProject()).addToCache(element, value);
        }
    }

    @Nullable
    public static PsiElement findRobot(@Nullable String resource, @NotNull Project project,
                                       @NotNull PsiElement originalElement) {
        LogUtil.debug("Start findRobot: "+ resource, "RobotFileManager", "findRobot", project);
        if (resource == null) {
            return null;
        }
        resource = replacePredefinedVariables(resource, project);
        String[] file = getFilename(resource, "");
        String path = file[0];
            if(!path.startsWith("/") && !path.startsWith(".")){
                path="./"+path;
        }
        if (path.contains("./")) {
            // contains a relative path
            VirtualFile workingDir = originalElement.getContainingFile().getOriginalFile().getVirtualFile().getParent();
            if (workingDir == null) workingDir = originalElement.getContainingFile().getVirtualFile().getParent();
            if (workingDir != null) {
                VirtualFile relativePath = workingDir.findFileByRelativePath(path);
                if (relativePath != null && relativePath.isDirectory() && relativePath.getCanonicalPath() != null) {
                    debug(resource, "changing relative path to: " + relativePath.getCanonicalPath(), project);
                    path = relativePath.getCanonicalPath();
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                }
            }
        }

        PsiElement result = getFromCache(path+file[1], project);
            if (result != null) {
            LogUtil.debug("Found ["+resource+"] in cache: "+ result.getContainingFile().getOriginalFile().getVirtualFile().getCanonicalPath(), "RobotFileManager", "findRobot", project);
            return result;
        }

        debug(resource, "Attempting global search (keywords)", project);
        result = findGlobalFile(resource, path, file[1], project, originalElement);
            if(result!= null){
            LogUtil.debug("Found: "+ result, "RobotFileManager", "findRobot", project);
            addToCache(result, result.getContainingFile().getOriginalFile().getVirtualFile().getCanonicalPath());
        }
        return result;
    }

    private static String replacePredefinedVariables(String target, Project project){
        for (Map.Entry<String, String> entry : RobotOptionsProvider.getInstance(project).getReplacementVariables().entrySet()) {
            target = target.replaceAll("([$@%&]\\{"+entry.getKey()+"+)}", entry.getValue());
        }
        return target;
    }

    @Nullable
    public static PsiElement findOtherFiles(@Nullable String library, @NotNull Project project,
                                        @NotNull PsiElement originalElement) {
        LogUtil.debug("Start findOtherFiles: "+ library, "RobotFileManager", "findOtherFiles", project);
        if (library == null) {
            return null;
        }
        library = replacePredefinedVariables(library, project);

        PsiElement result = getFromCache(library, project);
        if (result != null) {
            LogUtil.debug("Found from cached: "+ result, "RobotFileManager", "findPython", project);
            return result;
        }

        String[] file = getFilename(library, "");
        // search project scope
        debug(library, "Attempting project search (python)", project);
        result = findProjectFile(library, file[0], file[1], project, originalElement);
        if (result != null) {
            addToCache(result, library);
            return result;
        }
        // search global scope... this can get messy
        debug(library, "Attempting global search (python)", project);
        result = findGlobalFile(library, file[0], file[1], project, originalElement);
        if (result != null) {
            addToCache(result, library);
            return result;
        }
        return null;
    }

    @Nullable
    public static PsiElement findPython(@Nullable String library, @NotNull Project project,
                                        @NotNull PsiElement originalElement) {
        LogUtil.debug("Start findPython: "+ library, "RobotFileManager", "findPython", project);
        if (library == null) {
            return null;
        }
        library = replacePredefinedVariables(library, project);
        PsiElement result = getFromCache(library, project);
        if (result != null) {
            LogUtil.debug("Found from cached: "+ result, "RobotFileManager", "findPython", project);
            return result;
        }
        debug(library, "Attempting class search (python)", project);
        result = PythonResolver.findClass(library, project);
        if (result != null) {
            addToCache(result, library);
            return result;
        }

        debug(library, "Attemping module search", project);
        List<PyFile> results = PyModuleNameIndex.find(library, project, true);
        if (! results.isEmpty()) {
            result = results.get(0);
            addToCache(result, library);
            return result;
        }

        // only chop .py at the end
        String mod = library.replaceAll("\\.py$", "");
        // if mod contain "/", then "." is most probably part of pathname, not to be replaced with "/"
        if (! mod.contains("/"))
            mod = mod.replaceAll("\\.", "\\/");

        while (mod.contains("//")) {
            mod = mod.replace("//", "/");
        }
        if (mod.endsWith("/")) {
            mod += "__init__.py";
        }

        String[] file = getFilename(mod, ".py");
        // search project scope
        debug(library, "Attempting project search (python)", project);
        result = findProjectFile(library, file[0], file[1], project, originalElement);
        if (result != null) {
            addToCache(result, library);
            return result;
        }
        // search global scope... this can get messy
        debug(library, "Attempting global search (python)", project);
        result = findGlobalFile(library, file[0], file[1], project, originalElement);
        if (result != null) {
            addToCache(result, library);
            return result;
        }
        return null;
    }

    @Nullable
    private static PsiFile findProjectFile(@NotNull String original, @NotNull String path, @NotNull String fileName,
                                           @NotNull Project project, @NotNull PsiElement originalElement) {
        return findFile(original, path, fileName, project, ProjectScope.getContentScope(project), originalElement);
    }

    @Nullable
    private static PsiFile findGlobalFile(@NotNull String original, @NotNull String path, @NotNull String fileName,
                                          @NotNull Project project, @NotNull PsiElement originalElement) {
        return findFile(original, path, fileName, project, GlobalSearchScope.allScope(project), originalElement);
    }

    @Nullable
    private static PsiFile findFile(@NotNull String original, @NotNull String path, @NotNull String fileName,
                                    @NotNull Project project, @NotNull GlobalSearchScope search,
                                    @NotNull PsiElement originalElement) {
        debug(original, "path::" + path, project);
        debug(original, "file::" + fileName, project);

        if (path.contains("./")) {
            // contains a relative path
            VirtualFile workingDir = originalElement.getContainingFile().getOriginalFile().getVirtualFile().getParent();
            if (workingDir == null) workingDir = originalElement.getContainingFile().getVirtualFile().getParent();
            if (workingDir != null) {
                VirtualFile relativePath = workingDir.findFileByRelativePath(path);
                if (relativePath != null && relativePath.isDirectory() && relativePath.getCanonicalPath() != null) {
                    debug(original, "changing relative path to: " + relativePath.getCanonicalPath(), project);
                    path = relativePath.getCanonicalPath();
                    if (!path.endsWith("/")) {
                        path += "/";
                    }
                }
            }
        }

        Map files = Arrays.stream(FilenameIndex.getFilesByName(project, fileName, search))
                .map(f -> new Pair<String, PsiFile>(f.getOriginalFile().getVirtualFile().getCanonicalPath(), f))
                .collect(Collectors.toMap(p->p.getFirst(), p-> p.getSecond()));
        StringBuilder builder = new StringBuilder();
        builder.append(path);
        builder.append(fileName);
        //enhance: lookup using path mapping
        LogUtil.debug("lookup path: "+ builder.toString(), "RobotFileManager", "findFile", project);
        PsiFile result = (PsiFile) files.get(builder.toString());
        if(result == null){
            debug(original, "no acceptable matches", project);
        }else{
            debug(original, "matched: " + result.getOriginalFile().getVirtualFile().getCanonicalPath(), project);
        }
        return result;
    }

    private static String arrayToString(PsiFile[] files) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        if (files != null) {
            for (PsiFile file : files) {
                builder.append(file.getOriginalFile().getVirtualFile().getCanonicalPath());
                builder.append(";");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    private static boolean acceptablePath(@NotNull String path, @Nullable PsiFile file) {
        if (file == null) {
            return false;
        }
        String virtualFilePath = file.getOriginalFile().getVirtualFile().getCanonicalPath();
        if (virtualFilePath == null) {
            return false;
        }
        String filePath = new StringBuilder(virtualFilePath).reverse().toString();
        return filePath.startsWith(path);
    }

    @NotNull
    private static String[] getFilename(@NotNull String path, @NotNull String suffix) {
        // support either / or ${/}
        String[] pathElements = path.split("(\\$\\{)?/(\\})?");
        String result;
        if (pathElements.length == 0) {
            result = path;
        } else {
            result = pathElements[pathElements.length - 1];
        }
        String[] results = new String[2];
        results[0] = path.replace(result, "").replace("${/}", "/");
        if (!result.toLowerCase().endsWith(suffix.toLowerCase())) {
            result += suffix;
        }
        results[1] = result;
        return results;
    }

    private static void debug(@NotNull String lookup, String data, @NotNull Project project) {
        if (RobotOptionsProvider.getInstance(project).isDebug()) {
            String message = String.format("[%s] %s", lookup, data);
            LogUtil.debug(message, "RobotFileManager", "Debug", project);
        }

    }
}
