package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
public class NGramFilterFactory extends BaseTokenFilterFactory {
  private int maxGramSize = 0;
  private int minGramSize = 0;
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    String maxArg = args.get("maxGramSize");
    maxGramSize = (maxArg != null ? Integer.parseInt(maxArg)
        : NGramTokenFilter.DEFAULT_MAX_NGRAM_SIZE);
    String minArg = args.get("minGramSize");
    minGramSize = (minArg != null ? Integer.parseInt(minArg)
        : NGramTokenFilter.DEFAULT_MIN_NGRAM_SIZE);
  }
  public NGramTokenFilter create(TokenStream input) {
    return new NGramTokenFilter(input, minGramSize, maxGramSize);
  }
}
