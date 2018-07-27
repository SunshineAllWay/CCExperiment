import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import org.apache.xalan.lib.sql.DefaultConnectionPool;
import org.apache.xalan.lib.sql.ConnectionPoolManager;
import java.io.StringReader;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
public class ExternalConnection
{
	public static void main(String[] args)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException, IOException
  {
  DefaultConnectionPool cp = new DefaultConnectionPool();
  cp.setDriver("org.apache.derby.jdbc.EmbeddedDriver");
  cp.setURL("jdbc:derby:sampleDB");
  cp.setMinConnections(10);
  cp.setPoolEnabled(true);
  ConnectionPoolManager pm = new ConnectionPoolManager();
  pm.registerPool("extpool", cp);
	TransformerFactory tFactory = TransformerFactory.newInstance();
  if (args.length == 0)
  {
    System.out.println("You must provide the path and name to a stylesheet to process");
    System.exit(0);
  }
  String stylesheet = args[0];
  System.out.println("Transforming Stylesheet " + stylesheet);
	Transformer transformer = tFactory.newTransformer(
        new StreamSource(stylesheet));
  StringReader reader =
              new StringReader("<?xml version=\"1.0\"?> <doc/>");
	transformer.transform(
        new StreamSource(reader),
        new StreamResult(new FileOutputStream("dbtest-out.html")));
	System.out.println("************* The result is in dbtest-out.html *************");
  cp.setPoolEnabled(false);
  }
}
