package org.apache.solr.servlet.cache;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrConfig.HttpCachingConfig.LastModFrom;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.commons.codec.binary.Base64;
public final class HttpCacheHeaderUtil {
  public static void sendNotModified(HttpServletResponse res)
    throws IOException {
    res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
  }
  public static void sendPreconditionFailed(HttpServletResponse res)
    throws IOException {
    res.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);
  }
  private static Map<SolrCore, EtagCacheVal> etagCoreCache
    = new WeakHashMap<SolrCore, EtagCacheVal>();
  private static class EtagCacheVal {
    private final String etagSeed;
    private String etagCache = null;
    private long indexVersionCache=-1;
    public EtagCacheVal(final String etagSeed) {
      this.etagSeed = etagSeed;
    }
    public String calcEtag(final long currentIndexVersion) {
      if (currentIndexVersion != indexVersionCache) {
        indexVersionCache=currentIndexVersion;
        etagCache = "\""
          + new String(Base64.encodeBase64((Long.toHexString
                                            (Long.reverse(indexVersionCache))
                                            + etagSeed).getBytes()))
          + "\"";
      }
      return etagCache;
    }
  }
  public static String calcEtag(final SolrQueryRequest solrReq) {
    final SolrCore core = solrReq.getCore();
    final long currentIndexVersion
      = solrReq.getSearcher().getReader().getVersion();
    EtagCacheVal etagCache = etagCoreCache.get(core);
    if (null == etagCache) {
      final String etagSeed
        = core.getSolrConfig().getHttpCachingConfig().getEtagSeed();
      etagCache = new EtagCacheVal(etagSeed);
      etagCoreCache.put(core, etagCache);
    }
    return etagCache.calcEtag(currentIndexVersion);
  }
  public static boolean isMatchingEtag(final List<String> headerList,
      final String etag) {
    for (String header : headerList) {
      final String[] headerEtags = header.split(",");
      for (String s : headerEtags) {
        s = s.trim();
        if (s.equals(etag) || "*".equals(s)) {
          return true;
        }
      }
    }
    return false;
  }
  public static long calcLastModified(final SolrQueryRequest solrReq) {
    final SolrCore core = solrReq.getCore();
    final SolrIndexSearcher searcher = solrReq.getSearcher();
    final LastModFrom lastModFrom
      = core.getSolrConfig().getHttpCachingConfig().getLastModFrom();
    long lastMod;
    try {
      lastMod =
        LastModFrom.DIRLASTMOD == lastModFrom
        ? IndexReader.lastModified(searcher.getReader().directory())
        : searcher.getOpenTime();
    } catch (IOException e) {
      throw new SolrException(ErrorCode.SERVER_ERROR, e);
    }
    return lastMod - (lastMod % 1000L);
  }
  public static void setCacheControlHeader(final SolrConfig conf,
                                           final HttpServletResponse resp, final Method method) {
    if (Method.POST==method || Method.OTHER==method) {
      return;
    }
    final String cc = conf.getHttpCachingConfig().getCacheControlHeader();
    if (null != cc) {
      resp.setHeader("Cache-Control", cc);
    }
    Long maxAge = conf.getHttpCachingConfig().getMaxAge();
    if (null != maxAge) {
      resp.setDateHeader("Expires", System.currentTimeMillis()
                         + (maxAge * 1000L));
    }
    return;
  }
  public static boolean doCacheHeaderValidation(final SolrQueryRequest solrReq,
                                                final HttpServletRequest req,
                                                final Method reqMethod,
                                                final HttpServletResponse resp)
    throws IOException {
    if (Method.POST==reqMethod || Method.OTHER==reqMethod) {
      return false;
    }
    final long lastMod = HttpCacheHeaderUtil.calcLastModified(solrReq);
    final String etag = HttpCacheHeaderUtil.calcEtag(solrReq);
    resp.setDateHeader("Last-Modified", lastMod);
    resp.setHeader("ETag", etag);
    if (checkETagValidators(req, resp, reqMethod, etag)) {
      return true;
    }
    if (checkLastModValidators(req, resp, lastMod)) {
      return true;
    }
    return false;
  }
  @SuppressWarnings("unchecked")
  public static boolean checkETagValidators(final HttpServletRequest req,
                                            final HttpServletResponse resp,
                                            final Method reqMethod,
                                            final String etag)
    throws IOException {
    final List<String> ifNoneMatchList = Collections.list(req
        .getHeaders("If-None-Match"));
    if (ifNoneMatchList.size() > 0 && isMatchingEtag(ifNoneMatchList, etag)) {
      if (reqMethod == Method.GET || reqMethod == Method.HEAD) {
        sendNotModified(resp);
      } else {
        sendPreconditionFailed(resp);
      }
      return true;
    }
    final List<String> ifMatchList = Collections.list(req
        .getHeaders("If-Match"));
    if (ifMatchList.size() > 0 && !isMatchingEtag(ifMatchList, etag)) {
      sendPreconditionFailed(resp);
      return true;
    }
    return false;
  }
  public static boolean checkLastModValidators(final HttpServletRequest req,
                                               final HttpServletResponse resp,
                                               final long lastMod)
    throws IOException {
    try {
      final long modifiedSince = req.getDateHeader("If-Modified-Since");
      if (modifiedSince != -1L && lastMod <= modifiedSince) {
        sendNotModified(resp);
        return true;
      }
      final long unmodifiedSince = req.getDateHeader("If-Unmodified-Since");
      if (unmodifiedSince != -1L && lastMod > unmodifiedSince) {
        sendPreconditionFailed(resp);
        return true;
      }
    } catch (IllegalArgumentException iae) {
    }
    return false;
  }
  public static void checkHttpCachingVeto(final SolrQueryResponse solrRsp,
      HttpServletResponse resp, final Method reqMethod) {
    if (Method.POST == reqMethod || Method.OTHER == reqMethod) {
      return;
    }
    if (solrRsp.isHttpCaching() && solrRsp.getException() == null) {
      return;
    }
    resp.setHeader("Cache-Control", "no-cache, no-store");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "Sat, 01 Jan 2000 01:00:00 GMT");
    resp.setDateHeader("Last-Modified", System.currentTimeMillis());
    resp.setHeader("ETag", '"'+Long.toHexString(System.currentTimeMillis())+'"');
  } 
}
