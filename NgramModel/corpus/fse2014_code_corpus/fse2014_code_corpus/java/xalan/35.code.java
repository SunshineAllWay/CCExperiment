import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class UseXMLFilters
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    { 
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);
      XMLFilter xmlFilter1 = saxTFactory.newXMLFilter(new StreamSource("foo1.xsl"));
      XMLFilter xmlFilter2 = saxTFactory.newXMLFilter(new StreamSource("foo2.xsl"));
      XMLFilter xmlFilter3 = saxTFactory.newXMLFilter(new StreamSource("foo3.xsl"));
	    XMLReader reader = XMLReaderFactory.createXMLReader();
      xmlFilter1.setParent(reader);
      xmlFilter2.setParent(xmlFilter1);
      xmlFilter3.setParent(xmlFilter2);
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no"); 
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);                      
      serializer.setOutputStream(System.out);
      xmlFilter3.setContentHandler(serializer.asContentHandler());
      xmlFilter3.parse(new InputSource("foo.xml"));
    }
  }
}
