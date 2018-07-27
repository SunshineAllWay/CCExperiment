package org.apache.lucene.benchmark.quality.utils;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.NumberFormat;
import org.apache.lucene.benchmark.quality.QualityQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TopDocs;
public class SubmissionReport {
  private NumberFormat nf;
  private PrintWriter logger;
  private String name;
  public SubmissionReport (PrintWriter logger, String name) {
    this.logger = logger;
    this.name = name;
    nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(4);
    nf.setMinimumFractionDigits(4);
  }
  public void report(QualityQuery qq, TopDocs td, String docNameField, Searcher searcher) throws IOException {
    if (logger==null) {
      return;
    }
    ScoreDoc sd[] = td.scoreDocs;
    String sep = " \t ";
    DocNameExtractor xt = new DocNameExtractor(docNameField);
    for (int i=0; i<sd.length; i++) {
      String docName = xt.docName(searcher,sd[i].doc);
      logger.println(
          qq.getQueryID()       + sep +
          "Q0"                   + sep +
          format(docName,20)    + sep +
          format(""+i,7)        + sep +
          nf.format(sd[i].score) + sep +
          name
          );
    }
  }
  public void flush() {
    if (logger!=null) {
      logger.flush();
    }
  }
  private static String padd = "                                    ";
  private String format(String s, int minLen) {
    s = (s==null ? "" : s);
    int n = Math.max(minLen,s.length());
    return (s+padd).substring(0,n);
  }
}
