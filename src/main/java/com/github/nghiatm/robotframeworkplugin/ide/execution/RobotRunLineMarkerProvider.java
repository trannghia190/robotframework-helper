package com.github.nghiatm.robotframeworkplugin.ide.execution;

import com.github.nghiatm.robotframeworkplugin.ide.action.RunTestCaseAction;
import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotFeatureFileType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.tree.LeafElement;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.intellij.openapi.util.text.StringUtil.join;
import static com.intellij.util.containers.ContainerUtil.mapNotNull;

public class RobotRunLineMarkerProvider extends RunLineMarkerContributor {

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {
        if (element.getContainingFile().getVirtualFile().getFileType() instanceof RobotFeatureFileType) {
            try{
                PsiElement targetElement = element.getParent().getParent();
                if (element instanceof LeafElement &&
                        targetElement instanceof KeywordDefinitionImpl) {
                    Collection<DefinedKeyword> testCases = RobotUtil.getTestCasesFromElement(element);
                    for (DefinedKeyword testCase: testCases) {
                        if (testCase.getKeywordName().equals(element.getText())) {
                            final AnAction[] actions = ExecutorAction.getActions();
                            AnAction[] customActions = getCustomActions(element);
                            AnAction[] allActions = (AnAction[]) ArrayUtils.addAll(customActions, actions);

                            return new Info(AllIcons.RunConfigurations.TestState.Run, allActions,
                                    e -> join(mapNotNull(allActions, action -> getText(action, e)), "\n"));
                        }
                    }
                }else if (isTestCases(element.getText())){
                    final AnAction[] actions = ExecutorAction.getActions();
                    return new Info(AllIcons.RunConfigurations.TestState.Run, actions, e -> "Run Suite");
                }
            }catch (Exception e){
            }

        }
        return null;
    }

    private static boolean isTestCases(String line) {
        return "*** Test Cases ***".equals(line) || "*** Test Case ***".equals(line) || line.matches("\\*\\*\\* (Test Cases?|Tasks?) \\*\\*\\*");
    }

    private AnAction[] getCustomActions(PsiElement element){

        RobotOptionsProvider.getInstance(element.getProject()).setStateChangeListner(() -> {
            Project project = element.getProject();
            refreshAllLineMarkers(project);
        });
        Map<String, String> templates = RobotOptionsProvider.getInstance(element.getProject()).getExecParamsTemplate();
        AnAction[] anActions = new AnAction[templates.size()];
        int index = 0;
        for(Map.Entry<String, String> entry: templates.entrySet()){
            String name = entry.getKey();
            String value = entry.getValue();
            AnAction anAction = new RunTestCaseAction(name, value, element);
            anActions[index] = anAction;
            index++;
        }
        return anActions;
    }

    public static void refreshAllLineMarkers(Project project) {
        // Get all opened files in the editor
        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        List<TextEditor> textEditors = Arrays.asList(fileEditorManager.getAllEditors())
                .stream()
                .filter(TextEditor.class::isInstance)
                .map(TextEditor.class::cast)
                .collect(Collectors.toList());

        for (TextEditor textEditor : textEditors) {
            VirtualFile virtualFile = textEditor.getFile();
            if (virtualFile != null) {
                PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
                if (psiFile != null) {
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                }
            }
        }
    }

}
