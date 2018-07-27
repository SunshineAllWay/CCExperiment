package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import java.io.Reader;
public class StandardTokenizerFactory extends BaseTokenizerFactory {
  public StandardTokenizer create(Reader input) {
    assureMatchVersion();
    return new StandardTokenizer(luceneMatchVersion, input);
  }
}
