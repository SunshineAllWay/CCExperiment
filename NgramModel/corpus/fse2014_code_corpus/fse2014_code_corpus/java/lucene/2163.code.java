package org.apache.solr.common.params;
import java.util.Iterator;
import org.apache.solr.common.util.IteratorChain;
public class DefaultSolrParams extends SolrParams {
  protected final SolrParams params;
  protected final SolrParams defaults;
  public DefaultSolrParams(SolrParams params, SolrParams defaults) {
    this.params = params;
    this.defaults = defaults;
  }
  @Override
  public String get(String param) {
    String val = params.get(param);
    return val!=null ? val : defaults.get(param);
  }
  @Override
  public String[] getParams(String param) {
    String[] vals = params.getParams(param);
    return vals!=null ? vals : defaults.getParams(param);
  }
  @Override
  public Iterator<String> getParameterNamesIterator() {
    final IteratorChain<String> c = new IteratorChain<String>();
    c.addIterator(defaults.getParameterNamesIterator());
    c.addIterator(params.getParameterNamesIterator());
    return c;
  }
  @Override
  public String toString() {
    return "{params("+params+"),defaults("+defaults+")}";
  }
}
