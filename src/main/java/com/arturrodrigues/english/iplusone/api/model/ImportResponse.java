package com.arturrodrigues.english.iplusone.api.model;

/**
 * Body of the response of {@code POST /api/vocabulary/import}.
 *
 * @param importedWords number of unique words extracted from the uploaded PDF
 * @param knownWords    total number of words stored in memory after the import
 */
public record ImportResponse(int importedWords, int knownWords) {
}
