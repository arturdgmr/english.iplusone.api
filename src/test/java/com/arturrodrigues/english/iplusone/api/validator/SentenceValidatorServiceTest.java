package com.arturrodrigues.english.iplusone.api.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.arturrodrigues.english.iplusone.api.model.ValidationResult;
import com.arturrodrigues.english.iplusone.api.service.VocabularyService;

class SentenceValidatorServiceTest {

    private VocabularyService vocabularyService;
    private SentenceValidatorService validator;

    @BeforeEach
    void setUp() {
        vocabularyService = new VocabularyService();
        vocabularyService.addWords(List.of("i", "like", "soccer", "my", "friends"));
        validator = new SentenceValidatorService(vocabularyService);
    }

    @Test
    void targetWordIsNeverCountedAsUnknown() {
        ValidationResult result = validator.validate("I like soccer although my friends.", "although");

        assertThat(result.unknownWords()).isEmpty();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void countsDistinctUnknownWords() {
        ValidationResult result = validator.validate("I play soccer although my brothers run.", "although");

        assertThat(result.unknownWords()).containsExactly("play", "brothers", "run");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void acceptsAtMostOneUnknownWord() {
        ValidationResult result = validator.validate("I like soccer although my friends play.", "although");

        assertThat(result.unknownWords()).containsExactly("play");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void deduplicatesRepeatedUnknownWords() {
        ValidationResult result = validator.validate("play play play although", "although");

        assertThat(result.unknownWords()).containsExactly("play");
    }
}
