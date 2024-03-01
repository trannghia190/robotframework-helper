package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.nghiatm.robotframeworkplugin.ide.action.PabotRunActionGroup.getDisplayName;
import static com.github.nghiatm.robotframeworkplugin.ide.execution.RobotRunnerConfigurationProducer.getKeywordNameFromAnyElement;
import static com.github.nghiatm.robotframeworkplugin.ide.execution.RobotRunnerConfigurationProducer.getTestCaseName;
import static com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil.isTestCaseChildren;

public class PabotRunTestCaseAction extends RunTestCaseAction {
    private String displayName;
    private String params;
    private PsiElement targetElement;
    private boolean isByContext = true;

    private int numberOfProcesses = 3;
    private boolean isTestLevelSplit = true;

    public PabotRunTestCaseAction() {
        this("", "", null);
    }

    public PabotRunTestCaseAction(String displayName, String params) {
        this(displayName, params, null);
    }

    public PabotRunTestCaseAction(String displayName, String params, PsiElement element) {
        this.displayName = displayName;
        this.params = params;
        this.targetElement = element;
        isByContext = false;

        Integer numberOfProcessConfig = RobotOptionsProvider.getInstance().getNumberOfPabotProcess();
        if(numberOfProcessConfig!=null){
            this.numberOfProcesses = numberOfProcessConfig;
        }
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if(files != null && files.length > 1){
            String filenames = Arrays.stream(files).map(f -> f.getNameWithoutExtension()).collect(Collectors.joining(" "));
            enableAndShowAction(e, getDisplayName(filenames), "Pabot Run ");
            return;
        }
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file != null && file.isDirectory()) {
            enableAndShowAction(e, getDisplayName(file.getNameWithoutExtension()), "Pabot Run ");
            return;
        }
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement element = getTargetElement(e);
        if (editor != null && element != null) {
            PsiElement parentElement = element.getParent();

            if (parentElement != null) {
                if (parentElement.getText().trim().toLowerCase().startsWith("[tags]")) {
                    //show when click to tag
                    enableAndShowAction(e, element.getText(), "Run with tag ");
                    return;
                } else if (
                    //show when click test case children (Except KeywordDefinition)
//                        parentElement.getText().trim().toLowerCase().startsWith("*** test cases ***")
                        isTestCaseChildren(element)
                ) {
                    String testName = StringUtils.isEmpty(getTestCaseName(element)) ? element.getContainingFile().getName().replace(".robot", "") : getTestCaseName(element);
                    enableAndShowAction(e, getDisplayName(testName), "Run ");
                    return;
                } else if (element instanceof KeywordDefinitionImpl
                        && ((KeywordDefinitionImpl) element).getKeywordRef() != null
                        && isTestCaseChildren(((KeywordDefinitionImpl) element).getKeywordRef())
                ) {
                    //KeywordDefinition belong to Keyword heading -> need get from Keyword ref
                    String testName = getTestCaseName(((KeywordDefinitionImpl) element).getKeywordRef());
                    enableAndShowAction(e, getDisplayName(testName), "Run ");
                    return;
                }
            }
        } else if (file != null && file.getFileType() instanceof RobotFeatureFileType) {
            enableAndShowAction(e, getDisplayName(file.getNameWithoutExtension()), "Pabot Run ");
            return;
        }
        e.getPresentation().setEnabledAndVisible(false);
    }

    private void enableAndShowAction(AnActionEvent e, String testName, String s) {
        String prefix = StringUtils.isBlank(displayName) ? "" : "[" + displayName + "] ";
        showRunAction(prefix + s + "'" + testName + "'", e);
    }

    private void showRunAction(String text, AnActionEvent e) {
        e.getPresentation().setText(text);
        e.getPresentation().setIcon(AllIcons.Actions.Execute);
        e.getPresentation().setEnabledAndVisible(true);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        PsiElement psiElement = getTargetElement(e);
        if (editor != null && psiElement != null) {
            // Do something with the selected element
            String scriptParams = getRunParameters(project, psiElement, editor);
            String name = psiElement.getText();
            executeRunConfig(project, scriptParams, name, "pabot.pabot");
        } else if(file != null && file.isDirectory() || file.getFileType() instanceof  RobotFeatureFileType){
            if(file.isDirectory()){
                isTestLevelSplit = false;
            }
            String scriptParams;
            String name;
            if(files!= null && files.length > 1){
                scriptParams = getRunParameters(project, files);
                name = Arrays.stream(files).map(f -> f.getNameWithoutExtension()).collect(Collectors.joining(" "));
            }else{
                scriptParams = getRunParameters(project, file);
                name = file.getNameWithoutExtension();
            }

            executeRunConfig(project, scriptParams, name, "pabot.pabot");
        }

    }

    private String getRunParameters(Project project, VirtualFile file) {
        String result = "";

        result += getGeneralParams(project, numberOfProcesses, isTestLevelSplit).replace("browser:chrome", "browser:headlesschrome");
        String sourcePath = file.getPath();

        result += " -t \"" + "*" + "\" " + sourcePath;

        return result;
    }

    private String getRunParameters(Project project, VirtualFile[] files) {
        String result = "";

        result += getGeneralParams(project, numberOfProcesses, isTestLevelSplit).replace("browser:chrome", "browser:headlesschrome");
        String sourcePath = Arrays.stream(files).map(file -> file.getPath()).collect(Collectors.joining(" "));

        result += " -t \"" + "*" + "\" " + sourcePath;

        return result;
    }

    private String getRunParameters(Project project, PsiElement psiElement, Editor editor) {
        String result = "";


        PsiElement parentElement = psiElement.getParent();
        String sourcePath = psiElement.getContainingFile().getVirtualFile().getCanonicalPath();

        if (parentElement != null) {
            result += getGeneralParams(project, numberOfProcesses, isTestLevelSplit);

            if (parentElement.getText().trim().toLowerCase().startsWith("[tags]")) {
                result += " -i \"" + psiElement.getText() + "\" " + sourcePath;
            } else if (isTestCaseChildren(psiElement)) {
                String testName = StringUtils.isEmpty(getTestCaseName(psiElement)) ? "*" : getTestCaseName(psiElement);
                result += " -t \"" + testName + "\" " + sourcePath;
            } else if (psiElement instanceof KeywordDefinitionImpl
                    && ((KeywordDefinitionImpl) psiElement).getKeywordRef() != null
                    && isTestCaseChildren(((KeywordDefinitionImpl) psiElement).getKeywordRef())
            ) {
                String testName = getKeywordNameFromAnyElement(((KeywordDefinitionImpl) psiElement).getKeywordRef());
                LogUtil.debug("test name: " + testName, null, null, project);
                result += " -t \"" + testName + "\" " + sourcePath;
            } else {
                result += " -t \"" + "*" + "\" " + sourcePath;
            }
        }

        return result;
    }

    @NotNull
    private String getGeneralParams(Project project, int numberOfProcesses, boolean isTestLevelSplit) {
        String result = "";
        //pabot
        result += " --processes " + numberOfProcesses;
        if (isTestLevelSplit) {
            result += " --testlevelsplit";
        }

        //add output dir
        String outputDir = RobotOptionsProvider.getInstance(project).getOutputDir();
        if (StringUtils.isNotBlank(outputDir)) {
            result += " --outputdir " + outputDir;
        }

        //add parameters
        if (StringUtils.isBlank(params)) {
            //if not call from group action -> get first template to run
            Map<String, String> templates = RobotOptionsProvider.getInstance(project).getExecParamsTemplate();
            if (templates.size() >= 1) {
                Map.Entry<String, String> entry = templates.entrySet().iterator().next();
                String value = entry.getValue();
                result += " " + value;
            }
        } else {
            //use the chosen template to run
            result += " " + params;
        }

        //add variables
        Map<String, String> variables = RobotOptionsProvider.getInstance(project).getExecVariables();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result += " --variable " + entry.getKey() + ":" + entry.getValue();
        }
        return result;
    }
}
