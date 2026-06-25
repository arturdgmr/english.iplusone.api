package com.arturrodrigues.english.iplusone.api.validator;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.arturrodrigues.english.iplusone.api.model.ValidationResult;
import com.arturrodrigues.english.iplusone.api.service.VocabularyService;
import com.arturrodrigues.english.iplusone.api.util.TextTokenizer;

/**
 * Validates a sentence produced by the AI against the known vocabulary,
 * implementing the i+1 acceptance rules.
 */
@Service
public class SentenceValidatorService {

    private final VocabularyService vocabularyService;

    public SentenceValidatorService(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    /**
     * Tokenizes the sentence and collects the distinct words that are neither
     * the target word nor part of the known vocabulary.
     */
    public ValidationResult validate(String sentence, String targetWord) {
        String normalizedTarget = targetWord == null
                ? ""
                : targetWord.toLowerCase(Locale.ENGLISH).trim();

        Set<String> unknown = new LinkedHashSet<>();
        for (String word : TextTokenizer.tokenize(sentence)) {
            if (word.equals(normalizedTarget)) {
                continue;
            }
            if (!vocabularyService.contains(word)) {
                unknown.add(word);
            }
        }
        List<String> unknownWords = new ArrayList<>(unknown);
        return new ValidationResult(unknownWords);
    }
}
