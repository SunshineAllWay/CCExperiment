package org.apache.xpath.functions;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.SubContextList;
import org.apache.xpath.compiler.Compiler;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
public class FuncLast extends Function
{
    static final long serialVersionUID = 9205812403085432943L;
  private boolean m_isTopLevel;
  public void postCompileStep(Compiler compiler)
  {
    m_isTopLevel = compiler.getLocationPathDepth() == -1;
  }
  public int getCountOfContextNodeList(XPathContext xctxt)
          throws javax.xml.transform.TransformerException
  {
    SubContextList iter = m_isTopLevel ? null : xctxt.getSubContextList();
    if (null != iter)
      return iter.getLastPos(xctxt);
    DTMIterator cnl = xctxt.getContextNodeList();
    int count;
    if(null != cnl)
    {
      count = cnl.getLength();
    }
    else
      count = 0;   
    return count;
  }
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    XNumber xnum = new XNumber((double) getCountOfContextNodeList(xctxt));
    return xnum;
  }
  public void fixupVariables(java.util.Vector vars, int globalsSize)
  {
  }
}
