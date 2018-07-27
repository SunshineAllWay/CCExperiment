package org.apache.solr.util;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.ListIterator;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.*;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.highlight.DefaultSolrHighlighter;
import org.apache.lucene.analysis.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
public class HighlightingUtils implements HighlightParams {
   static SolrParams DEFAULTS = null;
   static {
      Map<String,String> map = new HashMap<String,String>();
      map.put(SNIPPETS, "1");
      map.put(FRAGSIZE, "100");
      map.put(FORMATTER, SIMPLE);
      map.put(SIMPLE_PRE, "<em>");
      map.put(SIMPLE_POST, "</em>");
      DEFAULTS = new MapSolrParams(map);
   }
  private static SolrHighlighterX HIGHLIGHTER = new SolrHighlighterX();
   static SolrParams getParams(SolrQueryRequest request) {
      return new DefaultSolrParams(request.getParams(), DEFAULTS);
   }
   public static boolean isHighlightingEnabled(SolrQueryRequest request) {
     return HIGHLIGHTER.isHighlightingEnabled(getParams(request));
   }
   public static Highlighter getHighlighter(Query query, String fieldName, SolrQueryRequest request) {
     return HIGHLIGHTER.getHighlighterX(query, fieldName, request);
   }
   public static String[] getHighlightFields(Query query, SolrQueryRequest request, String[] defaultFields) {
     return HIGHLIGHTER.getHighlightFields(query, request, defaultFields);
   }
   public static int getMaxSnippets(String fieldName, SolrQueryRequest request) {
     return HIGHLIGHTER.getMaxSnippetsX(fieldName, request);
   }
   public static Formatter getFormatter(String fieldName, SolrQueryRequest request) {
     return HIGHLIGHTER.getFormatterX(fieldName, request);
   }
   public static Fragmenter getFragmenter(String fieldName, SolrQueryRequest request) {
     return HIGHLIGHTER.getFragmenterX(fieldName, request);
   }
   @SuppressWarnings("unchecked")
   public static NamedList doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields) throws IOException {
     return HIGHLIGHTER.doHighlighting(docs, query, req, defaultFields);
   }
}
class SolrHighlighterX extends DefaultSolrHighlighter {
  Highlighter getHighlighterX(Query query, String fieldName, SolrQueryRequest request) {
    return getHighlighter(query, fieldName, request);
  }
  int getMaxSnippetsX(String fieldName, SolrQueryRequest request) {
    return getMaxSnippets(fieldName, HighlightingUtils.getParams(request));
  }
  Formatter getFormatterX(String fieldName, SolrQueryRequest request) {
        return getFormatter(fieldName, HighlightingUtils.getParams(request));
  }
  Fragmenter getFragmenterX(String fieldName, SolrQueryRequest request) {
    return getFragmenter(fieldName, HighlightingUtils.getParams(request));
  }
}
