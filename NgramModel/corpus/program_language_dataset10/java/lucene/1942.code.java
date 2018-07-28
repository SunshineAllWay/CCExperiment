package org.apache.lucene.search;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
public class TestDocBoost extends LuceneTestCase {
  public TestDocBoost(String name) {
    super(name);
  }
  public void testDocBoost() throws Exception {
    RAMDirectory store = new RAMDirectory();
    IndexWriter writer = new IndexWriter(store, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    Fieldable f1 = new Field("field", "word", Field.Store.YES, Field.Index.ANALYZED);
    Fieldable f2 = new Field("field", "word", Field.Store.YES, Field.Index.ANALYZED);
    f2.setBoost(2.0f);
    Document d1 = new Document();
    Document d2 = new Document();
    Document d3 = new Document();
    Document d4 = new Document();
    d3.setBoost(3.0f);
    d4.setBoost(2.0f);
    d1.add(f1);                                 
    d2.add(f2);                                 
    d3.add(f1);                                 
    d4.add(f2);                                 
    writer.addDocument(d1);
    writer.addDocument(d2);
    writer.addDocument(d3);
    writer.addDocument(d4);
    writer.optimize();
    writer.close();
    final float[] scores = new float[4];
    new IndexSearcher(store, true).search
      (new TermQuery(new Term("field", "word")),
       new Collector() {
         private int base = 0;
         private Scorer scorer;
         @Override
         public void setScorer(Scorer scorer) throws IOException {
          this.scorer = scorer;
         }
         @Override
         public final void collect(int doc) throws IOException {
           scores[doc + base] = scorer.score();
         }
         @Override
         public void setNextReader(IndexReader reader, int docBase) {
           base = docBase;
         }
         @Override
         public boolean acceptsDocsOutOfOrder() {
           return true;
         }
       });
    float lastScore = 0.0f;
    for (int i = 0; i < 4; i++) {
      assertTrue(scores[i] > lastScore);
      lastScore = scores[i];
    }
  }
}
