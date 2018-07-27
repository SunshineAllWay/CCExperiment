package org.apache.lucene.index;
import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestMultiLevelSkipList extends LuceneTestCase {
  public void testSimpleSkip() throws IOException {
    RAMDirectory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new PayloadAnalyzer()));
    Term term = new Term("test", "a");
    for (int i = 0; i < 5000; i++) {
      Document d1 = new Document();
      d1.add(new Field(term.field(), term.text(), Store.NO, Index.ANALYZED));
      writer.addDocument(d1);
    }
    writer.commit();
    writer.optimize();
    writer.close();
    IndexReader reader = SegmentReader.getOnlySegmentReader(dir);
    SegmentTermPositions tp = (SegmentTermPositions) reader.termPositions();
    tp.freqStream = new CountingStream(tp.freqStream);
    for (int i = 0; i < 2; i++) {
      counter = 0;
      tp.seek(term);
      checkSkipTo(tp, 14, 185); 
      checkSkipTo(tp, 17, 190); 
      checkSkipTo(tp, 287, 200); 
      checkSkipTo(tp, 4800, 250);
    }
  }
  public void checkSkipTo(TermPositions tp, int target, int maxCounter) throws IOException {
    tp.skipTo(target);
    if (maxCounter < counter) {
      fail("Too many bytes read: " + counter);
    }
    assertEquals("Wrong document " + tp.doc() + " after skipTo target " + target, target, tp.doc());
    assertEquals("Frequency is not 1: " + tp.freq(), 1,tp.freq());
    tp.nextPosition();
    byte[] b = new byte[1];
    tp.getPayload(b, 0);
    assertEquals("Wrong payload for the target " + target + ": " + b[0], (byte) target, b[0]);
  }
  private static class PayloadAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new PayloadFilter(new LowerCaseTokenizer(TEST_VERSION_CURRENT, reader));
    }
  }
  private static class PayloadFilter extends TokenFilter {
    static int count = 0;
    PayloadAttribute payloadAtt;
    protected PayloadFilter(TokenStream input) {
      super(input);
      payloadAtt = addAttribute(PayloadAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      boolean hasNext = input.incrementToken();
      if (hasNext) {
        payloadAtt.setPayload(new Payload(new byte[] { (byte) count++ }));
      } 
      return hasNext;
    }
  }
  private int counter = 0;
  class CountingStream extends IndexInput {
    private IndexInput input;
    CountingStream(IndexInput input) {
      this.input = input;
    }
    @Override
    public byte readByte() throws IOException {
      TestMultiLevelSkipList.this.counter++;
      return this.input.readByte();
    }
    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
      TestMultiLevelSkipList.this.counter += len;
      this.input.readBytes(b, offset, len);
    }
    @Override
    public void close() throws IOException {
      this.input.close();
    }
    @Override
    public long getFilePointer() {
      return this.input.getFilePointer();
    }
    @Override
    public void seek(long pos) throws IOException {
      this.input.seek(pos);
    }
    @Override
    public long length() {
      return this.input.length();
    }
    @Override
    public Object clone() {
      return new CountingStream((IndexInput) this.input.clone());
    }
  }
}
