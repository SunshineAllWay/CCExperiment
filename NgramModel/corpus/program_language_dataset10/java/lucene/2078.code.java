package org.apache.solr.handler.dataimport;
import java.util.Map;
public abstract class EntityProcessor {
  public abstract void init(Context context);
  public abstract Map<String, Object> nextRow();
  public abstract Map<String, Object> nextModifiedRowKey();
  public abstract Map<String, Object> nextDeletedRowKey();
  public abstract Map<String, Object> nextModifiedParentRowKey();
  public abstract void destroy();
  public void postTransform(Map<String, Object> r) {
  }
  public void close() {
  }
}
