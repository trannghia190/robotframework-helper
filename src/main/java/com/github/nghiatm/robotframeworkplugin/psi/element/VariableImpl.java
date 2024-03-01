package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.psi.ref.RobotVariableReference;
import com.github.nghiatm.robotframeworkplugin.psi.util.ExtRobotPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 * @since 2015-07-15
 */
public class VariableImpl extends RobotPsiElementBase implements Variable {

    public VariableImpl(@NotNull final ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        return new RobotVariableReference(this);
    }

    @Override
    public boolean isNested() {
        // TODO: this should become a check if the parent is a variable or a variable definition once we identify the nesting correctly
        String text = getPresentableText();
        return StringUtil.getOccurrenceCount(text, "}") > 1 &&
                (StringUtil.getOccurrenceCount(text, "${") + StringUtil.getOccurrenceCount(text, "@{") + StringUtil.getOccurrenceCount(text, "%{") > 1);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        super.setName(name);
        VariableImpl newElement = new ExtRobotPsiUtils(this).createVariable(name);
        this.getNode().getTreeParent().replaceChild(this.getNode(), newElement.getNode());
        return this;
    }
}
