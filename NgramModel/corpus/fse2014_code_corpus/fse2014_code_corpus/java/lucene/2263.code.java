package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.PorterStemFilter;
public class PorterStemFilterFactory extends BaseTokenFilterFactory {
  public PorterStemFilter create(TokenStream input) {
    return new PorterStemFilter(input);
  }
}
