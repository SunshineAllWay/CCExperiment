package jaxp;
import java.util.Vector;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
public class ParserAPIUsage extends DefaultHandler {
    protected static final String DEFAULT_API_TO_USE = "sax";
    protected static final boolean DEFAULT_XINCLUDE = false;
    protected static final boolean DEFAULT_SECURE_PROCESSING = false;
    public ParserAPIUsage() {
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
    public static void main(String[] argv) {
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }
        ParserAPIUsage parserAPIUsage = new ParserAPIUsage();
        Vector schemas = null;
        String docURI = argv[argv.length - 1];
        String apiToUse = DEFAULT_API_TO_USE;
        boolean xincludeProcessing = DEFAULT_XINCLUDE;
        boolean secureProcessing = DEFAULT_SECURE_PROCESSING;
        for (int i = 0; i < argv.length - 1; ++i) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (arg.equals("-a")) {
                    if (schemas == null) {
                        schemas = new Vector();
                    }
                    while (i + 1 < argv.length - 1 && !(arg = argv[i + 1]).startsWith("-")) {
                        schemas.add(arg);
                        ++i;
                    }
                    continue;
                }
                if (arg.equals("-api")) {
                    if (i + 1 < argv.length - 1 && !(arg = argv[i + 1]).startsWith("-")) {
                        if (arg.equals("sax") || arg.equals("dom")) {
                            apiToUse = arg;
                        }
                        else {
                            System.err.println("error: unknown source type ("+arg+").");
                        }
                    }
                    continue;
                }
                if (option.equalsIgnoreCase("xi")) {
                    xincludeProcessing = option.equals("xi");
                    continue;
                }
                if (option.equalsIgnoreCase("sp")) {
                    secureProcessing = option.equals("sp");
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
                System.err.println("error: unknown option ("+option+").");
                continue;
            }
        }
        try {
            Schema schema = null;
            if (schemas != null && schemas.size() > 0) {
                SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                factory.setErrorHandler(parserAPIUsage);
                final int length = schemas.size();
                StreamSource[] sources = new StreamSource[length];
                for (int j = 0; j < length; ++j) {
                    sources[j] = new StreamSource((String) schemas.elementAt(j));
                }
                schema = factory.newSchema(sources);
            }
            if ("dom".equals(apiToUse)) {
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setXIncludeAware(xincludeProcessing);
                dbf.setSchema(schema);
                dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, secureProcessing);
                DocumentBuilder db = dbf.newDocumentBuilder();
                db.setErrorHandler(parserAPIUsage);
                db.parse(docURI);
                db.reset();
                db.setErrorHandler(parserAPIUsage);
                db.parse(docURI);
            }
            else {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(true);
                spf.setXIncludeAware(xincludeProcessing);
                spf.setSchema(schema);
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, secureProcessing);
                SAXParser sp = spf.newSAXParser();
                sp.parse(docURI, parserAPIUsage);
                sp.reset();
                sp.parse(docURI, parserAPIUsage);
            }
        }
        catch (SAXParseException e) {
        }
        catch (Exception e) {
            System.err.println("error: Parse error occurred - "+e.getMessage());
            if (e instanceof SAXException) {
                Exception nested = ((SAXException)e).getException();
                if (nested != null) {
                    e = nested;
                } 
            }
            e.printStackTrace(System.err);
        }
    } 
    private static void printUsage() {
        System.err.println("usage: java jaxp.ParserAPIUsage (options) uri");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -a uri ...      Provide a list of schema documents.");
        System.err.println("  -api (sax|dom)  Select API to use (sax|dom).");
        System.err.println("  -xi | -XI       Turn on/off XInclude processing.");
        System.err.println("  -sp | -SP       Turn on/off secure processing.");
        System.err.println("  -h              This help screen.");
        System.err.println();
        System.err.println("defaults:");
        System.err.println("  API to use:            " + DEFAULT_API_TO_USE);
        System.err.print("  XInclude:              ");
        System.err.println(DEFAULT_XINCLUDE ? "on" : "off");
        System.err.print("  Secure processing:     ");
        System.err.println(DEFAULT_SECURE_PROCESSING ? "on" : "off");
    } 
} 
