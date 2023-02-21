package com.github.nghiatm.robotframeworkplugin.ide.inspections.compilation;

import com.github.nghiatm.robotframeworkplugin.RobotBundle;
import com.github.nghiatm.robotframeworkplugin.ide.inspections.SimpleRobotInspection;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 * @since 2014-06-07
 */
public class RobotKeywordNotFound extends SimpleRobotInspection {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return RobotBundle.message("INSP.NAME.keyword.undefined");
    }

    @Override
    public boolean skip(PsiElement element) {
        if (element instanceof KeywordInvokable) {
            PsiReference reference = element.getReference();
            if (reference != null && reference.resolve() != null) {
                return true;
            }
        }
        return true;
    }

    @Override
    public String getMessage() {
        return RobotBundle.message("INSP.keyword.undefined");
    }

    @NotNull
    @Override
    protected String getGroupNameKey() {
        return "INSP.GROUP.compilation";
    }
}
