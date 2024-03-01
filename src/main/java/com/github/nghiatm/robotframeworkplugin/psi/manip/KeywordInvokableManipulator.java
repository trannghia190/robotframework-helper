package com.github.nghiatm.robotframeworkplugin.psi.manip;

import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokable;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author nghiatm
 */
public class KeywordInvokableManipulator extends AbstractElementManipulator<KeywordInvokable> {

    @Override
    public KeywordInvokable handleContentChange(@NotNull KeywordInvokable element, @NotNull TextRange range,
                                                String newContent) throws IncorrectOperationException {
        LogUtil.debug(element.getText(), "KeywordInvokableManipulator", "handleContent", element.getProject());
        element.setName(newContent);
        return element;
    }
}
