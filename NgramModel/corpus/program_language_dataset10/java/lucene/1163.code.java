package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface AnalyzerAttribute extends Attribute {
  public void setAnalyzer(Analyzer analyzer);
  public Analyzer getAnalyzer();
}
