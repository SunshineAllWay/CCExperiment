package org.apache.solr.analysis;
import org.apache.lucene.analysis.de.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
import java.util.Set;
import java.util.Map;
public class GermanStemFilterFactory extends BaseTokenFilterFactory {
  public GermanStemFilter create(TokenStream in) {
    return new GermanStemFilter(in);
  }
}
