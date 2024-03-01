package com.github.nghiatm.robotframeworkplugin.psi.util;

import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.ArgumentImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokable;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFileImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.VariableDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.VariableImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.util.PsiTreeUtil;

public class ExtRobotPsiUtils {
    private PsiElement psiElement;


    public ExtRobotPsiUtils(PsiElement element){
        this.psiElement = element;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public VariableImpl createVariable(String name){
        RobotFileImpl dummyFile = createFileFromText(
                "*** Keywords ***\n" +
                        "Setup initial data\n" +
                        "    "+name);
        return PsiTreeUtil.findChildOfType(dummyFile.getNode().getPsi(), VariableImpl.class);
    }

    public VariableDefinitionImpl createVariableDefinition(String name){
        RobotFileImpl dummyFile = createFileFromText(
                "*** Keywords ***\n" +
                        "Keyword Def\n" +
                        "    [Arguments]\n" +
                        "        ...     "+name+"=value");
        return PsiTreeUtil.findChildOfType(dummyFile.getNode().getPsi(), VariableDefinitionImpl.class);
    }

    public KeywordInvokable createKeyword(String name){
        RobotFileImpl dummyFile = createFileFromText(
                "*** Keywords ***\n" +
                        "KeyDef\n" +
                        "   "+name
                );
        return PsiTreeUtil.findChildOfType(dummyFile.getNode().getPsi(), KeywordInvokable.class);
    }

    private RobotFileImpl createFileFromText(String content){
        return (RobotFileImpl) PsiFileFactory.getInstance(psiElement.getProject())
                .createFileFromText("dummy", RobotFeatureFileType.getInstance(), content.trim());
    }

    public KeywordDefinitionImpl createKeywordDefinitionImpl(String name) {
        RobotFileImpl dummyFile = createFileFromText(
                "*** Keywords ***\n" +
                        name
        );
        return PsiTreeUtil.findChildOfType(dummyFile.getNode().getPsi(), KeywordDefinitionImpl.class);
    }

    public ArgumentImpl createArgument(String name) {
        RobotFileImpl dummyFile = createFileFromText(
                "*** Settings ***\n" +
                        "Suite Setup     Run keywords\n" +
                        "                ...     " + name
        );
        return PsiTreeUtil.findChildOfType(dummyFile.getNode().getPsi(), ArgumentImpl.class);
    }
}
