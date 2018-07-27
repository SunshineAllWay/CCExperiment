package org.apache.lucene.search.regex;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.FilteredTermEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ToStringUtils;
import java.io.IOException;
public class RegexQuery extends MultiTermQuery implements RegexQueryCapable {
  private RegexCapabilities regexImpl = new JavaUtilRegexCapabilities();
  private Term term;
  public RegexQuery(Term term) {
    this.term = term;
  }
  public Term getTerm() { return term; }
  public void setRegexImplementation(RegexCapabilities impl) {
    this.regexImpl = impl;
  }
  public RegexCapabilities getRegexImplementation() {
    return regexImpl;
  }
  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new RegexTermEnum(reader, term, regexImpl);
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final RegexQuery that = (RegexQuery) o;
    return regexImpl.equals(that.regexImpl);
  }
  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 29 * result + regexImpl.hashCode();
    return result;
  }
}
