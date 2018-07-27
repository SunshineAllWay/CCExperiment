package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenStream;
import java.util.Map;
public interface TokenFilterFactory {
  public void init(Map<String,String> args);
  public Map<String,String> getArgs();
  public TokenStream create(TokenStream input);
}
