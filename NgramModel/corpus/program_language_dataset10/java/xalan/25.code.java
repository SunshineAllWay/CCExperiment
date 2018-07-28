package servlet;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.Enumeration;
import java.net.URL;
import org.xml.sax.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
public class XSLTServletWithParams extends HttpServlet {
  public final static String FS = System.getProperty("file.separator");
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }
  public void doGet (HttpServletRequest request,
                     HttpServletResponse response)
    throws ServletException, IOException
  {
    response.setContentType("text/html; charset=UTF-8"); 
    PrintWriter out = response.getWriter();
    try
    {	
      TransformerFactory tFactory = TransformerFactory.newInstance();
      String xml = getRequestParam(request, "URL");
      String xsl = getRequestParam(request, "xslURL");
      Source xmlSource = null;
      Source xslSource = null;
      Transformer transformer = null;
      String ctx = getServletContext().getRealPath("") + FS;
      if (xml != null && xml.length()> 0)
        xmlSource = new StreamSource(new URL("file", "", ctx + xml).openStream());
      if (xsl != null && xsl.length()> 0)
        xslSource = new StreamSource(new URL("file", "", ctx + xsl).openStream());
      if (xmlSource != null) 
      {
        if (xslSource == null) 
        {
     	    String media= null , title = null, charset = null;
          xslSource = tFactory.getAssociatedStylesheet(xmlSource,media, title, charset);
        }
        if (xslSource != null) 
        {
          transformer = tFactory.newTransformer(xslSource);
          setParameters(transformer, request); 
          transformer.transform(xmlSource, new StreamResult(out)); 
        }
        else
          out.write("No Stylesheet!");
      }
      else
        out.write("No XML Input Document!");
    }
    catch (Exception e)
    {
      e.printStackTrace(out);    
    }
    out.close();
  }
  String getRequestParam(HttpServletRequest request, String param)
  {
	  if (request != null) 
    { 
	    String paramVal = request.getParameter(param); 
		  return paramVal;
	  }
	  return null;
  }
  void setParameters(Transformer transformer, HttpServletRequest request)
  {
    Enumeration paramNames = request.getParameterNames();
    while (paramNames.hasMoreElements())
    {
      String paramName = (String) paramNames.nextElement();
      try
      {
        String paramVal = request.getParameter(paramName);
        if (paramVal != null)
          transformer.setParameter(paramName, paramVal);                                            
      }
      catch (Exception e)
      {
      }
    }
  }  
}
