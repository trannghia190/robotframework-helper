package com.github.nghiatm.robotframeworkplugin.psi.util;

import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.Heading;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFileImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class RobotUtil {

    public static Collection<DefinedKeyword> getTestCasesFromElement(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile instanceof RobotFileImpl) {
            List<DefinedKeyword> definedKeywords = new ArrayList<>();
            for(Heading heading: ((RobotFileImpl) psiFile).getHeadings()) {
                if (heading.containsTestCases()) {
                    definedKeywords.addAll(heading.getTestCases()) ;
                }
            }
            return definedKeywords;
        }
        return new LinkedHashSet<>();
    }

    public static boolean isTestCaseChildren(PsiElement element){
        PsiElement parent= element.getParent();
        if(parent != null && element.getText().trim().toLowerCase().startsWith("*** test case")
                || element.getText().trim().toLowerCase().startsWith("*** task")){
            return true;
        }else if(parent == null) {
            return false;
        }else {
            return isTestCaseChildren(parent);
        }
    }

    public static boolean isChildOf(PsiElement element, Class<? extends RobotStatement> type){
        PsiElement parent= element.getParent();
        if(type.isInstance(element)){
            return true;
        }else if(parent == null) {
            return false;
        }else {
            return isChildOf(parent, type);
        }
    }
}
