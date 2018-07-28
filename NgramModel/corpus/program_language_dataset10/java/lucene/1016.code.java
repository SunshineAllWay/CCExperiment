package org.apache.lucene.collation;
import com.ibm.icu.text.Collator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.KeywordTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import java.io.Reader;
import java.io.IOException;
public final class ICUCollationKeyAnalyzer extends Analyzer {
  private Collator collator;
  public ICUCollationKeyAnalyzer(Collator collator) {
    this.collator = collator;
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new KeywordTokenizer(reader);
    result = new ICUCollationKeyFilter(result, collator);
    return result;
  }
  private class SavedStreams {
    Tokenizer source;
    TokenStream result;
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) 
    throws IOException {
    SavedStreams streams = (SavedStreams)getPreviousTokenStream();
    if (streams == null) {
      streams = new SavedStreams();
      streams.source = new KeywordTokenizer(reader);
      streams.result = new ICUCollationKeyFilter(streams.source, collator);
      setPreviousTokenStream(streams);
    } else {
      streams.source.reset(reader);
    }
    return streams.result;
  }
}
