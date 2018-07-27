package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import java.util.Map;
import java.io.IOException;
public class LiteralValueSource extends ValueSource {
  protected final String string;
  public LiteralValueSource(String string) {
    this.string = string;
  }
  public String getValue() {
    return string;
  }
  @Override
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    return new DocValues() {
      @Override
      public String strVal(int doc) {
        return string;
      }
      @Override
      public String toString(int doc) {
        return string;
      }
    };
  }
  @Override
  public String description() {
    return "literal(" + string + ")";
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof LiteralValueSource)) return false;
    LiteralValueSource that = (LiteralValueSource) o;
    if (!string.equals(that.string)) return false;
    return true;
  }
  public static final int hash = LiteralValueSource.class.hashCode();
  @Override
  public int hashCode() {
    return hash + string.hashCode();
  }
}
