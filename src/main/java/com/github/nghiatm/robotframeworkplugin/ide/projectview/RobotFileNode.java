package com.github.nghiatm.robotframeworkplugin.ide.projectview;

import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFile;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFileImpl;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RobotFileNode extends PsiFileNode {
    public RobotFileNode(Project project, @NotNull PsiFile value, ViewSettings viewSettings) {
        super(project, value, viewSettings);
    }

    @Override
    public Collection<AbstractTreeNode<?>> getChildrenImpl() {
        RobotFile value = (RobotFile) getValue();
        List<AbstractTreeNode<?>> children = new ArrayList<>();

        ((RobotFileImpl)value).getHeadings().stream().forEach(heading -> {
            heading.getTestCases().forEach(testcase -> {
                children.add(new RobotKeywordNode(myProject, testcase, getSettings()));
            });
        });

        for (DefinedKeyword child : value.getDefinedKeywords()) {
            children.add(new RobotKeywordNode(myProject, child, getSettings()));
        }
        return children;
    }

    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }
}
