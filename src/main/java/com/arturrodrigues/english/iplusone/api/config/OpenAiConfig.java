package com.arturrodrigues.english.iplusone.api.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Request;

/**
 * Enables the {@link OpenAiProperties} binding and applies the configured
 * timeout to the OpenFeign client used to talk to the OpenAI API.
 */
@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiConfig {

    @Bean
    public Request.Options openAiFeignOptions(OpenAiProperties properties) {
        long timeoutMillis = properties.getTimeout().toMillis();
        return new Request.Options(
                timeoutMillis, TimeUnit.MILLISECONDS,
                timeoutMillis, TimeUnit.MILLISECONDS,
                true);
    }
}
