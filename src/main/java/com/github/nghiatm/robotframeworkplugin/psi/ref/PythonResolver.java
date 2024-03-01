package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import com.jetbrains.python.psi.stubs.PyVariableNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

/**
 * @author mrubino
 */
public class PythonResolver {

//    private static final String PYTHON_PLUGIN_U = "Pythonid";
//    private static final String PYTHON_PLUGIN_CE = "PythonCore";
//    private static Boolean hasPython;

    private PythonResolver() {
    }

    @Nullable
    public static PyClass castClass(@Nullable PsiElement element) {
        try{
            if (element != null
//                && hasPython(element.getProject())
            ) {
                if (element instanceof PyClass) {
                    return (PyClass) element;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Nullable
    public static PyFile castFile(@Nullable PsiElement element) {
        try{
            if (element != null
//                && hasPython(element.getProject())
            ) {
                if (element instanceof PyFile) {
                    return (PyFile) element;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

        return null;
    }

    @Nullable
    public static PyTargetExpression findVariable(@NotNull String name, @NotNull Project project) {
//        if (!hasPython(project)) {
//            return null;
//        }
        Collection<PyTargetExpression> variables = safeFindVariable(name, project);
        for (PyTargetExpression variable : variables) {
            String qName = variable.getQualifiedName();
            if (qName != null && qName.equals(name)) {
                return variable;
            }
            String vName = variable.getName();
            if (vName != null && vName.equals(name)) {
                return variable;
            }
        }
        return null;
    }

    @Nullable
    public static PyClass findClass(@NotNull String name, @NotNull Project project) {
//        if (!hasPython(project)) {
//            return null;
//        }
        String shortName = getShortName(name);
        Collection<PyClass> classes = safeFindClass(shortName, project);
        PyClass matchedByName = null;
//        debug(project, "Found classes for ["+name+"]["+shortName+"]: "+ classes);
//        for (PyClass pyClass : classes) {
//            debug(project, "Class name: "+ pyClass.getName());
//            debug(project, "Class qualified name: "+ pyClass.getQualifiedName());
//            if(pyClass.getClassAttributes().size()>0){
//                debug(project, "File name: "+pyClass.getClassAttributes().get(0).getContainingFile().getName());
//            }
//        }
        for (PyClass pyClass : classes) {
            String qName = pyClass.getQualifiedName();
            if (qName != null) {
                // For importing 'Library foo.bar.bar'
                if( qName.equals(name)) {
                    return pyClass;
                }

                // For importing 'Library foo.bar'
                if( qName.contains(name + "." + shortName )) {
                    return pyClass;
                }
            }

            // save last match on full name should qualify name never match
            String className = pyClass.getName();
            if (className != null && className.equals(name)) {
                matchedByName = pyClass;
            }
        }
        if (! name.startsWith("robot.libraries.")) {
            name = "robot.libraries." + name;
            for (PyClass pyClass : classes) {
                String qName = pyClass.getQualifiedName();
                if (qName != null) {
                    // For importing 'Library foo.bar.bar'
                    if (qName.equals(name)) {
                        return pyClass;
                    }

                    // For importing 'Library foo.bar'
                    if (qName.equals(name + "." + shortName)) {
                        return pyClass;
                    }
                }
            }
        }

        return matchedByName;
    }

    @NotNull
    private static String getShortName(@NotNull String name) {
        int pos = name.lastIndexOf(".");
        return pos > 0 ? name.substring(pos + 1) : name;
    }

//    private static synchronized boolean hasPython(Project project) {
//        if (hasPython == null) {
//            hasPython = detectPython(project);
//        }
//        return hasPython;
//    }
//
//    private static boolean detectPython(Project project) {
//        if (PlatformUtils.isPyCharm()) {
//            return true;
//        } else {
//            String pluginId = PlatformUtils.isCommunityEdition() ? PYTHON_PLUGIN_CE : PYTHON_PLUGIN_U;
//            PluginId pythonPluginId = PluginId.getId(pluginId);
//            IdeaPluginDescriptor pythonPlugin = PluginManagerCore.getPlugin(pythonPluginId);
//            if (pythonPlugin != null && pythonPlugin.isEnabled()) {
//                debug(project, String.format("python support enabled by '%s'", pluginId));
//                return true;
//            }
//            debug(project, String.format("no python support found, '%s' is not present/enabled.", pluginId));
//            if (PlatformUtils.isIntelliJ()) {
//                Notifications.Bus.notify(new Notification("intellibot.python",
//                        RobotBundle.message("plugin.python.missing.title"),
//                        RobotBundle.message("plugin.python.missing"),
//                        NotificationType.WARNING));
//            }
//            return false;
//        }
//    }

    @NotNull
    private static Collection<PyClass> safeFindClass(@NotNull String name, @NotNull Project project) {
        try {
            return PyClassNameIndex.find(name, project, true);
        } catch (NullPointerException | ClassCastException e) {
            e.printStackTrace();
            // seems to happen if python plugin dependency is not right in this project
            return Collections.emptyList();
        }
    }

    @NotNull
    private static Collection<PyTargetExpression> safeFindVariable(@NotNull String name, @NotNull Project project) {
        try {
            return PyVariableNameIndex.find(name, project, null);
        } catch (NullPointerException | ClassCastException e) {
            e.printStackTrace();
            // seems to happen if python plugin dependency is not right in this project
            return Collections.emptyList();
        }
    }
}
