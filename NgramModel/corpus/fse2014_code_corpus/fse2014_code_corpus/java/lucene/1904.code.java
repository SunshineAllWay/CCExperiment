package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import java.io.IOException;
import java.util.Collection;
public class TestSegmentMerger extends LuceneTestCase {
  private Directory mergedDir = new RAMDirectory();
  private String mergedSegment = "test";
  private Directory merge1Dir = new RAMDirectory();
  private Document doc1 = new Document();
  private SegmentReader reader1 = null;
  private Directory merge2Dir = new RAMDirectory();
  private Document doc2 = new Document();
  private SegmentReader reader2 = null;
  public TestSegmentMerger(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DocHelper.setupDoc(doc1);
    SegmentInfo info1 = DocHelper.writeDoc(merge1Dir, doc1);
    DocHelper.setupDoc(doc2);
    SegmentInfo info2 = DocHelper.writeDoc(merge2Dir, doc2);
    reader1 = SegmentReader.get(true, info1, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    reader2 = SegmentReader.get(true, info2, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
  }
  public void test() {
    assertTrue(mergedDir != null);
    assertTrue(merge1Dir != null);
    assertTrue(merge2Dir != null);
    assertTrue(reader1 != null);
    assertTrue(reader2 != null);
  }
  public void testMerge() throws IOException {                             
    SegmentMerger merger = new SegmentMerger(mergedDir, mergedSegment);
    merger.add(reader1);
    merger.add(reader2);
    int docsMerged = merger.merge();
    merger.closeReaders();
    assertTrue(docsMerged == 2);
    SegmentReader mergedReader = SegmentReader.get(true, new SegmentInfo(mergedSegment, docsMerged, mergedDir, false, true), IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    assertTrue(mergedReader != null);
    assertTrue(mergedReader.numDocs() == 2);
    Document newDoc1 = mergedReader.document(0);
    assertTrue(newDoc1 != null);
    assertTrue(DocHelper.numFields(newDoc1) == DocHelper.numFields(doc1) - DocHelper.unstored.size());
    Document newDoc2 = mergedReader.document(1);
    assertTrue(newDoc2 != null);
    assertTrue(DocHelper.numFields(newDoc2) == DocHelper.numFields(doc2) - DocHelper.unstored.size());
    TermDocs termDocs = mergedReader.termDocs(new Term(DocHelper.TEXT_FIELD_2_KEY, "field"));
    assertTrue(termDocs != null);
    assertTrue(termDocs.next() == true);
    Collection<String> stored = mergedReader.getFieldNames(IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR);
    assertTrue(stored != null);
    assertTrue("We do not have 3 fields that were indexed with term vector",stored.size() == 3);
    TermFreqVector vector = mergedReader.getTermFreqVector(0, DocHelper.TEXT_FIELD_2_KEY);
    assertTrue(vector != null);
    String [] terms = vector.getTerms();
    assertTrue(terms != null);
    assertTrue(terms.length == 3);
    int [] freqs = vector.getTermFrequencies();
    assertTrue(freqs != null);
    assertTrue(vector instanceof TermPositionVector == true);
    for (int i = 0; i < terms.length; i++) {
      String term = terms[i];
      int freq = freqs[i];
      assertTrue(DocHelper.FIELD_2_TEXT.indexOf(term) != -1);
      assertTrue(DocHelper.FIELD_2_FREQS[i] == freq);
    }
    TestSegmentReader.checkNorms(mergedReader);
  }    
}
