package com.github.nghiatm.robotframeworkplugin.ide.inspections.compilation;

import com.github.nghiatm.robotframeworkplugin.RobotBundle;
import com.github.nghiatm.robotframeworkplugin.ide.inspections.SimpleRobotInspection;
import com.github.nghiatm.robotframeworkplugin.psi.RobotTokenTypes;
import com.github.nghiatm.robotframeworkplugin.psi.element.Argument;
import com.github.nghiatm.robotframeworkplugin.psi.element.Import;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 * @since 2014-06-07
 */
public class RobotImportNotFound extends SimpleRobotInspection {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return RobotBundle.message("INSP.NAME.import.undefined");
    }

    @Override
    public boolean skip(PsiElement element) {
        if (!(element instanceof Argument)) {
            return true;
        }
        if (element.getNode().getElementType() != RobotTokenTypes.ARGUMENT) {
            return true;
        }
        PsiElement parent = element.getParent();
        if (parent instanceof Import) {
            PsiElement[] children = parent.getChildren();
            // first child seems to be different than this
            if (children.length > 0 && children[0] == element) {
                PsiReference reference = element.getReference();
                return reference != null && reference.resolve() != null;
            } else {
                return true;
            }
            
        } else {
            return true;
        }
    }

    @Override
    public String getMessage() {
        return RobotBundle.message("INSP.import.undefined");
    }

    @NotNull
    @Override
    protected String getGroupNameKey() {
        return "INSP.GROUP.compilation";
    }
}
