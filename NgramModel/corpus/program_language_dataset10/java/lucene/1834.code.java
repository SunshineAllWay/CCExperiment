package org.apache.lucene.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
public class TestCachingTokenFilter extends BaseTokenStreamTestCase {
  private String[] tokens = new String[] {"term1", "term2", "term3", "term2"};
  public void testCaching() throws IOException {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    TokenStream stream = new TokenStream() {
      private int index = 0;
      private TermAttribute termAtt = addAttribute(TermAttribute.class);
      private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
      @Override
      public boolean incrementToken() throws IOException {
        if (index == tokens.length) {
          return false;
        } else {
          clearAttributes();
          termAtt.setTermBuffer(tokens[index++]);
          offsetAtt.setOffset(0,0);
          return true;
        }        
      }
    };
    stream = new CachingTokenFilter(stream);
    doc.add(new Field("preanalyzed", stream, TermVector.NO));
    checkTokens(stream);
    stream.reset();  
    checkTokens(stream);
    writer.addDocument(doc);
    writer.close();
    IndexReader reader = IndexReader.open(dir, true);
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
    reader.close();
    stream.reset();
    checkTokens(stream);
  }
  private void checkTokens(TokenStream stream) throws IOException {
    int count = 0;
    TermAttribute termAtt = stream.getAttribute(TermAttribute.class);
    assertNotNull(termAtt);
    while (stream.incrementToken()) {
      assertTrue(count < tokens.length);
      assertEquals(tokens[count], termAtt.term());
      count++;
    }
    assertEquals(tokens.length, count);
  }
}
