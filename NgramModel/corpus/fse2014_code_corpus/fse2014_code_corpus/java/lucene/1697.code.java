package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.FieldSelector;
public abstract class Searcher implements Searchable {
  public TopFieldDocs search(Query query, Filter filter, int n,
                             Sort sort) throws IOException {
    return search(createWeight(query), filter, n, sort);
  }
 public void search(Query query, Collector results)
   throws IOException {
   search(createWeight(query), null, results);
 }
  public void search(Query query, Filter filter, Collector results)
  throws IOException {
    search(createWeight(query), filter, results);
  }
  public TopDocs search(Query query, Filter filter, int n)
    throws IOException {
    return search(createWeight(query), filter, n);
  }
  public TopDocs search(Query query, int n)
    throws IOException {
    return search(query, null, n);
  }
  public Explanation explain(Query query, int doc) throws IOException {
    return explain(createWeight(query), doc);
  }
  private Similarity similarity = Similarity.getDefault();
  public void setSimilarity(Similarity similarity) {
    this.similarity = similarity;
  }
  public Similarity getSimilarity() {
    return this.similarity;
  }
  protected Weight createWeight(Query query) throws IOException {
    return query.weight(this);
  }
  public int[] docFreqs(Term[] terms) throws IOException {
    int[] result = new int[terms.length];
    for (int i = 0; i < terms.length; i++) {
      result[i] = docFreq(terms[i]);
    }
    return result;
  }
  abstract public void search(Weight weight, Filter filter, Collector results) throws IOException;
  abstract public void close() throws IOException;
  abstract public int docFreq(Term term) throws IOException;
  abstract public int maxDoc() throws IOException;
  abstract public TopDocs search(Weight weight, Filter filter, int n) throws IOException;
  abstract public Document doc(int i) throws CorruptIndexException, IOException;
  abstract public Document doc(int docid, FieldSelector fieldSelector) throws CorruptIndexException, IOException;
  abstract public Query rewrite(Query query) throws IOException;
  abstract public Explanation explain(Weight weight, int doc) throws IOException;
  abstract public TopFieldDocs search(Weight weight, Filter filter, int n, Sort sort) throws IOException;
}
