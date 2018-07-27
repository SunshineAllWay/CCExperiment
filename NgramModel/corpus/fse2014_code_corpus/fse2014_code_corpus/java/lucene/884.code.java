package org.apache.lucene.benchmark.byTask.tasks;
import java.lang.reflect.Constructor;
import java.util.StringTokenizer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.util.Version;
public class NewShingleAnalyzerTask extends PerfTask {
  private String analyzerClassName = "standard.StandardAnalyzer";
  private int maxShingleSize = 2;
  private boolean outputUnigrams = true;
  public NewShingleAnalyzerTask(PerfRunData runData) {
    super(runData);
  }
  private void setAnalyzer() throws Exception {
    Class<? extends Analyzer> clazz = null;
    Analyzer wrappedAnalyzer;
    try {
      if (analyzerClassName == null || analyzerClassName.equals("")) {
        analyzerClassName 
          = "org.apache.lucene.analysis.standard.StandardAnalyzer"; 
      }
      if (analyzerClassName.indexOf(".") == -1 
          || analyzerClassName.startsWith("standard.")) {
        analyzerClassName = "org.apache.lucene.analysis." + analyzerClassName;
      }
      clazz = Class.forName(analyzerClassName).asSubclass(Analyzer.class);
      Constructor<? extends Analyzer> ctor = clazz.getConstructor(Version.class);
      wrappedAnalyzer = ctor.newInstance(Version.LUCENE_CURRENT);
    } catch (NoSuchMethodException e) {
      wrappedAnalyzer = clazz.newInstance();
    }
    ShingleAnalyzerWrapper analyzer 
      = new ShingleAnalyzerWrapper(wrappedAnalyzer, maxShingleSize);
    analyzer.setOutputUnigrams(outputUnigrams);
    getRunData().setAnalyzer(analyzer);
  }
  @Override
  public int doLogic() throws Exception {
    try {
      setAnalyzer();
      System.out.println
        ("Changed Analyzer to: ShingleAnalyzerWrapper, wrapping ShingleFilter over" 
         + analyzerClassName);
    } catch (Exception e) {
      throw new RuntimeException("Error creating Analyzer", e);
    }
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    StringTokenizer st = new StringTokenizer(params, ",");
    while (st.hasMoreTokens()) {
      String param = st.nextToken();
      StringTokenizer expr = new StringTokenizer(param, ":");
      String key = expr.nextToken();
      String value = expr.nextToken();
      if (key.equalsIgnoreCase("analyzer")) {
        analyzerClassName = value;
      } else if (key.equalsIgnoreCase("outputUnigrams")) {
        outputUnigrams = Boolean.parseBoolean(value);
      } else if (key.equalsIgnoreCase("maxShingleSize")) {
        maxShingleSize = (int)Double.parseDouble(value);
      } else {
        throw new RuntimeException("Unknown parameter " + param);
      }
    }
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
