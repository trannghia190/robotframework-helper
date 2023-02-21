package com.github.nghiatm.robotframeworkplugin.psi;

import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 */
public class RobotFileTypeHandler extends FileTypeFactory {

    @Override
    public void createFileTypes(@NotNull FileTypeConsumer consumer) {
        consumer.consume(RobotFeatureFileType.getInstance());
        consumer.consume(RobotResourceFileType.getInstance());
    }
}
