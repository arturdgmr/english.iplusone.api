package com.arturrodrigues.english.iplusone.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class TextTokenizerTest {

    @Test
    void lowercasesRemovesPunctuationAndSplitsIntoWords() {
        List<String> words = TextTokenizer.tokenize("I like soccer.\nHouse, Car and Family.");

        assertThat(words).containsExactly("i", "like", "soccer", "house", "car", "and", "family");
    }

    @Test
    void keepsDuplicatesWhichAreDeDupedDownstream() {
        List<String> words = TextTokenizer.tokenize("i like like like soccer");

        assertThat(words).containsExactly("i", "like", "like", "like", "soccer");
    }

    @Test
    void stripsSpecialCharactersAndDigits() {
        List<String> words = TextTokenizer.tokenize("hello!! @world #123 caf\u00e9");

        assertThat(words).containsExactly("hello", "world", "caf");
    }

    @Test
    void returnsEmptyListForNullOrBlank() {
        assertThat(TextTokenizer.tokenize(null)).isEmpty();
        assertThat(TextTokenizer.tokenize("   ")).isEmpty();
        assertThat(TextTokenizer.tokenize("12345 ---")).isEmpty();
    }
}
