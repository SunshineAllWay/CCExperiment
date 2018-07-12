package app;

import tokenunit.Tokensequence;

import java.util.ArrayList;

public interface CCRunApp<K> {
    ArrayList<K> completePostToken(Tokensequence<K> nseq);
}
