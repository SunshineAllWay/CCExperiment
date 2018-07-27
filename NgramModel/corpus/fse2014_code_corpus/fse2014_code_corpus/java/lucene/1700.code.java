package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
public class SingleTermEnum extends FilteredTermEnum {
  private Term singleTerm;
  private boolean endEnum = false;
  public SingleTermEnum(IndexReader reader, Term singleTerm) throws IOException {
    super();
    this.singleTerm = singleTerm;
    setEnum(reader.terms(singleTerm));
  }
  @Override
  public float difference() {
    return 1.0F;
  }
  @Override
  protected boolean endEnum() {
    return endEnum;
  }
  @Override
  protected boolean termCompare(Term term) {
    if (term.equals(singleTerm)) {
      return true;
    } else {
      endEnum = true;
      return false;
    }
  }
}
