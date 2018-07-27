package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.util.Attribute;
public interface BoostAttribute extends Attribute {
  public void setBoost(float boost);
  public float getBoost();
}
