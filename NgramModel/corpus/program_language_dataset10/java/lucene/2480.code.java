package org.apache.solr.search;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.util.OpenBitSet;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
class DocSetCollector extends Collector {
  int pos=0;
  OpenBitSet bits;
  final int maxDoc;
  final int smallSetSize;
  int base;
  final int[] scratch;
  DocSetCollector(int smallSetSize, int maxDoc) {
    this.smallSetSize = smallSetSize;
    this.maxDoc = maxDoc;
    this.scratch = new int[smallSetSize];
  }
  public void collect(int doc) throws IOException {
    doc += base;
    if (pos < scratch.length) {
      scratch[pos]=doc;
    } else {
      if (bits==null) bits = new OpenBitSet(maxDoc);
      bits.fastSet(doc);
    }
    pos++;
  }
  public DocSet getDocSet() {
    if (pos<=scratch.length) {
      return new SortedIntDocSet(scratch, pos);
    } else {
      for (int i=0; i<scratch.length; i++) bits.fastSet(scratch[i]);
      return new BitDocSet(bits,pos);
    }
  }
  public void setScorer(Scorer scorer) throws IOException {
  }
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.base = docBase;
  }
  public boolean acceptsDocsOutOfOrder() {
    return false;
  }
}
class DocSetDelegateCollector extends DocSetCollector {
  final Collector collector;
  DocSetDelegateCollector(int smallSetSize, int maxDoc, Collector collector) {
    super(smallSetSize, maxDoc);
    this.collector = collector;
  }
  public void collect(int doc) throws IOException {
    collector.collect(doc);
    doc += base;
    if (pos < scratch.length) {
      scratch[pos]=doc;
    } else {
      if (bits==null) bits = new OpenBitSet(maxDoc);
      bits.fastSet(doc);
    }
    pos++;
  }
  public DocSet getDocSet() {
    if (pos<=scratch.length) {
      return new SortedIntDocSet(scratch, pos);
    } else {
      for (int i=0; i<scratch.length; i++) bits.fastSet(scratch[i]);
      return new BitDocSet(bits,pos);
    }
  }
  public void setScorer(Scorer scorer) throws IOException {
    collector.setScorer(scorer);
  }
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    collector.setNextReader(reader, docBase);
    this.base = docBase;
  }
}
