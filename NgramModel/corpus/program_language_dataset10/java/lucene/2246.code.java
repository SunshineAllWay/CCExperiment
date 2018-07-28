package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LengthFilter;
import java.util.Map;
public class LengthFilterFactory extends BaseTokenFilterFactory {
  int min,max;
  public static final String MIN_KEY = "min";
  public static final String MAX_KEY = "max";
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    min=Integer.parseInt(args.get(MIN_KEY));
    max=Integer.parseInt(args.get(MAX_KEY));
  }
  public LengthFilter create(TokenStream input) {
    return new LengthFilter(input,min,max);
  }
}
