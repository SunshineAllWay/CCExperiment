package org.apache.lucene.analysis;
import org.apache.lucene.document.Fieldable;
import java.io.Reader;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
public class PerFieldAnalyzerWrapper extends Analyzer {
  private Analyzer defaultAnalyzer;
  private Map<String,Analyzer> analyzerMap = new HashMap<String,Analyzer>();
  public PerFieldAnalyzerWrapper(Analyzer defaultAnalyzer) {
    this(defaultAnalyzer, null);
  }
  public PerFieldAnalyzerWrapper(Analyzer defaultAnalyzer, 
      Map<String,Analyzer> fieldAnalyzers) {
    this.defaultAnalyzer = defaultAnalyzer;
    if (fieldAnalyzers != null) {
      analyzerMap.putAll(fieldAnalyzers);
    }
  }
  public void addAnalyzer(String fieldName, Analyzer analyzer) {
    analyzerMap.put(fieldName, analyzer);
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    Analyzer analyzer = analyzerMap.get(fieldName);
    if (analyzer == null) {
      analyzer = defaultAnalyzer;
    }
    return analyzer.tokenStream(fieldName, reader);
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
    if (overridesTokenStreamMethod) {
      return tokenStream(fieldName, reader);
    }
    Analyzer analyzer = analyzerMap.get(fieldName);
    if (analyzer == null)
      analyzer = defaultAnalyzer;
    return analyzer.reusableTokenStream(fieldName, reader);
  }
  @Override
  public int getPositionIncrementGap(String fieldName) {
    Analyzer analyzer = analyzerMap.get(fieldName);
    if (analyzer == null)
      analyzer = defaultAnalyzer;
    return analyzer.getPositionIncrementGap(fieldName);
  }
  @Override
  public int getOffsetGap(Fieldable field) {
    Analyzer analyzer = analyzerMap.get(field.name());
    if (analyzer == null)
      analyzer = defaultAnalyzer;
    return analyzer.getOffsetGap(field);
  }
  @Override
  public String toString() {
    return "PerFieldAnalyzerWrapper(" + analyzerMap + ", default=" + defaultAnalyzer + ")";
  }
}
