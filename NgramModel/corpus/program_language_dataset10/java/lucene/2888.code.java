package org.apache.solr.servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.WeakHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.core.*;
import org.apache.solr.request.*;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.cache.HttpCacheHeaderUtil;
import org.apache.solr.servlet.cache.Method;
public class SolrDispatchFilter implements Filter
{
  final Logger log = LoggerFactory.getLogger(SolrDispatchFilter.class);
  protected CoreContainer cores;
  protected String pathPrefix = null; 
  protected String abortErrorMessage = null;
  protected String solrConfigFilename = null;
  protected final Map<SolrConfig, SolrRequestParsers> parsers = new WeakHashMap<SolrConfig, SolrRequestParsers>();
  protected final SolrRequestParsers adminRequestParser;
  public SolrDispatchFilter() {
    try {
      adminRequestParser = new SolrRequestParsers(new Config(null,"solr",new ByteArrayInputStream("<root/>".getBytes()),"") );
    } catch (Exception e) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,e);
    }
  }
  public void init(FilterConfig config) throws ServletException
  {
    log.info("SolrDispatchFilter.init()");
    boolean abortOnConfigurationError = true;
    CoreContainer.Initializer init = createInitializer();
    try {
      this.pathPrefix = config.getInitParameter( "path-prefix" );
      init.setSolrConfigFilename(config.getInitParameter("solrconfig-filename"));
      this.cores = init.initialize();
      abortOnConfigurationError = init.isAbortOnConfigurationError();
      log.info("user.dir=" + System.getProperty("user.dir"));
    }
    catch( Throwable t ) {
      log.error( "Could not start Solr. Check solr/home property", t);
      SolrConfig.severeErrors.add( t );
      SolrCore.log( t );
    }
    if( abortOnConfigurationError && SolrConfig.severeErrors.size() > 0 ) {
      StringWriter sw = new StringWriter();
      PrintWriter out = new PrintWriter( sw );
      out.println( "Severe errors in solr configuration.\n" );
      out.println( "Check your log files for more detailed information on what may be wrong.\n" );
      out.println( "If you want solr to continue after configuration errors, change: \n");
      out.println( " <abortOnConfigurationError>false</abortOnConfigurationError>\n" );
      out.println( "in "+init.getSolrConfigFilename()+"\n" );
      for( Throwable t : SolrConfig.severeErrors ) {
        out.println( "-------------------------------------------------------------" );
        t.printStackTrace( out );
      }
      out.flush();
      abortErrorMessage = sw.toString();
    }
    log.info("SolrDispatchFilter.init() done");
  }
  protected CoreContainer.Initializer createInitializer() {
    return new CoreContainer.Initializer();
  }
  public void destroy() {
    if (cores != null) {
      cores.shutdown();
      cores = null;
    }    
  }
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if( abortErrorMessage != null ) {
      ((HttpServletResponse)response).sendError( 500, abortErrorMessage );
      return;
    }
    if( request instanceof HttpServletRequest) {
      HttpServletRequest req = (HttpServletRequest)request;
      HttpServletResponse resp = (HttpServletResponse)response;
      SolrRequestHandler handler = null;
      SolrQueryRequest solrReq = null;
      SolrCore core = null;
      String corename = "";
      try {
        req.setAttribute("org.apache.solr.CoreContainer", cores);
        String path = req.getServletPath();
        if( req.getPathInfo() != null ) {
          path += req.getPathInfo();
        }
        if( pathPrefix != null && path.startsWith( pathPrefix ) ) {
          path = path.substring( pathPrefix.length() );
        }
        String alternate = cores.getManagementPath();
        if (alternate != null && path.startsWith(alternate)) {
          path = path.substring(0, alternate.length());
        }
        int idx = path.indexOf( ':' );
        if( idx > 0 ) {
          path = path.substring( 0, idx );
        }
        if( path.equals( cores.getAdminPath() ) ) {
          handler = cores.getMultiCoreHandler();
          solrReq =  adminRequestParser.parse(null,path, req);
          handleAdminRequest(req, response, handler, solrReq);
          return;
        }
        else {
          idx = path.indexOf( "/", 1 );
          if( idx > 1 ) {
            corename = path.substring( 1, idx );
            core = cores.getCore(corename);
            if (core != null) {
              path = path.substring( idx );
            }
          }
          if (core == null) {
            corename = "";
            core = cores.getCore("");
          }
        }
        if( core != null ) {
          final SolrConfig config = core.getSolrConfig();
          SolrRequestParsers parser = null;
          parser = parsers.get(config);
          if( parser == null ) {
            parser = new SolrRequestParsers(config);
            parsers.put(config, parser );
          }
          if( handler == null && path.length() > 1 ) { 
            handler = core.getRequestHandler( path );
            if( handler == null && parser.isHandleSelect() ) {
              if( "/select".equals( path ) || "/select/".equals( path ) ) {
                solrReq = parser.parse( core, path, req );
                String qt = solrReq.getParams().get( CommonParams.QT );
                handler = core.getRequestHandler( qt );
                if( handler == null ) {
                  throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "unknown handler: "+qt);
                }
              }
            }
          }
          if( handler != null ) {
            if( solrReq == null ) {
              solrReq = parser.parse( core, path, req );
            }
            final Method reqMethod = Method.getMethod(req.getMethod());
            HttpCacheHeaderUtil.setCacheControlHeader(config, resp, reqMethod);
            if (config.getHttpCachingConfig().isNever304() ||
                !HttpCacheHeaderUtil.doCacheHeaderValidation(solrReq, req, reqMethod, resp)) {
                SolrQueryResponse solrRsp = new SolrQueryResponse();
                this.execute( req, handler, solrReq, solrRsp );
                HttpCacheHeaderUtil.checkHttpCachingVeto(solrRsp, resp, reqMethod);
               QueryResponseWriter responseWriter = core.getQueryResponseWriter(solrReq);
              writeResponse(solrRsp, response, responseWriter, solrReq, reqMethod);
            }
            return; 
          }
          else {
            req.setAttribute("org.apache.solr.SolrCore", core);
            if( path.startsWith( "/admin" ) ) {
              req.getRequestDispatcher( pathPrefix == null ? path : pathPrefix + path ).forward( request, response );
              return;
            }
          }
        }
        log.debug("no handler or core retrieved for " + path + ", follow through...");
      } 
      catch (Throwable ex) {
        sendError( (HttpServletResponse)response, ex );
        return;
      } 
      finally {
        if( solrReq != null ) {
          solrReq.close();
        }
        if (core != null) {
          core.close();
        }
      }
    }
    chain.doFilter(request, response);
  }
  private void handleAdminRequest(HttpServletRequest req, ServletResponse response, SolrRequestHandler handler,
                                  SolrQueryRequest solrReq) throws IOException {
    SolrQueryResponse solrResp = new SolrQueryResponse();
    final NamedList<Object> responseHeader = new SimpleOrderedMap<Object>();
    solrResp.add("responseHeader", responseHeader);
    NamedList toLog = solrResp.getToLog();
    toLog.add("webapp", req.getContextPath());
    toLog.add("path", solrReq.getContext().get("path"));
    toLog.add("params", "{" + solrReq.getParamString() + "}");
    handler.handleRequest(solrReq, solrResp);
    SolrCore.setResponseHeaderValues(handler, solrReq, solrResp);
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < toLog.size(); i++) {
      String name = toLog.getName(i);
      Object val = toLog.getVal(i);
      sb.append(name).append("=").append(val).append(" ");
    }
    QueryResponseWriter respWriter = SolrCore.DEFAULT_RESPONSE_WRITERS.get(solrReq.getParams().get(CommonParams.WT));
    if (respWriter == null) respWriter = SolrCore.DEFAULT_RESPONSE_WRITERS.get("standard");
    writeResponse(solrResp, response, respWriter, solrReq, Method.getMethod(req.getMethod()));
  }
  private void writeResponse(SolrQueryResponse solrRsp, ServletResponse response,
                             QueryResponseWriter responseWriter, SolrQueryRequest solrReq, Method reqMethod)
          throws IOException {
    if (solrRsp.getException() != null) {
      sendError((HttpServletResponse) response, solrRsp.getException());
    } else {
      response.setContentType(responseWriter.getContentType(solrReq, solrRsp));
      if (Method.HEAD != reqMethod) {
        if (responseWriter instanceof BinaryQueryResponseWriter) {
          BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter) responseWriter;
          binWriter.write(response.getOutputStream(), solrReq, solrRsp);
        } else {
          PrintWriter out = response.getWriter();
          responseWriter.write(out, solrReq, solrRsp);
        }
      }
    }
  }
  protected void execute( HttpServletRequest req, SolrRequestHandler handler, SolrQueryRequest sreq, SolrQueryResponse rsp) {
    sreq.getContext().put( "webapp", req.getContextPath() );
    sreq.getCore().execute( handler, sreq, rsp );
  }
  protected void sendError(HttpServletResponse res, Throwable ex) throws IOException {
    int code=500;
    String trace = "";
    if( ex instanceof SolrException ) {
      code = ((SolrException)ex).code();
    }
    if( code == 500 || code < 100 ) {
      StringWriter sw = new StringWriter();
      ex.printStackTrace(new PrintWriter(sw));
      trace = "\n\n"+sw.toString();
      SolrException.logOnce(log,null,ex );
      if( code < 100 ) {
        log.warn( "invalid return code: "+code );
        code = 500;
      }
    }
    res.sendError( code, ex.getMessage() + trace );
  }
  public void setPathPrefix(String pathPrefix) {
    this.pathPrefix = pathPrefix;
  }
  public String getPathPrefix() {
    return pathPrefix;
  }
}
