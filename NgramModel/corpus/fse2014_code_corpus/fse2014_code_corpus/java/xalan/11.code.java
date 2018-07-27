import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
public final class TransformServlet extends HttpServlet {
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
 String transletName = request.getParameter("class");
	String documentURI  = request.getParameter("source");
	try {
	    if ((transletName == null) || (documentURI == null)) {
	        out.println("<h1>XSL transformation error</h1>");
		out.println("The parameters <b><tt>class</tt></b> and " +
			    "<b><tt>source</tt></b> must be specified");
	    }
	    else {
                TransformerFactory tf = TransformerFactory.newInstance();
                try {
                    tf.setAttribute("use-classpath", Boolean.TRUE);
                } catch (IllegalArgumentException iae) {
                    System.err.println(
                           "Could not set XSLTC-specific TransformerFactory "
                         + "attributes.  Transformation failed.");
                }
                Transformer t =
                         tf.newTransformer(new StreamSource(transletName));
		final long start = System.currentTimeMillis();
		t.transform(new StreamSource(documentURI),
                            new StreamResult(out));
		final long done = System.currentTimeMillis() - start;
		out.println("<!-- transformed by XSLTC in "+done+"msecs -->");
	    }
	}
	catch (Exception e) {
	    out.println("<h1>Error</h1>");
	    out.println(e.toString());
	}
    }
}
