package engine;

import jdk.nashorn.internal.parser.TokenStream;
import model.BasicNGram;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public interface CCRunEngine<K> {
    ArrayList<K> completePostToken(Tokensequence<K> nseq);
    double calculateProbability(Tokensequence<K> nseq);
    BasicNGram<K>[] getNgramArray();
    void preAction();
    void run();
}
