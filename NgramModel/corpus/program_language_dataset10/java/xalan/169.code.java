package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.transformer.TransformerImpl;
public class ElemFallback extends ElemTemplateElement
{
    static final long serialVersionUID = 1782962139867340703L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_FALLBACK;
  }
  public String getNodeName()
  {
    return Constants.ELEMNAME_FALLBACK_STRING;
  }
  public void execute(
          TransformerImpl transformer)
            throws TransformerException
  {
  }
  public void executeFallback(
          TransformerImpl transformer)
            throws TransformerException
  {
    int parentElemType = m_parentNode.getXSLToken();
    if (Constants.ELEMNAME_EXTENSIONCALL == parentElemType 
        || Constants.ELEMNAME_UNDEFINED == parentElemType)
    {
      if (transformer.getDebug())
        transformer.getTraceManager().fireTraceEvent(this);
      transformer.executeChildTemplates(this, true);
      if (transformer.getDebug())
	    transformer.getTraceManager().fireTraceEndEvent(this); 
    }
    else
    {
      System.out.println(
        "Error!  parent of xsl:fallback must be an extension or unknown element!");
    }
  }
}
