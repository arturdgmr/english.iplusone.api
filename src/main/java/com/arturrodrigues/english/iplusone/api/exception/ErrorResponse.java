package com.arturrodrigues.english.iplusone.api.exception;

import java.time.Instant;

/**
 * Standard error body returned by {@link GlobalExceptionHandler}.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message) {

    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(Instant.now(), status, error, message);
    }
}
