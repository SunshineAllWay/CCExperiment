package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hi.HindiStemFilter;
public class HindiStemFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new HindiStemFilter(input);
  }
}
