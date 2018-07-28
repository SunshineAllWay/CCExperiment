package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.lang.reflect.Constructor;
public class NewAnalyzerTask extends PerfTask {
  private List<String> analyzerClassNames;
  private int current;
  public NewAnalyzerTask(PerfRunData runData) {
    super(runData);
    analyzerClassNames = new ArrayList<String>();
  }
  public static final Analyzer createAnalyzer(String className) throws Exception{
    final Class<? extends Analyzer> clazz = Class.forName(className).asSubclass(Analyzer.class);
    try {
      Constructor<? extends Analyzer> cnstr = clazz.getConstructor(Version.class);
      return cnstr.newInstance(Version.LUCENE_CURRENT);
    } catch (NoSuchMethodException nsme) {
      return clazz.newInstance();
    }
  }
  @Override
  public int doLogic() throws IOException {
    String className = null;
    try {
      if (current >= analyzerClassNames.size())
      {
        current = 0;
      }
      className = analyzerClassNames.get(current++);
      if (className == null || className.equals(""))
      {
        className = "org.apache.lucene.analysis.standard.StandardAnalyzer"; 
      }
      if (className.indexOf(".") == -1  || className.startsWith("standard."))
      {
        className = "org.apache.lucene.analysis." + className;
      }
      getRunData().setAnalyzer(createAnalyzer(className));
      System.out.println("Changed Analyzer to: " + className);
    } catch (Exception e) {
      throw new RuntimeException("Error creating Analyzer: " + className, e);
    }
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    for (StringTokenizer tokenizer = new StringTokenizer(params, ","); tokenizer.hasMoreTokens();) {
      String s = tokenizer.nextToken();
      analyzerClassNames.add(s.trim());
    }
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
