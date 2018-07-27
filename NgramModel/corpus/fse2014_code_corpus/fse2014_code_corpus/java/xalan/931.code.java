package org.apache.xpath.operations;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
public class Minus extends Operation
{
    static final long serialVersionUID = -5297672838170871043L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return new XNumber(left.num() - right.num());
  }
  public double num(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return (m_left.num(xctxt) - m_right.num(xctxt));
  }
}
