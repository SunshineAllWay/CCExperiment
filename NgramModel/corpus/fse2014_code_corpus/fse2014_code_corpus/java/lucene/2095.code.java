package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.XPathEntityProcessor.URL;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
public class PlainTextEntityProcessor extends EntityProcessorBase {
  private static final Logger LOG = LoggerFactory.getLogger(PlainTextEntityProcessor.class);
  private boolean ended = false;
  public void init(Context context) {
    super.init(context);
    ended = false;
  }
  public Map<String, Object> nextRow() {
    if (ended) return null;
    DataSource<Reader> ds = context.getDataSource();
    String url = context.replaceTokens(context.getEntityAttribute(URL));
    Reader r = null;
    try {
      r = ds.getData(url);
    } catch (Exception e) {
      wrapAndThrow(SEVERE, e, "Exception reading url : " + url);
    }
    StringWriter sw = new StringWriter();
    char[] buf = new char[1024];
    while (true) {
      int len = 0;
      try {
        len = r.read(buf);
      } catch (IOException e) {
        IOUtils.closeQuietly(r);
        wrapAndThrow(SEVERE, e, "Exception reading url : " + url);
      }
      if (len <= 0) break;
      sw.append(new String(buf, 0, len));
    }
    Map<String, Object> row = new HashMap<String, Object>();
    row.put(PLAIN_TEXT, sw.toString());
    ended = true;
    IOUtils.closeQuietly(r);
    return row;
  }
  public static final String PLAIN_TEXT = "plainText";
}
