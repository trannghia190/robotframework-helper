package com.github.nghiatm.robotframeworkplugin.psi;

import com.github.nghiatm.robotframeworkplugin.psi.element.KeywordDefinition;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.spellchecker.inspections.PlainTextSplitter;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.TokenConsumer;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;

public class RobotSpellCheckingStrategy extends SpellcheckingStrategy {
    @Override
    public @NotNull Tokenizer getTokenizer(PsiElement element) {
//        LogUtil.debug("getTokenizer of type ["+element.getNode().getElementType()+"] with text ["+element.getText()+"]", "", "", element.getProject());
        if(element instanceof KeywordDefinition){
            return new KeywordTokenizer();
        }
        return EMPTY_TOKENIZER;
    }

    private static class KeywordTokenizer extends Tokenizer<KeywordDefinition>{

        @Override
        public void tokenize(@NotNull KeywordDefinition element, TokenConsumer consumer) {
            consumer.consumeToken(element, element.getText(), false, 0,
                    TextRange.allOf(element.getText()),
                    PlainTextSplitter.getInstance());
        }
    }
}
