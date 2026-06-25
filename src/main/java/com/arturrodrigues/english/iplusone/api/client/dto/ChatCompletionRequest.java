package com.arturrodrigues.english.iplusone.api.client.dto;

import java.util.List;

/**
 * Minimal request payload for the OpenAI chat completions endpoint.
 */
public record ChatCompletionRequest(
        String model,
        List<Message> messages,
        double temperature) {

    public record Message(String role, String content) {

        public static Message system(String content) {
            return new Message("system", content);
        }

        public static Message user(String content) {
            return new Message("user", content);
        }
    }
}
