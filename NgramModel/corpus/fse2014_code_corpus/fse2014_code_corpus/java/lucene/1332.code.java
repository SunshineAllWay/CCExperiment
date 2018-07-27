package org.apache.lucene.queryParser.surround.query;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
public class SrndTermQuery extends SimpleTerm {
  public SrndTermQuery(String termText, boolean quoted) {
    super(quoted);
    this.termText = termText;
  }
  private final String termText;
  public String getTermText() {return termText;}
  public Term getLuceneTerm(String fieldName) {
    return new Term(fieldName, getTermText());
  }
  @Override
  public String toStringUnquoted() {return getTermText();}
  @Override
  public void visitMatchingTerms(
    IndexReader reader,
    String fieldName,
    MatchingTermVisitor mtv) throws IOException
  {
    TermEnum enumerator = reader.terms(getLuceneTerm(fieldName));
    try {
      Term it= enumerator.term(); 
      if ((it != null)
          && it.text().equals(getTermText())
          && it.field().equals(fieldName)) {
        mtv.visitMatchingTerm(it);
      }
    } finally {
      enumerator.close();
    }
  }
}
