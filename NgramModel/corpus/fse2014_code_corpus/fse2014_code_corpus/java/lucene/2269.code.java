package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.reverse.ReverseStringFilter;
public class ReverseStringFilterFactory extends BaseTokenFilterFactory {
  public ReverseStringFilter create(TokenStream in) {
    assureMatchVersion();
    return new ReverseStringFilter(luceneMatchVersion,in);
  }
}
