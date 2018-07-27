package org.apache.lucene.analysis.standard;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public final class StandardFilter extends TokenFilter {
  public StandardFilter(TokenStream in) {
    super(in);
    termAtt = addAttribute(TermAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
  }
  private static final String APOSTROPHE_TYPE = StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.APOSTROPHE];
  private static final String ACRONYM_TYPE = StandardTokenizerImpl.TOKEN_TYPES[StandardTokenizerImpl.ACRONYM];
  private TypeAttribute typeAtt;
  private TermAttribute termAtt;
  @Override
  public final boolean incrementToken() throws java.io.IOException {
    if (!input.incrementToken()) {
      return false;
    }
    char[] buffer = termAtt.termBuffer();
    final int bufferLength = termAtt.termLength();
    final String type = typeAtt.type();
    if (type == APOSTROPHE_TYPE &&      
  bufferLength >= 2 &&
        buffer[bufferLength-2] == '\'' &&
        (buffer[bufferLength-1] == 's' || buffer[bufferLength-1] == 'S')) {
      termAtt.setTermLength(bufferLength - 2);
    } else if (type == ACRONYM_TYPE) {      
      int upto = 0;
      for(int i=0;i<bufferLength;i++) {
        char c = buffer[i];
        if (c != '.')
          buffer[upto++] = c;
      }
      termAtt.setTermLength(upto);
    }
    return true;
  }
}
