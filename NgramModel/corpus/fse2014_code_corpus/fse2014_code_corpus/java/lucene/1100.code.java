package org.apache.lucene.queryParser.core.config;
import org.apache.lucene.util.AttributeSource;
public class FieldConfig extends AttributeSource {
  private CharSequence fieldName;
  public FieldConfig(CharSequence fieldName) {
    if (fieldName == null) {
      throw new IllegalArgumentException("field name should not be null!");
    }
    this.fieldName = fieldName;
  }
  public CharSequence getFieldName() {
    return this.fieldName;
  }
  @Override
  public String toString(){
    return "<fieldconfig name=\"" + this.fieldName + "\" attributes=\"" + super.toString() + "\"/>";
  }
}
