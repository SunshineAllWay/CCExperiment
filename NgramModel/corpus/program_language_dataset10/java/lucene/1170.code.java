package org.apache.lucene.queryParser.standard.config;
import org.apache.lucene.queryParser.core.config.QueryConfigHandler;
import org.apache.lucene.queryParser.standard.processors.GroupQueryNodeProcessor;
import org.apache.lucene.util.AttributeImpl;
public class DefaultOperatorAttributeImpl extends AttributeImpl
				implements DefaultOperatorAttribute {
  private static final long serialVersionUID = -6804760312723049526L;
  private Operator operator = Operator.OR;
  public DefaultOperatorAttributeImpl() {
  }
  public void setOperator(Operator operator) {
    if (operator == null) {
      throw new IllegalArgumentException("default operator cannot be null!");
    }
    this.operator = operator;
  }
  public Operator getOperator() {
    return this.operator;
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
    if (other instanceof DefaultOperatorAttributeImpl) {
    	DefaultOperatorAttributeImpl defaultOperatorAttr = (DefaultOperatorAttributeImpl) other;
      if (defaultOperatorAttr.getOperator() == this.getOperator()) {
        return true;
      }
    }
    return false;
  }
  @Override
  public int hashCode() {
    return getOperator().hashCode() * 31;
  }
  @Override
  public String toString() {
    return "<defaultOperatorAttribute operator=" + this.operator.name() + "/>";
  }
}
