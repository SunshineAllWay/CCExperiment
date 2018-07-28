import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
public class SimpleTransform
{
	public static void main(String[] args)
    throws TransformerException, TransformerConfigurationException, 
           FileNotFoundException, IOException
  {  
	TransformerFactory tFactory = TransformerFactory.newInstance();
	Transformer transformer = tFactory.newTransformer(new StreamSource("birds.xsl"));
	transformer.transform(new StreamSource("birds.xml"), new StreamResult(new FileOutputStream("birds.out")));
	System.out.println("************* The result is in birds.out *************");
  }
}
