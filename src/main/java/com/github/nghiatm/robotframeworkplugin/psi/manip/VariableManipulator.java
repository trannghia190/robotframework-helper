package com.github.nghiatm.robotframeworkplugin.psi.manip;

import com.github.nghiatm.robotframeworkplugin.psi.element.VariableImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.LogUtil;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author nghiatm
 */
public class VariableManipulator extends AbstractElementManipulator<VariableImpl> {

    @Override
    public VariableImpl handleContentChange(@NotNull VariableImpl element, @NotNull TextRange range,
                                        String newContent) throws IncorrectOperationException {
        LogUtil.debug(element.getText(), "VariableManipulator", "handleContent", element.getProject());
        element.setName(newContent);
        return element;
    }
}
