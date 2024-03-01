package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ShowResultAction extends AnAction {

    public ShowResultAction(){
        super("Open Result", "Open Result In Browser", AllIcons.General.Web);
    }
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        File file = new File(getLogFilePath(e.getProject()));
//        LogUtil.debug("File: "+ file.getAbsolutePath(), null, null, e.getProject());
        if(file.exists()){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(file.toURI());
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }

    @Override
    public void update(@NotNull AnActionEvent e) {

        File file = new File(getLogFilePath(e.getProject()));
        if(file.exists()){
            e.getPresentation().setEnabledAndVisible(true);
        }else{
            e.getPresentation().setEnabledAndVisible(false);
        }

    }

    private String getLogFilePath(Project project){
        String outputDir = RobotOptionsProvider.getInstance(project).getOutputDir();
        String path= project.getBasePath();
        if(StringUtils.isNotBlank(outputDir)) {
            path += File.separator + outputDir;
        }
        return path +"/log.html";
    }
}
