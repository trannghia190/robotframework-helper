package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.psi.dto.KeywordDto;
import com.github.nghiatm.robotframeworkplugin.psi.dto.VariableDto;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.util.PythonParser;
import com.github.nghiatm.robotframeworkplugin.psi.util.ReservedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.util.ReservedVariableScope;
import com.intellij.util.Processor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyTargetExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author mrubino
 * @since 2014-06-17
 */
public abstract class RobotPythonWrapper {


    protected static void addDefinedVariables(@NotNull PyClass pythonClass, @NotNull final Collection<DefinedVariable> results) {
        pythonClass.visitClassAttributes(
                new Processor<PyTargetExpression>() {
                    @Override
                    public boolean process(PyTargetExpression expression) {
                        String keyword = PythonParser.keywordName(expression);
                        if (keyword != null) {
                            // not formatted ${X}, assume scalar
                            results.add(new VariableDto(expression, ReservedVariable.wrapToScalar(keyword),
                                    ReservedVariableScope.TestCase));
                        }
                        return true;
                    }
                },
                true,
                null
        );
    }

    protected static void addDefinedKeywords(@NotNull PyClass pythonClass, @NotNull final String namespace, @NotNull final Collection<DefinedKeyword> results) {
        try{
            pythonClass.visitMethods(
                    function -> {
                        String keyword = PythonParser.keywordName(function);
                        if (keyword != null) {
                            // Get info from @keyword
                            results.add(new KeywordDto(function, namespace, keyword, PythonParser.keywordHasArguments(function)));
                        }
                        return true;
                    },
                    true,
                    null
            );
        }catch (Exception e){
            e.printStackTrace();
        }

        try {
            pythonClass.visitClassAttributes(
                    expression -> {
                        String keyword = PythonParser.keywordName(expression);
                        if (keyword != null) {
                            results.add(new KeywordDto(expression, namespace, keyword, false));
                        }
                        return true;
                    },
                    true,
                    null
            );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
