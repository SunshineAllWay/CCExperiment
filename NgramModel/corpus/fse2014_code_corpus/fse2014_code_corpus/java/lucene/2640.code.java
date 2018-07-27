package org.apache.solr.client.solrj.impl;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.UpdateParams;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class StreamingUpdateSolrServer extends CommonsHttpSolrServer
{
  static final Logger log = LoggerFactory.getLogger( StreamingUpdateSolrServer.class );
  final BlockingQueue<UpdateRequest> queue;
  final ExecutorService scheduler = Executors.newCachedThreadPool();
  final String updateUrl = "/update";
  final Queue<Runner> runners;
  volatile CountDownLatch lock = null;  
  final int threadCount;
  public StreamingUpdateSolrServer(String solrServerUrl, int queueSize, int threadCount) throws MalformedURLException {
    this(solrServerUrl, null, queueSize, threadCount);
  }
  public StreamingUpdateSolrServer(String solrServerUrl, HttpClient client, int queueSize, int threadCount) throws MalformedURLException {
    super(solrServerUrl, client);
    queue = new LinkedBlockingQueue<UpdateRequest>(queueSize);
    this.threadCount = threadCount;
    runners = new LinkedList<Runner>();
  }
  class Runner implements Runnable {
    final Lock runnerLock = new ReentrantLock();
    public void run() {
      runnerLock.lock();
      log.info( "starting runner: {}" , this );
      PostMethod method = null;
      try {
        do {
        RequestEntity request = new RequestEntity() {
          public long getContentLength() { return -1; }
          public String getContentType() { return ClientUtils.TEXT_XML; }
          public boolean isRepeatable()  { return false; }
          public void writeRequest(OutputStream out) throws IOException {
            try {
              OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8");
              writer.append( "<stream>" ); 
              UpdateRequest req = queue.poll( 250, TimeUnit.MILLISECONDS );
              while( req != null ) {
                log.debug( "sending: {}" , req );
                req.writeXML( writer ); 
                SolrParams params = req.getParams();
                if( params != null ) {
                  String fmt = null;
                  if( params.getBool( UpdateParams.OPTIMIZE, false ) ) {
                    fmt = "<optimize waitSearcher=\"%s\" waitFlush=\"%s\" />";
                  }
                  else if( params.getBool( UpdateParams.COMMIT, false ) ) {
                    fmt = "<commit waitSearcher=\"%s\" waitFlush=\"%s\" />";
                  }
                  if( fmt != null ) {
                    log.info( fmt );
                    writer.write( String.format( fmt, 
                        params.getBool( UpdateParams.WAIT_SEARCHER, false )+"",
                        params.getBool( UpdateParams.WAIT_FLUSH, false )+"") );
                  }
                }
                writer.flush();
                req = queue.poll( 250, TimeUnit.MILLISECONDS );
              }
              writer.append( "</stream>" );
              writer.flush();
            }
            catch (InterruptedException e) {
              e.printStackTrace();
            }
          }
        };
        method = new PostMethod(_baseURL+updateUrl );
        method.setRequestEntity( request );
        method.setFollowRedirects( false );
        method.addRequestHeader( "User-Agent", AGENT );
        int statusCode = getHttpClient().executeMethod(method);
        if (statusCode != HttpStatus.SC_OK) {
          StringBuilder msg = new StringBuilder();
          msg.append( method.getStatusLine().getReasonPhrase() );
          msg.append( "\n\n" );
          msg.append( method.getStatusText() );
          msg.append( "\n\n" );
          msg.append( "request: "+method.getURI() );
          handleError( new Exception( msg.toString() ) );
        }
        }  while( ! queue.isEmpty());
      }
      catch (Throwable e) {
        handleError( e );
      } 
      finally {
        try {
          if(method != null)
          method.releaseConnection();
        }
        catch( Exception ex ){}
        synchronized (runners) {
          runners.remove( this );
        }
        log.info( "finished: {}" , this );
        runnerLock.unlock();
      }
    }
  }
  @Override
  public NamedList<Object> request( final SolrRequest request ) throws SolrServerException, IOException
  {
    if( !(request instanceof UpdateRequest) ) {
      return super.request( request );
    }
    UpdateRequest req = (UpdateRequest)request;
    if( req.getDocuments()==null || req.getDocuments().isEmpty() ) {
      blockUntilFinished();
      return super.request( request );
    }
    SolrParams params = req.getParams();
    if( params != null ) {
      if( params.getBool( UpdateParams.WAIT_SEARCHER, false ) ) {
        log.info( "blocking for commit/optimize" );
        blockUntilFinished();  
        return super.request( request );
      }
    }
    try {
      CountDownLatch tmpLock = lock;
      if( tmpLock != null ) {
        tmpLock.await();
      }
      queue.put( req );
        synchronized( runners ) {
      if( runners.isEmpty() 
        || (queue.remainingCapacity() < queue.size() 
         && runners.size() < threadCount) ) 
      {
          Runner r = new Runner();
          scheduler.execute( r );
          runners.add( r );
        }
      }
    } 
    catch (InterruptedException e) {
      log.error( "interrupted", e );
      throw new IOException( e.getLocalizedMessage() );
    }
    NamedList<Object> dummy = new NamedList<Object>();
    dummy.add( "NOTE", "the request is processed in a background stream" );
    return dummy;
  }
  public synchronized void blockUntilFinished()
  {
    lock = new CountDownLatch(1);
    try {
      Runner runner = runners.peek();
      while( runner != null ) {
        runner.runnerLock.lock();
        runner.runnerLock.unlock();
        runner = runners.peek();
      }
    } finally {
      lock.countDown();
      lock=null;
    }
  }
  public void handleError( Throwable ex )
  {
    log.error( "error", ex );
  }
}
