package org.apache.lucene.search;
import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestBooleanScorer extends LuceneTestCase
{
  public TestBooleanScorer(String name) {
    super(name);
  }
  private static final String FIELD = "category";
  public void testMethod() {
    RAMDirectory directory = new RAMDirectory();
    String[] values = new String[] { "1", "2", "3", "4" };
    try {
      IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      for (int i = 0; i < values.length; i++) {
        Document doc = new Document();
        doc.add(new Field(FIELD, values[i], Field.Store.YES, Field.Index.NOT_ANALYZED));
        writer.addDocument(doc);
      }
      writer.close();
      BooleanQuery booleanQuery1 = new BooleanQuery();
      booleanQuery1.add(new TermQuery(new Term(FIELD, "1")), BooleanClause.Occur.SHOULD);
      booleanQuery1.add(new TermQuery(new Term(FIELD, "2")), BooleanClause.Occur.SHOULD);
      BooleanQuery query = new BooleanQuery();
      query.add(booleanQuery1, BooleanClause.Occur.MUST);
      query.add(new TermQuery(new Term(FIELD, "9")), BooleanClause.Occur.MUST_NOT);
      IndexSearcher indexSearcher = new IndexSearcher(directory, true);
      ScoreDoc[] hits = indexSearcher.search(query, null, 1000).scoreDocs;
      assertEquals("Number of matched documents", 2, hits.length);
    }
    catch (IOException e) {
      fail(e.getMessage());
    }
  }
  public void testEmptyBucketWithMoreDocs() throws Exception {
    Similarity sim = Similarity.getDefault();
    Scorer[] scorers = new Scorer[] {new Scorer(sim) {
      private int doc = -1;
      @Override public float score() throws IOException { return 0; }
      @Override public int docID() { return doc; }
      @Override public int nextDoc() throws IOException {
        return doc = doc == -1 ? 3000 : NO_MORE_DOCS;
      }
      @Override public int advance(int target) throws IOException {
        return doc = target <= 3000 ? 3000 : NO_MORE_DOCS;
      }
    }};
    BooleanScorer bs = new BooleanScorer(sim, 1, Arrays.asList(scorers), null);
    assertEquals("should have received 3000", 3000, bs.nextDoc());
    assertEquals("should have received NO_MORE_DOCS", DocIdSetIterator.NO_MORE_DOCS, bs.nextDoc());
  }
}
