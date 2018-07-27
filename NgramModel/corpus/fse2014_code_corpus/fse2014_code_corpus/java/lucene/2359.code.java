package org.apache.solr.handler.component;
import java.io.IOException;
import java.net.URL;
import org.apache.lucene.queryParser.ParseException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public abstract class SearchComponent implements SolrInfoMBean, NamedListInitializedPlugin
{
  public abstract void prepare(ResponseBuilder rb) throws IOException;
  public abstract void process(ResponseBuilder rb) throws IOException;
  public int distributedProcess(ResponseBuilder rb) throws IOException {
    return ResponseBuilder.STAGE_DONE;
  }
  public void modifyRequest(ResponseBuilder rb, SearchComponent who, ShardRequest sreq) {
  }
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
  }
  public void finishStage(ResponseBuilder rb) {
  }
  public void init( NamedList args )
  {
  }
  public String getName() {
    return this.getClass().getName();
  }
  public abstract String getDescription();
  public abstract String getSourceId();
  public abstract String getSource();
  public abstract String getVersion();
  public Category getCategory() {
    return Category.OTHER;
  }
  public URL[] getDocs() {
    return null;  
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    return lst;
  }
}
