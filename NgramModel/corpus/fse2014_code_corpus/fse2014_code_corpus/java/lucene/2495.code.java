package org.apache.solr.search;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.OpenBitSet;
import java.io.IOException;
public class PrefixFilter extends Filter {
  protected final Term prefix;
  PrefixFilter(Term prefix) {
    this.prefix = prefix;
  }
  Term getPrefix() { return prefix; }
 @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
    new PrefixGenerator(prefix) {
      public void handleDoc(int doc) {
        bitSet.set(doc);
      }
    }.generate(reader);
    return bitSet;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof PrefixFilter && ((PrefixFilter)o).prefix.equals(this.prefix);
  }
  @Override
  public int hashCode() {
    return 0xcecf7fe2 + prefix.hashCode();
  }
  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder();
    sb.append("PrefixFilter(");
    sb.append(prefix.toString());
    sb.append(")");
    return sb.toString();
  }
}
interface IdGenerator {
  public void generate(IndexReader reader) throws IOException;
  public void handleDoc(int doc);
}
abstract class PrefixGenerator implements IdGenerator {
  protected final Term prefix;
  PrefixGenerator(Term prefix) {
    this.prefix = prefix;
  }
  public void generate(IndexReader reader) throws IOException {
    TermEnum enumerator = reader.terms(prefix);
    TermDocs termDocs = reader.termDocs();
    try {
      String prefixText = prefix.text();
      String prefixField = prefix.field();
      do {
        Term term = enumerator.term();
        if (term != null &&
            term.text().startsWith(prefixText) &&
            term.field() == prefixField)
        {
          termDocs.seek(term);
          while (termDocs.next()) {
            handleDoc(termDocs.doc());
          }
        } else {
          break;
        }
      } while (enumerator.next());
    } finally {
      termDocs.close();
      enumerator.close();
    }
  }
}
