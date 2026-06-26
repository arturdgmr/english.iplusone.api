package com.arturrodrigues.english.iplusone.api.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionRequest;
import com.arturrodrigues.english.iplusone.api.client.dto.ChatCompletionResponse;

/**
 * Declarative OpenFeign client for the OpenAI chat completions endpoint. The
 * base URL is taken from the {@code openai.base-url} configuration property.
 */
@FeignClient(name = "openAiClient", url = "${openai.base-url}")
public interface OpenAiFeignClient {

    @PostMapping(
            value = "/chat/completions",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ChatCompletionResponse chatCompletions(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestBody ChatCompletionRequest request);
}
