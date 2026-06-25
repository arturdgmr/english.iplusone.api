package com.arturrodrigues.english.iplusone.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.arturrodrigues.english.iplusone.api.service.VocabularyService;
import com.arturrodrigues.english.iplusone.api.support.PdfTestFactory;

@SpringBootTest
class VocabularyControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private VocabularyService vocabularyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        vocabularyService.clear();
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void importStoresWordsAndStatsAndListReflectThem() throws Exception {
        byte[] pdf = PdfTestFactory.pdfWithLines("I like soccer.", "House, Car and Family.");
        MockMultipartFile file = new MockMultipartFile(
                "file", "vocab.pdf", "application/pdf", pdf);

        mockMvc.perform(multipart("/api/vocabulary/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedWords").value(7))
                .andExpect(jsonPath("$.knownWords").value(7));

        mockMvc.perform(get("/api/vocabulary/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.knownWords").value(7));

        mockMvc.perform(get("/api/vocabulary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.words").isArray())
                .andExpect(jsonPath("$.words[0]").value("and"));
    }

    @Test
    void reimportReportsAllExtractedWordsEvenWhenAlreadyKnown() throws Exception {
        byte[] pdf = PdfTestFactory.pdfWithLines("I like soccer.", "House, Car and Family.");
        MockMultipartFile file = new MockMultipartFile(
                "file", "vocab.pdf", "application/pdf", pdf);

        mockMvc.perform(multipart("/api/vocabulary/import").file(file))
                .andExpect(status().isCreated());

        // Re-importing the same PDF still reports all 7 extracted words, while the
        // total known words stays unchanged.
        mockMvc.perform(multipart("/api/vocabulary/import").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.importedWords").value(7))
                .andExpect(jsonPath("$.knownWords").value(7));
    }

    @Test
    void importRejectsEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/vocabulary/import").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void importRejectsNonPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "just some text".getBytes());

        mockMvc.perform(multipart("/api/vocabulary/import").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyVocabularyReturnsZeroStats() throws Exception {
        mockMvc.perform(get("/api/vocabulary/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.knownWords").value(0));
    }
}
