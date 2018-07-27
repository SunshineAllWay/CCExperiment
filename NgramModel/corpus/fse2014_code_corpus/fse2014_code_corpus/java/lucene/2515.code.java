package org.apache.solr.search;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.WildcardTermEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.OpenBitSet;
import java.io.IOException;
public class WildcardFilter extends Filter {
  protected final Term term;
  public WildcardFilter(Term wildcardTerm) {
    this.term = wildcardTerm;
  }
  public Term getTerm() { return term; }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    final OpenBitSet bitSet = new OpenBitSet(reader.maxDoc());
    new WildcardGenerator(term) {
      public void handleDoc(int doc) {
        bitSet.set(doc);
      }
    }.generate(reader);
    return bitSet;
  }
  @Override
  public boolean equals(Object o) {
    return o instanceof WildcardFilter && ((WildcardFilter)o).term.equals(this.term);
  }
  @Override  
  public int hashCode() {
    return term.hashCode();
  }
  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder();
    sb.append("WildcardFilter(");
    sb.append(term.toString());
    sb.append(")");
    return sb.toString();
  }
}
abstract class WildcardGenerator implements IdGenerator {
  protected final Term wildcard;
  WildcardGenerator(Term wildcard) {
    this.wildcard = wildcard;
  }
  public void generate(IndexReader reader) throws IOException {
    TermEnum enumerator = new WildcardTermEnum(reader, wildcard);
    TermDocs termDocs = reader.termDocs();
    try {
      do {
        Term term = enumerator.term();
        if (term==null) break;
        termDocs.seek(term);
        while (termDocs.next()) {
          handleDoc(termDocs.doc());
        }
      } while (enumerator.next());
    } finally {
      termDocs.close();
      enumerator.close();
    }
  }
}
