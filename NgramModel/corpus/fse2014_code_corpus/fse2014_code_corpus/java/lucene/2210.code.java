package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.bg.BulgarianStemFilter;
public class BulgarianStemFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new BulgarianStemFilter(input);
  }
}
