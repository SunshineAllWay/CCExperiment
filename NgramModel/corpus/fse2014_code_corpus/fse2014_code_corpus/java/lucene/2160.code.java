package org.apache.solr.common.params;
public class AppendedSolrParams extends DefaultSolrParams {
  public AppendedSolrParams(SolrParams main, SolrParams extra) {
    super(main, extra);
  }
  @Override
  public String[] getParams(String param) {
    String[] main = params.getParams(param);
    String[] extra = defaults.getParams(param);
    if (null == extra || 0 == extra.length) {
      return main;
    }
    if (null == main || 0 == main.length) {
      return extra;
    }
    String[] result = new String[main.length + extra.length];
    System.arraycopy(main,0,result,0,main.length);
    System.arraycopy(extra,0,result,main.length,extra.length);
    return result;
  }
  @Override
  public String toString() {
    return "{main("+params+"),extra("+defaults+")}";
  }
}
