package com.github.nghiatm.robotframeworkplugin.psi.util;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class LogUtil {

    public static void debug(@NotNull String logMessage, String className, String functionName, @NotNull Project project) {
        if (RobotOptionsProvider.getInstance(project).isDebug()) {
            String message = String.format("[%s][%s] %s", className, functionName, logMessage);
            Notifications.Bus.notify(new Notification("intellibot.debug", "Debug", message, NotificationType.INFORMATION));
        }
    }
}
