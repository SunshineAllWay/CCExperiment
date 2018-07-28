package org.apache.lucene.analysis.payloads;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class DelimitedPayloadTokenFilter extends TokenFilter {
  public static final char DEFAULT_DELIMITER = '|';
  private final char delimiter;
  private final TermAttribute termAtt;
  private final PayloadAttribute payAtt;
  private final PayloadEncoder encoder;
  public DelimitedPayloadTokenFilter(TokenStream input, char delimiter, PayloadEncoder encoder) {
    super(input);
    termAtt = addAttribute(TermAttribute.class);
    payAtt = addAttribute(PayloadAttribute.class);
    this.delimiter = delimiter;
    this.encoder = encoder;
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      final char[] buffer = termAtt.termBuffer();
      final int length = termAtt.termLength();
      for (int i = 0; i < length; i++) {
        if (buffer[i] == delimiter) {
          payAtt.setPayload(encoder.encode(buffer, i + 1, (length - (i + 1))));
          termAtt.setTermLength(i); 
          return true;
        }
      }
      payAtt.setPayload(null);
      return true;
    } else return false;
  }
}
