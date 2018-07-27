package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;
public class TurkishLowerCaseFilterFactory extends BaseTokenFilterFactory {
  public TokenStream create(TokenStream input) {
    return new TurkishLowerCaseFilter(input);
  }
}
