package org.apache.lucene.search.highlight;
import org.apache.lucene.analysis.TokenStream;
public class NullFragmenter implements Fragmenter {
  public void start(String s, TokenStream tokenStream) {
  }
  public boolean isNewFragment() {
    return false;
  }
}
