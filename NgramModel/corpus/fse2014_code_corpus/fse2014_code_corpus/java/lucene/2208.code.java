package org.apache.solr.analysis;
import org.apache.lucene.analysis.br.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Map;
public class BrazilianStemFilterFactory extends BaseTokenFilterFactory {
  public BrazilianStemFilter create(TokenStream in) {
    return new BrazilianStemFilter(in);
  }
}
