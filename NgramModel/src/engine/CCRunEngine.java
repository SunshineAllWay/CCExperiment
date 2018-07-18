package engine;

import jdk.nashorn.internal.parser.TokenStream;
import model.BasicNGram;
import tokenunit.Tokensequence;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

public interface CCRunEngine {
    ArrayList<String> completePostToken(Tokensequence nseq);
    double calculateProbability(Tokensequence nseq);
    BasicNGram[] getNgramArray();
    void preAction();
    void run();
}
