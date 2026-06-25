package com.arturrodrigues.english.iplusone.api.client;

/**
 * Abstraction over the chat completion capability of the OpenAI API, kept small
 * so it can be easily mocked in tests.
 */
public interface OpenAiClient {

    /**
     * Sends a system and a user message to the model and returns the raw text
     * content of the first completion choice.
     *
     * @throws com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException
     *         when the request fails or the response cannot be parsed
     */
    String complete(String systemPrompt, String userPrompt);
}
