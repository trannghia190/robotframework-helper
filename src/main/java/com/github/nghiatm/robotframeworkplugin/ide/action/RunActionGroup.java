package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.github.nghiatm.robotframeworkplugin.ide.action.PabotRunActionGroup.getDisplayName;
import static com.github.nghiatm.robotframeworkplugin.ide.action.PabotRunActionGroup.isContainRobotFile;
import static com.github.nghiatm.robotframeworkplugin.ide.execution.RobotRunnerConfigurationProducer.getTestCaseName;
import static com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil.isTestCaseChildren;

public class RunActionGroup extends DefaultActionGroup {
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        Map<String, String> templates = RobotOptionsProvider.getInstance().getExecParamsTemplate();
        AnAction[] anActions = new AnAction[templates.size()];
        int index = 0;
        for(Map.Entry<String, String> entry: templates.entrySet()){
            String name = entry.getKey();
            String value = entry.getValue();
            AnAction anAction = new RunTestCaseAction(name, value);
            anActions[index] = anAction;
            index++;
        }
        return anActions;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Map<String, String> templates = RobotOptionsProvider.getInstance().getExecParamsTemplate();
        if (templates.size()<2){
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if(files != null && files.length > 1){
            e.getPresentation().setText("Run all ["+ files.length +"] suites ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }

        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file != null && file.isDirectory() && isContainRobotFile(file)) {
            e.getPresentation().setText("Run suite '" + getDisplayName(file.getNameWithoutExtension()) + "' ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if ( editor != null && element!= null ){
            PsiElement parentElement = element.getParent();
            if(parentElement!=null){
                if (parentElement.getText().trim().toLowerCase().startsWith("[tags]") ){
                    e.getPresentation().setText("Run with tag '" + element.getText() + "'");
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                } else if(
                        isTestCaseChildren(element)
                ){
                    String testName = StringUtils.isEmpty(getTestCaseName(element))?element.getContainingFile().getName().replace(".robot", ""):getTestCaseName(element);
                    e.getPresentation().setText("Run '" + getDisplayName(testName) + "' ...");
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                }else if (element instanceof KeywordDefinitionImpl
                        && ((KeywordDefinitionImpl) element).getKeywordRef() != null
                        && isTestCaseChildren(((KeywordDefinitionImpl) element).getKeywordRef())
                ){
                    String testName = getTestCaseName(((KeywordDefinitionImpl) element).getKeywordRef());
                    e.getPresentation().setText("Run '" + getDisplayName(testName) + "' ...");
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                }
            }
        }else if (file != null && file.exists() && file.getFileType() instanceof RobotFeatureFileType){
            e.getPresentation().setText("Run suite '" + getDisplayName(file.getNameWithoutExtension()) + "' ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }
        e.getPresentation().setEnabledAndVisible(false);
    }

}
