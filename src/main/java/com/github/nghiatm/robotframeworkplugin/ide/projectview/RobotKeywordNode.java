package com.github.nghiatm.robotframeworkplugin.ide.projectview;

import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.BasePsiNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.util.Collection;
import java.util.Collections;

public class RobotKeywordNode extends BasePsiNode<DefinedKeyword> {

    public RobotKeywordNode(Project project, DefinedKeyword element, ViewSettings viewSettings) {
        super(project, element, viewSettings);
    }

    @Override
    protected @Nullable Collection<AbstractTreeNode<?>> getChildrenImpl() {
        return Collections.emptyList();
    }

    @Override
    protected void updateImpl(@NotNull PresentationData data) {
        final DefinedKeyword value = getValue();
        final String name = value.getKeywordName();
        String presentableText = name != null ? name : "<unnamed>";
        Icon presentableIcon = value.getIcon(0);
        data.setPresentableText(presentableText);
        data.setIcon(presentableIcon);
    }

    @Override
    public int getTypeSortWeight(boolean sortByType) {
        if (!sortByType) {
            return 0;
        }
        return getValue() instanceof KeywordDefinitionImpl && ((KeywordDefinitionImpl)getValue()).isTestCase()? 10 : 20;
    }


    @Override
    public boolean expandOnDoubleClick() {
        return false;
    }
}
