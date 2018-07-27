package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.XML11Char;
import org.apache.xpath.XPathContext;
public class ElemPI extends ElemTemplateElement
{
    static final long serialVersionUID = 5621976448020889825L;
  private AVT m_name_atv = null;
  public void setName(AVT v)
  {
    m_name_atv = v;
  }
  public AVT getName()
  {
    return m_name_atv;
  }
  public void compose(StylesheetRoot sroot) throws TransformerException
  {
    super.compose(sroot);
    java.util.Vector vnames = sroot.getComposeState().getVariableNames();
    if(null != m_name_atv)
      m_name_atv.fixupVariables(vnames, sroot.getComposeState().getGlobalsSize());
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_PI;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_PI_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEvent(this);
    XPathContext xctxt = transformer.getXPathContext();
    int sourceNode = xctxt.getCurrentNode();
    String piName = m_name_atv == null ? null : m_name_atv.evaluate(xctxt, sourceNode, this);
    if (piName == null) return;
    if (piName.equalsIgnoreCase("xml"))
    {
     	transformer.getMsgMgr().warn(
        this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NAME_CANT_BE_XML,
              new Object[]{ Constants.ATTRNAME_NAME, piName });
		return;
    }
    else if ((!m_name_atv.isSimple()) && (!XML11Char.isXML11ValidNCName(piName)))
    {
     	transformer.getMsgMgr().warn(
        this, XSLTErrorResources.WG_PROCESSINGINSTRUCTION_NOTVALID_NCNAME,
              new Object[]{ Constants.ATTRNAME_NAME, piName });
		return;    	
    }
    String data = transformer.transformToString(this);
    try
    {
      transformer.getResultTreeHandler().processingInstruction(piName, data);
    }
    catch(org.xml.sax.SAXException se)
    {
      throw new TransformerException(se);
    }
    if (transformer.getDebug())
      transformer.getTraceManager().fireTraceEndEvent(this);
  }
  public ElemTemplateElement appendChild(ElemTemplateElement newChild)
  {
    int type = ((ElemTemplateElement) newChild).getXSLToken();
    switch (type)
    {
    case Constants.ELEMNAME_TEXTLITERALRESULT :
    case Constants.ELEMNAME_APPLY_TEMPLATES :
    case Constants.ELEMNAME_APPLY_IMPORTS :
    case Constants.ELEMNAME_CALLTEMPLATE :
    case Constants.ELEMNAME_FOREACH :
    case Constants.ELEMNAME_VALUEOF :
    case Constants.ELEMNAME_COPY_OF :
    case Constants.ELEMNAME_NUMBER :
    case Constants.ELEMNAME_CHOOSE :
    case Constants.ELEMNAME_IF :
    case Constants.ELEMNAME_TEXT :
    case Constants.ELEMNAME_COPY :
    case Constants.ELEMNAME_VARIABLE :
    case Constants.ELEMNAME_MESSAGE :
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  
    }
    return super.appendChild(newChild);
  }
}
