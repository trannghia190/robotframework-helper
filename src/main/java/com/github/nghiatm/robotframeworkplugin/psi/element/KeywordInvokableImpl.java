package com.github.nghiatm.robotframeworkplugin.psi.element;

import com.github.nghiatm.robotframeworkplugin.psi.ref.RobotKeywordReference;
import com.github.nghiatm.robotframeworkplugin.psi.util.ExtRobotPsiUtils;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Stephen Abrams
 */
public class KeywordInvokableImpl extends RobotPsiElementBase implements KeywordInvokable {

    public KeywordInvokableImpl(@NotNull final ASTNode node) {
        super(node);
    }

    @NotNull
    @Override
    public Collection<Argument> getArguments() {
        PsiElement parent = getParent();
        if (parent instanceof KeywordStatement) {
            return ((KeywordStatement) parent).getArguments();
        }
        return Collections.emptySet();
    }

    @Override
    public PsiReference getReference() {
        return new RobotKeywordReference(this);
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        super.setName(name);
        KeywordInvokable newElement = new ExtRobotPsiUtils(this).createKeyword(name);
        this.getNode().getTreeParent().replaceChild(this.getNode(), newElement.getNode());
        return this;
    }
}