import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
public class TransformServlet extends HttpServlet {
    private final static String createErrorMsg =
	"<h1>XSL transformation bean error</h1>"+
	"<p>An XSL transformation bean could not be created.</p>";
    private TransformHome transformer;
    public void init(ServletConfig config) 
	throws ServletException{
	try{
	    InitialContext context = new InitialContext();
	    Object transformRef = context.lookup("transform");
	    transformer =
		(TransformHome)PortableRemoteObject.narrow(transformRef,
							   TransformHome.class);
	} catch (Exception NamingException) {
	    NamingException.printStackTrace();
	}
    }
    public void doGet (HttpServletRequest request, 
		       HttpServletResponse response) 
	throws ServletException, IOException {
	String document = request.getParameter("document");
	String translet = request.getParameter("translet");
	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	try{
	    TransformRemote xslt = transformer.create();
	    String result = xslt.transform(document, translet);
	    out.println(result);
	} catch(Exception CreateException){
	    out.println(createErrorMsg);
	}
	out.close();
    }
    public void destroy() {
	System.out.println("Destroy");
    }
}
