package org.apache.solr.handler.dataimport;
import java.io.*;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
public class FileDataSource extends DataSource<Reader> {
  public static final String BASE_PATH = "basePath";
  protected String basePath;
  protected String encoding = null;
  private static final Logger LOG = LoggerFactory.getLogger(FileDataSource.class);
  public void init(Context context, Properties initProps) {
    basePath = initProps.getProperty(BASE_PATH);
    if (initProps.get(URLDataSource.ENCODING) != null)
      encoding = initProps.getProperty(URLDataSource.ENCODING);
  }
  public Reader getData(String query) {
    File f = getFile(basePath,query);
    try {
      return openStream(f);
    } catch (Exception e) {
      wrapAndThrow(SEVERE,e,"Unable to open File : "+f.getAbsolutePath());
      return null;
    }
  }
  static File getFile(String basePath, String query) {
    try {
      File file0 = new File(query);
      File file = file0;
      if (!file.isAbsolute())
        file = new File(basePath + query);
      if (file.isFile() && file.canRead()) {
        LOG.debug("Accessing File: " + file.toString());
        return file;
      } else if (file != file0)
        if (file0.isFile() && file0.canRead()) {
          LOG.debug("Accessing File0: " + file0.toString());
          return  file0;
        }
      throw new FileNotFoundException("Could not find file: " + query);
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
  protected Reader openStream(File file) throws FileNotFoundException,
          UnsupportedEncodingException {
    if (encoding == null) {
      return new InputStreamReader(new FileInputStream(file));
    } else {
      return new InputStreamReader(new FileInputStream(file), encoding);
    }
  }
  public void close() {
  }
}
