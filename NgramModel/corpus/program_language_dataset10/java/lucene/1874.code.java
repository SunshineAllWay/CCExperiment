package org.apache.lucene.index;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
public class TestDocumentWriter extends LuceneTestCase {
  private RAMDirectory dir;
  public TestDocumentWriter(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = new RAMDirectory();
  }
  public void test() {
    assertTrue(dir != null);
  }
  public void testAddDocument() throws Exception {
    Document testDoc = new Document();
    DocHelper.setupDoc(testDoc);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    writer.addDocument(testDoc);
    writer.commit();
    SegmentInfo info = writer.newestSegment();
    writer.close();
    SegmentReader reader = SegmentReader.get(true, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    assertTrue(reader != null);
    Document doc = reader.document(0);
    assertTrue(doc != null);
    Fieldable [] fields = doc.getFields("textField2");
    assertTrue(fields != null && fields.length == 1);
    assertTrue(fields[0].stringValue().equals(DocHelper.FIELD_2_TEXT));
    assertTrue(fields[0].isTermVectorStored());
    fields = doc.getFields("textField1");
    assertTrue(fields != null && fields.length == 1);
    assertTrue(fields[0].stringValue().equals(DocHelper.FIELD_1_TEXT));
    assertFalse(fields[0].isTermVectorStored());
    fields = doc.getFields("keyField");
    assertTrue(fields != null && fields.length == 1);
    assertTrue(fields[0].stringValue().equals(DocHelper.KEYWORD_TEXT));
    fields = doc.getFields(DocHelper.NO_NORMS_KEY);
    assertTrue(fields != null && fields.length == 1);
    assertTrue(fields[0].stringValue().equals(DocHelper.NO_NORMS_TEXT));
    fields = doc.getFields(DocHelper.TEXT_FIELD_3_KEY);
    assertTrue(fields != null && fields.length == 1);
    assertTrue(fields[0].stringValue().equals(DocHelper.FIELD_3_TEXT));
    for (int i = 0; i < reader.core.fieldInfos.size(); i++) {
      FieldInfo fi = reader.core.fieldInfos.fieldInfo(i);
      if (fi.isIndexed) {
        assertTrue(fi.omitNorms == !reader.hasNorms(fi.name));
      }
    }
  }
  public void testPositionIncrementGap() throws IOException {
    Analyzer analyzer = new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
      }
      @Override
      public int getPositionIncrementGap(String fieldName) {
        return 500;
      }
    };
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("repeated", "repeated one", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("repeated", "repeated two", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    SegmentInfo info = writer.newestSegment();
    writer.close();
    SegmentReader reader = SegmentReader.get(true, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    TermPositions termPositions = reader.termPositions(new Term("repeated", "repeated"));
    assertTrue(termPositions.next());
    int freq = termPositions.freq();
    assertEquals(2, freq);
    assertEquals(0, termPositions.nextPosition());
    assertEquals(502, termPositions.nextPosition());
  }
  public void testTokenReuse() throws IOException {
    Analyzer analyzer = new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new TokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader)) {
          boolean first=true;
          AttributeSource.State state;
          @Override
          public boolean incrementToken() throws IOException {
            if (state != null) {
              restoreState(state);
              payloadAtt.setPayload(null);
              posIncrAtt.setPositionIncrement(0);
              termAtt.setTermBuffer(new char[]{'b'}, 0, 1);
              state = null;
              return true;
            }
            boolean hasNext = input.incrementToken();
            if (!hasNext) return false;
            if (Character.isDigit(termAtt.termBuffer()[0])) {
              posIncrAtt.setPositionIncrement(termAtt.termBuffer()[0] - '0');
            }
            if (first) {
              payloadAtt.setPayload(new Payload(new byte[]{100}));
              first = false;
            }
            state = captureState();
            return true;
          }
          TermAttribute termAtt = addAttribute(TermAttribute.class);
          PayloadAttribute payloadAtt = addAttribute(PayloadAttribute.class);
          PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);          
        };
      }
    };
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    Document doc = new Document();
    doc.add(new Field("f1", "a 5 a a", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    SegmentInfo info = writer.newestSegment();
    writer.close();
    SegmentReader reader = SegmentReader.get(true, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    TermPositions termPositions = reader.termPositions(new Term("f1", "a"));
    assertTrue(termPositions.next());
    int freq = termPositions.freq();
    assertEquals(3, freq);
    assertEquals(0, termPositions.nextPosition());
    assertEquals(true, termPositions.isPayloadAvailable());
    assertEquals(6, termPositions.nextPosition());
    assertEquals(false, termPositions.isPayloadAvailable());
    assertEquals(7, termPositions.nextPosition());
    assertEquals(false, termPositions.isPayloadAvailable());
  }
  public void testPreAnalyzedField() throws IOException {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(
        TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("preanalyzed", new TokenStream() {
      private String[] tokens = new String[] {"term1", "term2", "term3", "term2"};
      private int index = 0;
      private TermAttribute termAtt = addAttribute(TermAttribute.class);
      @Override
      public boolean incrementToken() throws IOException {
        if (index == tokens.length) {
          return false;
        } else {
          clearAttributes();
          termAtt.setTermBuffer(tokens[index++]);
          return true;
        }        
      }
    }, TermVector.NO));
    writer.addDocument(doc);
    writer.commit();
    SegmentInfo info = writer.newestSegment();
    writer.close();
    SegmentReader reader = SegmentReader.get(true, info, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    TermPositions termPositions = reader.termPositions(new Term("preanalyzed", "term1"));
    assertTrue(termPositions.next());
    assertEquals(1, termPositions.freq());
    assertEquals(0, termPositions.nextPosition());
    termPositions.seek(new Term("preanalyzed", "term2"));
    assertTrue(termPositions.next());
    assertEquals(2, termPositions.freq());
    assertEquals(1, termPositions.nextPosition());
    assertEquals(3, termPositions.nextPosition());
    termPositions.seek(new Term("preanalyzed", "term3"));
    assertTrue(termPositions.next());
    assertEquals(1, termPositions.freq());
    assertEquals(2, termPositions.nextPosition());
  }
  public void testMixedTermVectorSettingsSameField() throws Exception {
    Document doc = new Document();
    doc.add(new Field("f1", "v1", Store.YES, Index.NOT_ANALYZED, TermVector.NO));
    doc.add(new Field("f1", "v2", Store.YES, Index.NOT_ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field("f2", "v1", Store.YES, Index.NOT_ANALYZED, TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field("f2", "v2", Store.YES, Index.NOT_ANALYZED, TermVector.NO));
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(
        TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.close();
    _TestUtil.checkIndex(dir);
    IndexReader reader = IndexReader.open(dir, true);
    TermFreqVector tfv1 = reader.getTermFreqVector(0, "f1");
    assertNotNull(tfv1);
    assertEquals("the 'with_tv' setting should rule!",2,tfv1.getTerms().length);
    TermFreqVector tfv2 = reader.getTermFreqVector(0, "f2");
    assertNotNull(tfv2);
    assertEquals("the 'with_tv' setting should rule!",2,tfv2.getTerms().length);
  }
  public void testLUCENE_1590() throws Exception {
    Document doc = new Document();
    doc.add(new Field("f1", "v1", Store.NO, Index.ANALYZED_NO_NORMS));
    doc.add(new Field("f1", "v2", Store.YES, Index.NO));
    Field f = new Field("f2", "v1", Store.NO, Index.ANALYZED);
    f.setOmitTermFreqAndPositions(true);
    doc.add(f);
    doc.add(new Field("f2", "v2", Store.YES, Index.NO));
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(
        TEST_VERSION_CURRENT)));
    writer.addDocument(doc);
    writer.optimize(); 
    writer.close();
    _TestUtil.checkIndex(dir);
    SegmentReader reader = SegmentReader.getOnlySegmentReader(dir);
    FieldInfos fi = reader.fieldInfos();
    assertFalse("f1 should have no norms", reader.hasNorms("f1"));
    assertFalse("omitTermFreqAndPositions field bit should not be set for f1", fi.fieldInfo("f1").omitTermFreqAndPositions);
    assertTrue("f2 should have norms", reader.hasNorms("f2"));
    assertTrue("omitTermFreqAndPositions field bit should be set for f2", fi.fieldInfo("f2").omitTermFreqAndPositions);
  }
}
