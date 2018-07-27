package org.apache.lucene.search.regex;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import java.io.IOException;
public class RegexTermEnum extends FilteredTermEnum {
  private String field = "";
  private String pre = "";
  private boolean endEnum = false;
  private RegexCapabilities regexImpl;
  public RegexTermEnum(IndexReader reader, Term term, RegexCapabilities regexImpl) throws IOException {
    super();
    field = term.field();
    String text = term.text();
    this.regexImpl = regexImpl;
    regexImpl.compile(text);
    pre = regexImpl.prefix();
    if (pre == null) pre = "";
    setEnum(reader.terms(new Term(term.field(), pre)));
  }
  @Override
  protected final boolean termCompare(Term term) {
    if (field == term.field()) {
      String searchText = term.text();
      if (searchText.startsWith(pre)) {
        return regexImpl.match(searchText);
      }
    }
    endEnum = true;
    return false;
  }
  @Override
  public final float difference() {
    return 1.0f;
  }
  @Override
  public final boolean endEnum() {
    return endEnum;
  }
  @Override
  public void close() throws IOException {
    super.close();
    field = null;
  }
}
