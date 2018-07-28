package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.position.PositionFilter;
import java.util.Map;
public class PositionFilterFactory extends BaseTokenFilterFactory {
  private int positionIncrement;
  public void init(Map<String, String> args) {
    super.init(args);
    positionIncrement = getInt("positionIncrement", 0);
  }
  public PositionFilter create(TokenStream input) {
    return new PositionFilter(input, positionIncrement);
  }
}
