package com.arturrodrigues.english.iplusone.api.model;

import java.util.List;

/**
 * Outcome of validating an AI generated sentence against the known vocabulary.
 *
 * @param unknownWords distinct words (other than the target word) that are not
 *                     part of the known vocabulary, preserving their order of
 *                     appearance in the sentence
 */
public record ValidationResult(List<String> unknownWords) {

    /**
     * A sentence is accepted when it introduces at most one unknown word, as
     * defined by the i+1 acceptance criteria.
     */
    public boolean isValid() {
        return unknownWords.size() <= 1;
    }
}
