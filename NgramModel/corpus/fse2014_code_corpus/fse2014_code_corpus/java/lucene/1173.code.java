package org.apache.lucene.queryParser.standard.config;
import java.util.Map;
import org.apache.lucene.queryParser.core.config.FieldConfig;
import org.apache.lucene.util.Attribute;
public interface FieldBoostMapAttribute extends Attribute {
  public void setFieldBoostMap(Map<CharSequence, Float> boosts);
  public Map<CharSequence, Float> getFieldBoostMap();
}
