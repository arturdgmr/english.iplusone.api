package com.arturrodrigues.english.iplusone.api.model;

import java.util.List;

/**
 * Body of the response of {@code POST /api/sentences}.
 */
public record SentenceResponse(
        String targetWord,
        String sentence,
        List<String> unknownWords,
        int attempts) {
}
