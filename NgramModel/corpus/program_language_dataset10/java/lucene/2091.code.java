package org.apache.solr.handler.dataimport;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
public class LineEntityProcessor extends EntityProcessorBase {
  private Pattern acceptLineRegex, skipLineRegex;
  private String url;
  private BufferedReader reader;
  public void init(Context context) {
    super.init(context);
    String s;
    s = context.getResolvedEntityAttribute(ACCEPT_LINE_REGEX);
    if (s != null) {
      acceptLineRegex = Pattern.compile(s);
    }
    s = context.getResolvedEntityAttribute(SKIP_LINE_REGEX);
    if (s != null) {
      skipLineRegex = Pattern.compile(s);
    }
    url = context.getResolvedEntityAttribute(URL);
    if (url == null) throw
      new DataImportHandlerException(DataImportHandlerException.SEVERE,
           "'"+ URL +"' is a required attribute");
  }
  public Map<String, Object> nextRow() {
    if (reader == null) {
      reader = new BufferedReader((Reader) context.getDataSource().getData(url));
    }
    String line;
    while ( true ) { 
      try {
        line = reader.readLine();
      }
      catch (IOException exp) {
        throw new DataImportHandlerException(DataImportHandlerException.SEVERE,
             "Problem reading from input", exp);
      }
      if (line == null) return null; 
      if (acceptLineRegex != null && ! acceptLineRegex.matcher(line).find()) continue;
      if (skipLineRegex != null &&   skipLineRegex.matcher(line).find()) continue;
      Map<String, Object> row = new HashMap<String, Object>();
      row.put("rawLine", line);
      return row;
    }
  }
    @Override
    public void destroy() {
      if (reader != null) {
        IOUtils.closeQuietly(reader);
      }
      reader= null;
      super.destroy();
    }
  public static final String URL = "url";
  public static final String ACCEPT_LINE_REGEX = "acceptLineRegex";
  public static final String SKIP_LINE_REGEX = "skipLineRegex";
}
