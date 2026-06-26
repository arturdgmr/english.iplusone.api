package com.arturrodrigues.english.iplusone.api.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionRequest;
import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionResponse;
import com.arturrodrigues.english.iplusone.api.config.OpenAiProperties;
import com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException;

import feign.FeignException;

/**
 * Default {@link OpenAiClient} implementation backed by the declarative
 * {@link OpenAiFeignClient} pointed at the OpenAI chat completions endpoint.
 */
@Component
public class OpenAiChatClient implements OpenAiClient {

    private final OpenAiFeignClient feignClient;
    private final OpenAiProperties properties;

    public OpenAiChatClient(OpenAiFeignClient feignClient, OpenAiProperties properties) {
        this.feignClient = feignClient;
        this.properties = properties;
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        if (properties.getApiKey() == null || properties.getApiKey().isBlank()) {
            throw new OpenAiCommunicationException(
                    "OpenAI API key is not configured. Set the OPENAI_API_KEY environment variable.");
        }

        ChatCompletionRequest request = new ChatCompletionRequest(
                properties.getModel(),
                List.of(
                        ChatCompletionRequest.Message.system(systemPrompt),
                        ChatCompletionRequest.Message.user(userPrompt)),
                properties.getTemperature());

        ChatCompletionResponse response;
        try {
            response = feignClient.chatCompletions("Bearer " + properties.getApiKey(), request);
        } catch (FeignException ex) {
            throw new OpenAiCommunicationException("Failed to communicate with OpenAI: " + ex.getMessage(), ex);
        }

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new OpenAiCommunicationException("OpenAI returned an empty response");
        }

        ChatCompletionResponse.Choice choice = response.choices().get(0);
        if (choice.message() == null || choice.message().content() == null
                || choice.message().content().isBlank()) {
            throw new OpenAiCommunicationException("OpenAI returned a response without content");
        }

        return choice.message().content().trim();
    }
}
