package org.apache.solr.analysis;
import java.io.Reader;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.in.IndicTokenizer;
public class IndicTokenizerFactory extends BaseTokenizerFactory {
  public Tokenizer create(Reader input) {
    assureMatchVersion();
    return new IndicTokenizer(luceneMatchVersion, input);
  }
}
