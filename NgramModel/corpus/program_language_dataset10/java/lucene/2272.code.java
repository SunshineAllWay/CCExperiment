package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ru.RussianLowerCaseFilter;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
public class RussianLowerCaseFilterFactory extends BaseTokenFilterFactory {
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    if (args.containsKey("charset"))
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "The charset parameter is no longer supported.  "
          + "Please process your documents as Unicode instead.");
  }
  public RussianLowerCaseFilter create(TokenStream in) {
    return new RussianLowerCaseFilter(in);
  }
}
