package com.github.nghiatm.robotframeworkplugin.ide.inspections.compilation;

import com.github.nghiatm.robotframeworkplugin.RobotBundle;
import com.github.nghiatm.robotframeworkplugin.ide.inspections.SimpleRobotInspection;
import com.github.nghiatm.robotframeworkplugin.psi.element.Argument;
import com.github.nghiatm.robotframeworkplugin.psi.element.BracketSetting;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordStatement;
import com.github.nghiatm.robotframeworkplugin.psi.element.Variable;
import com.github.nghiatm.robotframeworkplugin.psi.util.RobotUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mrubino
 * @since 2014-06-18
 */
public class RobotVariableNotFound extends SimpleRobotInspection {

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return RobotBundle.message("INSP.NAME.variable.undefined");
    }

    @Override
    public boolean skip(PsiElement element) {
        String systemEnvVariablePattern = "%\\{(.*)\\}";
        if (element instanceof Variable) {
            if ( StringUtils.isNotEmpty(element.getText()) ){
                //ignore variable in bracket setting
                try {
                    if(RobotUtil.isChildOf(element, BracketSetting.class)){
                        return true;
                    }
                }catch (Exception ignored){
                }

                //ignore with env variables
                Pattern variablePattern = Pattern.compile(systemEnvVariablePattern);
                Matcher matcher = variablePattern.matcher(element.getText().trim());
                if(matcher.find()){
                    String variableName = matcher.group(1);
                    if(StringUtils.isNotEmpty(variableName) && StringUtils.isNotEmpty(System.getenv(variableName))){
                        return true;
                    }
                }
            }
            PsiReference reference = element.getReference();
            if (reference != null && reference.resolve() != null) {
                return true;
            }
            if (((Variable) element).isNested()) {
                // TODO: nested variables
                return true;
            }

            // TODO: what is needed below this point...
            PsiElement container = element.getParent();
            element = container;
            if (container instanceof Argument) {
                container = container.getParent();
            }
            if (container instanceof KeywordStatement) {
//                KeywordInvokable invokable = ((KeywordStatement) container).getInvokable();
//                String text = invokable == null ? null : invokable.getPresentableText();
//                if (text != null) {
//                    if (text.startsWith(":")) {
//                        // TODO: for loops
//                        return true;
//                    } else if (text.startsWith("\\")) {
//                        // TODO: for loops
//                        return true;
//                    }
//                }
                // this is the case where we have a 'set test variable' call with more than one arg
                // the first is the variable name, the second is the value
                // if there is only one argument then we might want to see where it was created
                if (((KeywordStatement) container).getGlobalVariable() != null) {
                    List<Argument> arguments = ((KeywordStatement) container).getArguments();
                    if (arguments.size() > 1 && element == arguments.get(0)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getMessage() {
        return RobotBundle.message("INSP.variable.undefined");
    }

    @NotNull
    @Override
    protected String getGroupNameKey() {
        return "INSP.GROUP.compilation";
    }
}
