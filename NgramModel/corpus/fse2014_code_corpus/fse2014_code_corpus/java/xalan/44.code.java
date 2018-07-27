package org.apache.xalan.extensions;
import javax.xml.transform.ErrorListener;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;
import org.w3c.dom.traversal.NodeIterator;
public interface ExpressionContext
{
  public Node getContextNode();
  public NodeIterator getContextNodes();
  public ErrorListener getErrorListener();
  public double toNumber(Node n);
  public String toString(Node n);
  public XObject getVariableOrParam(org.apache.xml.utils.QName qname)
            throws javax.xml.transform.TransformerException;
  public org.apache.xpath.XPathContext getXPathContext()
            throws javax.xml.transform.TransformerException;
}
