package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.psi.ref.RobotArgumentReference;
import com.github.nghiatm.robotframeworkplugin.psi.util.ExtRobotPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
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

    @Override
    public ArgumentImpl setName(@NotNull String name) throws IncorrectOperationException {
        super.setName(name);
        ArgumentImpl newElement = new ExtRobotPsiUtils(this).createArgument(name);
        this.getNode().getTreeParent().replaceChild(this.getNode(), newElement.getNode());
        return this;
    }
}
