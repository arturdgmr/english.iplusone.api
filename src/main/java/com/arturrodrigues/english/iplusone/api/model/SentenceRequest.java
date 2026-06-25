package com.arturrodrigues.english.iplusone.api.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Body of {@code POST /api/sentences}.
 */
public record SentenceRequest(
        @NotBlank(message = "targetWord must not be blank")
        String targetWord) {
}
