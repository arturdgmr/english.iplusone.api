package com.arturrodrigues.english.iplusone.api.model;

import java.util.List;

/**
 * Body of {@code GET /api/vocabulary}.
 */
public record VocabularyListResponse(List<String> words) {
}
