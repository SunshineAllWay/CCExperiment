package org.apache.lucene.collation;
import com.ibm.icu.text.Collator;
import com.ibm.icu.text.RawCollationKey;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.IndexableBinaryStringTools;
import java.io.IOException;
public final class ICUCollationKeyFilter extends TokenFilter {
  private Collator collator = null;
  private RawCollationKey reusableKey = new RawCollationKey();
  private TermAttribute termAtt;
  public ICUCollationKeyFilter(TokenStream input, Collator collator) {
    super(input);
    this.collator = collator;
    termAtt = addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      char[] termBuffer = termAtt.termBuffer();
      String termText = new String(termBuffer, 0, termAtt.termLength());
      collator.getRawCollationKey(termText, reusableKey);
      int encodedLength = IndexableBinaryStringTools.getEncodedLength(
          reusableKey.bytes, 0, reusableKey.size);
      if (encodedLength > termBuffer.length) {
        termAtt.resizeTermBuffer(encodedLength);
      }
      termAtt.setTermLength(encodedLength);
      IndexableBinaryStringTools.encode(reusableKey.bytes, 0, reusableKey.size,
          termAtt.termBuffer(), 0, encodedLength);
      return true;
    } else {
      return false;
    }
  }
}
