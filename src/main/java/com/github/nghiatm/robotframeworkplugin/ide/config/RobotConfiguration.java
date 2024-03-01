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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mrubino
 * @since 2014-06-26
 */
public class RobotConfiguration implements SearchableConfigurable, Configurable.NoScroll {

    private RobotOptionsProvider provider;

    private JPanel panel;
    private JCheckBox enableDebug;
    private JCheckBox allowTransitiveImports;
    private JCheckBox allowGlobalVariables;
    private JCheckBox capitalizeKeywords;
    private JCheckBox inlineVariableSearch;
    private JTable execVariablesTable;
    private JTabbedPane tabbedPane1;
    private JButton addVariableBtn;
    private JCheckBox searchChildKeywords;
    private JCheckBox expandSuperSpaces;
    private JTable replacementVariables;
    private JButton addReplacementVarBtn;
    private JTable paramTemplatesTbl;
    private JButton addTemplateBtn;
    private JTextField txtOutputDir;
    private JCheckBox enablePabotRunCheckBox;
    private JSpinner numberOfProcessesSpinner;

    public RobotConfiguration() {
        this.provider = getOptionProvider();
    }

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
        this.execVariablesTable.setEnabled(true);
        this.addVariableBtn.addActionListener(e -> ((DefaultTableModel) execVariablesTable.getModel()).addRow(new String[]{"",""}));

        this.replacementVariables.setEnabled(true);
        this.addReplacementVarBtn.addActionListener(e -> ((DefaultTableModel)replacementVariables.getModel()).addRow(new String[]{"",""}));

        this.paramTemplatesTbl.setEnabled(true);
        this.addTemplateBtn.addActionListener(e -> ((DefaultTableModel)paramTemplatesTbl.getModel()).addRow(new String[]{"",""}));
        return this.panel;
    }

    @Override
    public boolean isModified() {
        if (provider != null) {
            return provider.isDebug() != this.enableDebug.isSelected() ||
                    provider.allowTransitiveImports() != this.allowTransitiveImports.isSelected() ||
                    provider.allowGlobalVariables() != this.allowGlobalVariables.isSelected() ||
                    provider.capitalizeKeywords() != this.capitalizeKeywords.isSelected() ||
                    provider.inlineVariableSearch() != this.inlineVariableSearch.isSelected()||
                    provider.getExecVariables().size() != this.execVariablesTable.getRowCount() || isVariablesChange(provider.getExecVariables(), this.execVariablesTable.getModel()) ||
                    provider.getReplacementVariables().size() != this.replacementVariables.getRowCount() || isVariablesChange(provider.getReplacementVariables(), this.replacementVariables.getModel()) ||
                    provider.getExecParamsTemplate().size() != this.paramTemplatesTbl.getRowCount() || isVariablesChange(provider.getExecParamsTemplate(), this.paramTemplatesTbl.getModel()) ||
                    provider.searchChildKeywords() != this.searchChildKeywords.isSelected() ||
                    provider.expandSuperSpaces() != this.expandSuperSpaces.isSelected() ||
                    provider.getOutputDir() != this.txtOutputDir.getText() ||
                    provider.getIsEnablePabot() != this.enablePabotRunCheckBox.isSelected() ||
                    provider.getNumberOfPabotProcess() != this.numberOfProcessesSpinner.getValue()
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
        if (provider != null) {
            provider.setDebug(this.enableDebug.isSelected());
            provider.setTransitiveImports(this.allowTransitiveImports.isSelected());
            provider.setGlobalVariables(this.allowGlobalVariables.isSelected());
            provider.setCapitalizeKeywords(this.capitalizeKeywords.isSelected());
            provider.setInlineVariableSearch(this.inlineVariableSearch.isSelected());
            provider.setSearchChildKeywords(this.searchChildKeywords.isSelected());
            provider.setOutputDir(this.txtOutputDir.getText().trim());

            provider.setIsEnablePabot(this.enablePabotRunCheckBox.isSelected());
            provider.setNumberOfPabotProcess((Integer) (this.numberOfProcessesSpinner.getModel()).getValue());

            provider.setExecVariables(getMapDataFromTable(this.execVariablesTable));
            provider.setReplacementVariables(getMapDataFromTable(this.replacementVariables));
            provider.setExecParamsTemplate(getMapDataFromTable(this.paramTemplatesTbl));
        }
    }

    @NotNull
    private Map<String, String> getMapDataFromTable(JTable table) {
        int rowCount = table.getRowCount();
        TableModel tableModel = table.getModel();
        Map<String, String> variables = new LinkedHashMap<>();
        for(int i=0; i<rowCount;i++) {
            String key = (String) tableModel.getValueAt(i, 0);
            String value = (String) tableModel.getValueAt(i, 1);
            if(key!= null && value!= null && key.trim().length()>0 && value.trim().length()>0)
                variables.put(key,  value);
        }
        return variables;
    }

    @Override
    public void reset() {
        if (provider != null) {
            this.enableDebug.setSelected(provider.isDebug());
            this.allowTransitiveImports.setSelected(provider.allowTransitiveImports());
            this.allowGlobalVariables.setSelected(provider.allowGlobalVariables());
            this.capitalizeKeywords.setSelected(provider.capitalizeKeywords());
            this.inlineVariableSearch.setSelected(provider.inlineVariableSearch());
            this.searchChildKeywords.setSelected(provider.searchChildKeywords());
            this.expandSuperSpaces.setSelected(provider.expandSuperSpaces());
            this.txtOutputDir.setText(provider.getOutputDir());

            this.execVariablesTable.setModel(createKVModelFromMap(provider.getExecVariables()));
            this.replacementVariables.setModel(createKVModelFromMap(provider.getReplacementVariables()));
            this.paramTemplatesTbl.setModel(createKVModelFromMap(provider.getExecParamsTemplate()));

            this.enablePabotRunCheckBox.setSelected(provider.getIsEnablePabot());
            SpinnerModel spinnerModel = new SpinnerNumberModel(provider.getNumberOfPabotProcess()==null?3:provider.getNumberOfPabotProcess().intValue(), 1, 5, 1);
            this.numberOfProcessesSpinner.setModel(spinnerModel);
        }
    }

    @NotNull
    private DefaultTableModel createKVModelFromMap(Map<String, String> map) {
        DefaultTableModel defaultTableModel = new DefaultTableModel();
        defaultTableModel.addColumn("Key");
        defaultTableModel.addColumn("Value");
        for(Map.Entry<String, String> entry: map.entrySet()){
            String[] rowData = {entry.getKey(), entry.getValue()};
            defaultTableModel.addRow(rowData);
        }
        return defaultTableModel;
    }

    @Override
    public void disposeUIResources() {
    }
}
