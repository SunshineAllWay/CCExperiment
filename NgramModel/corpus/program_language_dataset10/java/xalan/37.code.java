import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
public class ValidateXMLInput
{
  public static void main(String[] args) 
    throws Exception
  {
    ValidateXMLInput v = new ValidateXMLInput();
    v.validate();
  }
  void validate()
    throws Exception
   {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if(tfactory.getFeature(SAXSource.FEATURE))
    {
      SAXParserFactory pfactory= SAXParserFactory.newInstance();
      pfactory.setNamespaceAware(true); 
      pfactory.setValidating(true);
      XMLReader reader = pfactory.newSAXParser().getXMLReader();
      Handler handler = new Handler();
      reader.setErrorHandler(handler);
      Transformer t = tfactory.newTransformer(
        new StreamSource("birds.xsl"));
      SAXSource source = new SAXSource(reader,
        new InputSource("birds.xml"));
      try
      {
        t.transform(source, new StreamResult("birds.out"));
      }
      catch (TransformerException te)
      {
        System.out.println("Not a SAXParseException warning or error: " + te.getMessage());
      }
      System.out.println("=====Done=====");
    }
    else
      System.out.println("tfactory does not support SAX features!");
  }
  class Handler extends DefaultHandler
  {
    public void warning (SAXParseException spe)
	     throws SAXException
    {
      System.out.println("SAXParseException warning: " + spe.getMessage());
    }    
    public void error (SAXParseException spe)
    	throws SAXException
    {
      System.out.println("SAXParseException error: " + spe.getMessage());
    }     
  }
}
