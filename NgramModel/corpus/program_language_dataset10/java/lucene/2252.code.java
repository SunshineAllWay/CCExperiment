package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import java.io.Reader;
import java.util.Map;
public class NGramTokenizerFactory extends BaseTokenizerFactory {
    private int maxGramSize = 0;
    private int minGramSize = 0;
    @Override
    public void init(Map<String, String> args) {
        super.init(args);
        String maxArg = args.get("maxGramSize");
        maxGramSize = (maxArg != null ? Integer.parseInt(maxArg) : NGramTokenizer.DEFAULT_MAX_NGRAM_SIZE);
        String minArg = args.get("minGramSize");
        minGramSize = (minArg != null ? Integer.parseInt(minArg) : NGramTokenizer.DEFAULT_MIN_NGRAM_SIZE);
    }
    public NGramTokenizer create(Reader input) {
        return new NGramTokenizer(input, minGramSize, maxGramSize);
    }
}
