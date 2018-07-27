package org.apache.lucene.collation;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.IndexableBinaryStringTools;
import java.io.IOException;
import java.text.Collator;
public final class CollationKeyFilter extends TokenFilter {
  private Collator collator = null;
  private TermAttribute termAtt;
  public CollationKeyFilter(TokenStream input, Collator collator) {
    super(input);
    this.collator = collator;
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] termBuffer = termAtt.termBuffer();
      String termText = new String(termBuffer, 0, termAtt.termLength());
      byte[] collationKey = collator.getCollationKey(termText).toByteArray();
      int encodedLength = IndexableBinaryStringTools.getEncodedLength(
          collationKey, 0, collationKey.length);
      if (encodedLength > termBuffer.length) {
        termAtt.resizeTermBuffer(encodedLength);
      }
      termAtt.setTermLength(encodedLength);
      IndexableBinaryStringTools.encode(collationKey, 0, collationKey.length,
          termAtt.termBuffer(), 0, encodedLength);
      return true;
    } else {
      return false;
    }
  }
}
