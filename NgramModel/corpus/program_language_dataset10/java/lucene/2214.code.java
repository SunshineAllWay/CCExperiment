package org.apache.solr.analysis;
import org.apache.lucene.analysis.cn.*;
import java.io.Reader;
import org.apache.lucene.analysis.*;
import java.util.Map;
public class ChineseTokenizerFactory extends BaseTokenizerFactory {
  public ChineseTokenizer create(Reader in) {
    return new ChineseTokenizer(in);
  }
}
