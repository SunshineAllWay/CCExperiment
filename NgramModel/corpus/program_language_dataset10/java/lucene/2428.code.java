package org.apache.solr.response;
import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.xslt.TransformerProvider;
public class XSLTResponseWriter implements QueryResponseWriter {
  public static final String DEFAULT_CONTENT_TYPE = "text/xml";
  public static final String TRANSFORM_PARAM = "tr";
  public static final String CONTEXT_TRANSFORMER_KEY = "xsltwriter.transformer";
  private Integer xsltCacheLifetimeSeconds = null; 
  public static final int XSLT_CACHE_DEFAULT = 60;
  private static final String XSLT_CACHE_PARAM = "xsltCacheLifetimeSeconds"; 
  private static final Logger log = LoggerFactory.getLogger(XSLTResponseWriter.class);
  public void init(NamedList n) {
      final SolrParams p = SolrParams.toSolrParams(n);
      xsltCacheLifetimeSeconds = p.getInt(XSLT_CACHE_PARAM,XSLT_CACHE_DEFAULT);
      log.info("xsltCacheLifetimeSeconds=" + xsltCacheLifetimeSeconds);
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    Transformer t = null;
    try {
      t = getTransformer(request);
    } catch(Exception e) {
      throw new RuntimeException("getTransformer fails in getContentType",e);
    }
    final String mediaTypeFromXslt = t.getOutputProperty("media-type");
    if(mediaTypeFromXslt == null || mediaTypeFromXslt.length()==0) {
      return DEFAULT_CONTENT_TYPE;
    }
    return mediaTypeFromXslt;
  }
  public void write(Writer writer, SolrQueryRequest request, SolrQueryResponse response) throws IOException {
    final Transformer t = getTransformer(request);
    final CharArrayWriter w = new CharArrayWriter();
    XMLWriter.writeResponse(w,request,response);
    final Reader r = new BufferedReader(new CharArrayReader(w.toCharArray()));
    final StreamSource source = new StreamSource(r);
    final StreamResult result = new StreamResult(writer);
    try {
      t.transform(source, result);
    } catch(TransformerException te) {
      final IOException ioe = new IOException("XSLT transformation error");
      ioe.initCause(te);
      throw ioe;
    }
  }
  protected Transformer getTransformer(SolrQueryRequest request) throws IOException {
    final String xslt = request.getParams().get(TRANSFORM_PARAM,null);
    if(xslt==null) {
      throw new IOException("'" + TRANSFORM_PARAM + "' request parameter is required to use the XSLTResponseWriter");
    }
    SolrConfig solrConfig = request.getCore().getSolrConfig();
    final Map<Object,Object> ctx = request.getContext();
    Transformer result = (Transformer)ctx.get(CONTEXT_TRANSFORMER_KEY);
    if(result==null) {
      result = TransformerProvider.instance.getTransformer(solrConfig, xslt,xsltCacheLifetimeSeconds.intValue());
      ctx.put(CONTEXT_TRANSFORMER_KEY,result);
    }
    return result;
  }
}
