package com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public class RobotRunConfigurationEditor extends SettingsEditor<RobotRunConfiguration> {
    private final RobotRunConfigurationForm form;

    public RobotRunConfigurationEditor(@NotNull Project project, RobotRunConfiguration configuration) {
        form = new RobotRunConfigurationForm(configuration);
        form.resetEditorFrom(configuration);
    }

    @Override
    protected void resetEditorFrom(@NotNull RobotRunConfiguration s) {
        form.resetEditorFrom(s);
    }

    @Override
    protected void applyEditorTo(@NotNull RobotRunConfiguration s) throws ConfigurationException {
        form.applyEditorTo(s);
    }

    @Override
    protected @NotNull JComponent createEditor() {
        return form.getPanel();
    }
}
