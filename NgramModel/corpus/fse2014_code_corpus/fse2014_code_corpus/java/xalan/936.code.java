package org.apache.xpath.operations;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
public class Number extends UnaryOperation
{
    static final long serialVersionUID = 7196954482871619765L;
  public XObject operate(XObject right) throws javax.xml.transform.TransformerException
  {
    if (XObject.CLASS_NUMBER == right.getType())
      return right;
    else
      return new XNumber(right.num());
  }
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return m_right.num(xctxt);
  }
}
