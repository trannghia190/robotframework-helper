package com.github.nghiatm.robotframeworkplugin.ide.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mrubino
 * @since 2014-06-26
 */
public class RobotConfiguration implements SearchableConfigurable, Configurable.NoScroll {

    private JPanel panel;
    private JCheckBox enableDebug;
    private JCheckBox allowTransitiveImports;
    private JCheckBox allowGlobalVariables;
    private JCheckBox capitalizeKeywords;
    private JCheckBox inlineVariableSearch;
    private JTable variables_table;
    private JTabbedPane tabbedPane1;
    private JButton addVariableBtn;

    @Nullable
    private RobotOptionsProvider getOptionProvider() {
        Project[] projects = ProjectManager.getInstance().getOpenProjects();
        if (projects.length > 0) {
            return RobotOptionsProvider.getInstance(projects[0]);
        } else {
            return null;
        }
    }

    @NotNull
    @Override
    public String getId() {
        return getHelpTopic();
    }

    @Nullable
    @Override
    public Runnable enableSearch(String s) {
        return null;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Robot Options";
    }

    @NotNull
    @Override
    public String getHelpTopic() {
        return "reference.idesettings.robot";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn("Key");
        defaultTableModel.addColumn("Value");
        defaultTableModel.addRow(new String[]{"Test", "Test"});
        this.variables_table.setModel(defaultTableModel);
        this.variables_table.setEnabled(true);

        this.addVariableBtn.addActionListener(e -> ((DefaultTableModel)variables_table.getModel()).addRow(new String[]{"",""}));
        return this.panel;
    }

    @Override
    public boolean isModified() {
        RobotOptionsProvider provider = getOptionProvider();
        if (provider != null) {
            return provider.isDebug() != this.enableDebug.isSelected() ||
                    provider.allowTransitiveImports() != this.allowTransitiveImports.isSelected() ||
                    provider.allowGlobalVariables() != this.allowGlobalVariables.isSelected() ||
                    provider.capitalizeKeywords() != this.capitalizeKeywords.isSelected() ||
                    provider.inlineVariableSearch() != this.inlineVariableSearch.isSelected()||
                    provider.getVariables().size() != this.variables_table.getRowCount() || isVariablesChange(provider.getVariables(), this.variables_table.getModel())
                    ;
        } else {
            return false;
        }
    }

    private boolean isVariablesChange(Map<String, String> providerVariables, TableModel tableModel){
        int index = 0;
        for(Map.Entry<String, String> entry : providerVariables.entrySet()){
            try {
                String pKey = entry.getKey();
                String pValue = entry.getValue();
                String tKey = (String) tableModel.getValueAt(index, 0);
                String tValue = (String) tableModel.getValueAt(index, 1);
                if(!pKey.equals(tKey) || !pValue.equals(tValue)){
                    return true;
                }
            }catch (Exception e){
                return true;
            }
            index++;
        }
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        RobotOptionsProvider provider = getOptionProvider();
        if (provider != null) {
            provider.setDebug(this.enableDebug.isSelected());
            provider.setTransitiveImports(this.allowTransitiveImports.isSelected());
            provider.setGlobalVariables(this.allowGlobalVariables.isSelected());
            provider.setCapitalizeKeywords(this.capitalizeKeywords.isSelected());
            provider.setInlineVariableSearch(this.inlineVariableSearch.isSelected());

            int rowCount = this.variables_table.getRowCount();
            TableModel tableModel = this.variables_table.getModel();
            Map<String, String> variables = new HashMap<>();
            for(int i=0; i<rowCount;i++) {
                String key = (String) tableModel.getValueAt(i, 0);
                String value = (String) tableModel.getValueAt(i, 1);
                if(key!= null && value!= null && key.trim().length()>0 && value.trim().length()>0)
                    variables.put(key,  value);
            }
            provider.setVariables(variables);
        }
    }

    @Override
    public void reset() {
        RobotOptionsProvider provider = getOptionProvider();
        if (provider != null) {
            this.enableDebug.setSelected(provider.isDebug());
            this.allowTransitiveImports.setSelected(provider.allowTransitiveImports());
            this.allowGlobalVariables.setSelected(provider.allowGlobalVariables());
            this.capitalizeKeywords.setSelected(provider.capitalizeKeywords());
            this.inlineVariableSearch.setSelected(provider.inlineVariableSearch());
            DefaultTableModel defaultTableModel = new DefaultTableModel();
            defaultTableModel.addColumn("Key");
            defaultTableModel.addColumn("Value");
            for(Map.Entry<String, String> entry: provider.getVariables().entrySet()){
                String[] rowData = {entry.getKey(), entry.getValue()};
                defaultTableModel.addRow(rowData);
            }
            this.variables_table.setModel(defaultTableModel);

        }
    }

    @Override
    public void disposeUIResources() {
    }
//
//    private void createUIComponents() {
//        String[] cols = {"Key", "Value"};
//        RobotOptionsProvider provider = getOptionProvider();
//        Map<String, String> variables = provider.getVariables();
//        String[][] data = new String[variables.size()][2];
//        int index = 0;
//        for(Map.Entry<String, String> entry: variables.entrySet()){
//            data[index][0] = entry.getKey();
//            data[index][1] = entry.getValue();
//            index++;
//        }
//        variables_table = new JTable(data, cols);
//    }
}
