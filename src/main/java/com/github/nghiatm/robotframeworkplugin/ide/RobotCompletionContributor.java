package com.github.nghiatm.robotframeworkplugin.ide;

import com.github.nghiatm.robotframeworkplugin.ide.config.RobotOptionsProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RecommendationWord;
import com.github.nghiatm.robotframeworkplugin.psi.RobotElementType;
import com.github.nghiatm.robotframeworkplugin.psi.RobotKeywordProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotTokenTypes;
import com.github.nghiatm.robotframeworkplugin.psi.dto.ImportType;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedKeyword;
import com.github.nghiatm.robotframeworkplugin.psi.element.DefinedVariable;
import com.github.nghiatm.robotframeworkplugin.psi.element.Heading;
import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordFile;
import com.github.nghiatm.robotframeworkplugin.psi.element.RobotFile;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.lookup.TailTypeDecorator;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * @author Stephen Abrams
 */
public class RobotCompletionContributor extends CompletionContributor {

    public static final int CELL_SEPRATOR_SPACE = 4;
    private static final TailType NEW_LINE = TailType.createSimpleTailType('\n');
    private static final TailType SUPER_SPACE = new TailType() {
        @Override
        public int processTail(Editor editor, int tailOffset) {
            Document document = editor.getDocument();
            int textLength = document.getTextLength();
            CharSequence chars = document.getCharsSequence();
            int spaceCount = 0;
            for (int i = tailOffset; i < textLength && chars.charAt(i) == ' '; i++) {
                if (++spaceCount >= CELL_SEPRATOR_SPACE)
                    break;
            }
            if (spaceCount < CELL_SEPRATOR_SPACE) {
                String toAdd = new String(new char[CELL_SEPRATOR_SPACE - spaceCount]).replace("\0", " ");
                //document.insertString(tailOffset, toAdd);
                Runnable runnable = () -> document.insertString(tailOffset, toAdd);
                WriteCommandAction.runWriteCommandAction(editor.getProject(), runnable);
            }
            return moveCaret(editor, tailOffset, CELL_SEPRATOR_SPACE);
        }
    };

    private static int insertNewlineIndentSpace(Editor editor, int tailOffset, int spaceCount) {
        Document document = editor.getDocument();
        CharSequence chars = document.getCharsSequence();
        int indentLength = 0;
        for (int i = tailOffset-1; i >= 0 && chars.charAt(i) != '\n'; i--) {
            if (chars.charAt(i) == ' ')
                indentLength++;
            else
                indentLength = 0;
        }

        String indent = "";
        indentLength += spaceCount;
        if (indentLength > 0) {
            indent = new String(new char[indentLength]).replace("\0", " ");
        }
        String toAdd = "\n" + indent;
        Runnable runnable = () -> document.insertString(tailOffset, toAdd);
        WriteCommandAction.runWriteCommandAction(editor.getProject(), runnable);
        return indentLength + 1;
    }
    private static final TailType NEWLINE_INDENT = new TailType() {
        @Override
        public int processTail(Editor editor, int tailOffset) {
            return moveCaret(editor, tailOffset, insertNewlineIndentSpace(editor, tailOffset, 0));
        }
    };
    private static final TailType NEWLINE_INDENT_SUPERSPACE = new TailType() {
        @Override
        public int processTail(Editor editor, int tailOffset) {
            return moveCaret(editor, tailOffset, insertNewlineIndentSpace(editor, tailOffset, CELL_SEPRATOR_SPACE));
        }
    };

    // TODO: code completion only be triggered when type letters or digits
    // how to do with ***, %{, @{ ?
    public RobotCompletionContributor() {
        // This is the rule for adding Headings (*** Settings ***, *** Test Cases ***)
        extend(CompletionType.BASIC,
                psiElement().inFile(psiElement(RobotFile.class)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  ProcessingContext context,
                                                  @NotNull CompletionResultSet results) {
                        PsiElement heading = getHeading(parameters.getOriginalPosition());
                        if (isElementInFirstColumn(parameters)) {
                            boolean isStartsWith3Star = parameters.getPosition().getText().startsWith("***");
                            // This is the rule for adding Headings (*** Settings ***, *** Test Cases ***)
                            if (isStartsWith3Star) {
                                addSyntaxLookup(RobotTokenTypes.HEADING, results, NEW_LINE, 4);
                            } else {
                                addSyntaxLookup(RobotTokenTypes.HEADING, results, NEW_LINE);
                                if (isInSettings(heading)) {
                                    // This is the rule for adding settings and imports (Library, Test Setup)
                                    addSyntaxLookup(RobotTokenTypes.SETTING, results, SUPER_SPACE);
                                    addSyntaxLookup(RobotTokenTypes.IMPORT, results, SUPER_SPACE);
                                // should we show following 2 if items? maybe can only help detect duplicated name
                                } else if (isInTestCases(heading) || isInKeywords(heading)) {
                                    // This is the rule for adding imported keywords and library methods
                                    addRobotKeywords(results, parameters.getOriginalFile());
                                } else if (isInVariables(heading)) {
                                    // This is the rule for adding included variable definitions
                                    addRobotVariables(results, parameters.getOriginalFile(), parameters.getOriginalPosition());
                                }
                            }
                        } else {
                            if (isInTestCases(heading)) {
                                // This is the rule for adding Bracket Settings ([Tags], [Setup])
                                // TODO: some brackets are only for Test Cases, some only Keywords, some both
                                addSyntaxLookup(RobotTokenTypes.BRACKET_SETTING, results, SUPER_SPACE);

                                // This is the rule for adding Gherkin (When, Then)
                                addSyntaxLookup(RobotTokenTypes.GHERKIN, results, TailType.SPACE);

                                // This is the rule for adding imported keywords and library methods
                                addRobotKeywords(results, parameters.getOriginalFile());

                                // This is the rule for adding included variable definitions
                                // TODO: include variables defined in the current statement
                                addRobotVariables(results, parameters.getOriginalFile(), parameters.getOriginalPosition());

                                // This is the rule for adding reserved word
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD, results, SUPER_SPACE, 0, true);
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD_NEWLINE_INDENT, results, NEWLINE_INDENT, 0, true);
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD_NEWLINE_INDENT_SUPERSPACE, results, NEWLINE_INDENT_SUPERSPACE, 0, true);
                            } else if (isInKeywords(heading)) {
                                // This is the rule for adding Bracket Settings ([Tags], [Setup])
                                // TODO: some brackets are only for Test Cases, some only Keywords, some both
                                addSyntaxLookup(RobotTokenTypes.BRACKET_SETTING, results, SUPER_SPACE);

                                // This is the rule for adding imported keywords and library methods
                                addRobotKeywords(results, parameters.getOriginalFile());

                                // This is the rule for adding imported keywords and library methods
                                addRobotVariables(results, parameters.getOriginalFile(), parameters.getOriginalPosition());

                                // This is the rule for adding reserved word
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD, results, SUPER_SPACE, 0, true);
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD_NEWLINE_INDENT, results, NEWLINE_INDENT, 0, true);
                                addSyntaxLookup(RobotTokenTypes.RESERVED_WORD_NEWLINE_INDENT_SUPERSPACE, results, NEWLINE_INDENT_SUPERSPACE, 0, true);
                            } else if (isInSettings(heading)) {
                                addSyntaxLookup(RobotTokenTypes.SETTING_RESERVED_WORD, results, SUPER_SPACE, 0, true);
                                // This is the rule for adding included variable definitions
                                addRobotVariables(results, parameters.getOriginalFile(), parameters.getOriginalPosition());
                            }
                        }
                    }
                });
    }

    private static PsiElement getHeading(PsiElement current) {
        if (current == null) {
            return null;
        }
        if (current instanceof Heading) {
            return current;
        } else {
            return getHeading(current.getParent());
        }
    }

    private static boolean isInSettings(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).isSettings();
        }
        return result;
    }

    private static boolean isInTestCases(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).containsTestCases();
        }
        return result;
    }

    private static boolean isInKeywords(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).containsKeywordDefinitions();
        }
        return result;
    }

    private static boolean isInVariables(@Nullable PsiElement element) {
        boolean result = false;
        if (element instanceof Heading) {
            result = ((Heading) element).getPresentableText().startsWith("*** Variable");
        }
        return result;
    }

    private static void addRobotKeywords(CompletionResultSet result, PsiFile file) {
        if (!(file instanceof RobotFile)) {
            return;
        }
        RobotFile robotFile = (RobotFile) file;

        boolean capitalize = RobotOptionsProvider.getInstance(robotFile.getProject()).capitalizeKeywords();
        addKeywordsToResult(robotFile.getDefinedKeywords(), result, capitalize, false);

        boolean includeTransitive = RobotOptionsProvider.getInstance(file.getProject()).allowTransitiveImports();
        Collection<KeywordFile> importedFiles = robotFile.getImportedFiles(includeTransitive);
        for (KeywordFile f : importedFiles) {
            if (f.getImportType() == ImportType.LIBRARY || f.getImportType() == ImportType.RESOURCE) {
                addKeywordsToResult(f.getDefinedKeywords(), result, capitalize, true);
            }
        }
    }

    private static void addRobotVariables(@NotNull CompletionResultSet result, @NotNull PsiFile file, @Nullable PsiElement position) {
        if (!(file instanceof RobotFile)) {
            return;
        }
        RobotFile robotFile = (RobotFile) file;
        addVariablesToResult(robotFile.getDefinedVariables(), result, position);

        // ROBOTFRAMEWORK only import variable from Variable and Resource
        // following code done in RobotFileImpl.getDefinedVariables()
        // bug: following will add variable in "Library xx.py" which should not
        boolean includeTransitive = RobotOptionsProvider.getInstance(file.getProject()).allowTransitiveImports();
        Collection<KeywordFile> importedFiles = robotFile.getImportedFiles(includeTransitive);
        for (KeywordFile f : importedFiles) {
            addVariablesToResult(f.getDefinedVariables(), result, position);
        }
    }

//LookupElementBuilder.create(String): with one parameter, it is the target text, and also the lookup text
//                    .withLookupString(text): add a lookup text
//                    .withPresentableText(prompt): the text displayed in popup window
    private static void addVariablesToResult(@NotNull final Collection<DefinedVariable> variables,
                                             @NotNull final CompletionResultSet result,
                                             @Nullable PsiElement position) {
        for (DefinedVariable variable : variables) {
            if (!variable.isInScope(position)) {
                continue;
            }
            String text = variable.getLookup();
            if (text != null) {
                // we only want the first word of the variable
                String[] words = text.split("\\s+");
                String lookupString = words.length > 0 ? words[0] : text;
                String[] lookupStrings = {text, StringUtils.capitalize(text),
                        text.toLowerCase(),
                        lookupString, lookupString.toLowerCase()};
                LookupElement element = TailTypeDecorator.withTail(
                        LookupElementBuilder.create(lookupString)
                                .withLookupStrings(Arrays.asList(lookupStrings))
                                .withPresentableText(lookupString)
                                .withCaseSensitivity(true),
                        TailType.NONE);
                result.addElement(element);
            }
        }
    }

    private static void addKeywordsToResult(final Collection<DefinedKeyword> keywords,
                                            final CompletionResultSet result,
                                            boolean capitalize, boolean addNamespace) {
        for (DefinedKeyword keyword : keywords) {
            String text = keyword.getKeywordName();
            String lookupString = capitalize ? StringUtils.capitalize(text) : text;
            String[] lookupStrings = {text, StringUtils.capitalize(text), text.toLowerCase()};
            LookupElement element = TailTypeDecorator.withTail(
                    LookupElementBuilder.create(lookupString)
                            .withLookupStrings(Arrays.asList(lookupStrings))
                            .withPresentableText(lookupString)
                            .withCaseSensitivity(true),
                    keyword.hasArguments() ? SUPER_SPACE : TailType.NONE);
            result.addElement(element);
            if (addNamespace) {
                String ns = keyword.getNamespace() + ".";
                element = TailTypeDecorator.withTail(
                        LookupElementBuilder.create(ns + lookupString)
                                .withLookupString(ns + text)
                                .withPresentableText(ns + lookupString)
                                .withCaseSensitivity(false),
                        keyword.hasArguments() ? SUPER_SPACE : TailType.NONE);
                result.addElement(element);
            }
        }
    }

    private static void addSyntaxLookup(@NotNull RobotElementType type, @NotNull CompletionResultSet results, @NotNull TailType tail) {
        addSyntaxLookup(type, results, tail, 0, false);
    }

    private static void addSyntaxLookup(@NotNull RobotElementType type, @NotNull CompletionResultSet results, @NotNull TailType tail, int removeTargetPrefix) {
        addSyntaxLookup(type, results, tail, removeTargetPrefix, false);
    }

    private static void addSyntaxLookup(@NotNull RobotElementType type, @NotNull CompletionResultSet results, @NotNull TailType tail, int removeTargetPrefix, boolean caseSensitivity) {
        Collection<RecommendationWord> words = RobotKeywordProvider.getInstance().getRecommendationsForType(type);
        for (RecommendationWord word : words) {
            String text = word.getLookup();
            String lookupString = word.getPresentation();
            String displayString = lookupString;
            if (removeTargetPrefix > 0 && lookupString.length() > removeTargetPrefix) {
                lookupString = lookupString.substring(removeTargetPrefix);
            }
            String[] lookupStrings = {text, StringUtils.capitalize(text),
                    lookupString, StringUtils.capitalize(lookupString),
                    lookupString.toLowerCase()};
            LookupElement element = TailTypeDecorator.withTail(
                    LookupElementBuilder.create(lookupString)
                            .withLookupStrings(Arrays.asList(lookupStrings))
                            .withPresentableText(displayString)
                            .withCaseSensitivity(caseSensitivity),
                    tail);
            results.addElement(element);
        }
    }

//    @Override
//    public void fillCompletionVariants(@NotNull final CompletionParameters parameters, @NotNull CompletionResultSet result) {
//        // debugging point
//        super.fillCompletionVariants(parameters, result);
//        debug("fillCompletionVariants", "called");
//    }

    private static boolean isElementInFirstColumn(@NotNull CompletionParameters parameters) {
        Editor editor = parameters.getEditor();
        CaretModel caretModel = editor.getCaretModel();
        int column = caretModel.getLogicalPosition().column;
        int elementStartOffset = parameters.getPosition().getTextRange().getStartOffset();
        column = column - (parameters.getOffset() - elementStartOffset) + 1;
        if (column > 1) {
            return false;
        } else {
            String text = parameters.getPosition().getText();
            if (text.startsWith("  ") || text.startsWith("\t") || text.startsWith(" \t")) {
                return false;
            }
            return true;
        }
    }
}