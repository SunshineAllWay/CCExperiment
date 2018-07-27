package org.apache.xpath.functions;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
public class FuncCount extends FunctionOneArg
{
    static final long serialVersionUID = -7116225100474153751L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
	DTMIterator nl = m_arg0.asIterator(xctxt, xctxt.getCurrentNode());
	int i = nl.getLength();	
	nl.detach();
    return new XNumber((double) i);
  }
}
