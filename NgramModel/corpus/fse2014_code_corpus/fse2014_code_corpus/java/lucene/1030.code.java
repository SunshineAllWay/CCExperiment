package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
public class InstantiatedTermEnum extends TermEnum {
  private final InstantiatedIndexReader reader;
  public InstantiatedTermEnum(InstantiatedIndexReader reader) {
    this.nextTermIndex = 0;
    this.reader = reader;
  }
  public InstantiatedTermEnum(InstantiatedIndexReader reader, int startPosition) {
    this.reader = reader;
    this.nextTermIndex = startPosition;
    next();
  }
  private int nextTermIndex;
  private InstantiatedTerm term;
  @Override
  public boolean next() {
    if (reader.getIndex().getOrderedTerms().length <= nextTermIndex) {
      return false;
    } else {
      term = reader.getIndex().getOrderedTerms()[nextTermIndex];
      nextTermIndex++;
      return true;
    }
  }
  @Override
  public Term term() {
    return term == null ? null : term.getTerm();
  }
  @Override
  public int docFreq() {
    return term.getAssociatedDocuments().length;
  }
  @Override
  public void close() {
  }
}
