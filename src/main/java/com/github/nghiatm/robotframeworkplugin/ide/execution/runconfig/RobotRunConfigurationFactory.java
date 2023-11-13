package com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RobotRunConfigurationFactory extends ConfigurationFactory {
    protected RobotRunConfigurationFactory(@NotNull ConfigurationType type) {
        super(type);
    }

    @Override
    public @NotNull String getId() {
        return "RobotFrameworkRunConfigFactory";
    }

    @Override
    public @NotNull String getName() {
        return "Robot Framework Run Configuration Factory";
    }

    @Override
    public @NotNull RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new RobotRunConfiguration(project, this);
    }
}
