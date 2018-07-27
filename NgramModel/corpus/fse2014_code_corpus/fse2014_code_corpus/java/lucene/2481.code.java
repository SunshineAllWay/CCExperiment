package org.apache.solr.search;
public class DocSlice extends DocSetBase implements DocList {
  final int offset;    
  final int len;       
  final int[] docs;    
  final float[] scores;  
  final int matches;
  final float maxScore;
  public DocSlice(int offset, int len, int[] docs, float[] scores, int matches, float maxScore) {
    this.offset=offset;
    this.len=len;
    this.docs=docs;
    this.scores=scores;
    this.matches=matches;
    this.maxScore=maxScore;
  }
  public DocList subset(int offset, int len) {
    if (this.offset == offset && this.len==len) return this;
    int requestedEnd = offset + len;
    if (requestedEnd > docs.length && this.matches > docs.length) return null;
    int realEndDoc = Math.min(requestedEnd, docs.length);
    int realLen = Math.max(realEndDoc-offset,0);
    if (this.offset == offset && this.len == realLen) return this;
    return new DocSlice(offset, realLen, docs, scores, matches, maxScore);
  }
  public boolean hasScores() {
    return scores!=null;
  }
  public float maxScore() {
    return maxScore;
  }
  public int offset()  { return offset; }
  public int size()    { return len; }
  public int matches() { return matches; }
  public long memSize() {
    return (docs.length<<2)
            + (scores==null ? 0 : (scores.length<<2))
            + 24;
  }
  public boolean exists(int doc) {
    int end = offset+len;
    for (int i=offset; i<end; i++) {
      if (docs[i]==doc) return true;
    }
    return false;
  }
  public DocIterator iterator() {
    return new DocIterator() {
      int pos=offset;
      final int end=offset+len;
      public boolean hasNext() {
        return pos < end;
      }
      public Integer next() {
        return nextDoc();
      }
      public void remove() {
        throw new UnsupportedOperationException("The remove  operation is not supported by this Iterator.");
      }
      public int nextDoc() {
        return docs[pos++];
      }
      public float score() {
        return scores[pos-1];
      }
    };
  }
  @Override
  public DocSet intersection(DocSet other) {
    if (other instanceof SortedIntDocSet || other instanceof HashDocSet) {
      return other.intersection(this);
    }
    HashDocSet h = new HashDocSet(docs,offset,len);
    return h.intersection(other);
  }
  @Override
  public int intersectionSize(DocSet other) {
    if (other instanceof SortedIntDocSet || other instanceof HashDocSet) {
      return other.intersectionSize(this);
    }
    HashDocSet h = new HashDocSet(docs,offset,len);
    return h.intersectionSize(other);  
  }
}
