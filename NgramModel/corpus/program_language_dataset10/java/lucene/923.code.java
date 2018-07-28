package org.apache.lucene.benchmark.quality;
import java.io.IOException;
import java.io.PrintWriter;
import org.apache.lucene.benchmark.quality.utils.DocNameExtractor;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
public class QualityBenchmark {
  protected QualityQuery qualityQueries[];
  protected QualityQueryParser qqParser;
  protected Searcher searcher;
  protected String docNameField;
  private int maxQueries = Integer.MAX_VALUE;
  private int maxResults = 1000;
  public QualityBenchmark(QualityQuery qqs[], QualityQueryParser qqParser, 
      Searcher searcher, String docNameField) {
    this.qualityQueries = qqs;
    this.qqParser = qqParser;
    this.searcher = searcher;
    this.docNameField = docNameField;
  }
  public  QualityStats [] execute(Judge judge, SubmissionReport submitRep, 
                                  PrintWriter qualityLog) throws Exception {
    int nQueries = Math.min(maxQueries, qualityQueries.length);
    QualityStats stats[] = new QualityStats[nQueries]; 
    for (int i=0; i<nQueries; i++) {
      QualityQuery qq = qualityQueries[i];
      Query q = qqParser.parse(qq);
      long t1 = System.currentTimeMillis();
      TopDocs td = searcher.search(q,null,maxResults);
      long searchTime = System.currentTimeMillis()-t1;
      if (judge!=null) {
        stats[i] = analyzeQueryResults(qq, q, td, judge, qualityLog, searchTime);
      }
      if (submitRep!=null) {
        submitRep.report(qq,td,docNameField,searcher);
      }
    } 
    if (submitRep!=null) {
      submitRep.flush();
    }
    return stats;
  }
  private QualityStats analyzeQueryResults(QualityQuery qq, Query q, TopDocs td, Judge judge, PrintWriter logger, long searchTime) throws IOException {
    QualityStats stts = new QualityStats(judge.maxRecall(qq),searchTime);
    ScoreDoc sd[] = td.scoreDocs;
    long t1 = System.currentTimeMillis(); 
    DocNameExtractor xt = new DocNameExtractor(docNameField);
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);
      long docNameExtractTime = System.currentTimeMillis() - t1;
      t1 = System.currentTimeMillis();
      boolean isRelevant = judge.isRelevant(docName,qq);
      stts.addResult(i+1,isRelevant, docNameExtractTime);
    }
    if (logger!=null) {
      logger.println(qq.getQueryID()+"  -  "+q);
      stts.log(qq.getQueryID()+" Stats:",1,logger,"  ");
    }
    return stts;
  }
  public int getMaxQueries() {
    return maxQueries;
  }
  public void setMaxQueries(int maxQueries) {
    this.maxQueries = maxQueries;
  }
  public int getMaxResults() {
    return maxResults;
  }
  public void setMaxResults(int maxResults) {
    this.maxResults = maxResults;
  }
}
