package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class AnalyzerAttributeImpl extends AttributeImpl 
				implements AnalyzerAttribute {
  private static final long serialVersionUID = -6804760312723049526L;
  private Analyzer analyzer;
  public AnalyzerAttributeImpl() {
    analyzer = null; 
  }
  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }
  public Analyzer getAnalyzer() {
    return this.analyzer;
  }
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }
  @Override
  public void copyTo(AttributeImpl target) {
    throw new UnsupportedOperationException();
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof AnalyzerAttributeImpl) {
    	AnalyzerAttributeImpl analyzerAttr = (AnalyzerAttributeImpl) other;
      if (analyzerAttr.analyzer == this.analyzer
          || (this.analyzer != null && analyzerAttr.analyzer != null && this.analyzer
              .equals(analyzerAttr.analyzer))) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode() {
    return (this.analyzer == null) ? 0 : this.analyzer.hashCode();
  }
  @Override
  public String toString() {
    return "<analyzerAttribute analyzer='" + this.analyzer + "'/>";
  }
}
