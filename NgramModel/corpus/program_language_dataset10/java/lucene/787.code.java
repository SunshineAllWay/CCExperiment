package org.apache.lucene.analysis.payloads;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;
import java.io.IOException;
import java.io.StringReader;
public class TokenOffsetPayloadTokenFilterTest extends BaseTokenStreamTestCase {
  public TokenOffsetPayloadTokenFilterTest(String s) {
    super(s);
  }
  public void test() throws IOException {
    String test = "The quick red fox jumped over the lazy brown dogs";
    TokenOffsetPayloadTokenFilter nptf = new TokenOffsetPayloadTokenFilter(new WhitespaceTokenizer(TEST_VERSION_CURRENT, new StringReader(test)));
    int count = 0;
    PayloadAttribute payloadAtt = nptf.getAttribute(PayloadAttribute.class);
    OffsetAttribute offsetAtt = nptf.getAttribute(OffsetAttribute.class);
    while (nptf.incrementToken()) {
      Payload pay = payloadAtt.getPayload();
      assertTrue("pay is null and it shouldn't be", pay != null);
      byte [] data = pay.getData();
      int start = PayloadHelper.decodeInt(data, 0);
      assertTrue(start + " does not equal: " + offsetAtt.startOffset(), start == offsetAtt.startOffset());
      int end = PayloadHelper.decodeInt(data, 4);
      assertTrue(end + " does not equal: " + offsetAtt.endOffset(), end == offsetAtt.endOffset());
      count++;
    }
    assertTrue(count + " does not equal: " + 10, count == 10);
  }
}