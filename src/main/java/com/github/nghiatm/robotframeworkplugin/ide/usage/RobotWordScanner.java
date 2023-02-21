package com.github.nghiatm.robotframeworkplugin.ide.usage;

import com.github.nghiatm.robotframeworkplugin.psi.RobotKeywordProvider;
import com.github.nghiatm.robotframeworkplugin.psi.RobotLexer;
import com.github.nghiatm.robotframeworkplugin.psi.RobotTokenTypes;
import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.psi.tree.TokenSet;

/**
 * @author mrubino
 * @since 2015-12-22
 */
public class RobotWordScanner extends DefaultWordsScanner {

    private static final TokenSet IDENTIFIERS = TokenSet.create(RobotTokenTypes.KEYWORD_DEFINITION, RobotTokenTypes.VARIABLE_DEFINITION);
    private static final TokenSet COMMENTS = TokenSet.create(RobotTokenTypes.COMMENT);
    private static final TokenSet LITERALS = TokenSet.create(RobotTokenTypes.ARGUMENT);

    public RobotWordScanner() {
        super(new RobotLexer(RobotKeywordProvider.getInstance()), IDENTIFIERS, COMMENTS, LITERALS);
        setMayHaveFileRefsInLiterals(true);
    }
}
