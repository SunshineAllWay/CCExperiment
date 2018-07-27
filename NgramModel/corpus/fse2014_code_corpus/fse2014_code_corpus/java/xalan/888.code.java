package org.apache.xpath.functions;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Properties;
import org.apache.xpath.XPathContext;
import org.apache.xpath.objects.XNumber;
import org.apache.xpath.objects.XObject;
import org.apache.xpath.objects.XString;
import org.apache.xpath.res.XPATHErrorResources;
public class FuncSystemProperty extends FunctionOneArg
{
    static final long serialVersionUID = 3694874980992204867L;
  static final String XSLT_PROPERTIES = 
            "org/apache/xalan/res/XSLTInfo.properties";
  public XObject execute(XPathContext xctxt) throws javax.xml.transform.TransformerException
  {
    String fullName = m_arg0.execute(xctxt).str();
    int indexOfNSSep = fullName.indexOf(':');
    String result;
    String propName = "";
    Properties xsltInfo = new Properties();
    loadPropertyFile(XSLT_PROPERTIES, xsltInfo);
    if (indexOfNSSep > 0)
    {
      String prefix = (indexOfNSSep >= 0)
                      ? fullName.substring(0, indexOfNSSep) : "";
      String namespace;
      namespace = xctxt.getNamespaceContext().getNamespaceForPrefix(prefix);
      propName = (indexOfNSSep < 0)
                 ? fullName : fullName.substring(indexOfNSSep + 1);
      if (namespace.startsWith("http://www.w3.org/XSL/Transform")
              || namespace.equals("http://www.w3.org/1999/XSL/Transform"))
      {
        result = xsltInfo.getProperty(propName);
        if (null == result)
        {
          warn(xctxt, XPATHErrorResources.WG_PROPERTY_NOT_SUPPORTED,
               new Object[]{ fullName });  
          return XString.EMPTYSTRING;
        }
      }
      else
      {
        warn(xctxt, XPATHErrorResources.WG_DONT_DO_ANYTHING_WITH_NS,
             new Object[]{ namespace,
                           fullName });  
        try
        {
          result = System.getProperty(propName);
          if (null == result)
          {
            return XString.EMPTYSTRING;
          }
        }
        catch (SecurityException se)
        {
          warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
               new Object[]{ fullName });  
          return XString.EMPTYSTRING;
        }
      }
    }
    else
    {
      try
      {
        result = System.getProperty(fullName);
        if (null == result)
        {
          return XString.EMPTYSTRING;
        }
      }
      catch (SecurityException se)
      {
        warn(xctxt, XPATHErrorResources.WG_SECURITY_EXCEPTION,
             new Object[]{ fullName });  
        return XString.EMPTYSTRING;
      }
    }
    if (propName.equals("version") && result.length() > 0)
    {
      try
      {
        return new XString("1.0");
      }
      catch (Exception ex)
      {
        return new XString(result);
      }
    }
    else
      return new XString(result);
  }
  public void loadPropertyFile(String file, Properties target)
  {
    try
    {
      SecuritySupport ss = SecuritySupport.getInstance();
      InputStream is = ss.getResourceAsStream(ObjectFactory.findClassLoader(),
                                              file);
      BufferedInputStream bis = new BufferedInputStream(is);
      target.load(bis);  
      bis.close();  
    }
    catch (Exception ex)
    {
      throw new org.apache.xml.utils.WrappedRuntimeException(ex);
    }
  }
}
