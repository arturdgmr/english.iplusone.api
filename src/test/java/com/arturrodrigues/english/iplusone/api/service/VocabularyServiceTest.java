package com.arturrodrigues.english.iplusone.api.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VocabularyServiceTest {

    private VocabularyService service;

    @BeforeEach
    void setUp() {
        service = new VocabularyService();
    }

    @Test
    void addsWordsIgnoringDuplicatesBlanksAndNulls() {
        int added = service.addWords(Arrays.asList("i", "like", "LIKE", "  ", null, "soccer"));

        assertThat(added).isEqualTo(3);
        assertThat(service.count()).isEqualTo(3);
        assertThat(service.getAllWords()).containsExactly("i", "like", "soccer");
    }

    @Test
    void addWordsHandlesNullCollection() {
        assertThat(service.addWords(null)).isZero();
        assertThat(service.count()).isZero();
    }

    @Test
    void containsIsCaseInsensitiveAndNullSafe() {
        service.addWords(List.of("house"));

        assertThat(service.contains("House")).isTrue();
        assertThat(service.contains("car")).isFalse();
        assertThat(service.contains(null)).isFalse();
    }

    @Test
    void snapshotAndClear() {
        service.addWords(List.of("a", "b"));

        assertThat(service.snapshot()).containsExactlyInAnyOrder("a", "b");

        service.clear();
        assertThat(service.count()).isZero();
    }
}
