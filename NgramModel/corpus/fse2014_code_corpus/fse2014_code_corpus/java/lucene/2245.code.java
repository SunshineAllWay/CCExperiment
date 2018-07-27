package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.KeywordTokenizer;
import java.io.Reader;
public class KeywordTokenizerFactory extends BaseTokenizerFactory {
  public KeywordTokenizer create(Reader input) {
    return new KeywordTokenizer(input);
  }
}
