package org.apache.solr.analysis;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekLowerCaseFilter;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
public class GreekLowerCaseFilterFactory extends BaseTokenFilterFactory 
{
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    if (args.containsKey("charset"))
      throw new SolrException(ErrorCode.SERVER_ERROR,
          "The charset parameter is no longer supported.  "
          + "Please process your documents as Unicode instead.");
  }
  public GreekLowerCaseFilter create(TokenStream in) {
    return new GreekLowerCaseFilter(in);
  }
}
