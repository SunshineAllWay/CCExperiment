package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cz.CzechStemFilter;
public class CzechStemFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new CzechStemFilter(input);
  }
}
