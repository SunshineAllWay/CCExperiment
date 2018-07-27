package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseFilter;
public class LowerCaseFilterFactory extends BaseTokenFilterFactory {
  public LowerCaseFilter create(TokenStream input) {
    assureMatchVersion();
    return new LowerCaseFilter(luceneMatchVersion,input);
  }
}
