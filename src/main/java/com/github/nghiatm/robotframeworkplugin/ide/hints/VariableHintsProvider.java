package com.github.nghiatm.robotframeworkplugin.ide.hints;

import com.github.nghiatm.robotframeworkplugin.psi.RobotLanguage;
import com.github.nghiatm.robotframeworkplugin.psi.element.Variable;
import com.intellij.codeInsight.hints.ChangeListener;
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector;
import com.intellij.codeInsight.hints.ImmediateConfigurable;
import com.intellij.codeInsight.hints.InlayHintsCollector;
import com.intellij.codeInsight.hints.InlayHintsProvider;
import com.intellij.codeInsight.hints.InlayHintsSink;
import com.intellij.codeInsight.hints.NoSettings;
import com.intellij.codeInsight.hints.SettingsKey;
import com.intellij.codeInsight.hints.presentation.InlayPresentation;
import com.intellij.codeInsight.hints.presentation.PresentationFactory;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ui.UI;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableHintsProvider implements InlayHintsProvider<NoSettings> {
    @Override
    public boolean isVisibleInSettings() {
        return true;
    }

    @NotNull
    @Override
    public SettingsKey getKey() {
        return new SettingsKey<NoSettings>("RobotVariableValue");
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getName() {
        return "Variable Value";
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return null;
    }

    @NotNull
    @Override
    public ImmediateConfigurable createConfigurable(@NotNull NoSettings o) {
        return new ImmediateConfigurable() {
            @NotNull
            @Override
            public JComponent createComponent(@NotNull ChangeListener listener) {
                JPanel panel = UI.PanelFactory.panel(new JLabel()).
                        withComment("This will display value of robot's variable").createPanel();


                return panel;
            }
        };
    }

    @NotNull
    @Override
    public NoSettings createSettings() {
        return new NoSettings();
    }

    @Nullable
    @Override
    public InlayHintsCollector getCollectorFor(@NotNull PsiFile psiFile, @NotNull Editor editor, @NotNull
            NoSettings o, @NotNull InlayHintsSink inlayHintsSink) {
        return  new FactoryInlayHintsCollector(editor) {
            @Override
            public boolean collect(@NotNull PsiElement element, @NotNull Editor editor, @NotNull InlayHintsSink inlayHintsSink) {
                String systemEnvVariablePattern = "%\\{(.*)\\}";
                if(element instanceof Variable){
                    if ( StringUtils.isNotEmpty(element.getText()) ){
                        //ignore with env variables
                        Pattern variablePattern = Pattern.compile(systemEnvVariablePattern);
                        Matcher matcher = variablePattern.matcher(element.getText().trim());
                        if(matcher.find()){
                            String variableName = matcher.group(1);
                            if(StringUtils.isNotEmpty(variableName) && StringUtils.isNotEmpty(System.getenv(variableName))){
                                PresentationFactory factory = this.getFactory();
                                int offset = element.getTextOffset()+element.getText().length();
                                InlayPresentation textPresent = factory.smallText(System.getenv(variableName));
                                textPresent = factory.roundWithBackground(textPresent);

                                inlayHintsSink.addInlineElement(offset, true, textPresent);
                                return true;
                            }
                        }
                    }
                }

                return true;
            }
        };
    }

    @Override
    public boolean isLanguageSupported(@NotNull Language language) {
        if(language instanceof RobotLanguage){
            return true;
        }
        return false;
    }


}
