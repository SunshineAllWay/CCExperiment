package org.apache.solr.handler.dataimport;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class SqlEntityProcessor extends EntityProcessorBase {
  private static final Logger LOG = LoggerFactory.getLogger(SqlEntityProcessor.class);
  protected DataSource<Iterator<Map<String, Object>>> dataSource;
  @SuppressWarnings("unchecked")
  public void init(Context context) {
    super.init(context);
    dataSource = context.getDataSource();
  }
  protected void initQuery(String q) {
    try {
      DataImporter.QUERY_COUNT.get().incrementAndGet();
      rowIterator = dataSource.getData(q);
      this.query = q;
    } catch (DataImportHandlerException e) {
      throw e;
    } catch (Exception e) {
      LOG.error( "The query failed '" + q + "'", e);
      throw new DataImportHandlerException(DataImportHandlerException.SEVERE, e);
    }
  }
  public Map<String, Object> nextRow() {    
    if (rowIterator == null) {
      String q = getQuery();
      initQuery(context.replaceTokens(q));
    }
    return getNext();
  }
  public Map<String, Object> nextModifiedRowKey() {
    if (rowIterator == null) {
      String deltaQuery = context.getEntityAttribute(DELTA_QUERY);
      if (deltaQuery == null)
        return null;
      initQuery(context.replaceTokens(deltaQuery));
    }
    return getNext();
  }
  public Map<String, Object> nextDeletedRowKey() {
    if (rowIterator == null) {
      String deletedPkQuery = context.getEntityAttribute(DEL_PK_QUERY);
      if (deletedPkQuery == null)
        return null;
      initQuery(context.replaceTokens(deletedPkQuery));
    }
    return getNext();
  }
  public Map<String, Object> nextModifiedParentRowKey() {
    if (rowIterator == null) {
      String parentDeltaQuery = context.getEntityAttribute(PARENT_DELTA_QUERY);
      if (parentDeltaQuery == null)
        return null;
      LOG.info("Running parentDeltaQuery for Entity: "
              + context.getEntityAttribute("name"));
      initQuery(context.replaceTokens(parentDeltaQuery));
    }
    return getNext();
  }
  public String getQuery() {
    String queryString = context.getEntityAttribute(QUERY);
    if (Context.FULL_DUMP.equals(context.currentProcess())) {
      return queryString;
    }
    if (Context.DELTA_DUMP.equals(context.currentProcess())) {
      String deltaImportQuery = context.getEntityAttribute(DELTA_IMPORT_QUERY);
      if(deltaImportQuery != null) return deltaImportQuery;
    }
    LOG.warn("'deltaImportQuery' attribute is not specified for entity : "+ entityName);
    return getDeltaImportQuery(queryString);
  }
  public String getDeltaImportQuery(String queryString) {    
    StringBuilder sb = new StringBuilder(queryString);
    if (SELECT_WHERE_PATTERN.matcher(queryString).find()) {
      sb.append(" and ");
    } else {
      sb.append(" where ");
    }
    boolean first = true;
    String[] primaryKeys = context.getEntityAttribute("pk").split(",");
    for (String primaryKey : primaryKeys) {
      if (!first) {
        sb.append(" and ");
      }
      first = false;
      Object val = context.resolve("dataimporter.delta." + primaryKey);
      if (val == null) {
        Matcher m = DOT_PATTERN.matcher(primaryKey);
        if (m.find()) {
          val = context.resolve("dataimporter.delta." + m.group(1));
        }
      }
      sb.append(primaryKey).append(" = ");
      if (val instanceof Number) {
        sb.append(val.toString());
      } else {
        sb.append("'").append(val.toString()).append("'");
      }
    }
    return sb.toString();
  }
  private static Pattern SELECT_WHERE_PATTERN = Pattern.compile(
          "^\\s*(select\\b.*?\\b)(where).*", Pattern.CASE_INSENSITIVE);
  public static final String QUERY = "query";
  public static final String DELTA_QUERY = "deltaQuery";
  public static final String DELTA_IMPORT_QUERY = "deltaImportQuery";
  public static final String PARENT_DELTA_QUERY = "parentDeltaQuery";
  public static final String DEL_PK_QUERY = "deletedPkQuery";
  public static final Pattern DOT_PATTERN = Pattern.compile(".*?\\.(.*)$");
}
