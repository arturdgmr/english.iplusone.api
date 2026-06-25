package com.arturrodrigues.english.iplusone.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.arturrodrigues.english.iplusone.api.model.SentenceRequest;
import com.arturrodrigues.english.iplusone.api.model.SentenceResponse;
import com.arturrodrigues.english.iplusone.api.service.SentenceGeneratorService;

import jakarta.validation.Valid;

/**
 * Endpoint for generating i+1 sentences.
 */
@RestController
@RequestMapping("/api/sentences")
public class SentenceController {

    private final SentenceGeneratorService sentenceGeneratorService;

    public SentenceController(SentenceGeneratorService sentenceGeneratorService) {
        this.sentenceGeneratorService = sentenceGeneratorService;
    }

    @PostMapping
    public SentenceResponse generate(@Valid @RequestBody SentenceRequest request) {
        return sentenceGeneratorService.generate(request.targetWord());
    }
}
