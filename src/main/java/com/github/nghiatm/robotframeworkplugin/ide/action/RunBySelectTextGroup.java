package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RunBySelectTextGroup extends DefaultActionGroup {

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        Map<String, String> templates = RobotOptionsProvider.getInstance().getExecParamsTemplate();
        AnAction[] anActions = new AnAction[templates.size()];
        int index = 0;
        for(Map.Entry<String, String> entry: templates.entrySet()){
            String name = entry.getKey();
            String value = entry.getValue();
            AnAction anAction = new RunBySelectTextAction(name, value);
            anActions[index] = anAction;
            index++;
        }
        return anActions;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {

        Map<String, String> templates = RobotOptionsProvider.getInstance().getExecParamsTemplate();
        if (templates.size() < 2) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement currentElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (editor != null && currentElement != null
                && currentElement instanceof KeywordDefinitionImpl) {
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (StringUtils.isNotBlank(selectedText)) {
                e.getPresentation().setText("Run TC contain: ["+selectedText+"] ...");
                e.getPresentation().setEnabledAndVisible(true);
                return;
            }
        }

        e.getPresentation().setEnabledAndVisible(false);
    }
}
