package org.apache.lucene.index;
import java.io.IOException;
public abstract class MergeScheduler {
  abstract void merge(IndexWriter writer)
    throws CorruptIndexException, IOException;
  abstract void close()
    throws CorruptIndexException, IOException;
}
