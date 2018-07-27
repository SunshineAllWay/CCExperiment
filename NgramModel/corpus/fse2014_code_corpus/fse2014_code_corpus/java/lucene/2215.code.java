package org.apache.solr.analysis;
import org.apache.lucene.analysis.cjk.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import java.io.Reader;
import java.util.Map;
public class CJKTokenizerFactory extends BaseTokenizerFactory {
  public CJKTokenizer create(Reader in) {
    return new CJKTokenizer(in);
  }
}
