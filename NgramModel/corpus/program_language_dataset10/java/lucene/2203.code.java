package org.apache.solr.analysis;
import org.apache.lucene.analysis.*;
import org.apache.lucene.util.ArrayUtil;
import java.util.Map;
public class ASCIIFoldingFilterFactory extends BaseTokenFilterFactory {
  public ASCIIFoldingFilter create(TokenStream input) {
    return new ASCIIFoldingFilter(input);
  }
}
