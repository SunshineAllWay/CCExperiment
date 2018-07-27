package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.ToStringUtils;
public class PrefixQuery extends MultiTermQuery {
  private Term prefix;
  public PrefixQuery(Term prefix) {
    this.prefix = prefix;
  }
  public Term getPrefix() { return prefix; }
  @Override
  protected FilteredTermEnum getEnum(IndexReader reader) throws IOException {
    return new PrefixTermEnum(reader, prefix);
  }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    if (!prefix.field().equals(field)) {
      buffer.append(prefix.field());
      buffer.append(":");
    }
    buffer.append(prefix.text());
    buffer.append('*');
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
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
    PrefixQuery other = (PrefixQuery) obj;
    if (prefix == null) {
      if (other.prefix != null)
        return false;
    } else if (!prefix.equals(other.prefix))
      return false;
    return true;
  }
}
