package searchunit;

import tokenunit.Tokensequence;

import java.util.ArrayList;

public interface ContextSearcher<K> {
    ArrayList<Tokensequence<K>> getSimilarSequences(Tokensequence<K> seq);
    double calculateSequenceSimilarity(Tokensequence<K> seq1, Tokensequence<K> seq2);
}
