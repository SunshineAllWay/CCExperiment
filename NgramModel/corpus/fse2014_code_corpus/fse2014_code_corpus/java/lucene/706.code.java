package org.apache.lucene.analysis.ru;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter; 
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.ru.RussianStemmer;
import org.apache.lucene.analysis.snowball.SnowballFilter; 
import java.io.IOException;
@Deprecated
public final class RussianStemFilter extends TokenFilter
{
    private RussianStemmer stemmer = null;
    private final TermAttribute termAtt;
    private final KeywordAttribute keywordAttr;
    public RussianStemFilter(TokenStream in)
    {
        super(in);
        stemmer = new RussianStemmer();
        termAtt = addAttribute(TermAttribute.class);
        keywordAttr = addAttribute(KeywordAttribute.class);
    }
    @Override
    public final boolean incrementToken() throws IOException
    {
      if (input.incrementToken()) {
        if(!keywordAttr.isKeyword()) {
          final String term = termAtt.term();
          final String s = stemmer.stem(term);
          if (s != null && !s.equals(term))
            termAtt.setTermBuffer(s);
        }
        return true;
      } else {
        return false;
      }
    }
    public void setStemmer(RussianStemmer stemmer)
    {
        if (stemmer != null)
        {
            this.stemmer = stemmer;
        }
    }
}
