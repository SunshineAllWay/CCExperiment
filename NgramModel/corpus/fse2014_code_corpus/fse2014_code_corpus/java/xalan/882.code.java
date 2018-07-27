package org.apache.xpath.functions;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
public class FuncString extends FunctionDef1Arg
{
    static final long serialVersionUID = -2206677149497712883L;
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    return (XString)getArg0AsString(xctxt);
  }
}
