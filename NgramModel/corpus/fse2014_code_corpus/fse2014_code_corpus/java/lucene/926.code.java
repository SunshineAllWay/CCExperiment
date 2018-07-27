package org.apache.lucene.benchmark.quality;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.util.ArrayList;
public class QualityStats {
  public static final int MAX_POINTS = 20;
  private double maxGoodPoints;
  private double recall;
  private double pAt[];
  private double pReleventSum = 0;
  private double numPoints = 0;
  private double numGoodPoints = 0;
  private double mrr = 0;
  private long searchTime;
  private long docNamesExtractTime;
  public static class RecallPoint {
    private int rank;
    private double recall;
    private RecallPoint(int rank, double recall) {
      this.rank = rank;
      this.recall = recall;
    }
    public int getRank() {
      return rank;
    }
    public double getRecall() {
      return recall;
    }
  }
  private ArrayList<RecallPoint> recallPoints;
  public QualityStats(double maxGoodPoints, long searchTime) {
    this.maxGoodPoints = maxGoodPoints;
    this.searchTime = searchTime;
    this.recallPoints = new ArrayList<RecallPoint>();
    pAt = new double[MAX_POINTS+1]; 
  }
  public void addResult(int n, boolean isRelevant, long docNameExtractTime) {
    if (Math.abs(numPoints+1 - n) > 1E-6) {
      throw new IllegalArgumentException("point "+n+" illegal after "+numPoints+" points!");
    }
    if (isRelevant) {
      numGoodPoints+=1;
      recallPoints.add(new RecallPoint(n,numGoodPoints));
      if (recallPoints.size()==1 && n<=5) { 
        mrr =  1.0 / n;
      }
    }
    numPoints = n;
    double p = numGoodPoints / numPoints;
    if (isRelevant) {
      pReleventSum += p;
    }
    if (n<pAt.length) {
      pAt[n] = p;
    }
    recall = maxGoodPoints<=0 ? p : numGoodPoints/maxGoodPoints;
    docNamesExtractTime += docNameExtractTime;
  }
  public double getPrecisionAt(int n) {
    if (n<1 || n>MAX_POINTS) {
      throw new IllegalArgumentException("n="+n+" - but it must be in [1,"+MAX_POINTS+"] range!"); 
    }
    if (n>numPoints) {
      return (numPoints * pAt[(int)numPoints])/n;
    }
    return pAt[n];
  }
  public double getAvp() {
    return maxGoodPoints==0 ? 0 : pReleventSum/maxGoodPoints;
  }
  public double getRecall() {
    return recall;
  }
  public void log(String title, int paddLines, PrintWriter logger, String prefix) {
    for (int i=0; i<paddLines; i++) {  
      logger.println();
    }
    if (title!=null && title.trim().length()>0) {
      logger.println(title);
    }
    prefix = prefix==null ? "" : prefix;
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(3);
    nf.setMinimumFractionDigits(3);
    nf.setGroupingUsed(true);
    int M = 19;
    logger.println(prefix+format("Search Seconds: ",M)+
        fracFormat(nf.format((double)searchTime/1000)));
    logger.println(prefix+format("DocName Seconds: ",M)+
        fracFormat(nf.format((double)docNamesExtractTime/1000)));
    logger.println(prefix+format("Num Points: ",M)+
        fracFormat(nf.format(numPoints)));
    logger.println(prefix+format("Num Good Points: ",M)+
        fracFormat(nf.format(numGoodPoints)));
    logger.println(prefix+format("Max Good Points: ",M)+
        fracFormat(nf.format(maxGoodPoints)));
    logger.println(prefix+format("Average Precision: ",M)+
        fracFormat(nf.format(getAvp())));
    logger.println(prefix+format("MRR: ",M)+
        fracFormat(nf.format(getMRR())));
    logger.println(prefix+format("Recall: ",M)+
        fracFormat(nf.format(getRecall())));
    for (int i=1; i<(int)numPoints && i<pAt.length; i++) {
      logger.println(prefix+format("Precision At "+i+": ",M)+
          fracFormat(nf.format(getPrecisionAt(i))));
    }
    for (int i=0; i<paddLines; i++) {  
      logger.println();
    }
  }
  private static String padd = "                                    ";
  private String format(String s, int minLen) {
    s = (s==null ? "" : s);
    int n = Math.max(minLen,s.length());
    return (s+padd).substring(0,n);
  }
  private String fracFormat(String frac) {
    int k = frac.indexOf('.');
    String s1 = padd+frac.substring(0,k);
    int n = Math.max(k,6);
    s1 = s1.substring(s1.length()-n);
    return s1 + frac.substring(k);
  }
  public static QualityStats average(QualityStats[] stats) {
    QualityStats avg = new QualityStats(0,0);
    if (stats.length==0) {
      return avg;
    }
    int m = 0; 
    for (int i=0; i<stats.length; i++) {
      avg.searchTime += stats[i].searchTime;
      avg.docNamesExtractTime += stats[i].docNamesExtractTime;
      if (stats[i].maxGoodPoints>0) {
        m++;
        avg.numGoodPoints += stats[i].numGoodPoints;
        avg.numPoints += stats[i].numPoints;
        avg.pReleventSum += stats[i].getAvp();
        avg.recall += stats[i].recall;
        avg.mrr += stats[i].getMRR();
        avg.maxGoodPoints += stats[i].maxGoodPoints;
        for (int j=1; j<avg.pAt.length; j++) {
          avg.pAt[j] += stats[i].getPrecisionAt(j);
        }
      }
    }
    assert m>0 : "Fishy: no \"good\" queries!";
    avg.searchTime /= stats.length;
    avg.docNamesExtractTime /= stats.length;
    avg.numGoodPoints /= m;
    avg.numPoints /= m;
    avg.recall /= m;
    avg.mrr /= m;
    avg.maxGoodPoints /= m;
    for (int j=1; j<avg.pAt.length; j++) {
      avg.pAt[j] /= m;
    }
    avg.pReleventSum /= m;                 
    avg.pReleventSum *= avg.maxGoodPoints; 
    return avg;
  }
  public long getDocNamesExtractTime() {
    return docNamesExtractTime;
  }
  public double getMaxGoodPoints() {
    return maxGoodPoints;
  }
  public double getNumGoodPoints() {
    return numGoodPoints;
  }
  public double getNumPoints() {
    return numPoints;
  }
  public RecallPoint [] getRecallPoints() {
    return recallPoints.toArray(new RecallPoint[0]);
  }
  public double getMRR() {
    return mrr;
  }
  public long getSearchTime() {
    return searchTime;
  }
}
