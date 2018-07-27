package org.apache.solr.util.xslt;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.solr.common.ResourceLoader;
import org.apache.solr.core.SolrConfig;
public class TransformerProvider {
  public static TransformerProvider instance = new TransformerProvider();
  private final TransformerFactory tFactory = TransformerFactory.newInstance();
  private String lastFilename;
  private Templates lastTemplates = null;
  private long cacheExpires = 0;
  private static Logger log;
  private TransformerProvider() {
    log = LoggerFactory.getLogger(TransformerProvider.class.getName());
    log.warn(
        "The TransformerProvider's simplistic XSLT caching mechanism is not appropriate "
        + "for high load scenarios, unless a single XSLT transform is used"
        + " and xsltCacheLifetimeSeconds is set to a sufficiently high value."
    );
  }
  public synchronized Transformer getTransformer(SolrConfig solrConfig, String filename,int cacheLifetimeSeconds) throws IOException {
    if(lastTemplates!=null && filename.equals(lastFilename) && System.currentTimeMillis() < cacheExpires) {
      if(log.isDebugEnabled()) {
        log.debug("Using cached Templates:" + filename);
      }
    } else {
      lastTemplates = getTemplates(solrConfig.getResourceLoader(), filename,cacheLifetimeSeconds);
    }
    Transformer result = null;
    try {
      result = lastTemplates.newTransformer();
    } catch(TransformerConfigurationException tce) {
      log.error(getClass().getName(), "getTransformer", tce);
      final IOException ioe = new IOException("newTransformer fails ( " + lastFilename + ")");
      ioe.initCause(tce);
      throw ioe;
    }
    return result;
  }
  private Templates getTemplates(ResourceLoader loader, String filename,int cacheLifetimeSeconds) throws IOException {
    Templates result = null;
    lastFilename = null;
    try {
      if(log.isDebugEnabled()) {
        log.debug("compiling XSLT templates:" + filename);
      }
      final InputStream xsltStream = loader.openResource("xslt/" + filename);
      result = tFactory.newTemplates(new StreamSource(xsltStream));
    } catch (Exception e) {
      log.error(getClass().getName(), "newTemplates", e);
      final IOException ioe = new IOException("Unable to initialize Templates '" + filename + "'");
      ioe.initCause(e);
      throw ioe;
    }
    lastFilename = filename;
    lastTemplates = result;
    cacheExpires = System.currentTimeMillis() + (cacheLifetimeSeconds * 1000);
    return result;
  }
}
