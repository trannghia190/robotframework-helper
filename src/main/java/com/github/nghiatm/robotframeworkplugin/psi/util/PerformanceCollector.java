package com.github.nghiatm.robotframeworkplugin.psi.util;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import org.jetbrains.annotations.NotNull;

/**
 * @author mrubino
 * @since 2014-06-26
 */
public class PerformanceCollector {

    private static final String MILLISECONDS = "ms";
    private static final long MINIMUM = 500;

    private final String context;
    private final PerformanceEntity entity;
    private final long start;

    public PerformanceCollector(@NotNull PerformanceEntity entity, @NotNull String context) {
        this.entity = entity;
        this.context = context;
        this.start = System.currentTimeMillis();
    }

    public void complete() {
        try {
            if (RobotOptionsProvider.getInstance(this.entity.getProject()).isDebug()) {
                long duration = System.currentTimeMillis() - this.start;
                if (duration > MINIMUM) {
                    String message = String.format("[%s][%s][%s] %d%s",
                            this.entity.getDebugFileName(), this.context, this.entity.getDebugText(), duration, MILLISECONDS);
                    LogUtil.debug(message, "PerformanceCollector", "complete", this.entity.getProject());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
