package com.arturrodrigues.english.iplusone.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.arturrodrigues.english.iplusone.api.exception.EmptyPdfException;
import com.arturrodrigues.english.iplusone.api.exception.InvalidPdfException;
import com.arturrodrigues.english.iplusone.api.support.PdfTestFactory;

class PdfVocabularyExtractorTest {

    private final PdfVocabularyExtractor extractor = new PdfVocabularyExtractor();

    @Test
    void extractsUniqueLowercaseWordsFromPdf() {
        byte[] pdf = PdfTestFactory.pdfWithLines("I like soccer.", "House, Car and Family.", "like like");

        Set<String> words = extractor.extractWords(pdf);

        assertThat(words).containsExactlyInAnyOrder("i", "like", "soccer", "house", "car", "and", "family");
    }

    @Test
    void throwsInvalidPdfWhenBytesAreNullOrEmpty() {
        assertThatThrownBy(() -> extractor.extractWords(null))
                .isInstanceOf(InvalidPdfException.class);
        assertThatThrownBy(() -> extractor.extractWords(new byte[0]))
                .isInstanceOf(InvalidPdfException.class);
    }

    @Test
    void throwsInvalidPdfWhenBytesAreNotAPdf() {
        assertThatThrownBy(() -> extractor.extractWords("this is not a pdf".getBytes()))
                .isInstanceOf(InvalidPdfException.class);
    }

    @Test
    void throwsEmptyPdfWhenNoWordsCanBeExtracted() {
        byte[] pdf = PdfTestFactory.emptyPdf();

        assertThatThrownBy(() -> extractor.extractWords(pdf))
                .isInstanceOf(EmptyPdfException.class);
    }
}
