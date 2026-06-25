package com.arturrodrigues.english.iplusone.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.arturrodrigues.english.iplusone.api.client.OpenAiClient;
import com.arturrodrigues.english.iplusone.api.exception.SentenceGenerationException;
import com.arturrodrigues.english.iplusone.api.model.SentenceResponse;
import com.arturrodrigues.english.iplusone.api.validator.SentenceValidatorService;

@ExtendWith(MockitoExtension.class)
class SentenceGeneratorServiceTest {

    @Mock
    private OpenAiClient openAiClient;

    private VocabularyService vocabularyService;
    private SentenceGeneratorService service;

    @BeforeEach
    void setUp() {
        vocabularyService = new VocabularyService();
        vocabularyService.addWords(List.of("i", "like", "soccer", "my", "friends", "play", "every", "week"));
        SentenceValidatorService validator = new SentenceValidatorService(vocabularyService);
        service = new SentenceGeneratorService(openAiClient, vocabularyService, validator);
    }

    @Test
    void returnsSentenceOnFirstValidAttempt() {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("I like soccer although my friends play every week.");

        SentenceResponse response = service.generate("although");

        assertThat(response.targetWord()).isEqualTo("although");
        assertThat(response.sentence()).isEqualTo("I like soccer although my friends play every week.");
        assertThat(response.unknownWords()).isEmpty();
        assertThat(response.attempts()).isEqualTo(1);
        verify(openAiClient, times(1)).complete(anyString(), anyString());
    }

    @Test
    void stripsSurroundingQuotesFromModelOutput() {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("\"I like soccer although my friends play.\"");

        SentenceResponse response = service.generate("although");

        assertThat(response.sentence()).isEqualTo("I like soccer although my friends play.");
    }

    @Test
    void regeneratesWhenFirstSentenceHasTooManyUnknownWords() {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("I enjoy basketball although strangers wander.")
                .thenReturn("I like soccer although my friends play.");

        SentenceResponse response = service.generate("although");

        assertThat(response.attempts()).isEqualTo(2);
        assertThat(response.sentence()).isEqualTo("I like soccer although my friends play.");
        // The retry prompt must explicitly list the previously unknown words.
        verify(openAiClient).complete(anyString(), contains("enjoy"));
    }

    @Test
    void regeneratesWhenTargetWordIsMissing() {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("I like soccer with my friends.")
                .thenReturn("I like soccer although my friends play.");

        SentenceResponse response = service.generate("although");

        assertThat(response.attempts()).isEqualTo(2);
        verify(openAiClient, times(2)).complete(anyString(), anyString());
    }

    @Test
    void throwsAfterThreeFailedAttempts() {
        when(openAiClient.complete(anyString(), anyString()))
                .thenReturn("strangers wander around random unknown places nonstop.");

        assertThatThrownBy(() -> service.generate("although"))
                .isInstanceOf(SentenceGenerationException.class)
                .hasMessageContaining("although");

        verify(openAiClient, times(3)).complete(anyString(), anyString());
    }

    @Test
    void buildInitialPromptContainsVocabularyTargetAndRules() {
        String prompt = service.buildInitialPrompt("although");

        assertThat(prompt).contains("Known vocabulary:");
        assertThat(prompt).contains("Target word:");
        assertThat(prompt).contains("although");
        assertThat(prompt).contains("Generate exactly one sentence.");
    }

    @Test
    void buildRetryPromptMentionsUnknownWords() {
        String prompt = service.buildRetryPrompt("although", "I run fast.", List.of("run", "fast"));

        assertThat(prompt).contains("run, fast");
        assertThat(prompt).contains("although");
    }
}
