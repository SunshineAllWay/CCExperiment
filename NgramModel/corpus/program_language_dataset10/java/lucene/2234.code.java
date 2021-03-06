package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hi.HindiNormalizationFilter;
public class HindiNormalizationFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new HindiNormalizationFilter(input);
  }
}
