package org.apache.solr.analysis;
import org.apache.lucene.analysis.cn.*;
import java.util.Hashtable;
import org.apache.lucene.analysis.*;
import java.util.Map;
public class ChineseFilterFactory extends BaseTokenFilterFactory {
  public ChineseFilter create(TokenStream in) {
    return new ChineseFilter(in);
  }
}
