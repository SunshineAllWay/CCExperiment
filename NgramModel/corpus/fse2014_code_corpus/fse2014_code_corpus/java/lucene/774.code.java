package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.TokenStream;
public class TestEmptyTokenStream extends LuceneTestCase {
  public void test() throws IOException {
    TokenStream ts = new EmptyTokenStream();
    assertFalse(ts.incrementToken());
    ts.reset();
    assertFalse(ts.incrementToken());
  }
}
