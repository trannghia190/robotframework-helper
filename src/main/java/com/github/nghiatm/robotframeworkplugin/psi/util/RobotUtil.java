package com.github.nghiatm.robotframeworkplugin.psi.util;

import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.Heading;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFileImpl;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedHashSet;

public class RobotUtil {

    public static Collection<DefinedKeyword> getTestCasesFromElement(@NotNull PsiElement element) {
        PsiFile psiFile = element.getContainingFile();
        if (psiFile instanceof RobotFileImpl) {
            for(Heading heading: ((RobotFileImpl) psiFile).getHeadings()) {
                if (heading.containsTestCases()) {
                    return heading.getTestCases();
                }
            }
        }
        return new LinkedHashSet<>();
    }

}
