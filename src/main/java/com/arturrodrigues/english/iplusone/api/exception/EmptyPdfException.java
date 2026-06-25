package com.arturrodrigues.english.iplusone.api.exception;

/**
 * Thrown when an uploaded PDF contains no extractable words.
 */
public class EmptyPdfException extends RuntimeException {

    public EmptyPdfException(String message) {
        super(message);
    }
}
