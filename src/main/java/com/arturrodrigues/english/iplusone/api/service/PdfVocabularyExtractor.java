package com.arturrodrigues.english.iplusone.api.service;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import com.arturrodrigues.english.iplusone.api.exception.EmptyPdfException;
import com.arturrodrigues.english.iplusone.api.exception.InvalidPdfException;
import com.arturrodrigues.english.iplusone.api.util.TextTokenizer;

/**
 * Extracts a de-duplicated set of words from the raw text content of a PDF.
 */
@Component
public class PdfVocabularyExtractor {

    /**
     * Reads the raw text of the supplied PDF and returns the unique words it
     * contains, in their order of first appearance.
     *
     * @throws InvalidPdfException if the bytes are missing or not a valid PDF
     * @throws EmptyPdfException   if the PDF has no extractable words
     */
    public Set<String> extractWords(byte[] pdfBytes) {
        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new InvalidPdfException("Uploaded PDF is empty");
        }

        String text;
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            text = stripper.getText(document);
        } catch (IOException ex) {
            throw new InvalidPdfException("Uploaded file is not a valid PDF", ex);
        }

        Set<String> words = new LinkedHashSet<>(TextTokenizer.tokenize(text));
        if (words.isEmpty()) {
            throw new EmptyPdfException("No words could be extracted from the PDF");
        }
        return words;
    }
}
