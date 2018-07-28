package org.apache.lucene.analysis.el;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class GreekLowerCaseFilter extends TokenFilter
{
    private TermAttribute termAtt;
    public GreekLowerCaseFilter(TokenStream in)
    {
    	super(in);
    	termAtt = addAttribute(TermAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (input.incrementToken()) {
        char[] chArray = termAtt.termBuffer();
        int chLen = termAtt.termLength();
        for (int i = 0; i < chLen; i++)
        {
          chArray[i] = (char) lowerCase(chArray[i]);
        }
        return true;
      } else {
        return false;
      }
    }
    private int lowerCase(int codepoint) {
      switch(codepoint) {
        case '\u03C2': 
          return '\u03C3'; 
        case '\u0386': 
        case '\u03AC': 
          return '\u03B1'; 
        case '\u0388': 
        case '\u03AD': 
          return '\u03B5'; 
        case '\u0389': 
        case '\u03AE': 
          return '\u03B7'; 
        case '\u038A': 
        case '\u03AA': 
        case '\u03AF': 
        case '\u03CA': 
        case '\u0390': 
          return '\u03B9'; 
        case '\u038E': 
        case '\u03AB': 
        case '\u03CD': 
        case '\u03CB': 
        case '\u03B0': 
          return '\u03C5'; 
        case '\u038C': 
        case '\u03CC': 
          return '\u03BF'; 
        case '\u038F': 
        case '\u03CE': 
          return '\u03C9'; 
        case '\u03A2': 
          return '\u03C2'; 
        default:
          return Character.toLowerCase(codepoint);
      }
    }
}
