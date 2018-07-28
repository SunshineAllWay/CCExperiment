package org.apache.xpath.functions;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
public class FuncQname extends FunctionDef1Arg
{
    static final long serialVersionUID = -1532307875532617380L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int context = getArg0AsNode(xctxt);
    XObject val;
    if (DTM.NULL != context)
    {
      DTM dtm = xctxt.getDTM(context);
      String qname = dtm.getNodeNameX(context);
      val = (null == qname) ? XString.EMPTYSTRING : new XString(qname);
    }
    else
    {
      val = XString.EMPTYSTRING;
    }
    return val;
  }
}
