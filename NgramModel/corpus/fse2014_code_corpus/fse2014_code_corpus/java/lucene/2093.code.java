package org.apache.solr.handler.dataimport;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
public class MockDataSource extends
        DataSource<Iterator<Map<String, Object>>> {
  private static Map<String, Iterator<Map<String, Object>>> cache = new HashMap<String, Iterator<Map<String, Object>>>();
  public static void setIterator(String query,
                                 Iterator<Map<String, Object>> iter) {
    cache.put(query, iter);
  }
  public static void clearCache() {
    cache.clear();
  }
  public void init(Context context, Properties initProps) {
  }
  public Iterator<Map<String, Object>> getData(String query) {
    return cache.get(query);
  }
  public void close() {
    cache.clear();
  }
}
