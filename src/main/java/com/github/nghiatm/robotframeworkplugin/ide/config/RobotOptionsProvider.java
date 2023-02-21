package com.github.nghiatm.robotframeworkplugin.ide.config;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author mrubino
 * @since 2014-06-26
 */
@State(
        name = "RobotOptionsProvider",
        storages = {
                @Storage(value = StoragePathMacros.WORKSPACE_FILE)
        }
)
public class RobotOptionsProvider implements PersistentStateComponent<RobotOptionsProvider.State> {

    public static class State {
        public boolean transitiveImports = true;
        public boolean globalVariables = true;
        public boolean debug = false;
        public boolean capitalizeKeywords = true;
        public boolean inlineVariableSearch = false;
        public Map<String, String> variables = new HashMap<>();

    }

    private final State state = new State();

    public static RobotOptionsProvider getInstance(Project project) {
        return ServiceManager.getService(project, RobotOptionsProvider.class);
    }

    @Nullable
    @Override
    public State getState() {
        return this.state;
    }

    @Override
    public void loadState(State state) {
        this.state.debug = state.debug;
        this.state.transitiveImports = state.transitiveImports;
        this.state.globalVariables = state.globalVariables;
        this.state.capitalizeKeywords = state.capitalizeKeywords;
        this.state.inlineVariableSearch = state.inlineVariableSearch;
        this.state.variables = state.variables;
    }

    public void setVariables(Map<String, String> variables){
        this.state.variables = variables;
    }

    public void setVariables(String[][] variables){
        for(int i=0; i< variables.length; i++){
            String key = variables[i][0];
            this.state.variables.put(key, variables[i][1]);
        }
    }

    public Map<String, String> getVariables(){
        return this.state.variables;
    }

    public boolean isDebug() {
        return this.state.debug;
    }

    public void setDebug(boolean debug) {
        this.state.debug = debug;
    }

    public boolean allowTransitiveImports() {
        return this.state.transitiveImports;
    }

    public void setTransitiveImports(boolean transitiveImports) {
        this.state.transitiveImports = transitiveImports;
    }

    public boolean allowGlobalVariables() {
        return this.state.globalVariables;
    }

    public void setGlobalVariables(boolean globalVariables) {
        this.state.globalVariables = globalVariables;
    }

    public boolean capitalizeKeywords() {
        return this.state.capitalizeKeywords;
    }

    public void setCapitalizeKeywords(boolean capitalizeKeywords) {
        this.state.capitalizeKeywords = capitalizeKeywords;
    }

    public boolean inlineVariableSearch() {
        return this.state.inlineVariableSearch;
    }

    public void setInlineVariableSearch(boolean inlineVariableSearch) {
        this.state.inlineVariableSearch = inlineVariableSearch;
    }
}
