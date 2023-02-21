package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.psi.ref.RobotArgumentReference;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

/**
 * @author Stephen Abrams
 */
public class ArgumentImpl extends RobotPsiElementBase implements Argument {

    public ArgumentImpl(@NotNull final ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new RobotArgumentReference(this);
    }
}
