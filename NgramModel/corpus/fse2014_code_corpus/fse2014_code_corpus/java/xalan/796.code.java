package org.apache.xpath;
public interface ExpressionOwner
{
  public Expression getExpression();
  public void setExpression(Expression exp);
}
