package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianStemFilter;
public class RussianStemFilterFactory extends BaseTokenFilterFactory {
  public RussianStemFilter create(TokenStream in) {
    return new RussianStemFilter(in);
  }
}
