package org.apache.xpath.operations;
import org.apache.xpath.Expression;
import org.apache.xpath.ExpressionOwner;
import org.apache.xpath.XPathContext;
import org.apache.xpath.XPathVisitor;
import org.apache.xpath.objects.XObject;
public class Operation extends Expression implements ExpressionOwner
{
    static final long serialVersionUID = -3037139537171050430L;
  protected Expression m_left;
  protected Expression m_right;
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
    m_left.fixupVariables(vars, globalsSize);
    m_right.fixupVariables(vars, globalsSize);
  }
  public boolean canTraverseOutsideSubtree()
  {
    if (null != m_left && m_left.canTraverseOutsideSubtree())
      return true;
    if (null != m_right && m_right.canTraverseOutsideSubtree())
      return true;
    return false;
  }
  public void setLeftRight(Expression l, Expression r)
  {
    m_left = l;
    m_right = r;
    l.exprSetParent(this);
    r.exprSetParent(this);
  }
  public XObject execute(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    XObject left = m_left.execute(xctxt, true);
    XObject right = m_right.execute(xctxt, true);
    XObject result = operate(left, right);
    left.detach();
    right.detach();
    return result;
  }
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return null;  
  }
  public Expression getLeftOperand(){
    return m_left;
  }
  public Expression getRightOperand(){
    return m_right;
  }
  class LeftExprOwner implements ExpressionOwner
  {
    public Expression getExpression()
    {
      return m_left;
    }
    public void setExpression(Expression exp)
    {
    	exp.exprSetParent(Operation.this);
    	m_left = exp;
    }
  }
  public void callVisitors(ExpressionOwner owner, XPathVisitor visitor)
  {
  	if(visitor.visitBinaryOperation(owner, this))
  	{
  		m_left.callVisitors(new LeftExprOwner(), visitor);
  		m_right.callVisitors(this, visitor);
  	}
  }
  public Expression getExpression()
  {
    return m_right;
  }
  public void setExpression(Expression exp)
  {
  	exp.exprSetParent(this);
  	m_right = exp;
  }
  public boolean deepEquals(Expression expr)
  {
  	if(!isSameClass(expr))
  		return false;
  	if(!m_left.deepEquals(((Operation)expr).m_left))
  		return false;
  	if(!m_right.deepEquals(((Operation)expr).m_right))
  		return false;
  	return true;
  }
}
