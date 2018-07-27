package servlet;
import java.net.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.util.Enumeration;
import java.util.Properties;
public class DefaultApplyXSLTProperties extends ApplyXSLTProperties {
    private final String DEFAULT_catalog;
    protected transient String localHost = null;
    protected static int port =0;
    public DefaultApplyXSLTProperties()
    {
	super();
	DEFAULT_catalog = null;
	setLocalHost();
    }
    public DefaultApplyXSLTProperties(ServletConfig config)
    {
	    super(config);
	    String cat = config.getInitParameter("catalog");
	    if (cat != null) DEFAULT_catalog = cat;
	    else DEFAULT_catalog = null;
	    setLocalHost();
	    setSystemProperties();
    }
    protected void setLocalHost()
    {
	    try 
	    { 
	        localHost = InetAddress.getLocalHost().getHostName();
	    } 
	    catch (Exception uhe) 
	    {
	      localHost = null;
	    }
    }
    public String getLocalHost()
    {
	    return localHost;
    }
    public URL toSafeURL(String xURL, HttpServletRequest request)
    throws MalformedURLException
    {
      if (port == 0)
        port = request.getServerPort();
	    if (xURL == null)
	      return null;
	    if (xURL.startsWith("/")) 
      {
	      try 
        {
		      return new URL("http", localHost, port, xURL);
	      }
        catch (MalformedURLException mue) 
        {
	        throw new MalformedURLException("toSafeURL(): " + xURL + 
					                                " did not map to local");
	      }
	    }
	    URL tempURL = null;
	    try 
      { 
	      tempURL = new URL(xURL);
	    } 
      catch (MalformedURLException mue) 
      {
	      throw new MalformedURLException("toSafeURL(): " + xURL + 
					                              " not a valid URL"); 
	    }
	    try 
      { 
	      return new URL(tempURL.getProtocol(), localHost, 
			                 port, tempURL.getFile());
	    } 
      catch (MalformedURLException mue) 
      {
	      throw new MalformedURLException("toSafeURL(): " + xURL + 
				                          	    " could not be converted to local host");
	    }
    }
    public String getXMLurl(HttpServletRequest request)
    throws MalformedURLException
    {
	    URL url = toSafeURL(getRequestParmString(request, "URL"),request);
	    if (url == null)
	      return super.getXMLurl(null);
	    return url.toExternalForm();
    }
    public String getXSLRequestURL(HttpServletRequest request)
    throws MalformedURLException
    {
	    URL url = toSafeURL(getRequestParmString(request, "xslURL"),request);
	    if (url == null)
	        return null;
	    return url.toExternalForm();
    }
    public String getXSLurl(HttpServletRequest request)
    throws MalformedURLException
    {
	    String reqURL = getXSLRequestURL(request);
	    if (reqURL != null)
	        return reqURL;
	    URL url = toSafeURL(super.getXSLurl(null), request);
	    return url.toExternalForm();
    }
    public String[] getCatalog(HttpServletRequest request)
    {
	    String temp[] = request.getParameterValues("catalog");
	    if (DEFAULT_catalog == null)
	        return temp;
	    if (temp == null) 
      {
	      String defaultArray[] = new String [1];
	      defaultArray[0] = DEFAULT_catalog;
	      return defaultArray;
	    }
	    int i, len = temp.length + 1;
	    String newCatalogs[] = new String[len];
	    newCatalogs[0] = DEFAULT_catalog;
	    for (i=1; i < len; i++) 
      {
	      newCatalogs[i] = temp[i-1];
	    }
	    return newCatalogs;
    }
    protected void setSystemProperties()
	{
	  Properties props = new Properties();
    props.put("javax.xml.transform.TransformerFactory", 
              "org.apache.xalan.processor.TransformerFactoryImpl");
    props.put("javax.xml.parsers.DocumentBuilderFactory", 
              "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
    props.put("javax.xml.parsers.SAXParserFactory", 
              "org.apache.xerces.jaxp.SAXParserFactoryImpl");
      Properties systemProps = System.getProperties();
      Enumeration propEnum = props.propertyNames();
      while(propEnum.hasMoreElements())
      {
        String prop = (String)propEnum.nextElement();
        if(!systemProps.containsKey(prop))
          systemProps.put(prop, props.getProperty(prop));
      }
      System.setProperties(systemProps);
	}
}
