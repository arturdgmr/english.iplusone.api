package com.arturrodrigues.english.iplusone.api.exception;

/**
 * Thrown when an uploaded file cannot be read as a valid PDF document.
 */
public class InvalidPdfException extends RuntimeException {

    public InvalidPdfException(String message) {
        super(message);
    }

    public InvalidPdfException(String message, Throwable cause) {
        super(message, cause);
    }
}
