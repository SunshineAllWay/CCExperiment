package org.apache.lucene.queryParser.standard.config;
import java.util.Map;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.util.Attribute;
public interface FieldDateResolutionMapAttribute extends Attribute {
  public void setFieldDateResolutionMap(Map<CharSequence, DateTools.Resolution> dateRes);
  public Map<CharSequence, DateTools.Resolution> getFieldDateResolutionMap();
}
