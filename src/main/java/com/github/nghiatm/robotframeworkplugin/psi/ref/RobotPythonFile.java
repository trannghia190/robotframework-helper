package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import com.github.nghiatm.robotframeworkplugin.psi.dto.KeywordDto;
import com.github.nghiatm.robotframeworkplugin.psi.dto.VariableDto;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordFile;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceCollector;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceEntity;
import com.github.nghiatm.robotframeworkplugin.psi.util.PythonParser;
import com.github.nghiatm.robotframeworkplugin.psi.util.ReservedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.util.ReservedVariableScope;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * @author mrubino
 * @since 2014-06-17
 */
public class RobotPythonFile extends RobotPythonWrapper implements KeywordFile, PerformanceEntity {

    private static final String EMPTY = "";

    private final String library;
    private final PyFile pythonFile;
    private final ImportType importType;
    private final String originalLibrary;


    public RobotPythonFile(@NotNull String library, @NotNull String originalLibrary, @NotNull PyFile pythonFile, @NotNull ImportType importType) {
        this.library = library;
        this.pythonFile = pythonFile;
        this.importType = importType;
        this.originalLibrary = originalLibrary;

    }

    @NotNull
    @Override
    public Collection<DefinedKeyword> getDefinedKeywords() {
        PerformanceCollector debug = new PerformanceCollector(this, "get defined keywords");
        Collection<DefinedKeyword> results = new HashSet<DefinedKeyword>();
        // find class with same name as library first
        boolean found = false;
        List<RatedResolveResult> ratedResolveResultList = this.pythonFile.multiResolveName(this.originalLibrary);
        for (RatedResolveResult ratedResolveResult: ratedResolveResultList) {
            if (ratedResolveResult.getElement() instanceof PyClass) {
                found = true;
                addDefinedKeywords((PyClass) ratedResolveResult.getElement(), this.library, results);
            }
        }
        if (found) {
            return results;
        }

        for (PyFunction function : this.pythonFile.getTopLevelFunctions()) {
            String keyword = PythonParser.keywordName(function);
            if (keyword != null) {
                results.add(new KeywordDto(function, this.library, keyword, PythonParser.keywordHasArguments(function)));
            }
        }
        for (PyTargetExpression expression : this.pythonFile.getTopLevelAttributes()) {
            String keyword = PythonParser.keywordName(expression);
            if (keyword != null) {
                results.add(new KeywordDto(expression, this.library, keyword, false));
            }
        }
        // if class name is not same as library name, robotframework will not use the method in that class as keyword, only function is imported
//        for (PyClass subClass : this.pythonFile.getTopLevelClasses()) {
//            //String namespace = subClass.getQualifiedName() == null ? EMPTY : subClass.getQualifiedName();
//            String namespace = this.library;
//            addDefinedKeywords(subClass, namespace, results);
//        }

        debug.complete();
        return results;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getDefinedVariables() {
        PerformanceCollector debug = new PerformanceCollector(this, "get defined variables");
        final Collection<DefinedVariable> results = new HashSet<DefinedVariable>();
        for (PyTargetExpression expression : this.pythonFile.getTopLevelAttributes()) {
            String keyword = expression.getName();
            if (keyword != null) {
                // not formatted ${X}, assume scalar
                results.add(new VariableDto(expression, ReservedVariable.wrapToScalar(keyword),
                        ReservedVariableScope.TestCase));
            }
        }
        for (PyClass subClass : this.pythonFile.getTopLevelClasses()) {
            addDefinedVariables(subClass, results);
        }
        debug.complete();
        return results;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getOwnDefinedVariables() {
        return getDefinedVariables();
    }

    @NotNull
    @Override
    public ImportType getImportType() {
        return this.importType;
    }

    @NotNull
    @Override
    public Collection<KeywordFile> getImportedFiles(boolean includeTransitive) {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RobotPythonFile that = (RobotPythonFile) o;
        return this.library.equals(that.library) && this.pythonFile.equals(that.pythonFile);
    }

    @Override
    public int hashCode() {
        int result = this.library.hashCode();
        result = 31 * result + this.pythonFile.hashCode();
        return result;
    }

    @NotNull
    public String getOriginalLibrary() {
        return this.originalLibrary;
    }

    @NotNull
    public String getLibrary() {
        return this.library;
    }


    @Override
    public String toString() {
        return this.library;
    }

    @NotNull
    @Override
    public String getDebugFileName() {
        return toString();
    }

    @NotNull
    @Override
    public String getDebugText() {
        return this.pythonFile.getContainingFile().getVirtualFile().getName();
    }

    @NotNull
    @Override
    public Project getProject() {
        return this.pythonFile.getProject();
    }
}
