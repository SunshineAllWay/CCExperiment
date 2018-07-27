package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.RAMDirectory;
public class TestSegmentReader extends LuceneTestCase {
  private RAMDirectory dir = new RAMDirectory();
  private Document testDoc = new Document();
  private SegmentReader reader = null;
  public TestSegmentReader(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DocHelper.setupDoc(testDoc);
    SegmentInfo info = DocHelper.writeDoc(dir, testDoc);
    reader = SegmentReader.get(true, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
  }
  public void test() {
    assertTrue(dir != null);
    assertTrue(reader != null);
    assertTrue(DocHelper.nameValues.size() > 0);
    assertTrue(DocHelper.numFields(testDoc) == DocHelper.all.size());
  }
  public void testDocument() throws IOException {
    assertTrue(reader.numDocs() == 1);
    assertTrue(reader.maxDoc() >= 1);
    Document result = reader.document(0);
    assertTrue(result != null);
    assertTrue(DocHelper.numFields(result) == DocHelper.numFields(testDoc) - DocHelper.unstored.size());
    List<Fieldable> fields = result.getFields();
    for (final Fieldable field : fields ) { 
      assertTrue(field != null);
      assertTrue(DocHelper.nameValues.containsKey(field.name()));
    }
  }
  public void testDelete() throws IOException {
    Document docToDelete = new Document();
    DocHelper.setupDoc(docToDelete);
    SegmentInfo info = DocHelper.writeDoc(dir, docToDelete);
    SegmentReader deleteReader = SegmentReader.get(false, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    assertTrue(deleteReader != null);
    assertTrue(deleteReader.numDocs() == 1);
    deleteReader.deleteDocument(0);
    assertTrue(deleteReader.isDeleted(0) == true);
    assertTrue(deleteReader.hasDeletions() == true);
    assertTrue(deleteReader.numDocs() == 0);
  }    
  public void testGetFieldNameVariations() {
    Collection<String> result = reader.getFieldNames(IndexReader.FieldOption.ALL);
    assertTrue(result != null);
    assertTrue(result.size() == DocHelper.all.size());
    for (Iterator<String> iter = result.iterator(); iter.hasNext();) {
      String s =  iter.next();
      assertTrue(DocHelper.nameValues.containsKey(s) == true || s.equals(""));
    }                                                                               
    result = reader.getFieldNames(IndexReader.FieldOption.INDEXED);
    assertTrue(result != null);
    assertTrue(result.size() == DocHelper.indexed.size());
    for (Iterator<String> iter = result.iterator(); iter.hasNext();) {
      String s = iter.next();
      assertTrue(DocHelper.indexed.containsKey(s) == true || s.equals(""));
    }
    result = reader.getFieldNames(IndexReader.FieldOption.UNINDEXED);
    assertTrue(result != null);
    assertTrue(result.size() == DocHelper.unindexed.size());
    result = reader.getFieldNames(IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR);
    assertTrue(result != null);
    assertTrue(result.size() == DocHelper.termvector.size());
    result = reader.getFieldNames(IndexReader.FieldOption.INDEXED_NO_TERMVECTOR);
    assertTrue(result != null);
    assertTrue(result.size() == DocHelper.notermvector.size());
  } 
  public void testTerms() throws IOException {
    TermEnum terms = reader.terms();
    assertTrue(terms != null);
    while (terms.next() == true)
    {
      Term term = terms.term();
      assertTrue(term != null);
      String fieldValue = (String)DocHelper.nameValues.get(term.field());
      assertTrue(fieldValue.indexOf(term.text()) != -1);
    }
    TermDocs termDocs = reader.termDocs();
    assertTrue(termDocs != null);
    termDocs.seek(new Term(DocHelper.TEXT_FIELD_1_KEY, "field"));
    assertTrue(termDocs.next() == true);
    termDocs.seek(new Term(DocHelper.NO_NORMS_KEY,  DocHelper.NO_NORMS_TEXT));
    assertTrue(termDocs.next() == true);
    TermPositions positions = reader.termPositions();
    assertTrue(positions != null);
    positions.seek(new Term(DocHelper.TEXT_FIELD_1_KEY, "field"));
    assertTrue(positions.doc() == 0);
    assertTrue(positions.nextPosition() >= 0);
  }    
  public void testNorms() throws IOException {
    checkNorms(reader);
  }
  public static void checkNorms(IndexReader reader) throws IOException {
    for (int i=0; i<DocHelper.fields.length; i++) {
      Fieldable f = DocHelper.fields[i];
      if (f.isIndexed()) {
        assertEquals(reader.hasNorms(f.name()), !f.getOmitNorms());
        assertEquals(reader.hasNorms(f.name()), !DocHelper.noNorms.containsKey(f.name()));
        if (!reader.hasNorms(f.name())) {
          byte [] norms = reader.norms(f.name());
          byte norm1 = Similarity.getDefault().encodeNormValue(1.0f);
          assertNull(norms);
          norms = new byte[reader.maxDoc()];
          reader.norms(f.name(),norms, 0);
          for (int j=0; j<reader.maxDoc(); j++) {
            assertEquals(norms[j], norm1);
          }
        }
      }
    }
  }
  public void testTermVectors() throws IOException {
    TermFreqVector result = reader.getTermFreqVector(0, DocHelper.TEXT_FIELD_2_KEY);
    assertTrue(result != null);
    String [] terms = result.getTerms();
    int [] freqs = result.getTermFrequencies();
    assertTrue(terms != null && terms.length == 3 && freqs != null && freqs.length == 3);
    for (int i = 0; i < terms.length; i++) {
      String term = terms[i];
      int freq = freqs[i];
      assertTrue(DocHelper.FIELD_2_TEXT.indexOf(term) != -1);
      assertTrue(freq > 0);
    }
    TermFreqVector [] results = reader.getTermFreqVectors(0);
    assertTrue(results != null);
    assertTrue("We do not have 3 term freq vectors, we have: " + results.length, results.length == 3);      
  }    
}
