package org.apache.solr.handler.dataimport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class URLDataSource extends DataSource<Reader> {
  Logger LOG = LoggerFactory.getLogger(URLDataSource.class);
  private String baseUrl;
  private String encoding;
  private int connectionTimeout = CONNECTION_TIMEOUT;
  private int readTimeout = READ_TIMEOUT;
  private Context context;
  private Properties initProps;
  public URLDataSource() {
  }
  public void init(Context context, Properties initProps) {
    this.context = context;
    this.initProps = initProps;
    baseUrl = getInitPropWithReplacements(BASE_URL);
    if (getInitPropWithReplacements(ENCODING) != null)
      encoding = getInitPropWithReplacements(ENCODING);
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
  public Reader getData(String query) {
    URL url = null;
    try {
      if (URIMETHOD.matcher(query).find()) url = new URL(query);
      else url = new URL(baseUrl + query);
      LOG.debug("Accessing URL: " + url.toString());
      URLConnection conn = url.openConnection();
      conn.setConnectTimeout(connectionTimeout);
      conn.setReadTimeout(readTimeout);
      InputStream in = conn.getInputStream();
      String enc = encoding;
      if (enc == null) {
        String cType = conn.getContentType();
        if (cType != null) {
          Matcher m = CHARSET_PATTERN.matcher(cType);
          if (m.find()) {
            enc = m.group(1);
          }
        }
      }
      if (enc == null)
        enc = UTF_8;
      DataImporter.QUERY_COUNT.get().incrementAndGet();
      return new InputStreamReader(in, enc);
    } catch (Exception e) {
      LOG.error("Exception thrown while getting data", e);
      throw new DataImportHandlerException(DataImportHandlerException.SEVERE,
              "Exception in invoking url " + url, e);
    }
  }
  public void close() {
  }
  public String getBaseUrl() {
    return baseUrl;
  }
  private String getInitPropWithReplacements(String propertyName) {
    final String expr = initProps.getProperty(propertyName);
    if (expr == null) {
      return null;
    }
    return context.replaceTokens(expr);
  }
  static final Pattern URIMETHOD = Pattern.compile("\\w{3,}:/");
  private static final Pattern CHARSET_PATTERN = Pattern.compile(".*?charset=(.*)$", Pattern.CASE_INSENSITIVE);
  public static final String ENCODING = "encoding";
  public static final String BASE_URL = "baseUrl";
  public static final String UTF_8 = "UTF-8";
  public static final String CONNECTION_TIMEOUT_FIELD_NAME = "connectionTimeout";
  public static final String READ_TIMEOUT_FIELD_NAME = "readTimeout";
  public static final int CONNECTION_TIMEOUT = 5000;
  public static final int READ_TIMEOUT = 10000;
}
