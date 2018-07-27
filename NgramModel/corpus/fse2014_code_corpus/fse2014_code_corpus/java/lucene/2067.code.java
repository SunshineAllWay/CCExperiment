package org.apache.solr.handler.dataimport;
import org.apache.solr.common.util.ContentStream;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
public class ContentStreamDataSource extends DataSource<Reader> {
  private ContextImpl context;
  private ContentStream contentStream;
  private Reader reader;
  public void init(Context context, Properties initProps) {
    this.context = (ContextImpl) context;
  }
  public Reader getData(String query) {
    contentStream = context.getDocBuilder().requestParameters.contentStream;
    if (contentStream == null)
      throw new DataImportHandlerException(SEVERE, "No stream available. The request has no body");
    try {
      return reader = contentStream.getReader();
    } catch (IOException e) {
      DataImportHandlerException.wrapAndThrow(SEVERE, e);
      return null;
    }
  }
  public void close() {
    if (contentStream != null) {
      try {
        if (reader == null) reader = contentStream.getReader();
        reader.close();
      } catch (IOException e) {
      }
    }
  }
}
