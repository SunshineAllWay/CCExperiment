package org.apache.solr.analysis;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenizer;
import java.io.Reader;
import java.util.Map;
public class EdgeNGramTokenizerFactory extends BaseTokenizerFactory {
    private int maxGramSize = 0;
    private int minGramSize = 0;
    private String side;
    @Override
    public void init(Map<String, String> args) {
        super.init(args);
        String maxArg = args.get("maxGramSize");
        maxGramSize = (maxArg != null ? Integer.parseInt(maxArg) : EdgeNGramTokenizer.DEFAULT_MAX_GRAM_SIZE);
        String minArg = args.get("minGramSize");
        minGramSize = (minArg != null ? Integer.parseInt(minArg) : EdgeNGramTokenizer.DEFAULT_MIN_GRAM_SIZE);
        side = args.get("side");
        if (side == null) {
            side = EdgeNGramTokenizer.Side.FRONT.getLabel();
        }
    }
    public EdgeNGramTokenizer create(Reader input) {
        return new EdgeNGramTokenizer(input, side, minGramSize, maxGramSize);
    }
}
