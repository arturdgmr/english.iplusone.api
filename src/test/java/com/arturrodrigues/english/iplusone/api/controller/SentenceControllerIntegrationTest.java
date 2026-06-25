package com.arturrodrigues.english.iplusone.api.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.arturrodrigues.english.iplusone.api.client.OpenAiClient;
import com.arturrodrigues.english.iplusone.api.service.VocabularyService;

@SpringBootTest
class SentenceControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private VocabularyService vocabularyService;

    @MockitoBean
    private OpenAiClient openAiClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        vocabularyService.clear();
        vocabularyService.addWords(List.of("i", "like", "soccer", "my", "friends", "play"));
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void generatesSentenceForTargetWord() throws Exception {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("I like soccer although my friends play.");

        mockMvc.perform(post("/api/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetWord\": \"although\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetWord").value("although"))
                .andExpect(jsonPath("$.sentence").value("I like soccer although my friends play."))
                .andExpect(jsonPath("$.unknownWords").isArray())
                .andExpect(jsonPath("$.attempts").value(1));
    }

    @Test
    void rejectsBlankTargetWord() throws Exception {
        mockMvc.perform(post("/api/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetWord\": \"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void returnsBadGatewayWhenOpenAiFails() throws Exception {
        when(openAiClient.complete(anyString(), anyString()))
                .thenThrow(new com.arturrodrigues.english.iplusone.api.exception.OpenAiCommunicationException(
                        "boom"));

        mockMvc.perform(post("/api/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetWord\": \"although\"}"))
                .andExpect(status().isBadGateway());
    }

    @Test
    void returnsUnprocessableWhenNoValidSentence() throws Exception {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("strangers wander random unknown places nonstop everywhere.");

        mockMvc.perform(post("/api/sentences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetWord\": \"although\"}"))
                .andExpect(status().isUnprocessableEntity());
    }
}
