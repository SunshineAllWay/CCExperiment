package org.apache.lucene.queryParser.spans;
import org.apache.lucene.queryParser.core.nodes.FieldableNode;
import org.apache.lucene.util.AttributeImpl;
public class UniqueFieldAttributeImpl extends AttributeImpl implements
    UniqueFieldAttribute {
  private static final long serialVersionUID = 8553318595851064232L;
  private CharSequence uniqueField;
  public UniqueFieldAttributeImpl() {
    clear();
  }
  @Override
  public void clear() {
    this.uniqueField = "";
  }
  public void setUniqueField(CharSequence uniqueField) {
    this.uniqueField = uniqueField;
  }
  public CharSequence getUniqueField() {
    return this.uniqueField;
  }
  @Override
  public void copyTo(AttributeImpl target) {
    if (!(target instanceof UniqueFieldAttributeImpl)) {
      throw new IllegalArgumentException(
          "cannot copy the values from attribute UniqueFieldAttribute to an instance of "
              + target.getClass().getName());
    }
    UniqueFieldAttributeImpl uniqueFieldAttr = (UniqueFieldAttributeImpl) target;
    uniqueFieldAttr.uniqueField = uniqueField.toString();
  }
  @Override
  public boolean equals(Object other) {
    if (other instanceof UniqueFieldAttributeImpl) {
      return ((UniqueFieldAttributeImpl) other).uniqueField
          .equals(this.uniqueField);
    }
    return false;
  }
  @Override
  public int hashCode() {
    return this.uniqueField.hashCode();
  }
  @Override
  public String toString() {
    return "<uniqueField uniqueField='" + this.uniqueField + "'/>";
  }
}
