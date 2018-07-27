package org.apache.solr.analysis;
import java.io.Reader;
import java.util.Map;
import org.apache.lucene.analysis.ru.RussianLetterTokenizer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
public class RussianLetterTokenizerFactory extends BaseTokenizerFactory {
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    if (args.containsKey("charset"))
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "The charset parameter is no longer supported.  "
          + "Please process your documents as Unicode instead.");
  }
  public RussianLetterTokenizer create(Reader in) {
    assureMatchVersion();
    return new RussianLetterTokenizer(luceneMatchVersion,in);
  }
}
