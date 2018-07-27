package org.apache.lucene.benchmark.quality.utils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.benchmark.quality.QualityQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
public class SimpleQQParser implements QualityQueryParser {
  private String qqNames[];
  private String indexField;
  ThreadLocal<QueryParser> queryParser = new ThreadLocal<QueryParser>();
  public SimpleQQParser(String qqNames[], String indexField) {
    this.qqNames = qqNames;
    this.indexField = indexField;
  }
  public SimpleQQParser(String qqName, String indexField) {
    this(new String[] { qqName }, indexField);
  }
  public Query parse(QualityQuery qq) throws ParseException {
    QueryParser qp = queryParser.get();
    if (qp==null) {
      qp = new QueryParser(Version.LUCENE_CURRENT, indexField, new StandardAnalyzer(Version.LUCENE_CURRENT));
      queryParser.set(qp);
    }
    BooleanQuery bq = new BooleanQuery();
    for (int i = 0; i < qqNames.length; i++)
      bq.add(qp.parse(QueryParser.escape(qq.getValue(qqNames[i]))), BooleanClause.Occur.SHOULD);
    return bq;
  }
}
