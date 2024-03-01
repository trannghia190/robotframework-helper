package com.github.nghiatm.robotframeworkplugin.ide.config;

import com.github.nghiatm.robotframeworkplugin.ide.listener.StateChangeListner;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationsConfiguration;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author mrubino
 * @since 2014-06-26
 */
@State(
        name = "RobotOptionsProvider",
        storages = {
                @Storage(value = "robot_helper.xml")
        }
)
public class RobotOptionsProvider implements PersistentStateComponent<RobotOptionsProvider.State> {

    static {
        NotificationsConfiguration.getNotificationsConfiguration().register(
                "robotframework.helper.debug", NotificationDisplayType.NONE);
    }


    public static class State {
        public boolean transitiveImports = true;
        public boolean globalVariables = true;
        public boolean debug = false;
        public boolean capitalizeKeywords = true;
        public boolean inlineVariableSearch = false;
        public boolean searchChildKeywords = true;
        public boolean expandSuperSpaces = true;
        public String outputDir = "";
        public Map<String, String> execVariables = new LinkedHashMap<>();
        public Map<String, String> replacementVariables = new LinkedHashMap<>();
        public Map<String, String> execParamsTemplate = new LinkedHashMap<>();
        public boolean isEnablePabot = false;
        public Integer numberOfPabotProcessess = null;

        public State(){
            execParamsTemplate.put("default", "-v browser:chrome");
        }
    }

    private final State state = new State();

    private StateChangeListner stateChangeListner;

    @Deprecated
    public static RobotOptionsProvider getInstance(Project project) {
        return getInstance();
    }

    public static RobotOptionsProvider getInstance() {
        return ServiceManager.getService(RobotOptionsProvider.class);
    }


    @Nullable
    @Override
    public State getState() {
        if(stateChangeListner!=null){
            stateChangeListner.onStateChange();
        }
        return this.state;
    }

    public void setStateChangeListner(StateChangeListner listner){
        this.stateChangeListner = listner;
    }

    @Override
    public void loadState(State state) {
        this.state.debug = state.debug;
        this.state.transitiveImports = state.transitiveImports;
        this.state.globalVariables = state.globalVariables;
        this.state.capitalizeKeywords = state.capitalizeKeywords;
        this.state.inlineVariableSearch = state.inlineVariableSearch;
        this.state.execVariables = state.execVariables;
        this.state.replacementVariables = state.replacementVariables;
        this.state.searchChildKeywords = state.searchChildKeywords;
        this.state.expandSuperSpaces = state.expandSuperSpaces;
        this.state.execParamsTemplate = state.execParamsTemplate;
        this.state.outputDir = state.outputDir;
        this.state.isEnablePabot = state.isEnablePabot;
        this.state.numberOfPabotProcessess = state.numberOfPabotProcessess;
    }

    public void setNumberOfPabotProcess(Integer number){
        this.state.numberOfPabotProcessess = number;
    }

    public Integer getNumberOfPabotProcess(){
        return this.state.numberOfPabotProcessess;
    }

    public void setIsEnablePabot (boolean isEnablePabot){
        this.state.isEnablePabot = isEnablePabot;
    }

    public boolean getIsEnablePabot (){
        return this.state.isEnablePabot;
    }

    public void setOutputDir(String dir){
        this.state.outputDir = dir;
    }

    public String getOutputDir(){
        return this.state.outputDir;
    }

    public void setExpandSuperSpaces(boolean expandSuperSpaces){
        this.state.expandSuperSpaces = expandSuperSpaces;
    }

    public boolean expandSuperSpaces(){
        return this.state.expandSuperSpaces;
    }

    public void setReplacementVariables(Map<String, String> variables){
        this.state.replacementVariables = variables;
    }

    public Map<String, String> getReplacementVariables(){
        return this.state.replacementVariables;
    }

    public void setExecVariables(Map<String, String> variables){
        this.state.execVariables = variables;
    }

    public void setExecVariables(String[][] variables){
        for(int i=0; i< variables.length; i++){
            String key = variables[i][0];
            this.state.execVariables.put(key, variables[i][1]);
        }
    }

    public Map<String, String> getExecVariables(){
        return this.state.execVariables;
    }

    public void setExecParamsTemplate(Map<String, String> template){
        this.state.execParamsTemplate = template;
    }

    public Map<String, String> getExecParamsTemplate(){
        return this.state.execParamsTemplate;
    }

    public boolean searchChildKeywords() {
        return this.state.searchChildKeywords;
    }

    public void setSearchChildKeywords(boolean searchChildKeywords) {
        this.state.searchChildKeywords = searchChildKeywords;
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
