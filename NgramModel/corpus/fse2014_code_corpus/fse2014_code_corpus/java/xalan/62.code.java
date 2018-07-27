package org.apache.xalan.extensions;
import javax.xml.transform.TransformerException;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.transformer.ClonerToResultTree;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xml.utils.QName;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.DescendantIterator;
import org.apache.xpath.axes.OneStepIterator;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XNodeSet;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XRTreeFrag;
import org.apache.xpath.objects.XString;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.traversal.NodeIterator;
public class XSLProcessorContext
{
  public XSLProcessorContext(TransformerImpl transformer,
                             Stylesheet stylesheetTree)
  {
    this.transformer = transformer;
    this.stylesheetTree = stylesheetTree;
    org.apache.xpath.XPathContext xctxt = transformer.getXPathContext();
    this.mode = transformer.getMode();
    this.sourceNode = xctxt.getCurrentNode();
    this.sourceTree = xctxt.getDTM(this.sourceNode);
  }
  private TransformerImpl transformer;
  public TransformerImpl getTransformer()
  {
    return transformer;
  }
  private Stylesheet stylesheetTree;
  public Stylesheet getStylesheet()
  {
    return stylesheetTree;
  }
  private org.apache.xml.dtm.DTM sourceTree;
  public org.w3c.dom.Node getSourceTree()
  {
    return sourceTree.getNode(sourceTree.getDocumentRoot(sourceNode));
  }
  private int sourceNode;
  public org.w3c.dom.Node getContextNode()
  {
    return sourceTree.getNode(sourceNode);
  }
  private QName mode;
  public QName getMode()
  {
    return mode;
  }
  public void outputToResultTree(Stylesheet stylesheetTree, Object obj)
          throws TransformerException, java.net.MalformedURLException,
                 java.io.FileNotFoundException, java.io.IOException
  {
    try
    {
      SerializationHandler rtreeHandler = transformer.getResultTreeHandler();
      XPathContext xctxt = transformer.getXPathContext();
      XObject value;
      if (obj instanceof XObject)
      {
        value = (XObject) obj;
      }
      else if (obj instanceof String)
      {
        value = new XString((String) obj);
      }
      else if (obj instanceof Boolean)
      {
        value = new XBoolean(((Boolean) obj).booleanValue());
      }
      else if (obj instanceof Double)
      {
        value = new XNumber(((Double) obj).doubleValue());
      }
      else if (obj instanceof DocumentFragment)
      {
        int handle = xctxt.getDTMHandleFromNode((DocumentFragment)obj);
        value = new XRTreeFrag(handle, xctxt);
      }
      else if (obj instanceof DTM)
      {
        DTM dtm = (DTM)obj;
        DTMIterator iterator = new DescendantIterator();
        iterator.setRoot(dtm.getDocument(), xctxt);
        value = new XNodeSet(iterator);
      }
      else if (obj instanceof DTMAxisIterator)
      {
        DTMAxisIterator iter = (DTMAxisIterator)obj;
        DTMIterator iterator = new OneStepIterator(iter, -1);
        value = new XNodeSet(iterator);
      }
      else if (obj instanceof DTMIterator)
      {
        value = new XNodeSet((DTMIterator) obj);
      }
      else if (obj instanceof NodeIterator)
      {
        value = new XNodeSet(new org.apache.xpath.NodeSetDTM(((NodeIterator)obj), xctxt));
      }
      else if (obj instanceof org.w3c.dom.Node)
      {
        value =
          new XNodeSet(xctxt.getDTMHandleFromNode((org.w3c.dom.Node) obj),
                       xctxt.getDTMManager());
      }
      else
      {
        value = new XString(obj.toString());
      }
      int type = value.getType();
      String s;
      switch (type)
      {
      case XObject.CLASS_BOOLEAN :
      case XObject.CLASS_NUMBER :
      case XObject.CLASS_STRING :
        s = value.str();
        rtreeHandler.characters(s.toCharArray(), 0, s.length());
        break;
      case XObject.CLASS_NODESET :  
        DTMIterator nl = value.iter();
        int pos;
        while (DTM.NULL != (pos = nl.nextNode()))
        {
          DTM dtm = nl.getDTM(pos);
          int top = pos;
          while (DTM.NULL != pos)
          {
            rtreeHandler.flushPending();
            ClonerToResultTree.cloneToResultTree(pos, dtm.getNodeType(pos), 
                                                   dtm, rtreeHandler, true);
            int nextNode = dtm.getFirstChild(pos);
            while (DTM.NULL == nextNode)
            {
              if (DTM.ELEMENT_NODE == dtm.getNodeType(pos))
              {
                rtreeHandler.endElement("", "", dtm.getNodeName(pos));
              }
              if (top == pos)
                break;
              nextNode = dtm.getNextSibling(pos);
              if (DTM.NULL == nextNode)
              {
                pos = dtm.getParent(pos);
                if (top == pos)
                {
                  if (DTM.ELEMENT_NODE == dtm.getNodeType(pos))
                  {
                    rtreeHandler.endElement("", "", dtm.getNodeName(pos));
                  }
                  nextNode = DTM.NULL;
                  break;
                }
              }
            }
            pos = nextNode;
          }
        }
        break;
      case XObject.CLASS_RTREEFRAG :
        SerializerUtils.outputResultTreeFragment(
            rtreeHandler, value, transformer.getXPathContext());
        break;
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
  }
}
