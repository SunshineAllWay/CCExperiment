package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.search.Explanation.IDFExplanation;
public class TestOmitTf extends LuceneTestCase {
  public static class SimpleSimilarity extends Similarity {
    @Override public float lengthNorm(String field, int numTerms) { return 1.0f; }
    @Override public float queryNorm(float sumOfSquaredWeights) { return 1.0f; }
    @Override public float tf(float freq) { return freq; }
    @Override public float sloppyFreq(int distance) { return 2.0f; }
    @Override public float idf(int docFreq, int numDocs) { return 1.0f; }
    @Override public float coord(int overlap, int maxOverlap) { return 1.0f; }
    @Override public IDFExplanation idfExplain(Collection<Term> terms, Searcher searcher) throws IOException {
      return new IDFExplanation() {
        @Override
        public float getIdf() {
          return 1.0f;
        }
        @Override
        public String explain() {
          return "Inexplicable";
        }
      };
    }
  }
  public void testOmitTermFreqAndPositions() throws Exception {
    Directory ram = new MockRAMDirectory();
    Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(ram, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    Document d = new Document();
    Field f1 = new Field("f1", "This field has term freqs", Field.Store.NO, Field.Index.ANALYZED);
    d.add(f1);
    Field f2 = new Field("f2", "This field has NO Tf in all docs", Field.Store.NO, Field.Index.ANALYZED);
    f2.setOmitTermFreqAndPositions(true);
    d.add(f2);
    writer.addDocument(d);
    writer.optimize();
    d = new Document();
    f1.setOmitTermFreqAndPositions(true);
    d.add(f1);
    f2.setOmitTermFreqAndPositions(false);        
    d.add(f2);
    writer.addDocument(d);
    writer.optimize();
    writer.close();
    _TestUtil.checkIndex(ram);
    SegmentReader reader = SegmentReader.getOnlySegmentReader(ram);
    FieldInfos fi = reader.fieldInfos();
    assertTrue("OmitTermFreqAndPositions field bit should be set.", fi.fieldInfo("f1").omitTermFreqAndPositions);
    assertTrue("OmitTermFreqAndPositions field bit should be set.", fi.fieldInfo("f2").omitTermFreqAndPositions);
    reader.close();
    ram.close();
  }
  public void testMixedMerge() throws Exception {
    Directory ram = new MockRAMDirectory();
    Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(ram, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setMaxBufferedDocs(3));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(2);
    Document d = new Document();
    Field f1 = new Field("f1", "This field has term freqs", Field.Store.NO, Field.Index.ANALYZED);
    d.add(f1);
    Field f2 = new Field("f2", "This field has NO Tf in all docs", Field.Store.NO, Field.Index.ANALYZED);
    f2.setOmitTermFreqAndPositions(true);
    d.add(f2);
    for(int i=0;i<30;i++)
      writer.addDocument(d);
    d = new Document();
    f1.setOmitTermFreqAndPositions(true);
    d.add(f1);
    f2.setOmitTermFreqAndPositions(false);        
    d.add(f2);
    for(int i=0;i<30;i++)
      writer.addDocument(d);
    writer.optimize();
    writer.close();
    _TestUtil.checkIndex(ram);
    SegmentReader reader = SegmentReader.getOnlySegmentReader(ram);
    FieldInfos fi = reader.fieldInfos();
    assertTrue("OmitTermFreqAndPositions field bit should be set.", fi.fieldInfo("f1").omitTermFreqAndPositions);
    assertTrue("OmitTermFreqAndPositions field bit should be set.", fi.fieldInfo("f2").omitTermFreqAndPositions);
    reader.close();
    ram.close();
  }
  public void testMixedRAM() throws Exception {
    Directory ram = new MockRAMDirectory();
    Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(ram, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setMaxBufferedDocs(10));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(2);
    Document d = new Document();
    Field f1 = new Field("f1", "This field has term freqs", Field.Store.NO, Field.Index.ANALYZED);
    d.add(f1);
    Field f2 = new Field("f2", "This field has NO Tf in all docs", Field.Store.NO, Field.Index.ANALYZED);
    d.add(f2);
    for(int i=0;i<5;i++)
      writer.addDocument(d);
    f2.setOmitTermFreqAndPositions(true);
    for(int i=0;i<20;i++)
      writer.addDocument(d);
    writer.optimize();
    writer.close();
    _TestUtil.checkIndex(ram);
    SegmentReader reader = SegmentReader.getOnlySegmentReader(ram);
    FieldInfos fi = reader.fieldInfos();
    assertTrue("OmitTermFreqAndPositions field bit should not be set.", !fi.fieldInfo("f1").omitTermFreqAndPositions);
    assertTrue("OmitTermFreqAndPositions field bit should be set.", fi.fieldInfo("f2").omitTermFreqAndPositions);
    reader.close();
    ram.close();
  }
  private void assertNoPrx(Directory dir) throws Throwable {
    final String[] files = dir.listAll();
    for(int i=0;i<files.length;i++)
      assertFalse(files[i].endsWith(".prx"));
  }
  public void testNoPrxFile() throws Throwable {
    Directory ram = new MockRAMDirectory();
    Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(ram, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setMaxBufferedDocs(3));
    LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
    lmp.setMergeFactor(2);
    lmp.setUseCompoundFile(false);
    lmp.setUseCompoundDocStore(false);
    Document d = new Document();
    Field f1 = new Field("f1", "This field has term freqs", Field.Store.NO, Field.Index.ANALYZED);
    f1.setOmitTermFreqAndPositions(true);
    d.add(f1);
    for(int i=0;i<30;i++)
      writer.addDocument(d);
    writer.commit();
    assertNoPrx(ram);
    writer.optimize();
    writer.close();
    assertNoPrx(ram);
    _TestUtil.checkIndex(ram);
    ram.close();
  }
  public void testBasic() throws Exception {
    Directory dir = new MockRAMDirectory();  
    Analyzer analyzer = new StandardAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setMaxBufferedDocs(2)
        .setSimilarity(new SimpleSimilarity()));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(2);
    StringBuilder sb = new StringBuilder(265);
    String term = "term";
    for(int i = 0; i<30; i++){
      Document d = new Document();
      sb.append(term).append(" ");
      String content  = sb.toString();
      Field noTf = new Field("noTf", content + (i%2==0 ? "" : " notf"), Field.Store.NO, Field.Index.ANALYZED);
      noTf.setOmitTermFreqAndPositions(true);
      d.add(noTf);
      Field tf = new Field("tf", content + (i%2==0 ? " tf" : ""), Field.Store.NO, Field.Index.ANALYZED);
      d.add(tf);
      writer.addDocument(d);
    }
    writer.optimize();
    writer.close();
    _TestUtil.checkIndex(dir);
    Searcher searcher = new IndexSearcher(dir, true);
    searcher.setSimilarity(new SimpleSimilarity());
    Term a = new Term("noTf", term);
    Term b = new Term("tf", term);
    Term c = new Term("noTf", "notf");
    Term d = new Term("tf", "tf");
    TermQuery q1 = new TermQuery(a);
    TermQuery q2 = new TermQuery(b);
    TermQuery q3 = new TermQuery(c);
    TermQuery q4 = new TermQuery(d);
    searcher.search(q1,
                    new CountingHitCollector() {
                      private Scorer scorer;
                      @Override
                      public final void setScorer(Scorer scorer) {
                        this.scorer = scorer;
                      }
                      @Override
                      public final void collect(int doc) throws IOException {
                        float score = scorer.score();
                        assertTrue(score==1.0f);
                        super.collect(doc);
                      }
                    });
    searcher.search(q2,
                    new CountingHitCollector() {
                      private Scorer scorer;
                      @Override
                      public final void setScorer(Scorer scorer) {
                        this.scorer = scorer;
                      }
                      @Override
                      public final void collect(int doc) throws IOException {
                        float score = scorer.score();
                        assertTrue(score==1.0f+doc);
                        super.collect(doc);
                      }
                    });
    searcher.search(q3,
                    new CountingHitCollector() {
                      private Scorer scorer;
                      @Override
                      public final void setScorer(Scorer scorer) {
                        this.scorer = scorer;
                      }
                      @Override
                      public final void collect(int doc) throws IOException {
                        float score = scorer.score();
                        assertTrue(score==1.0f);
                        assertFalse(doc%2==0);
                        super.collect(doc);
                      }
                    });
    searcher.search(q4,
                    new CountingHitCollector() {
                      private Scorer scorer;
                      @Override
                      public final void setScorer(Scorer scorer) {
                        this.scorer = scorer;
                      }
                      @Override
                      public final void collect(int doc) throws IOException {
                        float score = scorer.score();
                        assertTrue(score==1.0f);
                        assertTrue(doc%2==0);
                        super.collect(doc);
                      }
                    });
    BooleanQuery bq = new BooleanQuery();
    bq.add(q1,Occur.MUST);
    bq.add(q4,Occur.MUST);
    searcher.search(bq,
                    new CountingHitCollector() {
                      @Override
                      public final void collect(int doc) throws IOException {
                        super.collect(doc);
                      }
                    });
    assertTrue(15 == CountingHitCollector.getCount());
    searcher.close();     
    dir.close();
  }
  public static class CountingHitCollector extends Collector {
    static int count=0;
    static int sum=0;
    private int docBase = -1;
    CountingHitCollector(){count=0;sum=0;}
    @Override
    public void setScorer(Scorer scorer) throws IOException {}
    @Override
    public void collect(int doc) throws IOException {
      count++;
      sum += doc + docBase;  
    }
    public static int getCount() { return count; }
    public static int getSum() { return sum; }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      this.docBase = docBase;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  }
}
