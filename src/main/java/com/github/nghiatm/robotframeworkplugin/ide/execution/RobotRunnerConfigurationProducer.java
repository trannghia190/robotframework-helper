package com.github.nghiatm.robotframeworkplugin.ide.execution;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil;
import com.github.rey5137.robotrunnerplugin.runconfigurations.RobotRunConfiguration;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.run.PythonConfigurationType;
import com.jetbrains.python.run.PythonRunConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class RobotRunnerConfigurationProducer extends LazyRunConfigurationProducer<RunConfiguration> {

    private boolean isRobotRunnerPluginEnabled = false;
    public RobotRunnerConfigurationProducer() {
        IdeaPluginDescriptor plugin =PluginManager.getInstance().findEnabledPlugin(PluginId.getId("com.github.rey5137.robot-runner-plugin"));
        if(plugin != null && plugin.isEnabled()){
            isRobotRunnerPluginEnabled = true;
        }
    }

    @Override
    protected boolean setupConfigurationFromContext(@NotNull RunConfiguration runConfigObj,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        if (isValidRobotExecutableScript(context)) {
            Map<String, String> variables = RobotOptionsProvider.getInstance(context.getProject()).getVariables();
            if(isRobotRunnerPluginEnabled){
                RobotRunConfiguration runConfig = (RobotRunConfiguration) runConfigObj;
                runConfig.setName(getRunDisplayName(context));
                runConfig.getOptions().setTestNames(Arrays.asList(StringUtils.isEmpty(getTestCaseName(context))?"*":getTestCaseName(context)));
                runConfig.getOptions().setSuitePaths(Arrays.asList(sourceElement.get().getContainingFile().getVirtualFile().getCanonicalPath().toString()));
                runConfig.getOptions().setSdkHomePath(ProjectRootManager.getInstance(context.getProject()).getProjectSdk().getHomePath());
                runConfig.getOptions().setDefaultLogLevel("TRACE");
                runConfig.getOptions().setLogLevel("TRACE");
                runConfig.getOptions().setVariables(variables);
                Path resultPath= Paths.get(context.getProject().getBasePath()+ "/result");
                try {
                    Files.createDirectory(resultPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                runConfig.getOptions().setOutputDirPath(resultPath.toAbsolutePath().toString());
                return true;
            } else {
                PythonRunConfiguration runConfig = (PythonRunConfiguration) runConfigObj;
                String runParam = getRunParameters(context, variables, sourceElement);
                runConfig.setUseModuleSdk(false);
                runConfig.setModuleMode(true);
                runConfig.setScriptName("robot.run");
                runConfig.setWorkingDirectory(context.getProject().getBasePath());
                runConfig.setScriptParameters(runParam);
                Sdk sdk = ProjectRootManager.getInstance(context.getProject()).getProjectSdk();
                if (sdk != null) {
                    runConfig.setSdkHome(sdk.getHomePath());
                }
                runConfig.setName(getRunDisplayName(context));
                return true;
            }


        }
        return false;
    }

    @NotNull
    private static String getRunParameters(ConfigurationContext context, Map<String, String> variables, @NotNull Ref<PsiElement> sourceElement) {
        String variableString = "";
        for(Map.Entry<String, String> entry : variables.entrySet()){
            variableString += " --variable " + entry.getKey() +":"+entry.getValue();
        }
        String sourcePath = sourceElement.get().getContainingFile().getVirtualFile().getCanonicalPath();
        String testName = StringUtils.isEmpty(getTestCaseName(context))?"*":getTestCaseName(context);
        String testCaseString = " --test \"" + testName + "\" " + sourcePath ;
        return variableString + testCaseString;
    }

    @NotNull
    private static String getRunDisplayName(ConfigurationContext context) {
        Location<?> location = context.getLocation();
        assert location != null;
        VirtualFile file = location.getVirtualFile();
        assert file != null;
        String testCaseName = getTestCaseName(context);
        return !testCaseName.equals("") ? testCaseName : file.getName();
    }

    @NotNull
    private static String getSuiteName(ConfigurationContext context) {
        String projectName = context.getProject().getName();
        Location<?> location = context.getLocation();
        assert location != null;
        VirtualFile file = location.getVirtualFile();
        assert file != null;
        String suitePathName = file.getPath()
                .replace(context.getProject().getBasePath() + "/", "")
                .replace("/", ".");
        suitePathName = suitePathName.substring(0, suitePathName.lastIndexOf('.'));
        return projectName + "." + suitePathName;
    }

    @Override
    public boolean isConfigurationFromContext(@NotNull RunConfiguration runConfigObj,
                                              @NotNull ConfigurationContext context) {
        if (isValidRobotExecutableScript(context)) {
            Map<String, String> variables = RobotOptionsProvider.getInstance(context.getProject()).getVariables();
            if(isRobotRunnerPluginEnabled){
                RobotRunConfiguration runConfig = (RobotRunConfiguration) runConfigObj;
                boolean ret = runConfig.getOptions().getTestNames().size()== 1
                        && getTestCaseName(context).equals(runConfig.getOptions().getTestNames().get(0));
                if (ret) {
                    runConfig.setName(getRunDisplayName(context));
                }
                return ret;
            } else {
                PythonRunConfiguration runConfig = (PythonRunConfiguration) runConfigObj;
//                String runParam = getRunParameters(context, variables);
//                boolean ret = runParam.trim().
//                        equals(runConfig.getScriptParameters().trim());
                boolean ret = runConfig.getName().equals(getRunDisplayName(context));
                if (ret) {
                    runConfig.setName(getRunDisplayName(context));
                }
                return ret;
            }

        }
        return false;
    }

    private boolean isValidRobotExecutableScript(@NotNull ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location == null) {
            return false;
        }
        PsiElement psiElement = location.getPsiElement();
        VirtualFile file = location.getVirtualFile();
        if (file == null) {
            return false;
        }
        if (!(file.getFileType() instanceof RobotFeatureFileType)) {
            return false;
        }
        Collection<DefinedKeyword> testCases = RobotUtil.getTestCasesFromElement(psiElement);
        // do not have test case
        return !testCases.isEmpty();
    }

    @NotNull
    private static String getTestCaseName(@NotNull ConfigurationContext context) {
        Location<?> location = context.getLocation();
        if (location != null) {
            return getKeywordNameFromAnyElement(location.getPsiElement());
        }
        return "";
    }

    @NotNull
    private static String getKeywordNameFromAnyElement(PsiElement element) {
        while (true) {
            if (element instanceof KeywordDefinitionImpl) {
                return ((KeywordDefinitionImpl) element).getKeywordName();
            }
            element = element.getParent();
            if (element == null) {
                return "";
            }
        }
    }


    @NotNull
    @Override
    public ConfigurationFactory getConfigurationFactory() {
        if(isRobotRunnerPluginEnabled ){
            return ConfigurationTypeUtil.findConfigurationType("robot-runner").getConfigurationFactories()[0];
        }else{
            return PythonConfigurationType.getInstance().getConfigurationFactories()[0];
        }

    }
}