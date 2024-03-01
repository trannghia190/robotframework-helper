package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig.RobotConfigurationType;
import com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig.RobotRunConfiguration;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;
import java.awt.BorderLayout;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.nghiatm.robotframeworkplugin.ide.action.PabotRunActionGroup.getDisplayName;
import static com.github.nghiatm.robotframeworkplugin.ide.execution.RobotRunnerConfigurationProducer.getKeywordNameFromAnyElement;
import static com.github.nghiatm.robotframeworkplugin.ide.execution.RobotRunnerConfigurationProducer.getTestCaseName;
import static com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil.isTestCaseChildren;

public class RunTestCaseAction extends AnAction {
    protected String displayName;
    private String params;
    private PsiElement targetElement;
    private boolean isByContext = true;

    public RunTestCaseAction(){
        this("", "", null);
    }

    public RunTestCaseAction(String displayName, String params){
        this(displayName, params, null);
    }

    public RunTestCaseAction(String displayName, String params, PsiElement element){
        this.displayName = displayName;
        this.params = params;
        this.targetElement = element;
        isByContext = false;
    }

    protected PsiElement getTargetElement(AnActionEvent e){
        if(isByContext){
            return e.getData(LangDataKeys.PSI_ELEMENT);
        }
        if(this.targetElement == null) {
            return e.getData(LangDataKeys.PSI_ELEMENT);
        }

        return targetElement;
    }

    @Override
    public void update(AnActionEvent e) {
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if(files != null && files.length > 1){
            enableAndShowAction(e, "all "+ files.length+" suites", "Run '", displayName);
            return;
        }
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file != null && file.isDirectory()) {
            enableAndShowAction(e, getDisplayName(file.getNameWithoutExtension()), "Run '", displayName);
            return;
        }
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement element = getTargetElement(e);
        if ( editor != null && element!= null ){
            PsiElement parentElement = element.getParent();

            if(parentElement!=null){
                 if (parentElement.getText().trim().toLowerCase().startsWith("[tags]") ){
                     //show when click to tag
                    enableAndShowAction(e, element.getText(), "Run with tag '", displayName);
                    return;
                } else if(
                        //show when click test case children (Except KeywordDefinition)
//                        parentElement.getText().trim().toLowerCase().startsWith("*** test cases ***")
                        isTestCaseChildren(element)
                ){
                    String testName = StringUtils.isEmpty(getTestCaseName(element))?element.getContainingFile().getName().replace(".robot", ""):getTestCaseName(element);
                    enableAndShowAction(e, getDisplayName(testName), "Run '", displayName);
                    return;
                }else if (element instanceof KeywordDefinitionImpl
                        && ((KeywordDefinitionImpl) element).getKeywordRef() != null
                        && isTestCaseChildren(((KeywordDefinitionImpl) element).getKeywordRef())
                ){
                     //KeywordDefinition belong to Keyword heading -> need get from Keyword ref
                    String testName = getTestCaseName(((KeywordDefinitionImpl) element).getKeywordRef());
                    enableAndShowAction(e, getDisplayName(testName), "Run '", displayName);
                    return;
                }
            }
        }else if (file != null && file.getFileType() instanceof RobotFeatureFileType) {
            enableAndShowAction(e, getDisplayName(file.getNameWithoutExtension()), "Run '", displayName);
            return;
        }
        e.getPresentation().setEnabledAndVisible(false);
    }

    public static void enableAndShowAction(AnActionEvent e, String testName, String action, String label) {
        String prefix = StringUtils.isBlank(label) ? "" : "[" + label + "] ";
        e.getPresentation().setText(prefix + action + testName + "'");
        e.getPresentation().setEnabledAndVisible(true);
        e.getPresentation().setIcon(AllIcons.Actions.Execute);
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
            String name = StringUtils.isEmpty(getTestCaseName(psiElement)) ? file.getNameWithoutExtension() : getTestCaseName(psiElement);
            executeRunConfig(project, scriptParams, name);
        }else if(file != null && file.isDirectory() || file.getFileType() instanceof  RobotFeatureFileType){
            String scriptParams;
            String name;
            if(files!= null && files.length > 1){
                scriptParams = getRunParameters(project, files);
                name = Arrays.stream(files).map(f -> f.getNameWithoutExtension()).collect(Collectors.joining(" "));
            }else{
                scriptParams = getRunParameters(project, file);
                name = file.getNameWithoutExtension();
            }
            executeRunConfig(project, scriptParams, name);
        }
    }

    protected void executeRunConfig(Project project, String scriptParams, String name, String scriptName) {
        if (!scriptName.toLowerCase().startsWith("robot")){
            name += " (" + scriptName.split("\\.")[0] + ")";
        }

        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings configurationSettings = runManager.findConfigurationByTypeAndName(RobotConfigurationType.getInstance().getId(), name);
        RobotRunConfiguration runConfig = null;
        if (configurationSettings == null){
            LogUtil.debug("Create new config", "RunTestCaseAction", "executeRunConfig", project);
            configurationSettings = runManager.createConfiguration(name, RobotConfigurationType.getInstance().getConfigurationFactories()[0]);
            runConfig = (RobotRunConfiguration) configurationSettings.getConfiguration();
            runConfig.setUseModuleSdk(false);
            runConfig.setModuleMode(true);
            runConfig.setScriptName(scriptName);
            runConfig.setWorkingDirectory(project.getBasePath());
            runConfig.setScriptParameters(scriptParams);
            Sdk sdk = ProjectRootManager.getInstance(project).getProjectSdk();
            if (sdk != null) {
                runConfig.setSdkHome(sdk.getHomePath());
            }
            runConfig.setName(name);
            runManager.addConfiguration(configurationSettings);
            runManager.setSelectedConfiguration(configurationSettings);
        }else{
            LogUtil.debug("Get exist runconfig", "RunTestCaseAction", "executeRunConfig", project);
            runConfig = (RobotRunConfiguration) configurationSettings.getConfiguration();
        }
        try {
            // Get the runner and execute the configuration
            Executor executor = ExecutorRegistry.getInstance().getExecutorById(DefaultRunExecutor.EXECUTOR_ID);
            ProgramRunner runner = ProgramRunner.getRunner(executor.getId(), runConfig);
            // Build environment
            ExecutionEnvironmentBuilder environmentBuilder = ExecutionEnvironmentBuilder.create(executor, runConfig);
            ExecutionEnvironment environment = environmentBuilder.build(descriptor -> {
                ConsoleView consoleView = (ConsoleView) descriptor.getExecutionConsole();
                JComponent consoleComponent = consoleView.getComponent();
                DefaultActionGroup actionGroup = new DefaultActionGroup();
                actionGroup.add(new ShowResultAction());
                ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar("MyToolbar", actionGroup, true);
                consoleComponent.add(actionToolbar.getComponent(), BorderLayout.PAGE_START);
                consoleComponent.revalidate();
                consoleComponent.repaint();

            });
            //Execute
            runner.execute(environment);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void executeRunConfig(Project project, String scriptParams, String name) {
        executeRunConfig(project, scriptParams, name, "robot.run");
    }

    private String getRunParameters(Project project, VirtualFile file) {
        String result = "";

        result += getGeneralParams(project);
        String sourcePath = file.getPath();

        result += " -t \"" + "*" + "\" " + sourcePath;

        return result;
    }

    private String getRunParameters(Project project, VirtualFile[] files) {
        String result = "";

        result += getGeneralParams(project);
        String sourcePath = Arrays.stream(files).map(file -> file.getPath()).collect(Collectors.joining(" "));

        result += " -t \"" + "*" + "\" " + sourcePath;

        return result;
    }

    private String getRunParameters(Project project, PsiElement psiElement, Editor editor) {
        String result = "";


        PsiElement parentElement = psiElement.getParent();
        String sourcePath = psiElement.getContainingFile().getVirtualFile().getCanonicalPath();

        if (parentElement != null) {

            result += getGeneralParams(project);

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
    protected String getGeneralParams(Project project) {
        String result = "";

        //add output dir
        String outputDir = RobotOptionsProvider.getInstance(project).getOutputDir();
        if (StringUtils.isNotBlank(outputDir)) {
            result += " --outputdir " + outputDir;
        }

        //add parameters
        if (StringUtils.isBlank(params)) {
            //if not call from group action -> get first template to run
            Map<String, String> templates = RobotOptionsProvider.getInstance().getExecParamsTemplate();
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
        Map<String, String> variables = RobotOptionsProvider.getInstance().getExecVariables();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result += " --variable " + entry.getKey() + ":" + entry.getValue();
        }
        return result;
    }
}
