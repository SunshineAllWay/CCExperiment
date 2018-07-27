import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.xalan.trace.PrintTraceListener;
import org.apache.xalan.trace.TraceManager;
import org.apache.xalan.transformer.TransformerImpl;
public class Trace
{	
  public static void main (String[] args)
	  throws java.io.IOException, 
			 TransformerException, TransformerConfigurationException,
			 java.util.TooManyListenersException, 
			 org.xml.sax.SAXException			 
  {
    String fileName = "foo";
    if (args.length > 0)
      fileName = args[0];
    java.io.FileWriter fw = new java.io.FileWriter("events.log");  
    java.io.PrintWriter pw = new java.io.PrintWriter(fw, true);
    PrintTraceListener ptl = new PrintTraceListener(pw);
    ptl.m_traceElements = true;
    ptl.m_traceGeneration = true;
    ptl.m_traceSelection = true;
    ptl.m_traceTemplates = true;
    ptl.m_traceExtension = true;
   	TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(new StreamSource(fileName + ".xsl"));
    if (transformer instanceof TransformerImpl) 
	  {
      TransformerImpl transformerImpl = (TransformerImpl)transformer;
      TraceManager trMgr = transformerImpl.getTraceManager();
      trMgr.addTraceListener(ptl);
      transformer.transform
                         ( new StreamSource(fileName + ".xml"), 
                           new StreamResult(new java.io.FileWriter(fileName + ".out")) );
    }
    pw.close();
    fw.close();
   	System.out.println("**The output is in " + fileName + ".out; the log is in events.log ****");	
  }
}
