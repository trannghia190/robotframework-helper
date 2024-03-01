package com.github.nghiatm.robotframeworkplugin.psi;

import com.github.nghiatm.robotframeworkplugin.psi.element.ArgumentImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.BracketSettingImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.HeadingImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.ImportImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionIdImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordInvokableImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordStatementImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFileImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.SettingImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.VariableDefinitionIdImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.VariableDefinitionImpl;
import com.github.nghiatm.robotframeworkplugin.psi.element.VariableImpl;
import com.intellij.lang.ASTNode;
import com.intellij.lang.ParserDefinition;
import com.intellij.lang.PsiParser;
import com.intellij.lexer.Lexer;
import com.intellij.openapi.project.Project;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import com.intellij.psi.tree.TokenSet;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;

/**
 * @author Stephen Abrams
 */
public class RobotParserDefinition implements ParserDefinition {

    private static final TokenSet WHITESPACE_SET = TokenSet.create(RobotTokenTypes.WHITESPACE);
    private static final TokenSet COMMENTS_SET = TokenSet.create(RobotTokenTypes.COMMENT);
    private static final TokenSet STRING_SET = TokenSet.create(RobotTokenTypes.GHERKIN,
            RobotTokenTypes.SYNTAX_MARKER);

    @NotNull
    @Override
    public Lexer createLexer(Project project) {
        return new RobotLexer(RobotKeywordProvider.getInstance());
    }

    @Override
    public PsiParser createParser(Project project) {
        return new RobotParser();
    }

    @Override
    public IFileElementType getFileNodeType() {
        return RobotTokenTypes.FILE;
    }

    @NotNull
    @Override
    public TokenSet getWhitespaceTokens() {
        return WHITESPACE_SET;
    }

    @NotNull
    @Override
    public TokenSet getCommentTokens() {
        return COMMENTS_SET;
    }

    @NotNull
    @Override
    public TokenSet getStringLiteralElements() {
        return STRING_SET;
    }

    @NotNull
    @Override
    public PsiElement createElement(ASTNode node) {
        if (node.getElementType() == RobotTokenTypes.KEYWORD_DEFINITION) return new KeywordDefinitionImpl(node);
        if (node.getElementType() == RobotTokenTypes.KEYWORD_DEFINITION_ID) return new KeywordDefinitionIdImpl(node);
        if (node.getElementType() == RobotTokenTypes.KEYWORD_STATEMENT) return new KeywordStatementImpl(node);
        if (node.getElementType() == RobotTokenTypes.KEYWORD) return new KeywordInvokableImpl(node);
        if (node.getElementType() == RobotTokenTypes.VARIABLE_DEFINITION) return new VariableDefinitionImpl(node);
        if (node.getElementType() == RobotTokenTypes.VARIABLE_DEFINITION_ID) return new VariableDefinitionIdImpl(node);
        if (node.getElementType() == RobotTokenTypes.HEADING) return new HeadingImpl(node);
        if (node.getElementType() == RobotTokenTypes.ARGUMENT) return new ArgumentImpl(node);
        if (node.getElementType() == RobotTokenTypes.VARIABLE) return new VariableImpl(node);
        if (node.getElementType() == RobotTokenTypes.IMPORT) return new ImportImpl(node);
        if (node.getElementType() == RobotTokenTypes.SETTING) return new SettingImpl(node);
        if (node.getElementType() == RobotTokenTypes.BRACKET_SETTING) return new BracketSettingImpl(node);

        return PsiUtilCore.NULL_PSI_ELEMENT;

    }

    @Override
    public PsiFile createFile(FileViewProvider viewProvider) {
        return new RobotFileImpl(viewProvider);
    }

    @Override
    public SpaceRequirements spaceExistenceTypeBetweenTokens(ASTNode left, ASTNode right) {
        // TODO: guessing this is for code cleanup
        return SpaceRequirements.MAY;
    }
}
