package org.apache.xpath.objects;
import org.apache.xml.dtm.Axis;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMAxisIterator;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathContext;
import org.apache.xpath.axes.OneStepIterator;
public class XObjectFactory
{
  static public XObject create(Object val)
  {
    XObject result;
    if (val instanceof XObject)
    {
      result = (XObject) val;
    }
    else if (val instanceof String)
    {
      result = new XString((String) val);
    }
    else if (val instanceof Boolean)
    {
      result = new XBoolean((Boolean)val);
    }
    else if (val instanceof Double)
    {
      result = new XNumber(((Double) val));
    }
    else
    {
      result = new XObject(val);
    }
    return result;
  }
  static public XObject create(Object val, XPathContext xctxt)
  {
    XObject result;
    if (val instanceof XObject)
    {
      result = (XObject) val;
    }
    else if (val instanceof String)
    {
      result = new XString((String) val);
    }
    else if (val instanceof Boolean)
    {
      result = new XBoolean((Boolean)val);
    }
    else if (val instanceof Number)
    {
      result = new XNumber(((Number) val));
    }
    else if (val instanceof DTM)
    {
      DTM dtm = (DTM)val;
      try
      {
        int dtmRoot = dtm.getDocument();
        DTMAxisIterator iter = dtm.getAxisIterator(Axis.SELF);
        iter.setStartNode(dtmRoot);
        DTMIterator iterator = new OneStepIterator(iter, Axis.SELF);
        iterator.setRoot(dtmRoot, xctxt);
        result = new XNodeSet(iterator);
      }
      catch(Exception ex)
      {
        throw new org.apache.xml.utils.WrappedRuntimeException(ex);
      }
    }
    else if (val instanceof DTMAxisIterator)
    {
      DTMAxisIterator iter = (DTMAxisIterator)val;
      try
      {
        DTMIterator iterator = new OneStepIterator(iter, Axis.SELF);
        iterator.setRoot(iter.getStartNode(), xctxt);
        result = new XNodeSet(iterator);
      }
      catch(Exception ex)
      {
        throw new org.apache.xml.utils.WrappedRuntimeException(ex);
      }
    }
    else if (val instanceof DTMIterator)
    {
      result = new XNodeSet((DTMIterator) val);
    }
    else if (val instanceof org.w3c.dom.Node)
    {
      result = new XNodeSetForDOM((org.w3c.dom.Node)val, xctxt);
    }
    else if (val instanceof org.w3c.dom.NodeList)
    {
      result = new XNodeSetForDOM((org.w3c.dom.NodeList)val, xctxt);
    }
    else if (val instanceof org.w3c.dom.traversal.NodeIterator)
    {
      result = new XNodeSetForDOM((org.w3c.dom.traversal.NodeIterator)val, xctxt);
    }
    else
    {
      result = new XObject(val);
    }
    return result;
  }
}
