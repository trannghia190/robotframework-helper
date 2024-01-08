package com.github.nghiatm.robotframeworkplugin.ide.projectview;

import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFile;
import com.intellij.ide.projectView.SelectableTreeStructureProvider;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RobotStructureProvider implements SelectableTreeStructureProvider, DumbAware {
    @Override
    public @Nullable PsiElement getTopLevelElement(PsiElement element) {

        final Ref<PsiFile> containingFileRef = Ref.create();
        ApplicationManager.getApplication().runReadAction(() -> containingFileRef.set(element.getContainingFile()));
        final PsiFile containingFile = containingFileRef.get();
        if (!(containingFile instanceof RobotFile)) {
            return null;
        }

        return element.getContainingFile();
    }

    @Override
    public @NotNull Collection<AbstractTreeNode<?>> modify(@NotNull AbstractTreeNode<?> parent, @NotNull Collection<AbstractTreeNode<?>> children, ViewSettings settings) {
        final Project project = parent.getProject();

        if (settings != null && settings.isShowMembers()) {
            List<AbstractTreeNode<?>> newChildren = new ArrayList<>();
            for (AbstractTreeNode child : children) {
                PsiFile f;
                if (child instanceof PsiFileNode && (f = ((PsiFileNode)child).getValue()) instanceof RobotFile) {
                    newChildren.add(new RobotFileNode(project, f, settings));
                }
                else {
                    newChildren.add(child);
                }
            }
            return newChildren;
        }
        return children;
    }
}
