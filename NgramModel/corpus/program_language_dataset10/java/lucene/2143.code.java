package org.apache.solr.handler.extraction;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.handler.ContentStreamHandlerBase;
import org.apache.solr.handler.ContentStreamLoader;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
public class ExtractingRequestHandler extends ContentStreamHandlerBase implements SolrCoreAware {
  private transient static Logger log = LoggerFactory.getLogger(ExtractingRequestHandler.class);
  public static final String CONFIG_LOCATION = "tika.config";
  public static final String DATE_FORMATS = "date.formats";
  protected TikaConfig config;
  protected Collection<String> dateFormats = DateUtil.DEFAULT_DATE_FORMATS;
  protected SolrContentHandlerFactory factory;
  @Override
  public void init(NamedList args) {
    super.init(args);
  }
  public void inform(SolrCore core) {
    if (initArgs != null) {
      String tikaConfigLoc = (String) initArgs.get(CONFIG_LOCATION);
      if (tikaConfigLoc != null) {
        File configFile = new File(tikaConfigLoc);
        if (configFile.isAbsolute() == false) {
          configFile = new File(core.getResourceLoader().getConfigDir(), configFile.getPath());
        }
        try {
          config = new TikaConfig(configFile);
        } catch (Exception e) {
          throw new SolrException(ErrorCode.SERVER_ERROR, e);
        }
      } else {
        config = TikaConfig.getDefaultConfig();
      }
      NamedList configDateFormats = (NamedList) initArgs.get(DATE_FORMATS);
      if (configDateFormats != null && configDateFormats.size() > 0) {
        dateFormats = new HashSet<String>();
        Iterator<Map.Entry> it = configDateFormats.iterator();
        while (it.hasNext()) {
          String format = (String) it.next().getValue();
          log.info("Adding Date Format: " + format);
          dateFormats.add(format);
        }
      }
    } else {
      config = TikaConfig.getDefaultConfig();
    }
    factory = createFactory();
  }
  protected SolrContentHandlerFactory createFactory() {
    return new SolrContentHandlerFactory(dateFormats);
  }
  protected ContentStreamLoader newLoader(SolrQueryRequest req, UpdateRequestProcessor processor) {
    return new ExtractingDocumentLoader(req, processor, config, factory);
  }
  @Override
  public String getDescription() {
    return "Add/Update Rich document";
  }
  @Override
  public String getVersion() {
    return "$Revision:$";
  }
  @Override
  public String getSourceId() {
    return "$Id:$";
  }
  @Override
  public String getSource() {
    return "$URL:$";
  }
}
