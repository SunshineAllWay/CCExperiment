package searchunit;

import tokenunit.Tokensequence;

import java.util.ArrayList;

public interface ContextSearcher{
    ArrayList<Tokensequence> getSimilarSequences(Tokensequence seq);
    double calculateSequenceSimilarity(Tokensequence seq1, Tokensequence seq2);
}
