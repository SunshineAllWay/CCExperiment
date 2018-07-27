package org.apache.solr.handler.dataimport;
import org.apache.solr.core.SolrCore;
import java.util.List;
import java.util.Map;
public abstract class Context {
  public static final String FULL_DUMP = "FULL_DUMP", DELTA_DUMP = "DELTA_DUMP", FIND_DELTA = "FIND_DELTA";
  public static final String SCOPE_ENTITY = "entity";
  public static final String SCOPE_GLOBAL = "global";
  public static final String SCOPE_DOC = "document";
  public static final String SCOPE_SOLR_CORE = "solrcore";
  public abstract String getEntityAttribute(String name);
  public abstract String getResolvedEntityAttribute(String name);
  public abstract List<Map<String, String>> getAllEntityFields();
  public abstract VariableResolver getVariableResolver();
  public abstract DataSource getDataSource();
  public abstract DataSource getDataSource(String name);
  public abstract EntityProcessor getEntityProcessor();
  public abstract void setSessionAttribute(String name, Object val, String scope);
  public abstract Object getSessionAttribute(String name, String scope);
  public abstract Context getParentContext();
  public abstract Map<String, Object> getRequestParameters();
  public abstract boolean isRootEntity();
  public abstract String currentProcess();
  public abstract SolrCore getSolrCore();
  public abstract Map<String, Object> getStats();
  public abstract String getScript();
  public abstract String getScriptLanguage();
  public abstract void deleteDoc(String id);
  public abstract void deleteDocByQuery(String query);
  public abstract Object resolve(String var);
  public abstract String replaceTokens(String template);
  static final ThreadLocal<Context> CURRENT_CONTEXT = new ThreadLocal<Context>();
}
