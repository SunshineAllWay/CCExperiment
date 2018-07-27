package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.ClonerToResultTree;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.dtm.DTM;
import org.apache.xalan.serialize.SerializerUtils;
import org.apache.xml.serializer.SerializationHandler;
import org.apache.xpath.XPathContext;
public class ElemCopy extends ElemUse
{
    static final long serialVersionUID = 5478580783896941384L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_COPY;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_COPY_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
                XPathContext xctxt = transformer.getXPathContext();
    try
    {
      int sourceNode = xctxt.getCurrentNode();
      xctxt.pushCurrentNode(sourceNode);
      DTM dtm = xctxt.getDTM(sourceNode);
      short nodeType = dtm.getNodeType(sourceNode);
      if ((DTM.DOCUMENT_NODE != nodeType) && (DTM.DOCUMENT_FRAGMENT_NODE != nodeType))
      {
        SerializationHandler rthandler = transformer.getSerializationHandler();
        if (transformer.getDebug())
          transformer.getTraceManager().fireTraceEvent(this);
        ClonerToResultTree.cloneToResultTree(sourceNode, nodeType, dtm, 
                                             rthandler, false);
        if (DTM.ELEMENT_NODE == nodeType)
        {
          super.execute(transformer);
          SerializerUtils.processNSDecls(rthandler, sourceNode, nodeType, dtm);
          transformer.executeChildTemplates(this, true);
          String ns = dtm.getNamespaceURI(sourceNode);
          String localName = dtm.getLocalName(sourceNode);
          transformer.getResultTreeHandler().endElement(ns, localName,
                                                        dtm.getNodeName(sourceNode));
        }
        if (transformer.getDebug())
		  transformer.getTraceManager().fireTraceEndEvent(this);         
      }
      else
      {
        if (transformer.getDebug())
          transformer.getTraceManager().fireTraceEvent(this);
        super.execute(transformer);
        transformer.executeChildTemplates(this, true);
        if (transformer.getDebug())
          transformer.getTraceManager().fireTraceEndEvent(this);
      }
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    finally
    {
      xctxt.popCurrentNode();
    }
  }
}
