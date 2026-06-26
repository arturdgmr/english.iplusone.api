package com.arturrodrigues.english.iplusone.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionRequest;
import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionResponse;
import com.arturrodrigues.english.iplusone.api.config.OpenAiProperties;
import com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException;

import feign.FeignException;
import feign.Request;
import feign.RequestTemplate;

class OpenAiChatClientTest {

    private OpenAiProperties properties(String apiKey) {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey(apiKey);
        properties.setModel("gpt-4o-mini");
        return properties;
    }

    private ChatCompletionResponse response(String content) {
        return new ChatCompletionResponse(List.of(
                new ChatCompletionResponse.Choice(
                        new ChatCompletionResponse.Message("assistant", content))));
    }

    @Test
    void returnsContentFromFirstChoice() {
        OpenAiFeignClient feignClient = mock(OpenAiFeignClient.class);
        when(feignClient.chatCompletions(eq("Bearer test-key"), any(ChatCompletionRequest.class)))
                .thenReturn(response("  Hello world.  "));

        OpenAiChatClient client = new OpenAiChatClient(feignClient, properties("test-key"));

        String result = client.complete("system", "user");

        assertThat(result).isEqualTo("Hello world.");
        verify(feignClient).chatCompletions(eq("Bearer test-key"), any(ChatCompletionRequest.class));
    }

    @Test
    void throwsWhenApiKeyMissing() {
        OpenAiFeignClient feignClient = mock(OpenAiFeignClient.class);
        OpenAiChatClient client = new OpenAiChatClient(feignClient, properties(""));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("API key");

        verifyNoInteractions(feignClient);
    }

    @Test
    void throwsOnServerError() {
        OpenAiFeignClient feignClient = mock(OpenAiFeignClient.class);
        Request request = Request.create(Request.HttpMethod.POST, "/chat/completions",
                java.util.Map.of(), Request.Body.empty(), new RequestTemplate());
        when(feignClient.chatCompletions(any(), any()))
                .thenThrow(new FeignException.InternalServerError("boom", request, null, java.util.Map.of()));

        OpenAiChatClient client = new OpenAiChatClient(feignClient, properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class);
    }

    @Test
    void throwsWhenNoChoicesReturned() {
        OpenAiFeignClient feignClient = mock(OpenAiFeignClient.class);
        when(feignClient.chatCompletions(any(), any()))
                .thenReturn(new ChatCompletionResponse(List.of()));

        OpenAiChatClient client = new OpenAiChatClient(feignClient, properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void throwsWhenChoiceHasNoContent() {
        OpenAiFeignClient feignClient = mock(OpenAiFeignClient.class);
        when(feignClient.chatCompletions(any(), any()))
                .thenReturn(response("  "));

        OpenAiChatClient client = new OpenAiChatClient(feignClient, properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("content");
    }
}
