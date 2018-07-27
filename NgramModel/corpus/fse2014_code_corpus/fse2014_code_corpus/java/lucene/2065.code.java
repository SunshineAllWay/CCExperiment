package org.apache.solr.handler.dataimport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class CachedSqlEntityProcessor extends SqlEntityProcessor {
  private boolean isFirst;
  @SuppressWarnings("unchecked")
  public void init(Context context) {
    super.init(context);
    super.cacheInit();
    isFirst = true;
  }
  public Map<String, Object> nextRow() {
    if (dataSourceRowCache != null)
      return getFromRowCacheTransformed();
    if (!isFirst)
      return null;
    String query = context.replaceTokens(context.getEntityAttribute("query"));
    isFirst = false;
    if (simpleCache != null) {
      return getSimpleCacheData(query);
    } else {
      return getIdCacheData(query);
    }
  }
  protected List<Map<String, Object>> getAllNonCachedRows() {
    List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    String q = getQuery();
    initQuery(context.replaceTokens(q));
    if (rowIterator == null)
      return rows;
    while (rowIterator.hasNext()) {
      Map<String, Object> arow = rowIterator.next();
      if (arow == null) {
        break;
      } else {
        rows.add(arow);
      }
    }
    return rows;
  }
}
