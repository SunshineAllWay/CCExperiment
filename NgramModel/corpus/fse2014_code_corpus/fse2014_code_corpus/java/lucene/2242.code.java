package org.apache.solr.analysis;
import org.apache.lucene.analysis.ISOLatin1AccentFilter;
import org.apache.lucene.analysis.TokenStream;
public class ISOLatin1AccentFilterFactory extends BaseTokenFilterFactory {
  public ISOLatin1AccentFilter create(TokenStream input) {
    return new ISOLatin1AccentFilter(input);
  }
}
