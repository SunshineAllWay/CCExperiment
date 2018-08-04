package org.apache.xalan.trace;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
public class TracerEvent implements java.util.EventListener
{
  public final ElemTemplateElement m_styleNode;
  public final TransformerImpl m_processor;
  public final Node m_sourceNode;
  public final QName m_mode;
  public TracerEvent(TransformerImpl processor, Node sourceNode, QName mode,
                     ElemTemplateElement styleNode)
  {
    this.m_processor = processor;
    this.m_sourceNode = sourceNode;
    this.m_mode = mode;
    this.m_styleNode = styleNode;
  }
  public static String printNode(Node n)
  {
    String r = n.hashCode() + " ";
    if (n instanceof Element)
    {
      r += "<" + n.getNodeName();
      Node c = n.getFirstChild();
      while (null != c)
      {
        if (c instanceof Attr)
        {
          r += printNode(c) + " ";
        }
        c = c.getNextSibling();
      }
      r += ">";
    }
    else
    {
      if (n instanceof Attr)
      {
        r += n.getNodeName() + "=" + n.getNodeValue();
      }
      else
      {
        r += n.getNodeName();
      }
    }
    return r;
  }
  public static String printNodeList(NodeList l)
  {
    String r = l.hashCode() + "[";
    int len = l.getLength() - 1;
    int i = 0;
    while (i < len)
    {
      Node n = l.item(i);
      if (null != n)
      {
        r += printNode(n) + ", ";
      }
      ++i;
    }
    if (i == len)
    {
      Node n = l.item(len);
      if (null != n)
      {
        r += printNode(n);
      }
    }
    return r + "]";
  }
}