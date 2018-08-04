package org.apache.lucene.benchmark.byTask.feeds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.benchmark.byTask.tasks.NewAnalyzerTask;
import org.apache.lucene.util.Version;
public class EnwikiQueryMaker extends AbstractQueryMaker implements
    QueryMaker {
  private static String[] STANDARD_QUERIES = { "Images catbox gif",
      "Imunisasi haram", "Favicon ico", "Michael jackson", "Unknown artist",
      "Lily Thai", "Neda", "The Last Song", "Metallica", "Nicola Tesla",
      "Max B", "Skil Corporation", "\"The 100 Greatest Artists of All Time\"",
      "\"Top 100 Global Universities\"", "Pink floyd", "Bolton Sullivan",
      "Frank Lucas Jr", "Drake Woods", "Radiohead", "George Freeman",
      "Oksana Grigorieva", "The Elder Scrolls V", "Deadpool", "Green day",
      "\"Red hot chili peppers\"", "Jennifer Bini Taylor",
      "The Paradiso Girls", "Queen", "3Me4Ph", "Paloma Jimenez", "AUDI A4",
      "Edith Bouvier Beale: A Life In Pictures", "\"Skylar James Deleon\"",
      "Simple Explanation", "Juxtaposition", "The Woody Show", "London WITHER",
      "In A Dark Place", "George Freeman", "LuAnn de Lesseps", "Muhammad.",
      "U2", "List of countries by GDP", "Dean Martin Discography", "Web 3.0",
      "List of American actors", "The Expendables",
      "\"100 Greatest Guitarists of All Time\"", "Vince Offer.",
      "\"List of ZIP Codes in the United States\"", "Blood type diet",
      "Jennifer Gimenez", "List of hobbies", "The beatles", "Acdc",
      "Nightwish", "Iron maiden", "Murder Was the Case", "Pelvic hernia",
      "Naruto Shippuuden", "campaign", "Enthesopathy of hip region",
      "operating system", "mouse",
      "List of Xbox 360 games without region encoding", "Shakepearian sonnet",
      "\"The Monday Night Miracle\"", "India", "Dad's Army",
      "Solanum melanocerasum", "\"List of PlayStation Portable Wi-Fi games\"",
      "Little Pixie Geldof", "Planes, Trains & Automobiles", "Freddy Ingalls",
      "The Return of Chef", "Nehalem", "Turtle", "Calculus", "Superman-Prime",
      "\"The Losers\"", "pen-pal", "Audio stream input output", "lifehouse",
      "50 greatest gunners", "Polyfecalia", "freeloader", "The Filthy Youth" };
  private static Query[] getPrebuiltQueries(String field) {
    WildcardQuery wcq = new WildcardQuery(new Term(field, "fo*"));
    wcq .setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_FILTER_REWRITE);
    return new Query[] {
        new SpanFirstQuery(new SpanTermQuery(new Term(field, "ford")), 5),
        new SpanNearQuery(new SpanQuery[] {
            new SpanTermQuery(new Term(field, "night")),
            new SpanTermQuery(new Term(field, "trading")) }, 4, false),
        new SpanNearQuery(new SpanQuery[] {
            new SpanFirstQuery(new SpanTermQuery(new Term(field, "ford")), 10),
            new SpanTermQuery(new Term(field, "credit")) }, 10, false), wcq, };
  }
  private static Query[] createQueries(List<Object> qs, Analyzer a) {
    QueryParser qp = new QueryParser(Version.LUCENE_CURRENT, DocMaker.BODY_FIELD, a);
    List<Object> queries = new ArrayList<Object>();
    for (int i = 0; i < qs.size(); i++) {
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
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return queries.toArray(new Query[0]);
  }
  @Override
  protected Query[] prepareQueries() throws Exception {
    Analyzer anlzr = NewAnalyzerTask.createAnalyzer(config.get("analyzer", StandardAnalyzer.class.getName()));
    List<Object> queryList = new ArrayList<Object>(20);
    queryList.addAll(Arrays.asList(STANDARD_QUERIES));
    if(!config.get("enwikiQueryMaker.disableSpanQueries", false))
      queryList.addAll(Arrays.asList(getPrebuiltQueries(DocMaker.BODY_FIELD)));
    return createQueries(queryList, anlzr);
  }
}