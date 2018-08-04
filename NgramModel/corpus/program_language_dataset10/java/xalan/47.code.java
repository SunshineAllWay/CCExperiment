package org.apache.xalan.extensions;
import java.io.IOException;
import java.util.Vector;
import javax.xml.transform.TransformerException;
import org.apache.xalan.res.XSLMessages;
import org.apache.xalan.res.XSLTErrorResources;
import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemExsltFuncResult;
import org.apache.xalan.templates.ElemExsltFunction;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.templates.Stylesheet;
import org.apache.xalan.templates.StylesheetRoot;
import org.apache.xalan.transformer.TransformerImpl;
import org.apache.xml.utils.QName;
import org.apache.xpath.ExpressionNode;
import org.apache.xpath.XPathContext;
import org.apache.xpath.functions.FuncExtFunction;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
public class ExtensionHandlerExsltFunction extends ExtensionHandler
{
  private String m_namespace;
  private StylesheetRoot m_stylesheet;
  private static final QName RESULTQNAME = 
                  new QName(Constants.S_EXSLT_FUNCTIONS_URL,
                            Constants.EXSLT_ELEMNAME_FUNCRESULT_STRING);
  public ExtensionHandlerExsltFunction(String ns, StylesheetRoot stylesheet)
  {
    super(ns, "xml"); 
    m_namespace = ns;
    m_stylesheet = stylesheet;
  }
  public void processElement(
    String localPart, ElemTemplateElement element, TransformerImpl transformer,
    Stylesheet stylesheetTree, Object methodKey) throws TransformerException, IOException
  {}
  public ElemExsltFunction getFunction(String funcName)
  {
    QName qname = new QName(m_namespace, funcName);
    ElemTemplate templ = m_stylesheet.getTemplateComposed(qname);
    if (templ != null && templ instanceof ElemExsltFunction)
      return (ElemExsltFunction) templ;
    else
      return null;    
  }
  public boolean isFunctionAvailable(String funcName)
  {
    return getFunction(funcName)!= null;
  }
  public boolean isElementAvailable(String elemName)
  {
    if (!(new QName(m_namespace, elemName).equals(RESULTQNAME)))
    {
      return false;
    }
    else
    {
      ElemTemplateElement elem = m_stylesheet.getFirstChildElem();
      while (elem != null && elem != m_stylesheet)
      {
        if (elem instanceof ElemExsltFuncResult && ancestorIsFunction(elem))
          return true;
        ElemTemplateElement  nextElem = elem.getFirstChildElem();
        if (nextElem == null)
          nextElem = elem.getNextSiblingElem();
        if (nextElem == null)
          nextElem = elem.getParentElem();
        elem = nextElem;
      }
    }
    return false;
  }
  private boolean ancestorIsFunction(ElemTemplateElement child)
  {
    while (child.getParentElem() != null 
           && !(child.getParentElem() instanceof StylesheetRoot))
    {
      if (child.getParentElem() instanceof ElemExsltFunction)
        return true;
      child = child.getParentElem();      
    }
    return false;
  }
  public Object callFunction(
      String funcName, Vector args, Object methodKey,
      ExpressionContext exprContext) throws TransformerException
  {
    throw new TransformerException("This method should not be called.");
  }
  public Object callFunction(FuncExtFunction extFunction,
                             Vector args,
                             ExpressionContext exprContext)
      throws TransformerException
  {
    ExpressionNode parent = extFunction.exprGetParent();
    while (parent != null && !(parent instanceof ElemTemplate))
    {
      parent = parent.exprGetParent();
    }
    ElemTemplate callerTemplate = (parent != null) ? (ElemTemplate)parent: null;
    XObject[] methodArgs;
    methodArgs = new XObject[args.size()];
    try
    {
      for (int i = 0; i < methodArgs.length; i++)
      {
        methodArgs[i] =  XObject.create(args.get(i));
      }
      ElemExsltFunction elemFunc = getFunction(extFunction.getFunctionName());
      if (null != elemFunc) {
        XPathContext context = exprContext.getXPathContext();
        TransformerImpl transformer = (TransformerImpl)context.getOwnerObject();
        transformer.pushCurrentFuncResult(null);
        elemFunc.execute(transformer, methodArgs);
        XObject val = (XObject)transformer.popCurrentFuncResult();
        return (val == null) ? new XString("") 
                             : val;
      }
      else {
      	throw new TransformerException(XSLMessages.createMessage(XSLTErrorResources.ER_FUNCTION_NOT_FOUND, new Object[] {extFunction.getFunctionName()}));
      }
    }
    catch (TransformerException e)
    {
      throw e;
    }
    catch (Exception e)
    {
      throw new TransformerException(e);
    }    
  }
}