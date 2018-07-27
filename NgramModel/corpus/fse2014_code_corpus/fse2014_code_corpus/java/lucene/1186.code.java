package org.apache.lucene.queryParser.standard.config;
import java.util.Arrays;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.MultiFieldQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class MultiFieldAttributeImpl extends AttributeImpl
				implements MultiFieldAttribute {
  private static final long serialVersionUID = -6809760312720049526L;
  private CharSequence[] fields;
  public MultiFieldAttributeImpl() {
  }
  public void setFields(CharSequence[] fields) {
    this.fields = fields;
  }
  public CharSequence[] getFields() {
    return this.fields;
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
    if (other instanceof MultiFieldAttributeImpl) {
    	MultiFieldAttributeImpl fieldsAttr = (MultiFieldAttributeImpl) other;
      return Arrays.equals(this.fields, fieldsAttr.fields);
    }
    return false;
  }
  @Override
  public int hashCode() {
    return Arrays.hashCode(this.fields);
  }
  @Override
  public String toString() {
    return "<fieldsAttribute fields=" + Arrays.toString(this.fields) + "/>";
  }
}
