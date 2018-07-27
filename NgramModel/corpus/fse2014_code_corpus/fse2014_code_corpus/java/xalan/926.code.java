package org.apache.xpath.operations;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;
public class Equals extends Operation
{
    static final long serialVersionUID = -2658315633903426134L;
  public XObject operate(XObject left, XObject right)
          throws javax.xml.transform.TransformerException
  {
    return left.equals(right) ? XBoolean.S_TRUE : XBoolean.S_FALSE;
  }
  public boolean bool(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    XObject left = m_left.execute(xctxt, true);
    XObject right = m_right.execute(xctxt, true);
    boolean result = left.equals(right) ? true : false;
	left.detach();
	right.detach();
    return result;
  }
}
