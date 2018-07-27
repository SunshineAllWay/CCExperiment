package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
public class ElemAttributeSet extends ElemUse
{
    static final long serialVersionUID = -426740318278164496L;
  public QName m_qname = null;
  public void setName(QName name)
  {
    m_qname = name;
  }
  public QName getName()
  {
    return m_qname;
  }
  public int getXSLToken()
  {
    return Constants.ELEMNAME_DEFINEATTRIBUTESET;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_ATTRIBUTESET_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.getDebug())
	  transformer.getTraceManager().fireTraceEvent(this);
    if (transformer.isRecursiveAttrSet(this))
    {
      throw new TransformerException(
        XSLMessages.createMessage(
          XSLTErrorResources.ER_XSLATTRSET_USED_ITSELF,
          new Object[]{ m_qname.getLocalPart() }));  
    }
    transformer.pushElemAttributeSet(this);
    super.execute(transformer);
    ElemAttribute attr = (ElemAttribute) getFirstChildElem();
    while (null != attr)
    {
      attr.execute(transformer);
      attr = (ElemAttribute) attr.getNextSiblingElem();
    }
    transformer.popElemAttributeSet();
    if (transformer.getDebug())
	  transformer.getTraceManager().fireTraceEndEvent(this);
  }
  public ElemTemplateElement appendChildElem(ElemTemplateElement newChild)
  {
    int type = ((ElemTemplateElement) newChild).getXSLToken();
    switch (type)
    {
    case Constants.ELEMNAME_ATTRIBUTE :
      break;
    default :
      error(XSLTErrorResources.ER_CANNOT_ADD,
            new Object[]{ newChild.getNodeName(),
                          this.getNodeName() });  
    }
    return super.appendChild(newChild);
  }
  public void recompose(StylesheetRoot root)
  {
    root.recomposeAttributeSets(this);
  }
}
