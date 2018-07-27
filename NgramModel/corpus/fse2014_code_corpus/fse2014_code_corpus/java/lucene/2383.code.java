package org.apache.solr.highlight;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.search.Query;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.DocList;
import org.apache.solr.util.SolrPluginUtils;
public abstract class SolrHighlighter
{
  public static Logger log = LoggerFactory.getLogger(SolrHighlighter.class);
  protected final Map<String,SolrFormatter> formatters =
    new HashMap<String, SolrFormatter>();
  protected final Map<String,SolrFragmenter> fragmenters =
    new HashMap<String, SolrFragmenter>() ;
  protected final Map<String, SolrFragListBuilder> fragListBuilders =
    new HashMap<String, SolrFragListBuilder>() ;
  protected final Map<String, SolrFragmentsBuilder> fragmentsBuilders =
    new HashMap<String, SolrFragmentsBuilder>() ;
  @Deprecated
  public abstract void initalize( SolrConfig config );
  public boolean isHighlightingEnabled(SolrParams params) {
    return params.getBool(HighlightParams.HIGHLIGHT, false);
  }
  public String[] getHighlightFields(Query query, SolrQueryRequest request, String[] defaultFields) {
    String fields[] = request.getParams().getParams(HighlightParams.FIELDS);
    if(emptyArray(fields)) {
      if (emptyArray(defaultFields)) {
        String defaultSearchField = request.getSchema().getDefaultSearchFieldName();
        fields = null == defaultSearchField ? new String[]{} : new String[]{defaultSearchField};
      }
      else {
        fields = defaultFields;
      }
    }
    else if (fields.length == 1) {
      if (fields[0].contains("*")) {
        String fieldRegex = fields[0].replaceAll("\\*", ".*");
        Collection<String> storedHighlightFieldNames = request.getSearcher().getStoredHighlightFieldNames();
        List<String> storedFieldsToHighlight = new ArrayList<String>();
        for (String storedFieldName: storedHighlightFieldNames) {
            if (storedFieldName.matches(fieldRegex)) {
              storedFieldsToHighlight.add(storedFieldName);
            }
        }
        fields = storedFieldsToHighlight.toArray(new String[] {});
      } else {
        fields = SolrPluginUtils.split(fields[0]);
      }
    }
    return fields;
  }
  protected boolean emptyArray(String[] arr) {
    return (arr == null || arr.length == 0 || arr[0] == null || arr[0].trim().length() == 0);
  }
  @SuppressWarnings("unchecked")
  public abstract NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields) throws IOException;
}
