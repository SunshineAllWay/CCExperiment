package org.apache.solr.handler.dataimport;
import java.util.Map;
public abstract class Transformer {
  public abstract Object transformRow(Map<String, Object> row, Context context);
}
