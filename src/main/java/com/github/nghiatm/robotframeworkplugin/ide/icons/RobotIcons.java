package com.github.nghiatm.robotframeworkplugin.ide.icons;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.IconLoader;

import javax.swing.Icon;

/**
 * @author mrubino
 * @since 2016-01-28
 */
public class RobotIcons {

    public static final Icon FILE = IconLoader.findIcon("/images/robot.svg");
    public static final Icon RESOURCE = IconLoader.findIcon("/images/resource.svg");
    public static final Icon RUN_CONFIG = IconLoader.findIcon("/images/robot-runconfig-icon.svg");
    public static final Icon HEADING = AllIcons.Nodes.Tag;
    public static final Icon KEYWORD_DEFINITION = AllIcons.Nodes.Method;
    public static final Icon TEST_CASE = AllIcons.RunConfigurations.Junit;
    public static final Icon VARIABLE_DEFINITION = AllIcons.Nodes.Variable;

    private RobotIcons() {
    }
}
