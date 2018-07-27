package org.apache.lucene.analysis.payloads;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.Payload;
public class TokenOffsetPayloadTokenFilter extends TokenFilter {
  protected OffsetAttribute offsetAtt;
  protected PayloadAttribute payAtt;
  public TokenOffsetPayloadTokenFilter(TokenStream input) {
    super(input);
    offsetAtt = addAttribute(OffsetAttribute.class);
    payAtt = addAttribute(PayloadAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      byte[] data = new byte[8];
      PayloadHelper.encodeInt(offsetAtt.startOffset(), data, 0);
      PayloadHelper.encodeInt(offsetAtt.endOffset(), data, 4);
      Payload payload = new Payload(data);
      payAtt.setPayload(payload);
      return true;
    } else {
    return false;
    }
  }
}