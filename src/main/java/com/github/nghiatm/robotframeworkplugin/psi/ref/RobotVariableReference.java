package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.psi.element.Variable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceBase;
import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Scott Albertine
 */
public class RobotVariableReference extends PsiReferenceBase<Variable> {

    public RobotVariableReference(@NotNull Variable element) {
        super(element, false);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        String text = getElement().getPresentableText();
        PsiElement parent = getElement().getParent();
        PsiElement results = ResolverUtils.resolveVariableFromStatement(text, parent,
                RobotOptionsProvider.getInstance(getElement().getProject()).allowGlobalVariables());
        if (results != null) {
            return results;
        }
        PsiFile file = getElement().getContainingFile();
        return ResolverUtils.resolveVariableFromFile(text, file);
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return EMPTY_ARRAY;
    }
}
