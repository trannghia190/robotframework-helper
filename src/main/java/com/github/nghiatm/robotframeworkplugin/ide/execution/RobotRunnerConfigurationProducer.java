package com.github.nghiatm.robotframeworkplugin.ide.execution;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig.RobotConfigurationType;
import com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig.RobotRunConfiguration;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.LazyRunConfigurationProducer;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class RobotRunnerConfigurationProducer extends LazyRunConfigurationProducer<RunConfiguration> {

    private static String[] SPECIAL_CHARS = new String[]{"[","]","?","*","\""};

    public RobotRunnerConfigurationProducer() {}

    @Override
    protected boolean setupConfigurationFromContext(@NotNull RunConfiguration runConfigObj,
                                                    @NotNull ConfigurationContext context,
                                                    @NotNull Ref<PsiElement> sourceElement) {
        if (isValidRobotExecutableScript(context)) {
            Map<String, String> variables = RobotOptionsProvider.getInstance(context.getProject()).getExecVariables();
            Map<String, String> templates = RobotOptionsProvider.getInstance(context.getProject()).getExecParamsTemplate();

            RobotRunConfiguration runConfig = (RobotRunConfiguration) runConfigObj;
            String runParam = getRunParameters(context, variables, templates, sourceElement);
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
        return false;
    }

    @NotNull
    private static String getRunParameters(ConfigurationContext context, Map<String, String> variables, Map<String, String> templates, @NotNull Ref<PsiElement> sourceElement) {
        String variableString = "";
        //add output dir
        String outputDir = RobotOptionsProvider.getInstance(context.getProject()).getOutputDir();
        if(StringUtils.isNotBlank(outputDir)){
            variableString += " --outputdir "+ outputDir;
        }

        if (templates.size() >= 1){
            Map.Entry<String,String> entry = templates.entrySet().iterator().next();
            String value = entry.getValue();
            variableString += " " + value;
        }
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
            Map<String, String> variables = RobotOptionsProvider.getInstance(context.getProject()).getExecVariables();

            RobotRunConfiguration runConfig = (RobotRunConfiguration) runConfigObj;
//                String runParam = getRunParameters(context, variables);
//                boolean ret = runParam.trim().
//                        equals(runConfig.getScriptParameters().trim());
            boolean ret = runConfig.getName().equals(getRunDisplayName(context));
            if (ret) {
                runConfig.setName(getRunDisplayName(context));
            }
            return ret;

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
            return getTestCaseName(location.getPsiElement());
        }
        return "";
    }

    public static String getTestCaseName(PsiElement element){
        String keywordName = getKeywordNameFromAnyElement(element);
        int wildCardPosition = getWildcardCharacterPosition(keywordName);
        if (wildCardPosition>0){
            keywordName = keywordName.substring(0, wildCardPosition-1)+"*";
        }
        return keywordName;
    }

    private static int getWildcardCharacterPosition(String text){
        return Arrays.stream(SPECIAL_CHARS).filter(c -> text.contains(c))
                .map(c -> text.indexOf(c))
                .min(Integer::compare).orElse(-1);

    }

    @NotNull
    public static String getKeywordNameFromAnyElement(PsiElement element) {
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
        return RobotConfigurationType.getInstance().getConfigurationFactories()[0];
    }
}