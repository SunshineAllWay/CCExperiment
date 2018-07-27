package org.apache.solr.analysis;
import org.apache.lucene.analysis.fr.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
public class FrenchStemFilterFactory extends BaseTokenFilterFactory {
  public FrenchStemFilter create(TokenStream in) {
    return new FrenchStemFilter(in);
  }
}
