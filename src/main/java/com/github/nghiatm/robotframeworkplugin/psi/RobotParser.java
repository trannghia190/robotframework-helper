package com.github.nghiatm.robotframeworkplugin.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Stephen Abrams
 */
public class RobotParser implements PsiParser {

    private static void done(@Nullable PsiBuilder.Marker marker, @NotNull RobotElementType type) {
        if (marker != null) {
            marker.done(type);
        }
    }

    private static void parseFileTopLevel(@NotNull PsiBuilder builder) {
        while (!builder.eof()) {
            IElementType tokenType = builder.getTokenType();
            if (RobotTokenTypes.HEADING == tokenType) {
                parseHeading(builder);
            } else {
                builder.advanceLexer();
            }
        }
    }

    private static void parseHeading(@NotNull PsiBuilder builder) {
        assert RobotTokenTypes.HEADING == builder.getTokenType();
        PsiBuilder.Marker headingMarker = null;
        while (true) {
            IElementType type = builder.getTokenType();
            if (RobotTokenTypes.HEADING == type) {
                done(headingMarker, RobotTokenTypes.HEADING);
                headingMarker = builder.mark();
                builder.advanceLexer();
            }

            if (builder.eof()) {
                done(headingMarker, RobotTokenTypes.HEADING);
                break;
            } else {
                type = builder.getTokenType();
                if (RobotTokenTypes.HEADING == type) {
                    //noinspection UnnecessaryContinue
                    continue;
                } else if (RobotTokenTypes.IMPORT == type) {
                    parseImport(builder);
                } else if (RobotTokenTypes.VARIABLE_DEFINITION == type && isNextToken(builder, RobotTokenTypes.WHITESPACE)) {
                    parseVariableDefinition(builder);
                } else if (RobotTokenTypes.SETTING == type) {
                    parseSetting(builder);
                } else if (RobotTokenTypes.KEYWORD_DEFINITION == type ||
                        RobotTokenTypes.VARIABLE_DEFINITION == type && isNextToken(builder, RobotTokenTypes.KEYWORD_DEFINITION)) {
                    parseKeywordDefinition(builder);
                } else if (RobotTokenTypes.KEYWORD == type) {
                    parseKeywordStatement(builder, RobotTokenTypes.KEYWORD_STATEMENT, false);
                } else {
                    // other types; error
                    //System.out.println(type);
                    builder.advanceLexer();
                }
            }
        }
    }

    private static void parseKeywordDefinition(@NotNull PsiBuilder builder) {
        PsiBuilder.Marker keywordMarker = null;
        PsiBuilder.Marker keywordIdMarker = null;
        while (true) {
            IElementType type = builder.getTokenType();
            if(RobotTokenTypes.RESERVED_WORD == type){
                builder.advanceLexer();
            }
            if (RobotTokenTypes.KEYWORD_DEFINITION == type ||
                    RobotTokenTypes.VARIABLE_DEFINITION == type && isNextToken(builder, RobotTokenTypes.KEYWORD_DEFINITION)) {
                if (builder.rawLookup(-1) != RobotTokenTypes.VARIABLE_DEFINITION) {
                    done(keywordIdMarker, RobotTokenTypes.KEYWORD_DEFINITION_ID);
                    done(keywordMarker, RobotTokenTypes.KEYWORD_DEFINITION);
                    keywordMarker = builder.mark();
                    keywordIdMarker = builder.mark();
                }
                if (RobotTokenTypes.KEYWORD_DEFINITION == type) {
                    builder.advanceLexer();
                }
            }

            if (builder.eof()) {
                done(keywordIdMarker, RobotTokenTypes.KEYWORD_DEFINITION_ID);
                done(keywordMarker, RobotTokenTypes.KEYWORD_DEFINITION);
                break;
            } else {
                type = builder.getTokenType();
                // not all the time; all cases but VAR_DEF (when in keyword definition only)
                if (RobotTokenTypes.HEADING == type) {
                    done(keywordIdMarker, RobotTokenTypes.KEYWORD_DEFINITION_ID);
                    done(keywordMarker, RobotTokenTypes.KEYWORD_DEFINITION);
                    break;
                } else if (RobotTokenTypes.BRACKET_SETTING == type) {
                    done(keywordIdMarker, RobotTokenTypes.KEYWORD_DEFINITION_ID);
                    keywordIdMarker = null;
                    parseBracketSetting(builder);
                } else if (RobotTokenTypes.ERROR == type) {
                    // not sure
                    builder.advanceLexer();
                } else if (RobotTokenTypes.VARIABLE_DEFINITION == type) {
                    PsiBuilder.Marker statement = parseKeywordStatement(builder, RobotTokenTypes.VARIABLE_DEFINITION, true);
                    if (statement != null && keywordIdMarker != null) {
                        keywordIdMarker.doneBefore(RobotTokenTypes.KEYWORD_DEFINITION_ID, statement);
                        keywordIdMarker = null;
                    }
                } else {
                    done(keywordIdMarker, RobotTokenTypes.KEYWORD_DEFINITION_ID);
                    keywordIdMarker = null;
                    parseKeywordStatement(builder, RobotTokenTypes.KEYWORD_STATEMENT, false);
                }
            }
        }
    }

    private static PsiBuilder.Marker parseKeywordStatement(@NotNull PsiBuilder builder, @NotNull IElementType rootType, boolean skipGherkin) {
        PsiBuilder.Marker keywordStatementMarker = builder.mark();
        boolean seenGherkin = skipGherkin;
        boolean seenKeyword = false;
        boolean inline = false;
        while (!builder.eof()) {
            IElementType type = builder.getTokenType();
            if (type == RobotTokenTypes.GHERKIN) {
                // if we see a keyword or variable there should be no Gherkin unless we are on a new statement
                if (seenGherkin || seenKeyword) {
                    break;
                } else {
                    seenGherkin = true;
                    // nothing to do for this
                    builder.advanceLexer();
                }
            } else if (type == RobotTokenTypes.KEYWORD ||
                    (type == RobotTokenTypes.VARIABLE && isNextToken(builder, RobotTokenTypes.KEYWORD))) {
                if (seenKeyword) {
                    break;
                } else {
                    seenKeyword = true;
                    parseKeyword(builder);
                }
            } else if ((type == RobotTokenTypes.ARGUMENT || type == RobotTokenTypes.VARIABLE) &&
                    builder.rawLookup(1) != RobotTokenTypes.KEYWORD) {
                parseWith(builder, RobotTokenTypes.ARGUMENT);
            } else if (type == RobotTokenTypes.VARIABLE_DEFINITION) {
                if (seenKeyword) {
                    break;
                } else {
                    seenKeyword = true;
                    boolean isPartOfKeywordDefinition = builder.rawLookup(-1) == RobotTokenTypes.KEYWORD_DEFINITION ||
                            isNextToken(builder, RobotTokenTypes.KEYWORD_DEFINITION);
                    PsiBuilder.Marker id = builder.mark();
                    builder.advanceLexer();
                    done(id, RobotTokenTypes.VARIABLE_DEFINITION_ID);
                    inline = isPartOfKeywordDefinition;
                    if (!isPartOfKeywordDefinition && builder.getTokenType() == RobotTokenTypes.KEYWORD) {
                        parseKeywordStatement(builder, RobotTokenTypes.KEYWORD_STATEMENT, true);
                    }
                }
            } else {
                // other types; error?
                //System.out.println(type);
                break;
            }
        }

        keywordStatementMarker.done(rootType);
        return inline ? null : keywordStatementMarker;
    }

    /**
     * Checks to see if the next token in the builder is the given token.  In the case that the given token
     * is whitespace then we also allow for EOF.
     *
     * @param builder the spi builder that we are parsing.
     * @param type    the element type to check for.
     * @return true if the next element type matches the given or the given is whitespace and we are at EOF.
     */
    private static boolean isNextToken(@NotNull PsiBuilder builder, IElementType type) {
        boolean allowEof = type == RobotTokenTypes.WHITESPACE;
        IElementType next = builder.rawLookup(1);
        return next == type ||
                allowEof && next == null;
    }

    private static void parseKeyword(@NotNull PsiBuilder builder) {
        parseWith(builder, RobotTokenTypes.KEYWORD);
    }

    private static void parseBracketSetting(@NotNull PsiBuilder builder) {
        parseWithArguments(builder, RobotTokenTypes.BRACKET_SETTING);
    }

    private static void parseImport(@NotNull PsiBuilder builder) {
        parseWithArguments(builder, RobotTokenTypes.IMPORT);
    }

    private static void parseVariableDefinition(@NotNull PsiBuilder builder) {
        parseWithArguments(builder, RobotTokenTypes.VARIABLE_DEFINITION);
    }

    private static void parseVariableDefinitionWithDefaults(@NotNull PsiBuilder builder) {
        IElementType type = builder.getTokenType();
        assert RobotTokenTypes.VARIABLE_DEFINITION == type;
        PsiBuilder.Marker argMarker = builder.mark();
        PsiBuilder.Marker definitionMarker = builder.mark();
        PsiBuilder.Marker definitionIdMarker = builder.mark();
        builder.advanceLexer();
        definitionIdMarker.done(RobotTokenTypes.VARIABLE_DEFINITION_ID);
        definitionMarker.done(RobotTokenTypes.VARIABLE_DEFINITION);
        IElementType token = builder.getTokenType();
        while (!builder.eof() && (token == RobotTokenTypes.ARGUMENT || token == RobotTokenTypes.VARIABLE)) {
            PsiBuilder.Marker variableMarker = null;
            if (token == RobotTokenTypes.VARIABLE) {
                variableMarker = builder.mark();
            }
            builder.advanceLexer();
            if (token == RobotTokenTypes.VARIABLE) {
                done(variableMarker, RobotTokenTypes.VARIABLE);
            }
            token = builder.getTokenType();
        }
        argMarker.done(RobotTokenTypes.ARGUMENT);
    }

    private static void parseSetting(@NotNull PsiBuilder builder) {
        parseWithArguments(builder, RobotTokenTypes.SETTING);
    }

    private static void parseWithArguments(@NotNull PsiBuilder builder, @NotNull IElementType markType) {
        IElementType type = builder.getTokenType();
        assert markType == type;
        PsiBuilder.Marker importMarker = builder.mark();
        PsiBuilder.Marker id = null;
        if (type == RobotTokenTypes.VARIABLE_DEFINITION) {
            id = builder.mark();
        }
        builder.advanceLexer();
        if (id != null) {
            id.done(RobotTokenTypes.VARIABLE_DEFINITION_ID);
        }
        while (!builder.eof()) {
            type = builder.getTokenType();
            if (RobotTokenTypes.ARGUMENT == type || RobotTokenTypes.VARIABLE == type) {
                parseWith(builder, RobotTokenTypes.ARGUMENT);
            } else if(markType == RobotTokenTypes.BRACKET_SETTING && RobotTokenTypes.RESERVED_WORD == builder.getTokenType()){
                break;
            }
            else if (markType != RobotTokenTypes.VARIABLE_DEFINITION && RobotTokenTypes.VARIABLE_DEFINITION == type) {
//                if (markType == RobotTokenTypes.BRACKET_SETTING){
//                    String text = builder.getOriginalText().subSequence(0, builder.getCurrentOffset()).toString();
//                    if(text.replaceAll("(?<=\\n)\\s+", "").endsWith("\n")){
//                        break;
//                    }
//                }
                // we check the first two to see if we are in a new statement; the third handles ... cases
//                if (builder.rawLookup(-1) == RobotTokenTypes.WHITESPACE &&
//                        builder.rawLookup(-2) == RobotTokenTypes.WHITESPACE &&
//                        builder.rawLookup(-3) != RobotTokenTypes.WHITESPACE) {
//                    break;
//                }
                // also handle new statement after an/multiple empty Ellipsis
                //    [Arguments]    ${arg1}
                //    ...
                //    ...
                //    ...    ${arg2}=${-1}
                //    ...    ${arg3}
                //    ${test}    Set Variable    2222
                //    [Documentation]
                //    ...
                //    ...    document1
                //    ...
                //    ...    ${arg3}    is test
                //    ...
                //    ...
                //    ${var1}=    Set Variable    111
                //    Log    ${var1}    ${arg3}    ${test}
                //#shold resolve all variable

                int cnt = 0;
                for (int b=-1; builder.rawLookup(b) == RobotTokenTypes.WHITESPACE; b--)
                    cnt++;
                if (cnt % 3 != 1)
                    break;

                parseVariableDefinitionWithDefaults(builder);

            } else {
                break;
            }
        }
        importMarker.done(markType);
    }

    private static void parseWith(@NotNull PsiBuilder builder, @NotNull IElementType type) {
        PsiBuilder.Marker arg = builder.mark();
        IElementType current = builder.getTokenType();
        while (!builder.eof() && current != null && (type == current || RobotTokenTypes.VARIABLE == current || RobotTokenTypes.VARIABLE_DEFINITION == current)) {
            boolean end = isNextToken(builder, RobotTokenTypes.WHITESPACE);
            if (RobotTokenTypes.VARIABLE == current || RobotTokenTypes.VARIABLE_DEFINITION == current) {
                parseSimple(builder, current);
            } else {
                builder.advanceLexer();
            }
            if (end) {
                break;
            }
            current = builder.getTokenType();
        }
        arg.done(type);
    }

    private static void parseSimple(@NotNull PsiBuilder builder, @NotNull IElementType type) {
        assert builder.getTokenType() == type;
        PsiBuilder.Marker argumentMarker = builder.mark();
        builder.advanceLexer();
        argumentMarker.done(type);
    }

    @NotNull
    @Override
    public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder builder) {
        final PsiBuilder.Marker marker = builder.mark();
        parseFileTopLevel(builder);
        marker.done(RobotTokenTypes.FILE);
        return builder.getTreeBuilt();
    }
}

