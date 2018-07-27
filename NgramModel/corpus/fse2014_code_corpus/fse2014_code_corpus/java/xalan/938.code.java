package org.apache.xpath.operations;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XObject;
public class Or extends Operation
{
    static final long serialVersionUID = -644107191353853079L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    XObject expr1 = m_left.execute(xctxt);
    if (!expr1.bool())
    {
      XObject expr2 = m_right.execute(xctxt);
      return expr2.bool() ? XBoolean.S_TRUE : XBoolean.S_FALSE;
    }
    else
      return XBoolean.S_TRUE;
  }
  public boolean bool(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    return (m_left.bool(xctxt) || m_right.bool(xctxt));
  }
}
