package org.apache.solr.handler.component;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.RTimer;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.impl.BinaryResponseParser;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.core.SolrCore;
import org.apache.lucene.queryParser.ParseException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.*;
public class SearchHandler extends RequestHandlerBase implements SolrCoreAware
{
  static final String INIT_COMPONENTS = "components";
  static final String INIT_FIRST_COMPONENTS = "first-components";
  static final String INIT_LAST_COMPONENTS = "last-components";
  static final String INIT_SO_TIMEOUT = "shard-socket-timeout";
  static final String INIT_CONNECTION_TIMEOUT = "shard-connection-timeout";
  static int soTimeout = 0; 
  static int connectionTimeout = 0; 
  protected static Logger log = LoggerFactory.getLogger(SearchHandler.class);
  protected List<SearchComponent> components = null;
  protected List<String> getDefaultComponents()
  {
    ArrayList<String> names = new ArrayList<String>(6);
    names.add( QueryComponent.COMPONENT_NAME );
    names.add( FacetComponent.COMPONENT_NAME );
    names.add( MoreLikeThisComponent.COMPONENT_NAME );
    names.add( HighlightComponent.COMPONENT_NAME );
    names.add( StatsComponent.COMPONENT_NAME );
    names.add( DebugComponent.COMPONENT_NAME );
    return names;
  }
  @SuppressWarnings("unchecked")
  public void inform(SolrCore core)
  {
    Object declaredComponents = initArgs.get(INIT_COMPONENTS);
    List<String> first = (List<String>) initArgs.get(INIT_FIRST_COMPONENTS);
    List<String> last  = (List<String>) initArgs.get(INIT_LAST_COMPONENTS);
    List<String> list = null;
    boolean makeDebugLast = true;
    if( declaredComponents == null ) {
      list = getDefaultComponents();
      if( first != null ) {
        List<String> clist = first;
        clist.addAll( list );
        list = clist;
      }
      if( last != null ) {
        list.addAll( last );
      }
    }
    else {
      list = (List<String>)declaredComponents;
      if( first != null || last != null ) {
        throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,
            "First/Last components only valid if you do not declare 'components'");
      }
      makeDebugLast = false;
    }
    components = new ArrayList<SearchComponent>( list.size() );
    DebugComponent dbgCmp = null;
    for(String c : list){
      SearchComponent comp = core.getSearchComponent( c );
      if (comp instanceof DebugComponent && makeDebugLast == true){
        dbgCmp = (DebugComponent) comp;
      } else {
        components.add(comp);
        log.info("Adding  component:"+comp);
      }
    }
    if (makeDebugLast == true && dbgCmp != null){
      components.add(dbgCmp);
      log.info("Adding  debug component:" + dbgCmp);
    }
    Object co = initArgs.get(INIT_CONNECTION_TIMEOUT);
    if (co != null) {
      connectionTimeout = (Integer) co;
      log.info("Setting shard-connection-timeout to: " + connectionTimeout);
    }
    Object so = initArgs.get(INIT_SO_TIMEOUT);
    if (so != null) {
      soTimeout = (Integer) so;
      log.info("Setting shard-socket-timeout to: " + soTimeout);
    }
  }
  public List<SearchComponent> getComponents() {
    return components;
  }
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception, ParseException, InstantiationException, IllegalAccessException
  {
    ResponseBuilder rb = new ResponseBuilder();
    rb.req = req;
    rb.rsp = rsp;
    rb.components = components;
    rb.setDebug(req.getParams().getBool(CommonParams.DEBUG_QUERY, false));
    final RTimer timer = rb.isDebug() ? new RTimer() : null;
    if (timer == null) {
      for( SearchComponent c : components ) {
        c.prepare(rb);
      }
    } else {
      RTimer subt = timer.sub( "prepare" );
      for( SearchComponent c : components ) {
        rb.setTimer( subt.sub( c.getName() ) );
        c.prepare(rb);
        rb.getTimer().stop();
      }
      subt.stop();
    }
    if (rb.shards == null) {
      if(!rb.isDebug()) {
        for( SearchComponent c : components ) {
          c.process(rb);
        }
      }
      else {
        RTimer subt = timer.sub( "process" );
        for( SearchComponent c : components ) {
          rb.setTimer( subt.sub( c.getName() ) );
          c.process(rb);
          rb.getTimer().stop();
        }
        subt.stop();
        timer.stop();
        if( rb.getDebugInfo() == null ) {
          rb.setDebugInfo( new SimpleOrderedMap<Object>() );
        }
        rb.getDebugInfo().add( "timing", timer.asNamedList() );
      }
    } else {
      HttpCommComponent comm = new HttpCommComponent();
      if (rb.outgoing == null) {
        rb.outgoing = new LinkedList<ShardRequest>();
      }
      rb.finished = new ArrayList<ShardRequest>();
      int nextStage = 0;
      do {
        rb.stage = nextStage;
        nextStage = ResponseBuilder.STAGE_DONE;
        for( SearchComponent c : components ) {
          nextStage = Math.min(nextStage, c.distributedProcess(rb));
        }
        while (rb.outgoing.size() > 0) {
          while (rb.outgoing.size() > 0) {
            ShardRequest sreq = rb.outgoing.remove(0);
            sreq.actualShards = sreq.shards;
            if (sreq.actualShards==ShardRequest.ALL_SHARDS) {
              sreq.actualShards = rb.shards;
            }
            sreq.responses = new ArrayList<ShardResponse>();
            for (String shard : sreq.actualShards) {
              ModifiableSolrParams params = new ModifiableSolrParams(sreq.params);
              params.remove(ShardParams.SHARDS);      
              params.remove("indent");
              params.remove(CommonParams.HEADER_ECHO_PARAMS);
              params.set(ShardParams.IS_SHARD, true);  
              String shardHandler = req.getParams().get(ShardParams.SHARDS_QT);
              if (shardHandler == null) {
                params.remove(CommonParams.QT);
              } else {
                params.set(CommonParams.QT, shardHandler);
              }
              comm.submit(sreq, shard, params);
            }
          }
          while (rb.outgoing.size() == 0) {
            ShardResponse srsp = comm.takeCompletedOrError();
            if (srsp == null) break;  
            if (srsp.getException() != null) {
              comm.cancelAll();
              if (srsp.getException() instanceof SolrException) {
                throw (SolrException)srsp.getException();
              } else {
                throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, srsp.getException());
              }
            }
            rb.finished.add(srsp.getShardRequest());
            for(SearchComponent c : components) {
              c.handleResponses(rb, srsp.getShardRequest());
            }
          }
        }
        for(SearchComponent c : components) {
            c.finishStage(rb);
         }
      } while (nextStage != Integer.MAX_VALUE);
    }
  }
  @Override
  public String getDescription() {
    StringBuilder sb = new StringBuilder();
    sb.append("Search using components: ");
    if( components != null ) {
      for(SearchComponent c : components){
        sb.append(c.getName());
        sb.append(",");
      }
    }
    return sb.toString();
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: SearchHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/component/SearchHandler.java $";
  }
}
class HttpCommComponent {
  static Executor commExecutor = new ThreadPoolExecutor(
          0,
          Integer.MAX_VALUE,
          5, TimeUnit.SECONDS, 
          new SynchronousQueue<Runnable>()  
  );
  static HttpClient client;
  static {
    MultiThreadedHttpConnectionManager mgr = new MultiThreadedHttpConnectionManager();
    mgr.getParams().setDefaultMaxConnectionsPerHost(20);
    mgr.getParams().setMaxTotalConnections(10000);
    mgr.getParams().setConnectionTimeout(SearchHandler.connectionTimeout);
    mgr.getParams().setSoTimeout(SearchHandler.soTimeout);
    client = new HttpClient(mgr);    
  }
  CompletionService<ShardResponse> completionService = new ExecutorCompletionService<ShardResponse>(commExecutor);
  Set<Future<ShardResponse>> pending = new HashSet<Future<ShardResponse>>();
  HttpCommComponent() {
  }
  private static class SimpleSolrResponse extends SolrResponse {
    long elapsedTime;
    NamedList<Object> nl;
    @Override
    public long getElapsedTime() {
      return elapsedTime;
    }
    @Override
    public NamedList<Object> getResponse() {
      return nl;
    }
    @Override
    public void setResponse(NamedList<Object> rsp) {
      nl = rsp;
    }
  }
  void submit(final ShardRequest sreq, final String shard, final ModifiableSolrParams params) {
    Callable<ShardResponse> task = new Callable<ShardResponse>() {
      public ShardResponse call() throws Exception {
        ShardResponse srsp = new ShardResponse();
        srsp.setShardRequest(sreq);
        srsp.setShard(shard);
        SimpleSolrResponse ssr = new SimpleSolrResponse();
        srsp.setSolrResponse(ssr);
        long startTime = System.currentTimeMillis();
        try {
          String url = "http://" + shard;
          params.remove(CommonParams.WT); 
          params.remove(CommonParams.VERSION);
          SolrServer server = new CommonsHttpSolrServer(url, client);
          QueryRequest req = new QueryRequest(params);
          req.setMethod(SolrRequest.METHOD.POST);
          ssr.nl = server.request(req);
        } catch (Throwable th) {
          srsp.setException(th);
          if (th instanceof SolrException) {
            srsp.setResponseCode(((SolrException)th).code());
          } else {
            srsp.setResponseCode(-1);
          }
        }
        ssr.elapsedTime = System.currentTimeMillis() - startTime;
        return srsp;
      }
    };
    pending.add( completionService.submit(task) );
  }
  ShardResponse take() {
    while (pending.size() > 0) {
      try {
        Future<ShardResponse> future = completionService.take();
        pending.remove(future);
        ShardResponse rsp = future.get();
        rsp.getShardRequest().responses.add(rsp);
        if (rsp.getShardRequest().responses.size() == rsp.getShardRequest().actualShards.length) {
          return rsp;
        }
      } catch (InterruptedException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      } catch (ExecutionException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Impossible Exception",e);
      }
    }
    return null;
  }
  ShardResponse takeCompletedOrError() {
    while (pending.size() > 0) {
      try {
        Future<ShardResponse> future = completionService.take();
        pending.remove(future);
        ShardResponse rsp = future.get();
        if (rsp.getException() != null) return rsp; 
        rsp.getShardRequest().responses.add(rsp);
        if (rsp.getShardRequest().responses.size() == rsp.getShardRequest().actualShards.length) {
          return rsp;
        }
      } catch (InterruptedException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      } catch (ExecutionException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Impossible Exception",e);
      }
    }
    return null;
  }
  void cancelAll() {
    for (Future<ShardResponse> future : pending) {
      future.cancel(true);
    }
  }
}
