package com.arturrodrigues.english.iplusone.api.client;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionRequest;
import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionResponse;
import com.arturrodrigues.english.iplusone.api.config.OpenAiProperties;
import com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException;

/**
 * Default {@link OpenAiClient} implementation backed by a {@link RestClient}
 * pointed at the OpenAI chat completions endpoint.
 */
@Component
public class OpenAiChatClient implements OpenAiClient {

    private final RestClient restClient;
    private final OpenAiProperties properties;

    public OpenAiChatClient(RestClient openAiRestClient, OpenAiProperties properties) {
        this.restClient = openAiRestClient;
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
            response = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);
        } catch (RestClientException ex) {
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
