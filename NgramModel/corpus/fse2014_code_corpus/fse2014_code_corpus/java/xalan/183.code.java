package org.apache.xalan.templates;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xpath.XPathContext;
public class ElemUnknown extends ElemLiteralResult
{
    static final long serialVersionUID = -4573981712648730168L;
  public int getXSLToken()
  {
    return Constants.ELEMNAME_UNDEFINED;
  }
  private void executeFallbacks(
          TransformerImpl transformer)
            throws TransformerException
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
      {
        try
        {
          transformer.pushElemTemplateElement(child);
          ((ElemFallback) child).executeFallback(transformer);
        }
        finally
        {
          transformer.popElemTemplateElement();
        }
      }
    }
  }
  private boolean hasFallbackChildren()
  {
    for (ElemTemplateElement child = m_firstChild; child != null;
             child = child.m_nextSibling)
    {
      if (child.getXSLToken() == Constants.ELEMNAME_FALLBACK)
        return true;
    }
    return false;
  }
  public void execute(TransformerImpl transformer)
            throws TransformerException
  {
    if (transformer.getDebug())
		transformer.getTraceManager().fireTraceEvent(this);
	try {
		if (hasFallbackChildren()) {
			executeFallbacks(transformer);
		} else {
		}
	} catch (TransformerException e) {
		transformer.getErrorListener().fatalError(e);
	}
    if (transformer.getDebug())
		transformer.getTraceManager().fireTraceEndEvent(this);
  }
}
