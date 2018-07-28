package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.PhraseSlopQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface FuzzyAttribute extends Attribute {
  public void setPrefixLength(int prefixLength);
  public int getPrefixLength();
  public void setFuzzyMinSimilarity(float minSimilarity);
  public float getFuzzyMinSimilarity();
}
