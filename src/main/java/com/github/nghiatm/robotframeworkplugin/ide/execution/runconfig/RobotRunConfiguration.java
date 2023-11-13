package com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonRunConfiguration;

public class RobotRunConfiguration extends PythonRunConfiguration {
    protected RobotRunConfiguration(Project project, ConfigurationFactory configurationFactory) {
        super(project, configurationFactory);
    }

    @Override
    protected SettingsEditor<? extends RunConfiguration> createConfigurationEditor() {
        return new RobotRunConfigurationEditor(getProject(), this);
    }
}
