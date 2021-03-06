package org.apache.lucene.search.highlight;
import java.io.IOException;
import org.apache.lucene.analysis.TokenStream;
public interface Scorer {
  public TokenStream init(TokenStream tokenStream) throws IOException;
  public void startFragment(TextFragment newFragment);
  public float getTokenScore();
  public float getFragmentScore();
}
