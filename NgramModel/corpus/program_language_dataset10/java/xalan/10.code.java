import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.xalan.xsltc.compiler.XSLTC;
public class CompileServlet extends HttpServlet {
    public void doGet(HttpServletRequest request,
		      HttpServletResponse response)
	throws IOException, ServletException {
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	String stylesheetName = request.getParameter("sheet");
	out.println("<html><head>");
	out.println("<title>Servlet Stylesheet Compilation</title>");
	out.println("</head><body>");
	if (stylesheetName == null) {
	    out.println("<h1>Compilation error</h1>");
	    out.println("The parameter <b><tt>sheet</tt></b> "+
			"must be specified");
	}
	else {
	    XSLTC xsltc = new XSLTC();
	    xsltc.init();
	    xsltc.compile(new URL(stylesheetName));
	    out.println("<h1>Compilation successful</h1>");
	    out.println("The stylesheet was compiled into the translet "+
			"class "+xsltc.getClassName() + " and is now "+
			"available for transformations on this server.");
	}
	out.println("</body></html>");
    }
}
