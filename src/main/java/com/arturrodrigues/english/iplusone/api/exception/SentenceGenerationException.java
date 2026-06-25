package com.arturrodrigues.english.iplusone.api.exception;

/**
 * Thrown when the AI is unable to produce a valid i+1 sentence within the
 * maximum number of attempts.
 */
public class SentenceGenerationException extends RuntimeException {

    public SentenceGenerationException(String message) {
        super(message);
    }
}
