import java.io.FileOutputStream;
import java.io.IOException;
import javax.xml.transform.Result;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TemplatesHandler;
import javax.xml.transform.sax.TransformerHandler;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class SAX2SAX
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    { 
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
      TemplatesHandler templatesHandler = saxTFactory.newTemplatesHandler();
      XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(templatesHandler);
      reader.parse("birds.xsl");
      Templates templates = templatesHandler.getTemplates();
      TransformerHandler handler 
        = saxTFactory.newTransformerHandler(templates);
      reader.setContentHandler(handler);  
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
   	  FileOutputStream fos = new FileOutputStream("birds.out");
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");      
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);
      serializer.setOutputStream(fos);
      Result result = new SAXResult(serializer.asContentHandler());
      handler.setResult(result);
      reader.parse("birds.xml");
    	System.out.println("************* The result is in birds.out *************");	
    }	
    else
      System.out.println("The TransformerFactory does not support SAX input and SAX output");
  }
}
