package org.apache.lucene.benchmark.quality.trec;
import org.apache.lucene.benchmark.quality.trec.TrecJudge;
import org.apache.lucene.benchmark.quality.trec.TrecTopicsReader;
import org.apache.lucene.benchmark.quality.utils.SimpleQQParser;
import org.apache.lucene.benchmark.quality.utils.SubmissionReport;
import org.apache.lucene.benchmark.quality.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.store.FSDirectory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
public class QueryDriver {
  public static void main(String[] args) throws Exception {
    if (args.length < 4 || args.length > 5) {
      System.err.println("Usage: QueryDriver <topicsFile> <qrelsFile> <submissionFile> <indexDir> [querySpec]");
      System.err.println("topicsFile: input file containing queries");
      System.err.println("qrelsFile: input file containing relevance judgements");
      System.err.println("submissionFile: output submission file for trec_eval");
      System.err.println("indexDir: index directory");
      System.err.println("querySpec: string composed of fields to use in query consisting of T=title,D=description,N=narrative:");
      System.err.println("\texample: TD (query on Title + Description). The default is T (title only)");
      System.exit(1);
    }
    File topicsFile = new File(args[0]);
    File qrelsFile = new File(args[1]);
    SubmissionReport submitLog = new SubmissionReport(new PrintWriter(args[2]), "lucene");
    FSDirectory dir = FSDirectory.open(new File(args[3]));
    String fieldSpec = args.length == 5 ? args[4] : "T"; 
    Searcher searcher = new IndexSearcher(dir, true);
    int maxResults = 1000;
    String docNameField = "docname";
    PrintWriter logger = new PrintWriter(System.out, true);
    TrecTopicsReader qReader = new TrecTopicsReader();
    QualityQuery qqs[] = qReader.readQueries(new BufferedReader(new FileReader(topicsFile)));
    Judge judge = new TrecJudge(new BufferedReader(new FileReader(qrelsFile)));
    judge.validateData(qqs, logger);
    Set<String> fieldSet = new HashSet<String>();
    if (fieldSpec.indexOf('T') >= 0) fieldSet.add("title");
    if (fieldSpec.indexOf('D') >= 0) fieldSet.add("description");
    if (fieldSpec.indexOf('N') >= 0) fieldSet.add("narrative");
    QualityQueryParser qqParser = new SimpleQQParser(fieldSet.toArray(new String[0]), "body");
    QualityBenchmark qrun = new QualityBenchmark(qqs, qqParser, searcher, docNameField);
    qrun.setMaxResults(maxResults);
    QualityStats stats[] = qrun.execute(judge, submitLog, logger);
    QualityStats avg = QualityStats.average(stats);
    avg.log("SUMMARY", 2, logger, "  ");
  }
}
