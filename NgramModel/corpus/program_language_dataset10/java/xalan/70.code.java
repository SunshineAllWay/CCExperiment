package org.apache.xalan.lib;
import java.util.Hashtable;
import java.util.StringTokenizer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xalan.extensions.ExpressionContext;
import org.apache.xalan.xslt.EnvironmentCheck;
import org.apache.xpath.NodeSet;
import org.apache.xpath.objects.XBoolean;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.SAXNotSupportedException;
public class Extensions
{
  private Extensions(){}  
  public static NodeSet nodeset(ExpressionContext myProcessor, Object rtf)
  {
    String textNodeValue;
    if (rtf instanceof NodeIterator)
    {
      return new NodeSet((NodeIterator) rtf);
    }
    else
    {
      if (rtf instanceof String)
      {
        textNodeValue = (String) rtf;
      }
      else if (rtf instanceof Boolean)
      {
        textNodeValue = new XBoolean(((Boolean) rtf).booleanValue()).str();
      }
      else if (rtf instanceof Double)
      {
        textNodeValue = new XNumber(((Double) rtf).doubleValue()).str();
      }
      else
      {
        textNodeValue = rtf.toString();
      }
      try
      {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document myDoc = db.newDocument();
        Text textNode = myDoc.createTextNode(textNodeValue);
        DocumentFragment docFrag = myDoc.createDocumentFragment();
        docFrag.appendChild(textNode);
        return new NodeSet(docFrag);
      }
      catch(ParserConfigurationException pce)
      {
        throw new org.apache.xml.utils.WrappedRuntimeException(pce);
      }
    }
  }
  public static NodeList intersection(NodeList nl1, NodeList nl2)
  {
    return ExsltSets.intersection(nl1, nl2);
  }
  public static NodeList difference(NodeList nl1, NodeList nl2)
  {
    return ExsltSets.difference(nl1, nl2);
  }
  public static NodeList distinct(NodeList nl)
  {
    return ExsltSets.distinct(nl);
  }
  public static boolean hasSameNodes(NodeList nl1, NodeList nl2)
  {
    NodeSet ns1 = new NodeSet(nl1);
    NodeSet ns2 = new NodeSet(nl2);
    if (ns1.getLength() != ns2.getLength())
      return false;
    for (int i = 0; i < ns1.getLength(); i++)
    {
      Node n = ns1.elementAt(i);
      if (!ns2.contains(n))
        return false;
    }
    return true;
  }
  public static XObject evaluate(ExpressionContext myContext, String xpathExpr)
         throws SAXNotSupportedException
  {
    return ExsltDynamic.evaluate(myContext, xpathExpr);
  }
  public static NodeList tokenize(String toTokenize, String delims)
  {
    Document doc = DocumentHolder.m_doc;
    StringTokenizer lTokenizer = new StringTokenizer(toTokenize, delims);
    NodeSet resultSet = new NodeSet();
    synchronized (doc)
    {
      while (lTokenizer.hasMoreTokens())
      {
        resultSet.addNode(doc.createTextNode(lTokenizer.nextToken()));
      }
    }
    return resultSet;
  }
  public static NodeList tokenize(String toTokenize)
  {
    return tokenize(toTokenize, " \t\n\r");
  }
  public static Node checkEnvironment(ExpressionContext myContext)
  {
    Document factoryDocument;
    try
    {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      factoryDocument = db.newDocument();
    }
    catch(ParserConfigurationException pce)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(pce);
    }
    Node resultNode = null;
    try
    {
      resultNode = checkEnvironmentUsingWhich(myContext, factoryDocument);
      if (null != resultNode)
        return resultNode;
      EnvironmentCheck envChecker = new EnvironmentCheck();
      Hashtable h = envChecker.getEnvironmentHash();
      resultNode = factoryDocument.createElement("checkEnvironmentExtension");
      envChecker.appendEnvironmentReport(resultNode, factoryDocument, h);
      envChecker = null;
    }
    catch(Exception e)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(e);
    }
    return resultNode;
  }
  private static Node checkEnvironmentUsingWhich(ExpressionContext myContext, 
        Document factoryDocument)
  {
    final String WHICH_CLASSNAME = "org.apache.env.Which";
    final String WHICH_METHODNAME = "which";
    final Class WHICH_METHOD_ARGS[] = { java.util.Hashtable.class,
                                        java.lang.String.class,
                                        java.lang.String.class };
    try
    {
      Class clazz = ObjectFactory.findProviderClass(
        WHICH_CLASSNAME, ObjectFactory.findClassLoader(), true);
      if (null == clazz)
        return null;
      java.lang.reflect.Method method = clazz.getMethod(WHICH_METHODNAME, WHICH_METHOD_ARGS);
      Hashtable report = new Hashtable();
      Object[] methodArgs = { report, "XmlCommons;Xalan;Xerces;Crimson;Ant", "" };
      Object returnValue = method.invoke(null, methodArgs);
      Node resultNode = factoryDocument.createElement("checkEnvironmentExtension");
      org.apache.xml.utils.Hashtree2Node.appendHashToNode(report, "whichReport", 
            resultNode, factoryDocument);
      return resultNode;
    }
    catch (Throwable t)
    {
      return null;
    }
  }
    private static class DocumentHolder
    {
        private static final Document m_doc;
        static 
        {
            try
            {
                m_doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            }
            catch(ParserConfigurationException pce)
            {
                  throw new org.apache.xml.utils.WrappedRuntimeException(pce);
            }
        }
    }  
}