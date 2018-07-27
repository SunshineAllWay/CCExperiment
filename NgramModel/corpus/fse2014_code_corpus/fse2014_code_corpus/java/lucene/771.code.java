package org.apache.lucene.analysis.in;
import java.io.IOException;
import java.io.StringReader;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.TokenStream;
public class TestIndicTokenizer extends BaseTokenStreamTestCase {
  public void testBasics() throws IOException {
    TokenStream ts = new IndicTokenizer(TEST_VERSION_CURRENT,
        new StringReader("मुझे हिंदी का और अभ्यास करना होगा ।"));
    assertTokenStreamContents(ts,
        new String[] { "मुझे", "हिंदी", "का", "और", "अभ्यास", "करना", "होगा" });
  }
  public void testFormat() throws Exception {
    TokenStream ts = new IndicTokenizer(TEST_VERSION_CURRENT,
        new StringReader("शार्‍मा शार्‍मा"));
    assertTokenStreamContents(ts, new String[] { "शार्‍मा", "शार्‍मा" });
  }
}
