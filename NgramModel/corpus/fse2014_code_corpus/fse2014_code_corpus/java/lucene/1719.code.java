package org.apache.lucene.search;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
public class WildcardQuery extends MultiTermQuery {
  private boolean termContainsWildcard;
  private boolean termIsPrefix;
  protected Term term;
  public WildcardQuery(Term term) {
    this.term = term;
    String text = term.text();
    this.termContainsWildcard = (text.indexOf('*') != -1)
        || (text.indexOf('?') != -1);
    this.termIsPrefix = termContainsWildcard 
        && (text.indexOf('?') == -1) 
        && (text.indexOf('*') == text.length() - 1);
  }
  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    if (termContainsWildcard)
      return new WildcardTermEnum(reader, getTerm());
    else
      return new SingleTermEnum(reader, getTerm());
  }
  public Term getTerm() {
    return term;
  }
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    if (termIsPrefix) {
      MultiTermQuery rewritten = new PrefixQuery(term.createTerm(term.text()
          .substring(0, term.text().indexOf('*'))));
      rewritten.setBoost(getBoost());
      rewritten.setRewriteMethod(getRewriteMethod());
      return rewritten;
    } else {
      return super.rewrite(reader);
    }
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!term.field().equals(field)) {
      buffer.append(term.field());
      buffer.append(":");
    }
    buffer.append(term.text());
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
    WildcardQuery other = (WildcardQuery) obj;
    if (term == null) {
      if (other.term != null)
        return false;
    } else if (!term.equals(other.term))
      return false;
    return true;
  }
}
