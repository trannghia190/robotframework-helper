package com.github.nghiatm.robotframeworkplugin.ide.action;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class PabotRunActionGroup extends DefaultActionGroup {
    public static final int DISPLAY_TEXT_LENGTH = 18;
    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        Project project = e.getProject();
        Map<String, String> templates = RobotOptionsProvider.getInstance(project).getExecParamsTemplate();
        AnAction[] anActions = new AnAction[templates.size()];
        int index = 0;
        for(Map.Entry<String, String> entry: templates.entrySet()){
            String name = entry.getKey();
            String value = entry.getValue();
            AnAction anAction = new PabotRunTestCaseAction(name, value);
            anActions[index] = anAction;
            index++;
        }
        return anActions;
    }

    public static boolean isContainRobotFile(VirtualFile folder){
        if(folder == null || !folder.isDirectory()){
            return false;
        }

        VirtualFile[] files = folder.getChildren();
        for (VirtualFile file : files){
            if(!file.isDirectory()){
                if(file.getFileType() instanceof RobotFeatureFileType){
                    return true;
                }
            }else{
                return isContainRobotFile(file);
            }
        }
        return false;
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        boolean isEnablePabot = RobotOptionsProvider.getInstance().getIsEnablePabot();
        if(BooleanUtils.isFalse(isEnablePabot)) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }
        VirtualFile[] files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY);
        if(files != null && files.length > 1){
            e.getPresentation().setText("[Pabot] Run all ["+ files.length +"] suites ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file != null && file.isDirectory() && isContainRobotFile(file)) {
            e.getPresentation().setText("[Pabot] Run suite '" + getDisplayName(file.getNameWithoutExtension()) + "' ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        PsiElement element = e.getData(LangDataKeys.PSI_ELEMENT);
        if ( editor != null && element!= null ){
            PsiElement parentElement = element.getParent();
            if(parentElement!=null){
                if (parentElement.getText().trim().toLowerCase().startsWith("[tags]") ){
                    e.getPresentation().setText("[Pabot] Run with tag '" + element.getText() + "' ...");
                    e.getPresentation().setEnabledAndVisible(true);
                    return;
                }
            }
        }else if (file != null && file.exists() && file.getFileType() instanceof RobotFeatureFileType){
            e.getPresentation().setText("[Pabot] Run suite '" + getDisplayName(file.getNameWithoutExtension()) + "' ...");
            e.getPresentation().setEnabledAndVisible(true);
            return;
        }
        e.getPresentation().setEnabledAndVisible(false);
    }

    public static String getDisplayName(String name){
        if(name.length() > DISPLAY_TEXT_LENGTH){
            name= name.substring(0, DISPLAY_TEXT_LENGTH) + " ...";
        }
        return name;
    }

}
