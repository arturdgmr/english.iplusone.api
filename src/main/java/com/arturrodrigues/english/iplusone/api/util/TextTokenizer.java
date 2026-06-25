package com.arturrodrigues.english.iplusone.api.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shared text normalization used both when extracting vocabulary from a PDF and
 * when validating a sentence returned by the AI.
 *
 * <p>The normalization rules are:
 * <ol>
 *   <li>convert everything to lower case;</li>
 *   <li>remove punctuation and any character that is not a latin letter;</li>
 *   <li>split the remaining content into words.</li>
 * </ol>
 */
public final class TextTokenizer {

    private TextTokenizer() {
    }

    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        String lower = text.toLowerCase(Locale.ENGLISH);
        String[] parts = lower.split("[^a-z]+");
        List<String> words = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (!part.isBlank()) {
                words.add(part);
            }
        }
        return words;
    }
}
