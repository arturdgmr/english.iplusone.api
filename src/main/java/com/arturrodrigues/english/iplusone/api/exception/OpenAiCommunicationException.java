package com.arturrodrigues.english.iplusone.api.exception;

/**
 * Thrown when the application fails to communicate with the OpenAI API
 * (network failure, timeout, non successful HTTP status, malformed response).
 */
public class OpenAiCommunicationException extends RuntimeException {

    public OpenAiCommunicationException(String message) {
        super(message);
    }

    public OpenAiCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
