package com.github.nghiatm.robotframeworkplugin.ide.usage;

import com.github.nghiatm.robotframeworkplugin.psi.element.Argument;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokable;
import com.github.nghiatm.robotframeworkplugin.psi.element.Variable;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.psi.PsiElement;
import com.intellij.usages.impl.rules.UsageType;
import com.intellij.usages.impl.rules.UsageTypeProvider;
import com.jetbrains.python.psi.PyReferenceExpression;
import org.jetbrains.annotations.Nullable;

public class RobotUsageTypeProvider implements UsageTypeProvider {
    @Override
    public @Nullable UsageType getUsageType(PsiElement element) {
        LogUtil.debug(element.toString(), "RobotUsageTypeProvider", "getUsageType", element.getProject());
        if (element instanceof KeywordInvokable){
            return new UsageType(() -> "Keyword");
        } else if (element instanceof Argument){
            return new UsageType(() -> "Argument");
        } else if (element instanceof Variable){
            return new UsageType(() -> "Variable");
        } else if (element instanceof PyReferenceExpression){
            return new UsageType(() -> "Python Method");
        }
        return UsageType.UNCLASSIFIED;
    }
}
