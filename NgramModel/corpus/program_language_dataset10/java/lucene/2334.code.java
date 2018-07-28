package org.apache.solr.handler;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocSet;
import org.apache.solr.util.SolrPluginUtils;
import org.apache.lucene.queryParser.ParseException;
import java.net.URL;
public abstract class RequestHandlerBase implements SolrRequestHandler, SolrInfoMBean {
  volatile long numRequests;
  volatile long numErrors;
  volatile long numTimeouts;
  protected NamedList initArgs = null;
  protected SolrParams defaults;
  protected SolrParams appends;
  protected SolrParams invariants;
  volatile long totalTime = 0;
  long handlerStart = System.currentTimeMillis();
  protected boolean httpCaching = true;
  public void init(NamedList args) {
    initArgs = args;
    if( args != null ) {
      Object o = args.get("defaults");
      if (o != null && o instanceof NamedList) {
        defaults = SolrParams.toSolrParams((NamedList)o);
      }
      o = args.get("appends");
      if (o != null && o instanceof NamedList) {
        appends = SolrParams.toSolrParams((NamedList)o);
      }
      o = args.get("invariants");
      if (o != null && o instanceof NamedList) {
        invariants = SolrParams.toSolrParams((NamedList)o);
      }
    }
    if (initArgs != null) {
      Object caching = initArgs.get("httpCaching");
      httpCaching = caching != null ? Boolean.parseBoolean(caching.toString()) : true;
    }
  }
  public NamedList getInitArgs() {
    return initArgs;
  }
  public abstract void handleRequestBody( SolrQueryRequest req, SolrQueryResponse rsp ) throws Exception;
  public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
    numRequests++;
    try {
      SolrPluginUtils.setDefaults(req,defaults,appends,invariants);
      rsp.setHttpCaching(httpCaching);
      handleRequestBody( req, rsp );
      NamedList header = rsp.getResponseHeader();
      if(header != null) {
        Object partialResults = header.get("partialResults");
        boolean timedOut = partialResults == null ? false : (Boolean)partialResults;
        if( timedOut ) {
          numTimeouts++;
          rsp.setHttpCaching(false);
        }
      }
    } catch (Exception e) {
      SolrException.log(SolrCore.log,e);
      if (e instanceof ParseException) {
        e = new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
      }
      rsp.setException(e);
      numErrors++;
    }
    totalTime += rsp.getEndTime() - req.getStartTime();
  }
  public String getName() {
    return this.getClass().getName();
  }
  public abstract String getDescription();
  public abstract String getSourceId();
  public abstract String getSource();
  public abstract String getVersion();
  public Category getCategory() {
    return Category.QUERYHANDLER;
  }
  public URL[] getDocs() {
    return null;  
  }
  public NamedList getStatistics() {
    NamedList lst = new SimpleOrderedMap();
    lst.add("handlerStart",handlerStart);
    lst.add("requests", numRequests);
    lst.add("errors", numErrors);
    lst.add("timeouts", numTimeouts);
    lst.add("totalTime",totalTime);
    lst.add("avgTimePerRequest", (float) totalTime / (float) this.numRequests);
    lst.add("avgRequestsPerSecond", (float) numRequests*1000 / (float)(System.currentTimeMillis()-handlerStart));   
    return lst;
  }
}
