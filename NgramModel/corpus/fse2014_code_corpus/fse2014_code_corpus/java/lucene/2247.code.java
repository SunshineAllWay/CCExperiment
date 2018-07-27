package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LetterTokenizer;
import java.io.Reader;
public class LetterTokenizerFactory extends BaseTokenizerFactory {
  public LetterTokenizer create(Reader input) {
    assureMatchVersion();
    return new LetterTokenizer(luceneMatchVersion, input);
  }
}
