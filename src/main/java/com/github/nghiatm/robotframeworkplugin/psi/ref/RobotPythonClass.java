package com.github.nghiatm.robotframeworkplugin.psi.ref;

import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordFile;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceCollector;
import com.github.nghiatm.robotframeworkplugin.psi.util.PerformanceEntity;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * @author mrubino
 */
public class RobotPythonClass extends RobotPythonWrapper implements KeywordFile, PerformanceEntity {

    private final String library;
    private final PyClass pythonClass;
    private final ImportType importType;
    private final String originalLibrary;


    public RobotPythonClass(@NotNull String library,@NotNull String originalLibrary, @NotNull PyClass pythonClass, @NotNull ImportType importType) {
        this.library = library;
        this.pythonClass = pythonClass;
        this.importType = importType;
        this.originalLibrary = originalLibrary;
    }

    @NotNull
    @Override
    public Collection<DefinedKeyword> getDefinedKeywords() {
        final Collection<DefinedKeyword> results = new HashSet<>();
        PerformanceCollector debug = new PerformanceCollector(this, "get defined keywords");
        addDefinedKeywords(this.pythonClass, this.library, results);
        debug.complete();
        return results;
    }

    @NotNull
    @Override
    public Collection<DefinedVariable> getDefinedVariables() {
        final Collection<DefinedVariable> results = new HashSet<DefinedVariable>();
        PerformanceCollector debug = new PerformanceCollector(this, "get defined variables");
        addDefinedVariables(this.pythonClass, results);
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

        RobotPythonClass that = (RobotPythonClass) o;
        return this.library.equals(that.library) && this.pythonClass.equals(that.pythonClass);
    }

    @Override
    public int hashCode() {
        int result = this.library.hashCode();
        result = 31 * result + this.pythonClass.hashCode();
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
        return this.library;
    }

    @NotNull
    @Override
    public String getDebugText() {
        return this.pythonClass.getContainingFile().getVirtualFile().getName();
    }

    @NotNull
    @Override
    public Project getProject() {
        return this.pythonClass.getProject();
    }
}
