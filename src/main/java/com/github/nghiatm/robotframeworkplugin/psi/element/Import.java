package com.github.nghiatm.robotframeworkplugin.psi.element;

/**
 * @author mrubino
 */
public interface Import extends RobotStatement {

    boolean isResource();

    boolean isLibrary();
    
    boolean isVariables();
}
