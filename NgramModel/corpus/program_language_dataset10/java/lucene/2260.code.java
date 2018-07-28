package org.apache.solr.analysis;
import org.apache.lucene.analysis.fa.*;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.util.Map;
public class PersianNormalizationFilterFactory extends BaseTokenFilterFactory {
  public PersianNormalizationFilter create(TokenStream input) {
    return new PersianNormalizationFilter(input);
  }
}
