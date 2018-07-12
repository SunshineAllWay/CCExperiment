package engine;

import tokenunit.Tokensequence;

import java.io.File;
import java.util.Optional;

public interface CacheRunEngine<K> extends CCRunEngine<K>{
    void reloadCacheContent();
    void updateCacheList(File newCurFile);
}
