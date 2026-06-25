package com.arturrodrigues.english.iplusone.api.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.arturrodrigues.english.iplusone.api.exception.InvalidPdfException;
import com.arturrodrigues.english.iplusone.api.model.ImportResponse;
import com.arturrodrigues.english.iplusone.api.model.VocabularyListResponse;
import com.arturrodrigues.english.iplusone.api.model.VocabularyStatsResponse;
import com.arturrodrigues.english.iplusone.api.service.PdfVocabularyExtractor;
import com.arturrodrigues.english.iplusone.api.service.VocabularyService;

/**
 * Endpoints for managing the user's known vocabulary.
 */
@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final PdfVocabularyExtractor extractor;
    private final VocabularyService vocabularyService;

    public VocabularyController(PdfVocabularyExtractor extractor, VocabularyService vocabularyService) {
        this.extractor = extractor;
        this.vocabularyService = vocabularyService;
    }

    @PostMapping(path = "/import", consumes = "multipart/form-data")
    public ResponseEntity<ImportResponse> importPdf(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPdfException("Uploaded PDF is empty");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (java.io.IOException ex) {
            throw new InvalidPdfException("Could not read the uploaded file", ex);
        }

        Set<String> words = extractor.extractWords(bytes);
        int imported = vocabularyService.addWords(words);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ImportResponse(imported, vocabularyService.count()));
    }

    @GetMapping("/stats")
    public VocabularyStatsResponse stats() {
        return new VocabularyStatsResponse(vocabularyService.count());
    }

    @GetMapping
    public VocabularyListResponse list() {
        return new VocabularyListResponse(vocabularyService.getAllWords());
    }
}
