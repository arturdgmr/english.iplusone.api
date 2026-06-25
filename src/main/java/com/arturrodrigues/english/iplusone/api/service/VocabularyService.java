package com.arturrodrigues.english.iplusone.api.service;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

/**
 * In-memory store of the user's known vocabulary.
 *
 * <p>Backed by a hash based {@link Set} so that membership checks run in
 * (amortised) constant time, as required by the i+1 validation.
 */
@Service
public class VocabularyService {

    private final Set<String> knownWords = ConcurrentHashMap.newKeySet();

    /**
     * Adds the given words to the known vocabulary, ignoring blanks and
     * duplicates.
     *
     * @return the number of words that were not already present
     */
    public int addWords(Collection<String> words) {
        if (words == null) {
            return 0;
        }
        int added = 0;
        for (String word : words) {
            if (word != null && !word.isBlank() && knownWords.add(word.toLowerCase())) {
                added++;
            }
        }
        return added;
    }

    public boolean contains(String word) {
        return word != null && knownWords.contains(word.toLowerCase());
    }

    public int count() {
        return knownWords.size();
    }

    /**
     * @return the known words, sorted alphabetically for a stable response
     */
    public List<String> getAllWords() {
        return knownWords.stream().sorted().toList();
    }

    public Set<String> snapshot() {
        return Set.copyOf(knownWords);
    }

    public void clear() {
        knownWords.clear();
    }
}
