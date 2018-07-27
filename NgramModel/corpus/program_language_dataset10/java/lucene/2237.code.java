package org.apache.solr.analysis;
import org.apache.lucene.analysis.CharStream;
public class HTMLStripCharFilterFactory extends BaseCharFilterFactory {
  public HTMLStripCharFilter create(CharStream input) {
    return new HTMLStripCharFilter(input);
  }
}
