package org.apache.lucene.search.spans;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
import java.util.Set;
public class SpanTermQuery extends SpanQuery {
  protected Term term;
  public SpanTermQuery(Term term) { this.term = term; }
  public Term getTerm() { return term; }
  @Override
  public String getField() { return term.field(); }
  @Override
  public void extractTerms(Set<Term> terms) {
	  terms.add(term);
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (term.field().equals(field))
      buffer.append(term.text());
    else
      buffer.append(term.toString());
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((term == null) ? 0 : term.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (getClass() != obj.getClass())
      return false;
    SpanTermQuery other = (SpanTermQuery) obj;
    if (term == null) {
      if (other.term != null)
        return false;
    } else if (!term.equals(other.term))
      return false;
    return true;
  }
  @Override
  public Spans getSpans(final IndexReader reader) throws IOException {
    return new TermSpans(reader.termPositions(term), term);
  }
}
