package org.apache.lucene.analysis.nl;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
@Deprecated
public final class DutchStemFilter extends TokenFilter {
  private DutchStemmer stemmer = null;
  private Set<?> exclusions = null;
  private final TermAttribute termAtt;
  private final KeywordAttribute keywordAttr;
  public DutchStemFilter(TokenStream _in) {
    super(_in);
    stemmer = new DutchStemmer();
    termAtt = addAttribute(TermAttribute.class);
    keywordAttr = addAttribute(KeywordAttribute.class);
  }
  @Deprecated
  public DutchStemFilter(TokenStream _in, Set<?> exclusiontable) {
    this(_in);
    exclusions = exclusiontable;
  }
  public DutchStemFilter(TokenStream _in,  Map<?,?> stemdictionary) {
    this(_in);
    stemmer.setStemDictionary(stemdictionary);
  }
  @Deprecated
  public DutchStemFilter(TokenStream _in, Set<?> exclusiontable, Map<?,?> stemdictionary) {
    this(_in, exclusiontable);
    stemmer.setStemDictionary(stemdictionary);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      final String term = termAtt.term();
      if (!keywordAttr.isKeyword() && (exclusions == null || !exclusions.contains(term))) {
        final String s = stemmer.stem(term);
        if ((s != null) && !s.equals(term))
          termAtt.setTermBuffer(s);
      }
      return true;
    } else {
      return false;
    }
  }
  public void setStemmer(DutchStemmer stemmer) {
    if (stemmer != null) {
      this.stemmer = stemmer;
    }
  }
  @Deprecated
  public void setExclusionTable(HashSet<?> exclusiontable) {
    exclusions = exclusiontable;
  }
  public void setStemDictionary(HashMap<?,?> dict) {
    if (stemmer != null)
      stemmer.setStemDictionary(dict);
  }
}