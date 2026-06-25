package com.arturrodrigues.english.iplusone.api.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the OpenAI integration, bound from the {@code openai.*}
 * namespace in {@code application.yml} (or the matching environment variables).
 */
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    /** OpenAI API key. Should be provided via the OPENAI_API_KEY environment variable. */
    private String apiKey = "";

    /** Base URL of the OpenAI compatible API. */
    private String baseUrl = "https://api.openai.com/v1";

    /** Chat model used to generate sentences. */
    private String model = "gpt-4o-mini";

    /** Sampling temperature passed to the model. */
    private double temperature = 0.7;

    /** Request timeout for calls to the OpenAI API. */
    private Duration timeout = Duration.ofSeconds(30);

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}
