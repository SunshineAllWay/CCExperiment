package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.cache.Cache;
import org.apache.lucene.util.cache.DoubleBarrelLRUCache;
import org.apache.lucene.util.CloseableThreadLocal;
final class TermInfosReader {
  private final Directory directory;
  private final String segment;
  private final FieldInfos fieldInfos;
  private final CloseableThreadLocal<ThreadResources> threadResources = new CloseableThreadLocal<ThreadResources>();
  private final SegmentTermEnum origEnum;
  private final long size;
  private final Term[] indexTerms;
  private final TermInfo[] indexInfos;
  private final long[] indexPointers;
  private final int totalIndexInterval;
  private final static int DEFAULT_CACHE_SIZE = 1024;
  private final static class TermInfoAndOrd extends TermInfo {
    final int termOrd;
    public TermInfoAndOrd(TermInfo ti, int termOrd) {
      super(ti);
      this.termOrd = termOrd;
    }
  }
  private final Cache<Term,TermInfoAndOrd> termsCache = new DoubleBarrelLRUCache<Term,TermInfoAndOrd>(DEFAULT_CACHE_SIZE);
  private static final class ThreadResources {
    SegmentTermEnum termEnum;
  }
  TermInfosReader(Directory dir, String seg, FieldInfos fis, int readBufferSize, int indexDivisor)
       throws CorruptIndexException, IOException {
    boolean success = false;
    if (indexDivisor < 1 && indexDivisor != -1) {
      throw new IllegalArgumentException("indexDivisor must be -1 (don't load terms index) or greater than 0: got " + indexDivisor);
    }
    try {
      directory = dir;
      segment = seg;
      fieldInfos = fis;
      origEnum = new SegmentTermEnum(directory.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_EXTENSION),
          readBufferSize), fieldInfos, false);
      size = origEnum.size;
      if (indexDivisor != -1) {
        totalIndexInterval = origEnum.indexInterval * indexDivisor;
        final SegmentTermEnum indexEnum = new SegmentTermEnum(directory.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.TERMS_INDEX_EXTENSION),
                                                                                  readBufferSize), fieldInfos, true);
        try {
          int indexSize = 1+((int)indexEnum.size-1)/indexDivisor;  
          indexTerms = new Term[indexSize];
          indexInfos = new TermInfo[indexSize];
          indexPointers = new long[indexSize];
          for (int i = 0; indexEnum.next(); i++) {
            indexTerms[i] = indexEnum.term();
            indexInfos[i] = indexEnum.termInfo();
            indexPointers[i] = indexEnum.indexPointer;
            for (int j = 1; j < indexDivisor; j++)
              if (!indexEnum.next())
                break;
          }
        } finally {
          indexEnum.close();
        }
      } else {
        totalIndexInterval = -1;
        indexTerms = null;
        indexInfos = null;
        indexPointers = null;
      }
      success = true;
    } finally {
      if (!success) {
        close();
      }
    }
  }
  public int getSkipInterval() {
    return origEnum.skipInterval;
  }
  public int getMaxSkipLevels() {
    return origEnum.maxSkipLevels;
  }
  final void close() throws IOException {
    if (origEnum != null)
      origEnum.close();
    threadResources.close();
    termsCache.close();
  }
  final long size() {
    return size;
  }
  private ThreadResources getThreadResources() {
    ThreadResources resources = threadResources.get();
    if (resources == null) {
      resources = new ThreadResources();
      resources.termEnum = terms();
      threadResources.set(resources);
    }
    return resources;
  }
  private final int getIndexOffset(Term term) {
    int lo = 0;					  
    int hi = indexTerms.length - 1;
    while (hi >= lo) {
      int mid = (lo + hi) >>> 1;
      int delta = term.compareTo(indexTerms[mid]);
      if (delta < 0)
	hi = mid - 1;
      else if (delta > 0)
	lo = mid + 1;
      else
	return mid;
    }
    return hi;
  }
  private final void seekEnum(SegmentTermEnum enumerator, int indexOffset) throws IOException {
    enumerator.seek(indexPointers[indexOffset],
                   ((long) indexOffset * totalIndexInterval) - 1,
                   indexTerms[indexOffset], indexInfos[indexOffset]);
  }
  TermInfo get(Term term) throws IOException {
    return get(term, false);
  }
  private TermInfo get(Term term, boolean mustSeekEnum) throws IOException {
    if (size == 0) return null;
    ensureIndexIsRead();
    TermInfoAndOrd tiOrd = termsCache.get(term);
    ThreadResources resources = getThreadResources();
    if (!mustSeekEnum && tiOrd != null) {
      return tiOrd;
    }
    SegmentTermEnum enumerator = resources.termEnum;
    if (enumerator.term() != null                 
	&& ((enumerator.prev() != null && term.compareTo(enumerator.prev())> 0)
	    || term.compareTo(enumerator.term()) >= 0)) {
      int enumOffset = (int)(enumerator.position/totalIndexInterval)+1;
      if (indexTerms.length == enumOffset	  
    || term.compareTo(indexTerms[enumOffset]) < 0) {
        final TermInfo ti;
        int numScans = enumerator.scanTo(term);
        if (enumerator.term() != null && term.compareTo(enumerator.term()) == 0) {
          ti = enumerator.termInfo();
          if (numScans > 1) {
            if (tiOrd == null) {
              termsCache.put(term, new TermInfoAndOrd(ti, (int) enumerator.position));
            } else {
              assert sameTermInfo(ti, tiOrd, enumerator);
              assert (int) enumerator.position == tiOrd.termOrd;
            }
          }
        } else {
          ti = null;
        }
        return ti;
      }  
    }
    final int indexPos;
    if (tiOrd != null) {
      indexPos = tiOrd.termOrd / totalIndexInterval;
    } else {
      indexPos = getIndexOffset(term);
    }
    seekEnum(enumerator, indexPos);
    enumerator.scanTo(term);
    final TermInfo ti;
    if (enumerator.term() != null && term.compareTo(enumerator.term()) == 0) {
      ti = enumerator.termInfo();
      if (tiOrd == null) {
        termsCache.put(term, new TermInfoAndOrd(ti, (int) enumerator.position));
      } else {
        assert sameTermInfo(ti, tiOrd, enumerator);
        assert (int) enumerator.position == tiOrd.termOrd;
      }
    } else {
      ti = null;
    }
    return ti;
  }
  private final boolean sameTermInfo(TermInfo ti1, TermInfo ti2, SegmentTermEnum enumerator) {
    if (ti1.docFreq != ti2.docFreq) {
      return false;
    }
    if (ti1.freqPointer != ti2.freqPointer) {
      return false;
    }
    if (ti1.proxPointer != ti2.proxPointer) {
      return false;
    }
    if (ti1.docFreq >= enumerator.skipInterval &&
        ti1.skipOffset != ti2.skipOffset) {
      return false;
    }
    return true;
  }
  private void ensureIndexIsRead() {
    if (indexTerms == null) {
      throw new IllegalStateException("terms index was not loaded when this reader was created");
    }
  }
  final long getPosition(Term term) throws IOException {
    if (size == 0) return -1;
    ensureIndexIsRead();
    int indexOffset = getIndexOffset(term);
    SegmentTermEnum enumerator = getThreadResources().termEnum;
    seekEnum(enumerator, indexOffset);
    while(term.compareTo(enumerator.term()) > 0 && enumerator.next()) {}
    if (term.compareTo(enumerator.term()) == 0)
      return enumerator.position;
    else
      return -1;
  }
  public SegmentTermEnum terms() {
    return (SegmentTermEnum)origEnum.clone();
  }
  public SegmentTermEnum terms(Term term) throws IOException {
    get(term, true);
    return (SegmentTermEnum)getThreadResources().termEnum.clone();
  }
}
