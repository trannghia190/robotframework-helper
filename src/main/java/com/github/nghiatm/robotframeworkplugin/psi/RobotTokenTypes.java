package com.github.nghiatm.robotframeworkplugin.psi;

import com.intellij.psi.tree.IFileElementType;

public interface RobotTokenTypes {

    IFileElementType FILE = new RobotFileElementType("ROBOT_FILE", RobotLanguage.INSTANCE);
    RobotElementType HEADING = new RobotElementType("HEADING");
    RobotElementType SETTING = new RobotElementType("SETTING");
    RobotElementType BRACKET_SETTING = new RobotElementType("BRACKET_SETTING");
    RobotElementType IMPORT = new RobotElementType("IMPORT");
    RobotElementType KEYWORD_DEFINITION = new RobotElementType("KEYWORD_DEFINITION");
    RobotElementType KEYWORD_DEFINITION_ID = new RobotElementType("KEYWORD_DEFINITION_ID");
    RobotElementType KEYWORD = new RobotElementType("KEYWORD");
    RobotElementType ARGUMENT = new RobotElementType("ARGUMENT");
    RobotElementType VARIABLE_DEFINITION = new RobotElementType("VARIABLE_DEFINITION");
    RobotElementType VARIABLE_DEFINITION_ID = new RobotElementType("VARIABLE_DEFINITION_ID");
    RobotElementType VARIABLE = new RobotElementType("VARIABLE");
    RobotElementType COMMENT = new RobotElementType("COMMENT");
    RobotElementType GHERKIN = new RobotElementType("GHERKIN");
    RobotElementType SYNTAX_MARKER = new RobotElementType("SYNTAX_MARKER");
    RobotElementType KEYWORD_STATEMENT = new RobotElementType("KEYWORD_STATEMENT");
    RobotElementType RESERVED_WORD = new RobotElementType("RESERVED_WORD");
    RobotElementType RESERVED_WORD_NEWLINE_INDENT = new RobotElementType("RESERVED_WORD_NEWLINE_INDENT");
    RobotElementType RESERVED_WORD_NEWLINE_INDENT_SUPERSPACE = new RobotElementType("RESERVED_WORD_NEWLINE_INDENT_SUPERSPACE");
    RobotElementType SETTING_RESERVED_WORD = new RobotElementType("SETTING_RESERVED_WORD");

    RobotElementType ERROR = new RobotElementType("ERROR");
    RobotElementType WHITESPACE = new RobotElementType("WHITESPACE");
}
