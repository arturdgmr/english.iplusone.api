package com.arturrodrigues.english.iplusone.api.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.arturrodrigues.english.iplusone.api.config.OpenAiProperties;
import com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException;

class OpenAiChatClientTest {

    private OpenAiProperties properties(String apiKey) {
        OpenAiProperties properties = new OpenAiProperties();
        properties.setApiKey(apiKey);
        properties.setModel("gpt-4o-mini");
        return properties;
    }

    @Test
    void returnsContentFromFirstChoice() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess("""
                        {
                          "choices": [
                            {"message": {"role": "assistant", "content": "  Hello world.  "}}
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        OpenAiChatClient client = new OpenAiChatClient(builder.build(), properties("test-key"));

        String result = client.complete("system", "user");

        assertThat(result).isEqualTo("Hello world.");
        server.verify();
    }

    @Test
    void throwsWhenApiKeyMissing() {
        OpenAiChatClient client = new OpenAiChatClient(RestClient.builder().build(), properties(""));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("API key");
    }

    @Test
    void throwsOnServerError() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/chat/completions"))
                .andRespond(withServerError());

        OpenAiChatClient client = new OpenAiChatClient(builder.build(), properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class);
    }

    @Test
    void throwsWhenNoChoicesReturned() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/chat/completions"))
                .andRespond(withSuccess("{\"choices\": []}", MediaType.APPLICATION_JSON));

        OpenAiChatClient client = new OpenAiChatClient(builder.build(), properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void throwsWhenChoiceHasNoContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("/chat/completions"))
                .andRespond(withSuccess("""
                        {"choices": [{"message": {"role": "assistant", "content": "  "}}]}
                        """, MediaType.APPLICATION_JSON));

        OpenAiChatClient client = new OpenAiChatClient(builder.build(), properties("test-key"));

        assertThatThrownBy(() -> client.complete("system", "user"))
                .isInstanceOf(OpenAiCommunicationException.class)
                .hasMessageContaining("content");
    }
}
