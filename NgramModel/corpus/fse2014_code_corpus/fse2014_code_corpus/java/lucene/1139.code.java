package org.apache.lucene.queryParser.standard;
import java.util.Map;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
@Deprecated
public class MultiFieldQueryParserWrapper extends QueryParserWrapper {
  @SuppressWarnings("unchecked")
public MultiFieldQueryParserWrapper(String[] fields, Analyzer analyzer, Map boosts) {
    this(fields, analyzer);
    StandardQueryParser qpHelper = getQueryParserHelper();
    qpHelper.setMultiFields(fields);
    qpHelper.setFieldsBoost(boosts);
  }
  public MultiFieldQueryParserWrapper(String[] fields, Analyzer analyzer) {
    super(null, analyzer);
    StandardQueryParser qpHelper = getQueryParserHelper();
    qpHelper.setAnalyzer(analyzer);
    qpHelper.setMultiFields(fields);
  }
  public static Query parse(String[] queries, String[] fields, Analyzer analyzer)
      throws ParseException {
    if (queries.length != fields.length)
      throw new IllegalArgumentException("queries.length != fields.length");
    BooleanQuery bQuery = new BooleanQuery();
    for (int i = 0; i < fields.length; i++) {
      QueryParserWrapper qp = new QueryParserWrapper(fields[i], analyzer);
      Query q = qp.parse(queries[i]);
      if (q != null && 
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, BooleanClause.Occur.SHOULD);
      }
    }
    return bQuery;
  }
  public static Query parse(String query, String[] fields,
      BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
    if (fields.length != flags.length)
      throw new IllegalArgumentException("fields.length != flags.length");
    BooleanQuery bQuery = new BooleanQuery();
    for (int i = 0; i < fields.length; i++) {
      QueryParserWrapper qp = new QueryParserWrapper(fields[i], analyzer);
      Query q = qp.parse(query);
      if (q != null && 
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, flags[i]);
      }
    }
    return bQuery;
  }
  public static Query parse(String[] queries, String[] fields,
      BooleanClause.Occur[] flags, Analyzer analyzer) throws ParseException {
    if (!(queries.length == fields.length && queries.length == flags.length))
      throw new IllegalArgumentException(
          "queries, fields, and flags array have have different length");
    BooleanQuery bQuery = new BooleanQuery();
    for (int i = 0; i < fields.length; i++) {
      QueryParserWrapper qp = new QueryParserWrapper(fields[i], analyzer);
      Query q = qp.parse(queries[i]);
      if (q != null && 
          (!(q instanceof BooleanQuery) || ((BooleanQuery) q).getClauses().length > 0)) {
        bQuery.add(q, flags[i]);
      }
    }
    return bQuery;
  }
}
