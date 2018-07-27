import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Properties;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
public class JAXPTransletMultipleTransformations 
{
 static void doTransform(Templates translet, String xmlInURI, String htmlOutURI)
        throws TransformerException, FileNotFoundException     
  {
    Transformer transformer = translet.newTransformer();
    transformer.transform( new StreamSource(xmlInURI),
                           new StreamResult(new FileOutputStream(htmlOutURI)));
  }
  public static void main(String argv[])        
  { 
    String key = "javax.xml.transform.TransformerFactory";
    String value = "org.apache.xalan.xsltc.trax.TransformerFactoryImpl";
    Properties props = System.getProperties();
    props.put(key, value);
    System.setProperties(props);
    String xslInURI = "todo.xsl";
    try
    {
      TransformerFactory tFactory = TransformerFactory.newInstance();
      Templates translet = tFactory.newTemplates(new StreamSource(xslInURI));
      doTransform(translet, "todo.xml", "todo.html");
      System.out.println("Produced todo.html");
      doTransform(translet, "todotoo.xml", "todotoo.html");
      System.out.println("Produced todotoo.html");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }    
  } 
}
