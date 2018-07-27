package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
public class PrefixTermEnum extends FilteredTermEnum {
  private final Term prefix;
  private boolean endEnum = false;
  public PrefixTermEnum(IndexReader reader, Term prefix) throws IOException {
    this.prefix = prefix;
    setEnum(reader.terms(new Term(prefix.field(), prefix.text())));
  }
  @Override
  public float difference() {
    return 1.0f;
  }
  @Override
  protected boolean endEnum() {
    return endEnum;
  }
  protected Term getPrefixTerm() {
      return prefix;
  }
  @Override
  protected boolean termCompare(Term term) {
    if (term.field() == prefix.field() && term.text().startsWith(prefix.text())) {                                                                              
      return true;
    }
    endEnum = true;
    return false;
  }
}
