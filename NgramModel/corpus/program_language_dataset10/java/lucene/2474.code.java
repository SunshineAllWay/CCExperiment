package org.apache.solr.search;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.DefaultSolrParams;
import org.apache.solr.common.params.DisMaxParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.util.SolrPluginUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class DisMaxQParser extends QParser {
  private static String IMPOSSIBLE_FIELD_NAME = "\uFFFC\uFFFC\uFFFC";
  public DisMaxQParser(String qstr, SolrParams localParams, SolrParams params, SolrQueryRequest req) {
    super(qstr, localParams, params, req);
  }
  protected Map<String, Float> queryFields;
  protected Query parsedUserQuery;
  protected String[] boostParams;
  protected List<Query> boostQueries;
  protected Query altUserQuery;
  protected QParser altQParser;
  public Query parse() throws ParseException {
    SolrParams solrParams = localParams == null ? params : new DefaultSolrParams(localParams, params);
    queryFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.QF));
    if (0 == queryFields.size()) {
      queryFields.put(req.getSchema().getDefaultSearchFieldName(), 1.0f);
    }
    BooleanQuery query = new BooleanQuery(true);
    addMainQuery(query, solrParams);
    addBoostQuery(query, solrParams);
    addBoostFunctions(query, solrParams);
    return query;
  }
  protected void addBoostFunctions(BooleanQuery query, SolrParams solrParams) throws ParseException {
    String[] boostFuncs = solrParams.getParams(DisMaxParams.BF);
    if (null != boostFuncs && 0 != boostFuncs.length) {
      for (String boostFunc : boostFuncs) {
        if (null == boostFunc || "".equals(boostFunc)) continue;
        Map<String, Float> ff = SolrPluginUtils.parseFieldBoosts(boostFunc);
        for (String f : ff.keySet()) {
          Query fq = subQuery(f, FunctionQParserPlugin.NAME).parse();
          Float b = ff.get(f);
          if (null != b) {
            fq.setBoost(b);
          }
          query.add(fq, BooleanClause.Occur.SHOULD);
        }
      }
    }
  }
  protected void addBoostQuery(BooleanQuery query, SolrParams solrParams) throws ParseException {
    boostParams = solrParams.getParams(DisMaxParams.BQ);
    boostQueries = null;
    if (boostParams != null && boostParams.length > 0) {
      boostQueries = new ArrayList<Query>();
      for (String qs : boostParams) {
        if (qs.trim().length() == 0) continue;
        Query q = subQuery(qs, null).parse();
        boostQueries.add(q);
      }
    }
    if (null != boostQueries) {
      if (1 == boostQueries.size() && 1 == boostParams.length) {
        Query f = boostQueries.get(0);
        if (1.0f == f.getBoost() && f instanceof BooleanQuery) {
          for (Object c : ((BooleanQuery) f).clauses()) {
            query.add((BooleanClause) c);
          }
        } else {
          query.add(f, BooleanClause.Occur.SHOULD);
        }
      } else {
        for (Query f : boostQueries) {
          query.add(f, BooleanClause.Occur.SHOULD);
        }
      }
    }
  }
  protected void addMainQuery(BooleanQuery query, SolrParams solrParams) throws ParseException {
    Map<String, Float> phraseFields = SolrPluginUtils.parseFieldBoosts(solrParams.getParams(DisMaxParams.PF));
    float tiebreaker = solrParams.getFloat(DisMaxParams.TIE, 0.0f);
    SolrPluginUtils.DisjunctionMaxQueryParser up = getParser(queryFields, DisMaxParams.QS, solrParams, tiebreaker);
    SolrPluginUtils.DisjunctionMaxQueryParser pp = getParser(phraseFields, DisMaxParams.PS, solrParams, tiebreaker);
    parsedUserQuery = null;
    String userQuery = getString();
    altUserQuery = null;
    if (userQuery == null || userQuery.trim().length() < 1) {
      altUserQuery = getAlternateUserQuery(solrParams);
      query.add(altUserQuery, BooleanClause.Occur.MUST);
    } else {
      userQuery = SolrPluginUtils.partialEscape(SolrPluginUtils.stripUnbalancedQuotes(userQuery)).toString();
      userQuery = SolrPluginUtils.stripIllegalOperators(userQuery).toString();
      parsedUserQuery = getUserQuery(userQuery, up, solrParams);
      query.add(parsedUserQuery, BooleanClause.Occur.MUST);
      Query phrase = getPhraseQuery(userQuery, pp);
      if (null != phrase) {
        query.add(phrase, BooleanClause.Occur.SHOULD);
      }
    }
  }
  protected Query getAlternateUserQuery(SolrParams solrParams) throws ParseException {
    String altQ = solrParams.get(DisMaxParams.ALTQ);
    if (altQ != null) {
      QParser altQParser = subQuery(altQ, null);
      return altQParser.parse();
    } else {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "missing query string");
    }
  }
  protected Query getPhraseQuery(String userQuery, SolrPluginUtils.DisjunctionMaxQueryParser pp) throws ParseException {
    String userPhraseQuery = userQuery.replace("\"", "");
    return pp.parse("\"" + userPhraseQuery + "\"");
  }
  protected Query getUserQuery(String userQuery, SolrPluginUtils.DisjunctionMaxQueryParser up, SolrParams solrParams)
          throws ParseException {
    String minShouldMatch = solrParams.get(DisMaxParams.MM, "100%");
    Query dis = up.parse(userQuery);
    Query query = dis;
    if (dis instanceof BooleanQuery) {
      BooleanQuery t = new BooleanQuery();
      SolrPluginUtils.flattenBooleanQuery(t, (BooleanQuery) dis);
      SolrPluginUtils.setMinShouldMatch(t, minShouldMatch);
      query = t;
    }
    return query;
  }
  protected SolrPluginUtils.DisjunctionMaxQueryParser getParser(Map<String, Float> fields, String paramName,
                                                                SolrParams solrParams, float tiebreaker) {
    int slop = solrParams.getInt(paramName, 0);
    SolrPluginUtils.DisjunctionMaxQueryParser parser = new SolrPluginUtils.DisjunctionMaxQueryParser(this,
            IMPOSSIBLE_FIELD_NAME);
    parser.addAlias(IMPOSSIBLE_FIELD_NAME, tiebreaker, fields);
    parser.setPhraseSlop(slop);
    return parser;
  }
  @Override
  public String[] getDefaultHighlightFields() {
    return queryFields.keySet().toArray(new String[queryFields.keySet().size()]);
  }
  @Override
  public Query getHighlightQuery() throws ParseException {
    return parsedUserQuery;
  }
  public void addDebugInfo(NamedList<Object> debugInfo) {
    super.addDebugInfo(debugInfo);
    debugInfo.add("altquerystring", altUserQuery);
    if (null != boostQueries) {
      debugInfo.add("boost_queries", boostParams);
      debugInfo.add("parsed_boost_queries",
              QueryParsing.toString(boostQueries, req.getSchema()));
    }
    debugInfo.add("boostfuncs", req.getParams().getParams(DisMaxParams.BF));
  }
}
