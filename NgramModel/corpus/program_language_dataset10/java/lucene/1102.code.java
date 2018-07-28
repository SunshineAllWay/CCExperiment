package org.apache.lucene.queryParser.core.config;
import java.util.LinkedList;
import org.apache.lucene.queryParser.core.processors.QueryNodeProcessor;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeSource;
public abstract class QueryConfigHandler extends AttributeSource {
  private LinkedList<FieldConfigListener> listeners = new LinkedList<FieldConfigListener>();
  public FieldConfig getFieldConfig(CharSequence fieldName) {
    FieldConfig fieldConfig = new FieldConfig(fieldName);
    for (FieldConfigListener listener : this.listeners) {
      listener.buildFieldConfig(fieldConfig);
    }
    return fieldConfig;
  }
  public void addFieldConfigListener(FieldConfigListener listener) {
    this.listeners.add(listener);
  }
}
