package socket;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Random;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Parser;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.ParserAdapter;
import org.xml.sax.helpers.ParserFactory;
import org.xml.sax.helpers.XMLReaderFactory;
public class DelayedInput
    extends DefaultHandler {
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    protected static final boolean DEFAULT_NAMESPACES = true;
    protected static final boolean DEFAULT_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    protected PrintWriter fOut;
    public DelayedInput() {
    } 
    public void startElement(String uri, String localpart, String rawname,
                             Attributes attrs) throws SAXException {
        System.out.println("("+rawname);
        int length = attrs != null ? attrs.getLength() : 0;
        for (int i = 0; i < length; i++) {
            System.out.println("A"+attrs.getQName(i)+' '+attrs.getValue(i));
        }
    } 
    public void endElement(String uri, String localpart, String rawname)
        throws SAXException {
        System.out.println(")"+rawname);
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
    static class DelayedInputStream
        extends FilterInputStream {
        private Random fRandom = new Random(System.currentTimeMillis());
        public DelayedInputStream(InputStream in) {
            super(in);
        } 
        public int read(byte[] buffer, int offset, int length) throws IOException {
            if (length > 48) {
                length = 48;
            }
            int count = 0;
            long before = System.currentTimeMillis();
            count = in.read(buffer, offset, length);
            try {
                Thread.sleep(Math.abs(fRandom.nextInt()) % 2000);
            }
            catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            long after = System.currentTimeMillis();
            System.out.print("read "+count+" bytes in "+(after-before)+" ms: ");
            printBuffer(buffer, offset, count);
            System.out.println();
            return count;
        } 
        private void printBuffer(byte[] buffer, int offset, int length) {
            if (length <= 0) {
                System.out.print("no data read");
                return;
            }
            System.out.print('[');
            for (int i = 0; i < length; i++) {
                switch ((char)buffer[offset + i]) {
                    case '\r': {
                        System.out.print("\\r");
                        break;
                    }
                    case '\n': {
                        System.out.print("\\n");
                        break;
                    }
                    default: {
                        System.out.print((char)buffer[offset + i]);
                    }
                }
            }
            System.out.print(']');
        } 
    } 
    public static void main(String argv[]) {
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }
        DefaultHandler handler = new DelayedInput();
        XMLReader parser = null;
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean validation = DEFAULT_VALIDATION;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (option.equals("p")) {
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -p option.");
                    }
                    String parserName = argv[i];
                    try {
                        parser = XMLReaderFactory.createXMLReader(parserName);
                    }
                    catch (Exception e) {
                        try {
                            Parser sax1Parser = ParserFactory.makeParser(parserName);
                            parser = new ParserAdapter(sax1Parser);
                            System.err.println("warning: Features and properties not supported on SAX1 parsers.");
                        }
                        catch (Exception ex) {
                            parser = null;
                            System.err.println("error: Unable to instantiate parser ("+DEFAULT_PARSER_NAME+")");
                        }
                    }
                    continue;
                }
                if (option.equalsIgnoreCase("n")) {
                    namespaces = option.equals("n");
                    continue;
                }
                if (option.equalsIgnoreCase("v")) {
                    validation = option.equals("v");
                    continue;
                }
                if (option.equalsIgnoreCase("s")) {
                    schemaValidation = option.equals("s");
                    continue;
                }
                if (option.equalsIgnoreCase("f")) {
                    schemaFullChecking = option.equals("f");
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
            }
            if (parser == null) {
                try {
                    parser = XMLReaderFactory.createXMLReader(DEFAULT_PARSER_NAME);
                }
                catch (Exception e) {
                    System.err.println("error: Unable to instantiate parser ("+DEFAULT_PARSER_NAME+")");
                    continue;
                }
            }
            try {
                parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+NAMESPACES_FEATURE_ID+")");
            }
            try {
                parser.setFeature(VALIDATION_FEATURE_ID, validation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+VALIDATION_FEATURE_ID+")");
            }
            try {
                parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, schemaValidation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+SCHEMA_VALIDATION_FEATURE_ID+")");
            }
            try {
                parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+SCHEMA_FULL_CHECKING_FEATURE_ID+")");
            }
            parser.setContentHandler(handler);
            parser.setErrorHandler(handler);
            try {
                System.out.println("# filename: "+arg);
                InputStream stream = new DelayedInputStream(new FileInputStream(arg));
                InputSource source = new InputSource(stream);
                source.setSystemId(arg);
                parser.parse(source);
            }
            catch (SAXParseException e) {
            }
            catch (Exception e) {
                System.err.println("error: Parse error occurred - "+e.getMessage());
                if (e instanceof SAXException) {
                    e = ((SAXException)e).getException();
                }
                e.printStackTrace(System.err);
            }
        }
    } 
    private static void printUsage() {
        System.err.println("usage: java socket.DelayedInput (options) filename ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name  Select parser by name.");
        System.err.println("  -n | -N  Turn on/off namespace processing.");
        System.err.println("  -v | -V  Turn on/off validation.");
        System.err.println("  -s | -S  Turn on/off Schema validation support.");
        System.err.println("           NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F Turn on/off Schema full checking.");
        System.err.println("           NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -h       This help screen.");
        System.err.println();
        System.err.println("defaults:");
        System.err.println("  Parser:     "+DEFAULT_PARSER_NAME);
        System.err.print("  Namespaces: ");
        System.err.println(DEFAULT_NAMESPACES ? "on" : "off");
        System.err.print("  Validation: ");
        System.err.println(DEFAULT_VALIDATION ? "on" : "off");
        System.err.print("  Schema:     ");
        System.err.println(DEFAULT_SCHEMA_VALIDATION ? "on" : "off");
        System.err.print("  Schema full checking:     ");
        System.err.println(DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off");
    } 
} 
