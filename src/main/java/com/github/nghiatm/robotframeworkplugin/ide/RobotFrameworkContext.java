package com.github.nghiatm.robotframeworkplugin.ide;

import com.github.nghiatm.robotframeworkplugin.psi.RobotLanguage;
import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;

public class RobotFrameworkContext extends TemplateContextType {
    protected RobotFrameworkContext(){
        super("Robot", "Robot Framework");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return templateActionContext.getFile().getLanguage().is(RobotLanguage.INSTANCE);
    }
}
