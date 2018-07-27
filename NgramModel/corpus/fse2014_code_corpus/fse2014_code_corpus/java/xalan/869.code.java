package org.apache.xpath.functions;
import org.apache.xml.dtm.DTM;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
public class FuncGenerateId extends FunctionDef1Arg
{
    static final long serialVersionUID = 973544842091724273L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    int which = getArg0AsNode(xctxt);
    if (DTM.NULL != which)
    {
      return new XString("N" + Integer.toHexString(which).toUpperCase());
    }
    else
      return XString.EMPTYSTRING;
  }
}
