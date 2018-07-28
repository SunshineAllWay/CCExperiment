package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.*;
import static org.apache.solr.handler.dataimport.URLDataSource.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
public class BinURLDataSource extends DataSource<InputStream>{
  private static final Logger LOG = LoggerFactory.getLogger(BinURLDataSource.class);
  private String baseUrl;
  private int connectionTimeout = CONNECTION_TIMEOUT;
  private int readTimeout = READ_TIMEOUT;
  private Context context;
  private Properties initProps;
  public BinURLDataSource() { }
  public void init(Context context, Properties initProps) {
      this.context = context;
    this.initProps = initProps;
    baseUrl = getInitPropWithReplacements(BASE_URL);
    String cTimeout = getInitPropWithReplacements(CONNECTION_TIMEOUT_FIELD_NAME);
    String rTimeout = getInitPropWithReplacements(READ_TIMEOUT_FIELD_NAME);
    if (cTimeout != null) {
      try {
        connectionTimeout = Integer.parseInt(cTimeout);
      } catch (NumberFormatException e) {
        LOG.warn("Invalid connection timeout: " + cTimeout);
      }
    }
    if (rTimeout != null) {
      try {
        readTimeout = Integer.parseInt(rTimeout);
      } catch (NumberFormatException e) {
        LOG.warn("Invalid read timeout: " + rTimeout);
      }
    }
  }
  public InputStream getData(String query) {
    URL url = null;
    try {
      if (URIMETHOD.matcher(query).find()) url = new URL(query);
      else url = new URL(baseUrl + query);
      LOG.debug("Accessing URL: " + url.toString());
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(connectionTimeout);
      conn.setReadTimeout(readTimeout);
      return conn.getInputStream();
    } catch (Exception e) {
      LOG.error("Exception thrown while getting data", e);
      wrapAndThrow (SEVERE, e, "Exception in invoking url " + url);
      return null;
    }
  }
  public void close() { }
  private String getInitPropWithReplacements(String propertyName) {
    final String expr = initProps.getProperty(propertyName);
    if (expr == null) {
      return null;
    }
    return context.replaceTokens(expr);
  }
}
