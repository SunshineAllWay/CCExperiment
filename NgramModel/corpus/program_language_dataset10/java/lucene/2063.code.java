package org.apache.solr.handler.dataimport;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.wrapAndThrow;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
public class BinFileDataSource extends DataSource<InputStream>{
   protected String basePath;
  public void init(Context context, Properties initProps) {
     basePath = initProps.getProperty(FileDataSource.BASE_PATH);
  }
  public InputStream getData(String query) {
    File f = FileDataSource.getFile(basePath,query);
    try {
      return new FileInputStream(f);
    } catch (FileNotFoundException e) {
      wrapAndThrow(SEVERE,e,"Unable to open file "+f.getAbsolutePath());
      return null;
    }
  }
  public void close() {
  }
}
