package org.apache.solr.spelling;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import java.util.Collection;
public abstract class QueryConverter implements NamedListInitializedPlugin {
  private NamedList args;
  protected Analyzer analyzer;
  public void init(NamedList args) {
    this.args = args;
  }
  public abstract Collection<Token> convert(String original);
  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }
  public Analyzer getAnalyzer() {
    return analyzer;
  }
}
