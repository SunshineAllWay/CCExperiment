package jaxp;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
public class PropertyTest extends DefaultHandler{
	public static void main(String[] argv) {
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			spf.setValidating(true);
			spf.setNamespaceAware(true);
			SAXParser parser = spf.newSAXParser();
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				new String[] { "personal.xsd", "ipo.xsd" });
			parser.parse("tests/jaxp/data/personal-schema.xml", new PropertyTest());
			parser = spf.newSAXParser();
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				new String[] { "address.xsd", "ipo.xsd", });
			try {
				parser.parse("tests/jaxp/data/personal-schema.xml", new PropertyTest());
				System.err.println("ERROR!");
			} catch (Exception e) {
			}
			parser = spf.newSAXParser();
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
				"http://www.w3.org/2001/XMLSchema");
			parser.setProperty(
				"http://java.sun.com/xml/jaxp/properties/schemaSource",
				new String[] { "personal.xsd", "ipo.xsd", "a.xsd"});
			try {
				parser.parse("tests/jaxp/data/personal-schema.xml", new PropertyTest());
				System.err.println("ERROR!");
			} catch (Exception e) {
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("done.");
	}
	public void warning(SAXParseException ex) throws SAXException {
		printError("Warning", ex);
	} 
	public void error(SAXParseException ex) throws SAXException {
		printError("Error", ex);
	} 
	public void fatalError(SAXParseException ex) throws SAXException {
		printError("Fatal Error", ex);
		throw ex;
	} 
	protected void printError(String type, SAXParseException ex) {
		System.err.print("[");
		System.err.print(type);
		System.err.print("] ");
		String systemId = ex.getSystemId();
		if (systemId != null) {
			int index = systemId.lastIndexOf('/');
			if (index != -1)
				systemId = systemId.substring(index + 1);
			System.err.print(systemId);
		}
		System.err.print(':');
		System.err.print(ex.getLineNumber());
		System.err.print(':');
		System.err.print(ex.getColumnNumber());
		System.err.print(": ");
		System.err.print(ex.getMessage());
		System.err.println();
		System.err.flush();
	} 
}
