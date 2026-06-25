package com.arturrodrigues.english.iplusone.api.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.arturrodrigues.english.iplusone.api.client.OpenAiClient;
import com.arturrodrigues.english.iplusone.api.exception.SentenceGenerationException;
import com.arturrodrigues.english.iplusone.api.model.SentenceResponse;
import com.arturrodrigues.english.iplusone.api.model.ValidationResult;
import com.arturrodrigues.english.iplusone.api.util.TextTokenizer;
import com.arturrodrigues.english.iplusone.api.validator.SentenceValidatorService;

/**
 * Orchestrates the i+1 sentence generation: builds the prompt, asks the AI for a
 * sentence, validates it and, when needed, asks for a corrected version.
 */
@Service
public class SentenceGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(SentenceGeneratorService.class);

    static final int MAX_ATTEMPTS = 3;
    private static final String SYSTEM_PROMPT = "You are an English teacher.";

    private final OpenAiClient openAiClient;
    private final VocabularyService vocabularyService;
    private final SentenceValidatorService validatorService;

    public SentenceGeneratorService(OpenAiClient openAiClient,
                                    VocabularyService vocabularyService,
                                    SentenceValidatorService validatorService) {
        this.openAiClient = openAiClient;
        this.vocabularyService = vocabularyService;
        this.validatorService = validatorService;
    }

    public SentenceResponse generate(String targetWord) {
        String normalizedTarget = targetWord.toLowerCase().trim();

        String sentence = null;
        ValidationResult lastResult = null;

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String prompt = (attempt == 1)
                    ? buildInitialPrompt(normalizedTarget)
                    : buildRetryPrompt(normalizedTarget, sentence, lastResult.unknownWords());

            sentence = cleanSentence(openAiClient.complete(SYSTEM_PROMPT, prompt));

            lastResult = validatorService.validate(sentence, normalizedTarget);
            boolean targetPresent = sentenceContainsTarget(sentence, normalizedTarget);

            if (targetPresent && lastResult.isValid()) {
                return new SentenceResponse(normalizedTarget, sentence, lastResult.unknownWords(), attempt);
            }

            log.info("Attempt {} rejected for target '{}': targetPresent={}, unknownWords={}",
                    attempt, normalizedTarget, targetPresent, lastResult.unknownWords());
        }

        throw new SentenceGenerationException(
                "Could not generate a valid i+1 sentence for '" + normalizedTarget
                        + "' within " + MAX_ATTEMPTS + " attempts");
    }

    private boolean sentenceContainsTarget(String sentence, String normalizedTarget) {
        return TextTokenizer.tokenize(sentence).contains(normalizedTarget);
    }

    private String cleanSentence(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.trim();
        if (cleaned.startsWith("\"") && cleaned.endsWith("\"") && cleaned.length() > 1) {
            cleaned = cleaned.substring(1, cleaned.length() - 1).trim();
        }
        return cleaned;
    }

    String buildInitialPrompt(String targetWord) {
        StringBuilder sb = new StringBuilder();
        sb.append("Known vocabulary:\n");
        sb.append(String.join(", ", vocabularyService.getAllWords()));
        sb.append("\n\nTarget word:\n").append(targetWord);
        sb.append("\n\nRules:\n");
        sb.append("- Generate exactly one sentence.\n");
        sb.append("- Use the target word.\n");
        sb.append("- Use only words from the known vocabulary whenever possible.\n");
        sb.append("- Avoid introducing new unknown words.\n");
        sb.append("- The sentence should preferably contain between 8 and 15 words.\n");
        sb.append("- Return only the sentence.\n");
        sb.append("- The sentence must be grammatically correct.\n");
        sb.append("- The sentence must sound natural.\n");
        return sb.toString();
    }

    String buildRetryPrompt(String targetWord, String previousSentence, List<String> unknownWords) {
        StringBuilder sb = new StringBuilder(buildInitialPrompt(targetWord));
        sb.append("\n\nThe previous sentence was: ").append(previousSentence);
        sb.append("\nIt contained the following unknown words that are not in the known vocabulary: ");
        sb.append(String.join(", ", unknownWords));
        sb.append("\nRewrite the sentence so that it does not use those unknown words, ");
        sb.append("while still using the target word '").append(targetWord).append("'.");
        return sb.toString();
    }
}
