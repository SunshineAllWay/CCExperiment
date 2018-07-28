package org.apache.lucene.search;
import org.apache.lucene.index.Term;
public class PrefixFilter extends MultiTermQueryWrapperFilter<PrefixQuery> {
  public PrefixFilter(Term prefix) {
    super(new PrefixQuery(prefix));
  }
  public Term getPrefix() { return query.getPrefix(); }
  @Override
  public String toString () {
    StringBuilder buffer = new StringBuilder();
    buffer.append("PrefixFilter(");
    buffer.append(getPrefix().toString());
    buffer.append(")");
    return buffer.toString();
  }
}
