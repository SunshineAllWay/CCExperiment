package org.apache.solr.request;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MultiMapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
public class LocalSolrQueryRequest extends SolrQueryRequestBase {
  public final static Map emptyArgs = new HashMap(0,1);
  protected static SolrParams makeParams(String query, String qtype, int start, int limit, Map args) {
    Map<String,String[]> map = new HashMap<String,String[]>();
    for (Iterator iter = args.entrySet().iterator(); iter.hasNext();) {
      Map.Entry e = (Map.Entry)iter.next();
      String k = e.getKey().toString();
      Object v = e.getValue();
      if (v instanceof String[]) map.put(k,(String[])v);
      else map.put(k,new String[]{v.toString()});
    }
    if (query!=null) map.put(CommonParams.Q, new String[]{query});
    if (qtype!=null) map.put(CommonParams.QT, new String[]{qtype});
    map.put(CommonParams.START, new String[]{Integer.toString(start)});
    map.put(CommonParams.ROWS, new String[]{Integer.toString(limit)});
    return new MultiMapSolrParams(map);
  }
  public LocalSolrQueryRequest(SolrCore core, String query, String qtype, int start, int limit, Map args) {
    super(core,makeParams(query,qtype,start,limit,args));
  }
  public LocalSolrQueryRequest(SolrCore core, NamedList args) {
    super(core, SolrParams.toSolrParams(args));
  }
  public LocalSolrQueryRequest(SolrCore core, Map<String,String[]> args) {
    super(core, new MultiMapSolrParams(args));
  }
  public LocalSolrQueryRequest(SolrCore core, SolrParams args) {
    super(core, args);
  }
}
