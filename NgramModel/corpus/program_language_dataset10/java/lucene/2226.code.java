package org.apache.solr.analysis;
import org.apache.lucene.analysis.nl.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Map;
public class DutchStemFilterFactory extends BaseTokenFilterFactory {
  public DutchStemFilter create(TokenStream _in) {
    return new DutchStemFilter(_in);
  }
}
