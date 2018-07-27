package org.apache.solr.util;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.index.LogByteSizeMergePolicy;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.AppendedSolrParams;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.highlight.SolrHighlighter;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;
import org.apache.solr.update.DocumentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
public class SolrPluginUtils {
  final static Logger log = LoggerFactory.getLogger( SolrPluginUtils.class );
  public static void setDefaults(SolrQueryRequest req, SolrParams defaults) {
    setDefaults(req, defaults, null, null);
  }
  public static void setDefaults(SolrQueryRequest req, SolrParams defaults,
                                 SolrParams appends, SolrParams invariants) {
      SolrParams p = req.getParams();
      if (defaults != null) {
        p = new DefaultSolrParams(p,defaults);
      }
      if (appends != null) {
        p = new AppendedSolrParams(p,appends);
      }
      if (invariants != null) {
        p = new DefaultSolrParams(invariants,p);
      }
      req.setParams(p);
  }
  @Deprecated
  public static String FL = org.apache.solr.common.params.CommonParams.FL;
  public static int numDocs(SolrIndexSearcher s, Query q, Query f)
    throws IOException {
    return (null == f) ? s.getDocSet(q).size() : s.numDocs(q,f);
  }
  public static String getParam(SolrQueryRequest req,
                                String param, String def) {
    String v = req.getParam(param);
    if (null == v || "".equals(v.trim())) {
      return def;
    }
    return v;
  }
  public static Number getNumberParam(SolrQueryRequest req,
                                      String param, Number def) {
    Number r = def;
    String v = req.getParam(param);
    if (null == v || "".equals(v.trim())) {
      return r;
    }
    try {
      r = new Float(v);
    } catch (NumberFormatException e) {
    }
    return r;
  }
  public static boolean getBooleanParam(SolrQueryRequest req,
                                       String param, boolean def) {        
    String v = req.getParam(param);
    if (null == v || "".equals(v.trim())) {
      return def;
    }
    return !"false".equals(v.trim());
  }
  private final static Pattern splitList=Pattern.compile(",| ");
  public static String[] split(String value){
     return splitList.split(value.trim(), 0);
  }
  public static int setReturnFields(SolrQueryRequest req,
                                    SolrQueryResponse res) {
    return setReturnFields(req.getParams().get(org.apache.solr.common.params.CommonParams.FL), res);
  }
  public static int setReturnFields(String fl,
                                    SolrQueryResponse res) {
    int flags = 0;
    if (fl != null) {
      String[] flst = split(fl);
      if (flst.length > 0 && !(flst.length==1 && flst[0].length()==0)) {
        Set<String> set = new HashSet<String>();
        for (String fname : flst) {
          if("score".equalsIgnoreCase(fname))
            flags |= SolrIndexSearcher.GET_SCORES;
          set.add(fname);
        }
        res.setReturnFields(set);
      }
    }
    return flags;
  }
  public static void optimizePreFetchDocs(DocList docs,
                                          Query query,
                                          SolrQueryRequest req,
                                          SolrQueryResponse res) throws IOException {
    SolrIndexSearcher searcher = req.getSearcher();
    if(!searcher.enableLazyFieldLoading) {
      return;
    }
    Set<String> fieldFilter = null;
    Set<String> returnFields = res.getReturnFields();
    if(returnFields != null) {
      fieldFilter = new HashSet<String>(returnFields);
      SolrHighlighter highligher = req.getCore().getHighlighter();
      if(highligher.isHighlightingEnabled(req.getParams())) {
        for(String field: highligher.getHighlightFields(query, req, null)) 
          fieldFilter.add(field);        
      }
      SchemaField keyField = req.getSearcher().getSchema().getUniqueKeyField();
      if(null != keyField)
          fieldFilter.add(keyField.getName());  
    }
    DocIterator iter = docs.iterator();
    for (int i=0; i<docs.size(); i++) {
      searcher.doc(iter.nextDoc(), fieldFilter);
    }
  }
  public static NamedList doStandardDebug(SolrQueryRequest req,
                                          String userQuery,
                                          Query query,
                                          DocList results,
                                          CommonParams params)
    throws IOException {
    String debug = getParam(req, org.apache.solr.common.params.CommonParams.DEBUG_QUERY, params.debugQuery);
    NamedList dbg = null;
    if (debug!=null) {
      dbg = new SimpleOrderedMap();
      dbg.add("rawquerystring", req.getQueryString());
      dbg.add("querystring", userQuery);
      dbg.add("parsedquery",QueryParsing.toString(query, req.getSchema()));
      dbg.add("parsedquery_toString", query.toString());
      dbg.add("explain", getExplainList
              (query, results, req.getSearcher(), req.getSchema()));
      String otherQueryS = req.getParam("explainOther");
      if (otherQueryS != null && otherQueryS.length() > 0) {
        DocList otherResults = doSimpleQuery
          (otherQueryS,req.getSearcher(), req.getSchema(),0,10);
        dbg.add("otherQuery",otherQueryS);
        dbg.add("explainOther", getExplainList
                (query, otherResults,
                 req.getSearcher(),
                 req.getSchema()));
      }
    }
    return dbg;
  }
  public static NamedList doStandardDebug(SolrQueryRequest req,
                                          String userQuery,
                                          Query query,
                                          DocList results)
    throws IOException {
    String debug = req.getParams().get(org.apache.solr.common.params.CommonParams.DEBUG_QUERY);
    NamedList dbg = null;
    if (debug!=null) {
      dbg = new SimpleOrderedMap();
      dbg.add("rawquerystring", req.getParams().get(org.apache.solr.common.params.CommonParams.Q));
      dbg.add("querystring", userQuery);
      dbg.add("parsedquery",QueryParsing.toString(query, req.getSchema()));
      dbg.add("parsedquery_toString", query.toString());
      dbg.add("explain", getExplainList
              (query, results, req.getSearcher(), req.getSchema()));
      String otherQueryS = req.getParams().get(org.apache.solr.common.params.CommonParams.EXPLAIN_OTHER);
      if (otherQueryS != null && otherQueryS.length() > 0) {
        DocList otherResults = doSimpleQuery
          (otherQueryS,req.getSearcher(), req.getSchema(),0,10);
        dbg.add("otherQuery",otherQueryS);
        dbg.add("explainOther", getExplainList
                (query, otherResults,
                 req.getSearcher(),
                 req.getSchema()));
      }
    }
    return dbg;
  }
  public static NamedList getExplainList(Query query, DocList docs,
                                         SolrIndexSearcher searcher,
                                         IndexSchema schema)
    throws IOException {
    NamedList explainList = new SimpleOrderedMap();
    DocIterator iterator = docs.iterator();
    for (int i=0; i<docs.size(); i++) {
      int id = iterator.nextDoc();
      Explanation explain = searcher.explain(query, id);
      Document doc = searcher.doc(id);
      String strid = schema.printableUniqueKey(doc);
      explainList.add(strid, "\n" +explain.toString());
    }
    return explainList;
  }
  public static DocList doSimpleQuery(String sreq,
                                      SolrIndexSearcher searcher,
                                      IndexSchema schema,
                                      int start, int limit) throws IOException {
    List<String> commands = StrUtils.splitSmart(sreq,';');
    String qs = commands.size() >= 1 ? commands.get(0) : "";
    Query query = QueryParsing.parseQuery(qs, schema);
    Sort sort = null;
    if (commands.size() >= 2) {
      sort = QueryParsing.parseSort(commands.get(1), schema);
    }
    DocList results = searcher.getDocList(query,(DocSet)null, sort, start, limit);
    return results;
  }
  public static Map<String,Float> parseFieldBoosts(String in) {
    return parseFieldBoosts(new String[]{in});
  }
  public static Map<String,Float> parseFieldBoosts(String[] fieldLists) {
    if (null == fieldLists || 0 == fieldLists.length) {
      return new HashMap<String,Float>();
    }
    Map<String, Float> out = new HashMap<String,Float>(7);
    for (String in : fieldLists) {
      if (null == in || "".equals(in.trim()))
        continue;
      String[] bb = in.trim().split("\\s+");
      for (String s : bb) {
        String[] bbb = s.split("\\^");
        out.put(bbb[0], 1 == bbb.length ? null : Float.valueOf(bbb[1]));
      }
    }
    return out;
  }
  public static List<Query> parseFuncs(IndexSchema s, String in)
    throws ParseException {
    Map<String,Float> ff = parseFieldBoosts(in);
    List<Query> funcs = new ArrayList<Query>(ff.keySet().size());
    for (String f : ff.keySet()) {
      Query fq = QueryParsing.parseFunction(f, s);
      Float b = ff.get(f);
      if (null != b) {
        fq.setBoost(b);
      }
      funcs.add(fq);
    }
    return funcs;
  }
  public static void setMinShouldMatch(BooleanQuery q, String spec) {
    int optionalClauses = 0;
    for (BooleanClause c : (List<BooleanClause>)q.clauses()) {
      if (c.getOccur() == Occur.SHOULD) {
        optionalClauses++;
      }
    }
    int msm = calculateMinShouldMatch(optionalClauses, spec);
    if (0 < msm) {
      q.setMinimumNumberShouldMatch(msm);
    }
  }
  static int calculateMinShouldMatch(int optionalClauseCount, String spec) {
    int result = optionalClauseCount;
    if (-1 < spec.indexOf("<")) {
      for (String s : spec.trim().split(" ")) {
        String[] parts = s.split("<");
        int upperBound = (new Integer(parts[0])).intValue();
        if (optionalClauseCount <= upperBound) {
          return result;
        } else {
          result = calculateMinShouldMatch
            (optionalClauseCount, parts[1]);
        }
      }
      return result;
    }
    if (-1 < spec.indexOf("%")) {
      int percent = new Integer(spec.replace("%","")).intValue();
      float calc = (result * percent) / 100f;
      result = calc < 0 ? result + (int)calc : (int)calc;
    } else {
      int calc = (new Integer(spec)).intValue();
      result = calc < 0 ? result + calc : calc;
    }
    return (optionalClauseCount < result ?
            optionalClauseCount : (result < 0 ? 0 : result));
  }
  public static void flattenBooleanQuery(BooleanQuery to, BooleanQuery from) {
    for (BooleanClause clause : (List<BooleanClause>)from.clauses()) {
      Query cq = clause.getQuery();
      cq.setBoost(cq.getBoost() * from.getBoost());
      if (cq instanceof BooleanQuery
          && !clause.isRequired()
          && !clause.isProhibited()) {
        flattenBooleanQuery(to, (BooleanQuery)cq);
      } else {
        to.add(clause);
      }
    }
  }
  public static CharSequence partialEscape(CharSequence s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '\\' || c == '!' || c == '(' || c == ')' ||
          c == ':'  || c == '^' || c == '[' || c == ']' ||
          c == '{'  || c == '}' || c == '~' || c == '*' || c == '?'
          ) {
        sb.append('\\');
      }
      sb.append(c);
    }
    return sb;
  }
  private final static Pattern DANGLING_OP_PATTERN = Pattern.compile( "\\s+[-+\\s]+$" );
  private final static Pattern CONSECUTIVE_OP_PATTERN = Pattern.compile( "\\s+[+-](?:\\s*[+-]+)+" );    
  public static CharSequence stripIllegalOperators(CharSequence s) {
    String temp = CONSECUTIVE_OP_PATTERN.matcher( s ).replaceAll( " " );
    return DANGLING_OP_PATTERN.matcher( temp ).replaceAll( "" );
  }
  public static CharSequence stripUnbalancedQuotes(CharSequence s) {
    int count = 0;
    for (int i = 0; i < s.length(); i++) {
      if (s.charAt(i) == '\"') { count++; }
    }
    if (0 == (count & 1)) {
      return s;
    }
    return s.toString().replace("\"","");
  }
  public static class DisjunctionMaxQueryParser extends SolrQueryParser {
    protected static class Alias {
      public float tie;
      public Map<String,Float> fields;
    }
    protected Map<String,Alias> aliases = new HashMap<String,Alias>(3);
    public DisjunctionMaxQueryParser(QParser qp, String defaultField) {
      super(qp,defaultField);
      setDefaultOperator(QueryParser.Operator.OR);
    }
    public DisjunctionMaxQueryParser(IndexSchema s, String defaultField) {
      super(s,defaultField);
      setDefaultOperator(QueryParser.Operator.OR);
    }
    public DisjunctionMaxQueryParser(IndexSchema s) {
      this(s,null);
    }
    public void addAlias(String field, float tiebreaker,
                         Map<String,Float> fieldBoosts) {
      Alias a = new Alias();
      a.tie = tiebreaker;
      a.fields = fieldBoosts;
      aliases.put(field, a);
    }
    protected Query getFieldQuery(String field, String queryText)
      throws ParseException {
      if (aliases.containsKey(field)) {
        Alias a = aliases.get(field);
        DisjunctionMaxQuery q = new DisjunctionMaxQuery(a.tie);
        boolean ok = false;
        for (String f : a.fields.keySet()) {
          Query sub = getFieldQuery(f,queryText);
          if (null != sub) {
            if (null != a.fields.get(f)) {
              sub.setBoost(a.fields.get(f));
            }
            q.add(sub);
            ok = true;
          }
        }
        return ok ? q : null;
      } else {
        try {
          return super.getFieldQuery(field, queryText);
        } catch (Exception e) {
          return null;
        }
      }
    }
  }
  public static Sort getSort(SolrQueryRequest req) {
    String sort = req.getParams().get(org.apache.solr.common.params.CommonParams.SORT);
    if (null == sort || sort.equals("")) {
      return null;
    }
    SolrException sortE = null;
    Sort ss = null;
    try {
      ss = QueryParsing.parseSort(sort, req.getSchema());
    } catch (SolrException e) {
      sortE = e;
    }
    if ((null == ss) || (null != sortE)) {
      SolrCore.log.warn("Invalid sort \""+sort+"\" was specified, ignoring", sortE);
      return null;
    }
    return ss;
  }
  public static List<Query> parseFilterQueries(SolrQueryRequest req) throws ParseException {
    return parseQueryStrings(req, req.getParams().getParams(org.apache.solr.common.params.CommonParams.FQ));
  }
  public static List<Query> parseQueryStrings(SolrQueryRequest req, 
                                              String[] queries) throws ParseException {    
    if (null == queries || 0 == queries.length) return null;
    List<Query> out = new ArrayList<Query>(queries.length);
    for (String q : queries) {
      if (null != q && 0 != q.trim().length()) {
        out.add(QParser.getParser(q, null, req).getQuery());
      }
    }
    return out;
  }
  public static class IdentityRegenerator implements CacheRegenerator {
    public boolean regenerateItem(SolrIndexSearcher newSearcher,
                                  SolrCache newCache,
                                  SolrCache oldCache,
                                  Object oldKey,
                                  Object oldVal)
      throws IOException {
      newCache.put(oldKey,oldVal);
      return true;
    }
  }
  public static SolrDocumentList docListToSolrDocumentList(
      DocList docs, 
      SolrIndexSearcher searcher, 
      Set<String> fields, 
      Map<SolrDocument, Integer> ids ) throws IOException
  {
    DocumentBuilder db = new DocumentBuilder(searcher.getSchema());
    SolrDocumentList list = new SolrDocumentList();
    list.setNumFound(docs.matches());
    list.setMaxScore(docs.maxScore());
    list.setStart(docs.offset());
    DocIterator dit = docs.iterator();
    while (dit.hasNext()) {
      int docid = dit.nextDoc();
      Document luceneDoc = searcher.doc(docid, fields);
      SolrDocument doc = new SolrDocument();
      db.loadStoredFields(doc, luceneDoc);
      if (docs.hasScores()) {
        doc.addField("score", dit.score());
      } else {
        doc.addField("score", 0.0f);
      }
      list.add( doc );
      if( ids != null ) {
        ids.put( doc, new Integer(docid) );
      }
    }
    return list;
  }
  public static void addOrReplaceResults(SolrQueryResponse rsp, SolrDocumentList docs) 
  {
    NamedList vals = rsp.getValues();
    int idx = vals.indexOf( "response", 0 );
    if( idx >= 0 ) {
      log.debug("Replacing DocList with SolrDocumentList " + docs.size());
      vals.setVal( idx, docs );
    }
    else {
      log.debug("Adding SolrDocumentList response" + docs.size());
      vals.add( "response", docs );
    }
  }
  public static void invokeSetters(Object bean, NamedList initArgs) {
    if (initArgs == null) return;
    Class clazz = bean.getClass();
    Method[] methods = clazz.getMethods();
    Iterator<Map.Entry<String, Object>> iterator = initArgs.iterator();
    while (iterator.hasNext()) {
      Map.Entry<String, Object> entry = iterator.next();
      String key = entry.getKey();
      String setterName = "set" + String.valueOf(Character.toUpperCase(key.charAt(0))) + key.substring(1);
      Method method = null;
      try {
        for (Method m : methods) {
          if (m.getName().equals(setterName) && m.getParameterTypes().length == 1) { 
            method = m;
            break;
          }
        }
        if (method == null) {
          throw new RuntimeException("no setter corrresponding to '" + key + "' in " + clazz.getName());
        }
        Class pClazz = method.getParameterTypes()[0];
        Object val = entry.getValue();
        method.invoke(bean, val);
      } catch (InvocationTargetException e1) {
        throw new RuntimeException("Error invoking setter " + setterName + " on class : " + clazz.getName(), e1);
      } catch (IllegalAccessException e1) {
        throw new RuntimeException("Error invoking setter " + setterName + " on class : " + clazz.getName(), e1);
      }
    }
  }
}
