package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
public class StandardFilterFactory extends BaseTokenFilterFactory {
  public StandardFilter create(TokenStream input) {
    return new StandardFilter(input);
  }
}
