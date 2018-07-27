package org.apache.lucene.analysis.payloads;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.Payload;
import java.io.IOException;
public class NumericPayloadTokenFilter extends TokenFilter {
  private String typeMatch;
  private Payload thePayload;
  private PayloadAttribute payloadAtt;
  private TypeAttribute typeAtt;
  public NumericPayloadTokenFilter(TokenStream input, float payload, String typeMatch) {
    super(input);
    thePayload = new Payload(PayloadHelper.encodeFloat(payload));
    this.typeMatch = typeMatch;
    payloadAtt = addAttribute(PayloadAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      if (typeAtt.type().equals(typeMatch))
        payloadAtt.setPayload(thePayload);
      return true;
    } else {
      return false;
    }
  }
}
