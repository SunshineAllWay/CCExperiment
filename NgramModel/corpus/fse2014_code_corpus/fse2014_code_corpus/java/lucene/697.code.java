package org.apache.lucene.analysis.payloads;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.index.Payload;
import java.io.IOException;
public class TypeAsPayloadTokenFilter extends TokenFilter {
  private PayloadAttribute payloadAtt;
  private TypeAttribute typeAtt;
  public TypeAsPayloadTokenFilter(TokenStream input) {
    super(input);
    payloadAtt = addAttribute(PayloadAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      String type = typeAtt.type();
      if (type != null && type.equals("") == false) {
        payloadAtt.setPayload(new Payload(type.getBytes("UTF-8")));
      }
      return true;
    } else {
      return false;
    }
  }
}