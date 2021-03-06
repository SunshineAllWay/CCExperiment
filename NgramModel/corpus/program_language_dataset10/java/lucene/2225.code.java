package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
public class DoubleMetaphoneFilterFactory extends BaseTokenFilterFactory 
{
  public static final String INJECT = "inject"; 
  public static final String MAX_CODE_LENGTH = "maxCodeLength"; 
  public static final int DEFAULT_MAX_CODE_LENGTH = 4;
  private boolean inject = true;
  private int maxCodeLength = DEFAULT_MAX_CODE_LENGTH;
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    inject = getBoolean(INJECT, true);
    if (args.get(MAX_CODE_LENGTH) != null) {
      maxCodeLength = Integer.parseInt(args.get(MAX_CODE_LENGTH));
    }
  }
  public DoubleMetaphoneFilter create(TokenStream input) {
    return new DoubleMetaphoneFilter(input, maxCodeLength, inject);
  }
}
