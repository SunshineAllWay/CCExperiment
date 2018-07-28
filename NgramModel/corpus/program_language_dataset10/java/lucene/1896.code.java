package org.apache.lucene.index;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestNorms extends LuceneTestCase {
  private class SimilarityOne extends DefaultSimilarity {
    @Override
    public float lengthNorm(String fieldName, int numTerms) {
      return 1;
    }
  }
  private static final int NUM_FIELDS = 10;
  private Similarity similarityOne;
  private Analyzer anlzr;
  private int numDocNorms;
  private ArrayList<Float> norms; 
  private ArrayList<Float> modifiedNorms; 
  private float lastNorm = 0;
  private float normDelta = (float) 0.001;
  public TestNorms(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    similarityOne = new SimilarityOne();
    anlzr = new StandardAnalyzer(TEST_VERSION_CURRENT);
  }
  public void testNorms() throws IOException {
    Directory dir1 = new RAMDirectory();
    norms = new ArrayList<Float>();
    modifiedNorms = new ArrayList<Float>();
    createIndex(dir1);
    doTestNorms(dir1);
    ArrayList<Float> norms1 = norms;
    ArrayList<Float> modifiedNorms1 = modifiedNorms;
    int numDocNorms1 = numDocNorms;
    norms = new ArrayList<Float>();
    modifiedNorms = new ArrayList<Float>();
    numDocNorms = 0;
    Directory dir2 = new RAMDirectory();
    createIndex(dir2);
    doTestNorms(dir2);
    Directory dir3 = new RAMDirectory();
    createIndex(dir3);
    IndexWriter iw = new IndexWriter(dir3, new IndexWriterConfig(
        TEST_VERSION_CURRENT, anlzr).setOpenMode(OpenMode.APPEND)
        .setMaxBufferedDocs(5));
    ((LogMergePolicy) iw.getMergePolicy()).setMergeFactor(3);
    iw.addIndexesNoOptimize(new Directory[]{dir1,dir2});
    iw.optimize();
    iw.close();
    norms1.addAll(norms);
    norms = norms1;
    modifiedNorms1.addAll(modifiedNorms);
    modifiedNorms = modifiedNorms1;
    numDocNorms += numDocNorms1;
    verifyIndex(dir3);
    doTestNorms(dir3);
    iw = new IndexWriter(dir3, new IndexWriterConfig(TEST_VERSION_CURRENT,
        anlzr).setOpenMode(OpenMode.APPEND).setMaxBufferedDocs(5));
    ((LogMergePolicy) iw.getMergePolicy()).setMergeFactor(3);
    iw.optimize();
    iw.close();
    verifyIndex(dir3);
    dir1.close();
    dir2.close();
    dir3.close();
  }
  private void doTestNorms(Directory dir) throws IOException {
    for (int i=0; i<5; i++) {
      addDocs(dir,12,true);
      verifyIndex(dir);
      modifyNormsForF1(dir);
      verifyIndex(dir);
      addDocs(dir,12,false);
      verifyIndex(dir);
      modifyNormsForF1(dir);
      verifyIndex(dir);
    }
  }
  private void createIndex(Directory dir) throws IOException {
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, anlzr).setOpenMode(OpenMode.CREATE)
        .setMaxBufferedDocs(5).setSimilarity(similarityOne));
    LogMergePolicy lmp = (LogMergePolicy) iw.getMergePolicy();
    lmp.setMergeFactor(3);
    lmp.setUseCompoundFile(true);
    lmp.setUseCompoundDocStore(true);
    iw.close();
  }
  private void modifyNormsForF1(Directory dir) throws IOException {
    IndexReader ir = IndexReader.open(dir, false);
    int n = ir.maxDoc();
    for (int i = 0; i < n; i+=3) { 
      int k = (i*3) % modifiedNorms.size();
      float origNorm = modifiedNorms.get(i).floatValue();
      float newNorm = modifiedNorms.get(k).floatValue();
      modifiedNorms.set(i, Float.valueOf(newNorm));
      modifiedNorms.set(k, Float.valueOf(origNorm));
      ir.setNorm(i, "f"+1, newNorm); 
      ir.setNorm(k, "f"+1, origNorm); 
    }
    ir.close();
  }
  private void verifyIndex(Directory dir) throws IOException {
    IndexReader ir = IndexReader.open(dir, false);
    for (int i = 0; i < NUM_FIELDS; i++) {
      String field = "f"+i;
      byte b[] = ir.norms(field);
      assertEquals("number of norms mismatches",numDocNorms,b.length);
      ArrayList<Float> storedNorms = (i==1 ? modifiedNorms : norms);
      for (int j = 0; j < b.length; j++) {
        float norm = similarityOne.decodeNormValue(b[j]);
        float norm1 = storedNorms.get(j).floatValue();
        assertEquals("stored norm value of "+field+" for doc "+j+" is "+norm+" - a mismatch!", norm, norm1, 0.000001);
      }
    }
  }
  private void addDocs(Directory dir, int ndocs, boolean compound) throws IOException {
    IndexWriter iw = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, anlzr).setOpenMode(OpenMode.APPEND)
        .setMaxBufferedDocs(5).setSimilarity(similarityOne));
    LogMergePolicy lmp = (LogMergePolicy) iw.getMergePolicy();
    lmp.setMergeFactor(3);
    lmp.setUseCompoundFile(compound);
    lmp.setUseCompoundDocStore(compound);
    for (int i = 0; i < ndocs; i++) {
      iw.addDocument(newDoc());
    }
    iw.close();
  }
  private Document newDoc() {
    Document d = new Document();
    float boost = nextNorm();
    for (int i = 0; i < 10; i++) {
      Field f = new Field("f"+i,"v"+i,Store.NO,Index.NOT_ANALYZED);
      f.setBoost(boost);
      d.add(f);
    }
    return d;
  }
  private float nextNorm() {
    float norm = lastNorm + normDelta;
    do {
      float norm1 = similarityOne.decodeNormValue(similarityOne.encodeNormValue(norm));
      if (norm1 > lastNorm) {
        norm = norm1;
        break;
      }
      norm += normDelta;
    } while (true);
    norms.add(numDocNorms, Float.valueOf(norm));
    modifiedNorms.add(numDocNorms, Float.valueOf(norm));
    numDocNorms ++;
    lastNorm = (norm>10 ? 0 : norm); 
    return norm;
  }
}
