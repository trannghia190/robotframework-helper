package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;


public class RunBySelectTextAction extends RunTestCaseAction {

    public RunBySelectTextAction(){
        super();
    }

    public RunBySelectTextAction(String name, String value) {
        super(name, value);
    }

    @Override
    public void update(AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement currentElement = e.getData(LangDataKeys.PSI_ELEMENT);
        if (editor != null && currentElement != null
                && currentElement instanceof KeywordDefinitionImpl ){
            String selectedText = editor.getSelectionModel().getSelectedText();
            if(StringUtils.isNotBlank(selectedText)){
                enableAndShowAction(e, selectedText, "Run TCs contain: '", displayName);
                return;
            }
        }
        e.getPresentation().setEnabledAndVisible(false);
    }



    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement psiElement = getTargetElement(e);
        if (editor != null) {
            String selectedText = editor.getSelectionModel().getSelectedText();
            if (selectedText != null && !selectedText.isEmpty()) {
                // Do something with the selected text
                String scriptParams = getRunParameters(e.getProject(), psiElement, selectedText);
                String name = psiElement.getText();
                executeRunConfig(e.getProject(), scriptParams, name, "robot.run");
            }
        }
    }

    private String getRunParameters(Project project, PsiElement psiElement, String selectedText){
        String result = "";
        String sourcePath = psiElement.getContainingFile().getVirtualFile().getCanonicalPath();

        result += getGeneralParams(project);
        if (psiElement instanceof KeywordDefinitionImpl){
            String testName = "*"+selectedText+"*";
            result += " -t \"" + testName + "\" " + sourcePath;
        }
        return result;
    }
}
