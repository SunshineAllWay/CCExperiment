package engine;

import tokenunit.Tokensequence;

import java.io.File;
import java.util.Optional;

public interface CacheRunEngine<K> {
    Optional<K> completePostToken();
    double calculateProbability(Tokensequence<K> nseq);
    void reloadCacheContent();
    void updateCacheList(File newCurFile);
    void preAction();
    void run();
}
