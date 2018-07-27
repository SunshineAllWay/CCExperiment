package org.apache.solr.handler.dataimport;
import org.apache.solr.common.util.ContentStream;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
public class BinContentStreamDataSource extends DataSource<InputStream> {
  private ContextImpl context;
  private ContentStream contentStream;
  private InputStream in;
  public void init(Context context, Properties initProps) {
    this.context = (ContextImpl) context;
  }
  public InputStream getData(String query) {
     contentStream = context.getDocBuilder().requestParameters.contentStream;
    if (contentStream == null)
      throw new DataImportHandlerException(SEVERE, "No stream available. The request has no body");
    try {
      return in = contentStream.getStream();
    } catch (IOException e) {
      DataImportHandlerException.wrapAndThrow(SEVERE, e);
      return null;
    }
  }
  public void close() {
     if (contentStream != null) {
      try {
        if (in == null) in = contentStream.getStream();
        in.close();
      } catch (IOException e) {
      }
    } 
  }
}
