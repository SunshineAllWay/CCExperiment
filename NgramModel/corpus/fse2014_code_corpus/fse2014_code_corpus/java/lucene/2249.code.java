package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import java.io.Reader;
public class LowerCaseTokenizerFactory extends BaseTokenizerFactory {
  public LowerCaseTokenizer create(Reader input) {
    assureMatchVersion();
    return new LowerCaseTokenizer(luceneMatchVersion,input);
  }
}
