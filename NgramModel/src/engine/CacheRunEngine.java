package engine;

import tokenunit.Tokensequence;

import java.io.File;
import java.util.Optional;

public interface CacheRunEngine<K> {
    Optional<K> completePostToken(Tokensequence<K> nseq);
    double calculateProbability(Tokensequence<K> nseq);
    void reloadCacheContent();
    void updateCacheList(File newCurFile);
    void preAction();
    void run();
}
