package com.github.nghiatm.robotframeworkplugin.ide.execution.runconfig;

import com.github.nghiatm.robotframeworkplugin.ide.icons.RobotIcons;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.ConfigurationTypeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.Icon;

public class RobotConfigurationType implements ConfigurationType {
    public static RobotConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(RobotConfigurationType.class);
    }

    @Override
    public @NotNull @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Robot Framework";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Sentence) String getConfigurationTypeDescription() {
        return "Robot Framework Run Configuration";
    }

    @Override
    public Icon getIcon() {
        return RobotIcons.RUN_CONFIG;
    }

    @Override
    public @NotNull
    @NonNls
    String getId() {
        return "RobotFramework";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{new RobotRunConfigurationFactory(this)};
    }
}
