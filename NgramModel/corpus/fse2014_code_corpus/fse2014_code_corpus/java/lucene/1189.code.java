package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.AnalyzerQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface PositionIncrementsAttribute extends Attribute {
  public void setPositionIncrementsEnabled(boolean positionIncrementsEnabled);
  public boolean isPositionIncrementsEnabled();
}
