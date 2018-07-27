import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import sunlabs.brazil.server.Handler;
import sunlabs.brazil.server.Request;
import sunlabs.brazil.server.Server;
public class TransformHandler implements Handler {
    private TransformerFactory m_tf = null;
    private final String PARAM_TRANSLET = "translet=";
    private final String PARAM_DOCUMENT = "document=";
    private final String PARAM_STATS = "stats=";
    private PrintWriter m_out = null;
    public void errorMessage(String message, Exception e) {
	if (m_out == null) {
            return;
        }
	m_out.println("<h1>XSL transformation error</h1>"+message);
	m_out.println("<br>Exception:</br>"+e.toString());
    }
    public void errorMessage(String message) {
	if (m_out == null) return;
	m_out.println("<h1>XSL transformation error</h1>"+message);
    }
    public boolean init(Server server, String prefix) {
	return true;
    }
    public boolean respond(Request request) throws IOException {
	final StringWriter sout = new StringWriter();
	m_out = new PrintWriter(sout);
	String transletName = null;
	String document = null;
	String stats = null;
	final StringTokenizer params = new StringTokenizer(request.query,"&");
	while (params.hasMoreElements()) {
	    final String param = params.nextToken();
	    if (param.startsWith(PARAM_TRANSLET)) {
		transletName = param.substring(PARAM_TRANSLET.length());
	    }
	    else if (param.startsWith(PARAM_DOCUMENT)) {
		document = param.substring(PARAM_DOCUMENT.length());
	    }
	    else if (param.startsWith(PARAM_STATS)) {
		stats = param.substring(PARAM_STATS.length());
	    }
	}
	try {
	    if ((transletName == null) || (document == null)) {
		errorMessage("Parameters <b><tt>translet</tt></b> and/or "+
			     "<b><tt>document</tt></b> not specified.");
	    }
	    else {
                if (m_tf == null) {
                    m_tf = TransformerFactory.newInstance();
                    try {
                        m_tf.setAttribute("use-classpath", Boolean.TRUE);
                    } catch (IllegalArgumentException iae) {
                        System.err.println(
                            "Could not set XSLTC-specific TransformerFactory "
                          + "attributes.  Transformation failed.");
                    }
                }
                Transformer t =
                     m_tf.newTransformer(new StreamSource(transletName));
		final long start = System.currentTimeMillis();
		t.transform(new StreamSource(document),
                            new StreamResult(m_out));
		final long done = System.currentTimeMillis() - start;
		m_out.println("<!-- transformed by XSLTC in "+done+"ms -->");
	    }
	}
	catch (Exception e) {
	    errorMessage("Internal error.",e);
	}
	request.sendResponse(sout.toString());
	return true;
    }
}
