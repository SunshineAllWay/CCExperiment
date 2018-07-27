package org.apache.solr.handler.dataimport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Blob;
import java.util.Properties;
import static org.apache.solr.handler.dataimport.DataImportHandlerException.SEVERE;
public class FieldStreamDataSource extends DataSource<InputStream> {
  private static final Logger LOG = LoggerFactory.getLogger(FieldReaderDataSource.class);
  protected VariableResolver vr;
  protected String dataField;
  private EntityProcessorWrapper wrapper;
  public void init(Context context, Properties initProps) {
    dataField = context.getEntityAttribute("dataField");
    wrapper = (EntityProcessorWrapper) context.getEntityProcessor();
  }
  public InputStream getData(String query) {
    Object o = wrapper.getVariableResolver().resolve(dataField);
    if (o == null) {
      throw new DataImportHandlerException(SEVERE, "No field available for name : " + dataField);
    }
    if (o instanceof Blob) {
      Blob blob = (Blob) o;
      try {
        Method m = blob.getClass().getDeclaredMethod("getBinaryStream");
        if (Modifier.isPublic(m.getModifiers())) {
          return (InputStream) m.invoke(blob);
        } else {
          m.setAccessible(true);
          return (InputStream) m.invoke(blob);
        }
      } catch (Exception e) {
        LOG.info("Unable to get data from BLOB");
        return null;
      }
    } else if (o instanceof byte[]) {
      byte[] bytes = (byte[]) o;
      return new ByteArrayInputStream(bytes);
    } else {
      throw new RuntimeException("unsupported type : " + o.getClass());
    } 
  }
  public void close() {
  }
}
