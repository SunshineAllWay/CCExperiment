package org.apache.lucene.benchmark.byTask.feeds;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.benchmark.byTask.tasks.NewAnalyzerTask;
import org.apache.lucene.util.Version;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class ReutersQueryMaker extends AbstractQueryMaker implements QueryMaker {
  private static String [] STANDARD_QUERIES = {
    "Salomon", "Comex", "night trading", "Japan Sony",
    "\"Sony Japan\"", "\"food needs\"~3",
    "\"World Bank\"^2 AND Nigeria", "\"World Bank\" -Nigeria",
    "\"Ford Credit\"~5",
    "airline Europe Canada destination",
    "Long term pressure by trade " +
    "ministers is necessary if the current Uruguay round of talks on " +
    "the General Agreement on Trade and Tariffs (GATT) is to " +
    "succeed"
  };
  private static Query[] getPrebuiltQueries(String field) {
    return new Query[] {
        new SpanFirstQuery(new SpanTermQuery(new Term(field, "ford")), 5),
        new SpanNearQuery(new SpanQuery[]{new SpanTermQuery(new Term(field, "night")), new SpanTermQuery(new Term(field, "trading"))}, 4, false),
        new SpanNearQuery(new SpanQuery[]{new SpanFirstQuery(new SpanTermQuery(new Term(field, "ford")), 10), new SpanTermQuery(new Term(field, "credit"))}, 10, false),
        new WildcardQuery(new Term(field, "fo*")),
    };
  }
  private static Query[] createQueries(List<Object> qs, Analyzer a) {
    QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, DocMaker.BODY_FIELD, a);
    List<Object> queries = new ArrayList<Object>();
    for (int i = 0; i < qs.size(); i++)  {
      try {
        Object query = qs.get(i);
        Query q = null;
        if (query instanceof String) {
          q = qp.parse((String) query);
        } else if (query instanceof Query) {
          q = (Query) query;
        } else {
          System.err.println("Unsupported Query Type: " + query);
        }
        if (q != null) {
          queries.add(q);
        }
      } catch (Exception e)  {
        e.printStackTrace();
      }
    }
    return queries.toArray(new Query[0]);
  }
  @Override
  protected Query[] prepareQueries() throws Exception {
    Analyzer anlzr= NewAnalyzerTask.createAnalyzer(config.get("analyzer",
    "org.apache.lucene.analysis.standard.StandardAnalyzer")); 
    List<Object> queryList = new ArrayList<Object>(20);
    queryList.addAll(Arrays.asList(STANDARD_QUERIES));
    queryList.addAll(Arrays.asList(getPrebuiltQueries(DocMaker.BODY_FIELD)));
    return createQueries(queryList, anlzr);
  }
}
