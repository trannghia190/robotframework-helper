package com.github.nghiatm.robotframeworkplugin.psi;

import com.intellij.lang.Language;
import com.intellij.psi.tree.IStubFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class RobotFileElementType extends IStubFileElementType {
    public static final String ID_PREFIX = "robot.";
    public RobotFileElementType(@NotNull @NonNls String debugName, @NotNull @NonNls Language language) {
        super(debugName, language);
    }

    @Override
    public @NotNull String getExternalId() {
        return ID_PREFIX + super.getExternalId();
    }
}
