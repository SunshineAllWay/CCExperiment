package org.apache.lucene.analysis.ru;
import java.io.IOException;
import org.apache.lucene.analysis.LowerCaseFilter; 
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
@Deprecated
public final class RussianLowerCaseFilter extends TokenFilter
{
    private TermAttribute termAtt;
    public RussianLowerCaseFilter(TokenStream in)
    {
        super(in);
        termAtt = addAttribute(TermAttribute.class);
    }
    @Override
    public final boolean incrementToken() throws IOException
    {
      if (input.incrementToken()) {
        char[] chArray = termAtt.termBuffer();
        int chLen = termAtt.termLength();
        for (int i = 0; i < chLen; i++)
        {
          chArray[i] = Character.toLowerCase(chArray[i]);
        }
        return true;
      } else {
        return false;
      }
    }
}
