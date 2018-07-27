package servlet;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.net.URL;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
public class SimpleXSLTServlet extends HttpServlet {
  public final static String FS = System.getProperty("file.separator");
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
  }
  public void doGet (HttpServletRequest request,
                     HttpServletResponse response)
    throws ServletException, IOException, java.net.MalformedURLException
  {
    response.setContentType("text/html; charset=UTF-8");    
    PrintWriter out = response.getWriter();
    try
    {	
      TransformerFactory tFactory = TransformerFactory.newInstance();
      String ctx = getServletContext().getRealPath("") + FS;
      Source xmlSource = new StreamSource(new URL("file", "", ctx+"birds.xml").openStream());
      Source xslSource = new StreamSource(new URL("file", "", ctx+"birds.xsl").openStream());
      Transformer transformer = tFactory.newTransformer(xslSource);
      transformer.transform(xmlSource, new StreamResult(out));
    }
    catch (Exception e)
    {
      out.write(e.getMessage());
      e.printStackTrace(out);    
    }
    out.close();
  }
}
