package dom;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
public class Writer {
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";
    protected static final String VALIDATE_ANNOTATIONS_ID = "http://apache.org/xml/features/validate-annotations";
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS_ID = "http://apache.org/xml/features/generate-synthetic-annotations";
    protected static final String DYNAMIC_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/dynamic";
    protected static final String LOAD_EXTERNAL_DTD_FEATURE_ID = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    protected static final String XINCLUDE_FEATURE_ID = "http://apache.org/xml/features/xinclude";
    protected static final String XINCLUDE_FIXUP_BASE_URIS_FEATURE_ID = "http://apache.org/xml/features/xinclude/fixup-base-uris";
    protected static final String XINCLUDE_FIXUP_LANGUAGE_FEATURE_ID = "http://apache.org/xml/features/xinclude/fixup-language";
    protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";
    protected static final boolean DEFAULT_NAMESPACES = true;
    protected static final boolean DEFAULT_VALIDATION = false;
    protected static final boolean DEFAULT_LOAD_EXTERNAL_DTD = true;
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    protected static final boolean DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS = false;
    protected static final boolean DEFAULT_VALIDATE_ANNOTATIONS = false;
    protected static final boolean DEFAULT_GENERATE_SYNTHETIC_ANNOTATIONS = false;
    protected static final boolean DEFAULT_DYNAMIC_VALIDATION = false;
    protected static final boolean DEFAULT_XINCLUDE = false;
    protected static final boolean DEFAULT_XINCLUDE_FIXUP_BASE_URIS = true;
    protected static final boolean DEFAULT_XINCLUDE_FIXUP_LANGUAGE = true;
    protected static final boolean DEFAULT_CANONICAL = false;
    protected PrintWriter fOut;
    protected boolean fCanonical;
    protected boolean fXML11;
    public Writer() {
    } 
    public Writer(boolean canonical) {
        fCanonical = canonical;
    } 
    public void setCanonical(boolean canonical) {
        fCanonical = canonical;
    } 
    public void setOutput(OutputStream stream, String encoding)
        throws UnsupportedEncodingException {
        if (encoding == null) {
            encoding = "UTF8";
        }
        java.io.Writer writer = new OutputStreamWriter(stream, encoding);
        fOut = new PrintWriter(writer);
    } 
    public void setOutput(java.io.Writer writer) {
        fOut = writer instanceof PrintWriter
             ? (PrintWriter)writer : new PrintWriter(writer);
    } 
    public void write(Node node) {
        if (node == null) {
            return;
        }
        short type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE: {
                Document document = (Document)node;
                fXML11 = "1.1".equals(getVersion(document));
                if (!fCanonical) {
                    if (fXML11) {
                        fOut.println("<?xml version=\"1.1\" encoding=\"UTF-8\"?>");
                    }
                    else {
                        fOut.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                    }
                    fOut.flush();
                    write(document.getDoctype());
                }
                write(document.getDocumentElement());
                break;
            }
            case Node.DOCUMENT_TYPE_NODE: {
                DocumentType doctype = (DocumentType)node;
                fOut.print("<!DOCTYPE ");
                fOut.print(doctype.getName());
                String publicId = doctype.getPublicId();
                String systemId = doctype.getSystemId();
                if (publicId != null) {
                    fOut.print(" PUBLIC '");
                    fOut.print(publicId);
                    fOut.print("' '");
                    fOut.print(systemId);
                    fOut.print('\'');
                }
                else if (systemId != null) {
                    fOut.print(" SYSTEM '");
                    fOut.print(systemId);
                    fOut.print('\'');
                }
                String internalSubset = doctype.getInternalSubset();
                if (internalSubset != null) {
                    fOut.println(" [");
                    fOut.print(internalSubset);
                    fOut.print(']');
                }
                fOut.println('>');
                break;
            }
            case Node.ELEMENT_NODE: {
                fOut.print('<');
                fOut.print(node.getNodeName());
                Attr attrs[] = sortAttributes(node.getAttributes());
                for (int i = 0; i < attrs.length; i++) {
                    Attr attr = attrs[i];
                    fOut.print(' ');
                    fOut.print(attr.getNodeName());
                    fOut.print("=\"");
                    normalizeAndPrint(attr.getNodeValue(), true);
                    fOut.print('"');
                }
                fOut.print('>');
                fOut.flush();
                Node child = node.getFirstChild();
                while (child != null) {
                    write(child);
                    child = child.getNextSibling();
                }
                break;
            }
            case Node.ENTITY_REFERENCE_NODE: {
                if (fCanonical) {
                    Node child = node.getFirstChild();
                    while (child != null) {
                        write(child);
                        child = child.getNextSibling();
                    }
                }
                else {
                    fOut.print('&');
                    fOut.print(node.getNodeName());
                    fOut.print(';');
                    fOut.flush();
                }
                break;
            }
            case Node.CDATA_SECTION_NODE: {
                if (fCanonical) {
                    normalizeAndPrint(node.getNodeValue(), false);
                }
                else {
                    fOut.print("<![CDATA[");
                    fOut.print(node.getNodeValue());
                    fOut.print("]]>");
                }
                fOut.flush();
                break;
            }
            case Node.TEXT_NODE: {
                normalizeAndPrint(node.getNodeValue(), false);
                fOut.flush();
                break;
            }
            case Node.PROCESSING_INSTRUCTION_NODE: {
                fOut.print("<?");
                fOut.print(node.getNodeName());
                String data = node.getNodeValue();
                if (data != null && data.length() > 0) {
                    fOut.print(' ');
                    fOut.print(data);
                }
                fOut.print("?>");
                fOut.flush();
                break;
            }
            case Node.COMMENT_NODE: {
                if (!fCanonical) {
                    fOut.print("<!--");
                    String comment = node.getNodeValue();
                    if (comment != null && comment.length() > 0) {
                        fOut.print(comment);
                    }
                    fOut.print("-->");
                    fOut.flush();
                }
            }
        }
        if (type == Node.ELEMENT_NODE) {
            fOut.print("</");
            fOut.print(node.getNodeName());
            fOut.print('>');
            fOut.flush();
        }
    } 
    protected Attr[] sortAttributes(NamedNodeMap attrs) {
        int len = (attrs != null) ? attrs.getLength() : 0;
        Attr array[] = new Attr[len];
        for (int i = 0; i < len; i++) {
            array[i] = (Attr)attrs.item(i);
        }
        for (int i = 0; i < len - 1; i++) {
            String name = array[i].getNodeName();
            int index = i;
            for (int j = i + 1; j < len; j++) {
                String curName = array[j].getNodeName();
                if (curName.compareTo(name) < 0) {
                    name = curName;
                    index = j;
                }
            }
            if (index != i) {
                Attr temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
        }
        return array;
    } 
    protected void normalizeAndPrint(String s, boolean isAttValue) {
        int len = (s != null) ? s.length() : 0;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            normalizeAndPrint(c, isAttValue);
        }
    } 
    protected void normalizeAndPrint(char c, boolean isAttValue) {
        switch (c) {
            case '<': {
                fOut.print("&lt;");
                break;
            }
            case '>': {
                fOut.print("&gt;");
                break;
            }
            case '&': {
                fOut.print("&amp;");
                break;
            }
            case '"': {
                if (isAttValue) {
                    fOut.print("&quot;");
                }
                else {
                    fOut.print("\"");
                }
                break;
            }
            case '\r': {
            	fOut.print("&#xD;");
            	break;
            }
            case '\n': {
                if (fCanonical) {
                    fOut.print("&#xA;");
                    break;
                }
            }
            default: {
            	if (fXML11 && ((c >= 0x01 && c <= 0x1F && c != 0x09 && c != 0x0A) 
            	    || (c >= 0x7F && c <= 0x9F) || c == 0x2028)
            	    || isAttValue && (c == 0x09 || c == 0x0A)) {
            	    fOut.print("&#x");
            	    fOut.print(Integer.toHexString(c).toUpperCase());
            	    fOut.print(";");
                }
                else {
                    fOut.print(c);
                }        
            }
        }
    } 
    protected String getVersion(Document document) {
    	if (document == null) {
    	    return null;
    	}
        String version = null;
        Method getXMLVersion = null;
        try {
            getXMLVersion = document.getClass().getMethod("getXmlVersion", new Class[]{});
            if (getXMLVersion != null) {
                version = (String) getXMLVersion.invoke(document, (Object[]) null);
            }
        } 
        catch (Exception e) { 
        }
        return version;
    } 
    public static void main(String argv[]) {
        if (argv.length == 0) {
            printUsage();
            System.exit(1);
        }
        Writer writer = null;
        ParserWrapper parser = null;
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean validation = DEFAULT_VALIDATION;
        boolean externalDTD = DEFAULT_LOAD_EXTERNAL_DTD;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        boolean honourAllSchemaLocations = DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS;
        boolean validateAnnotations = DEFAULT_VALIDATE_ANNOTATIONS;
        boolean generateSyntheticAnnotations = DEFAULT_GENERATE_SYNTHETIC_ANNOTATIONS;
        boolean dynamicValidation = DEFAULT_DYNAMIC_VALIDATION;
        boolean xincludeProcessing = DEFAULT_XINCLUDE;
        boolean xincludeFixupBaseURIs = DEFAULT_XINCLUDE_FIXUP_BASE_URIS;
        boolean xincludeFixupLanguage = DEFAULT_XINCLUDE_FIXUP_LANGUAGE;
        boolean canonical = DEFAULT_CANONICAL;
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
                        parser = (ParserWrapper)Class.forName(parserName).newInstance();
                    }
                    catch (Exception e) {
                        parser = null;
                        System.err.println("error: Unable to instantiate parser ("+parserName+")");
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
                if (option.equalsIgnoreCase("xd")) {
                    externalDTD = option.equals("xd");
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
                if (option.equalsIgnoreCase("hs")) {
                    honourAllSchemaLocations = option.equals("hs");
                    continue;
                }
                if (option.equalsIgnoreCase("va")) {
                    validateAnnotations = option.equals("va");
                    continue;
                }
                if (option.equalsIgnoreCase("ga")) {
                    generateSyntheticAnnotations = option.equals("ga");
                    continue;
                }
                if (option.equalsIgnoreCase("dv")) {
                    dynamicValidation = option.equals("dv");
                    continue;
                }
                if (option.equalsIgnoreCase("xi")) {
                    xincludeProcessing = option.equals("xi");
                    continue;
                }
                if (option.equalsIgnoreCase("xb")) {
                    xincludeFixupBaseURIs = option.equals("xb");
                    continue;
                }
                if (option.equalsIgnoreCase("xl")) {
                    xincludeFixupLanguage = option.equals("xl");
                    continue;
                }
                if (option.equalsIgnoreCase("c")) {
                    canonical = option.equals("c");
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
            }
            if (parser == null) {
                try {
                    parser = (ParserWrapper)Class.forName(DEFAULT_PARSER_NAME).newInstance();
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
                parser.setFeature(LOAD_EXTERNAL_DTD_FEATURE_ID, externalDTD);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+LOAD_EXTERNAL_DTD_FEATURE_ID+")");
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
            try {
                parser.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, honourAllSchemaLocations);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+HONOUR_ALL_SCHEMA_LOCATIONS_ID+")");
            }
            try {
                parser.setFeature(VALIDATE_ANNOTATIONS_ID, validateAnnotations);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+VALIDATE_ANNOTATIONS_ID+")");
            }
            try {
                parser.setFeature(GENERATE_SYNTHETIC_ANNOTATIONS_ID, generateSyntheticAnnotations);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+GENERATE_SYNTHETIC_ANNOTATIONS_ID+")");
            }
            try {
                parser.setFeature(DYNAMIC_VALIDATION_FEATURE_ID, dynamicValidation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+DYNAMIC_VALIDATION_FEATURE_ID+")");
            }
            try {
                parser.setFeature(XINCLUDE_FEATURE_ID, xincludeProcessing);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+XINCLUDE_FEATURE_ID+")");
            }
            try {
                parser.setFeature(XINCLUDE_FIXUP_BASE_URIS_FEATURE_ID, xincludeFixupBaseURIs);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+XINCLUDE_FIXUP_BASE_URIS_FEATURE_ID+")");
            }
            try {
                parser.setFeature(XINCLUDE_FIXUP_LANGUAGE_FEATURE_ID, xincludeFixupLanguage);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("+XINCLUDE_FIXUP_LANGUAGE_FEATURE_ID+")");
            }
            if (writer == null) {
                writer = new Writer();
                try {
                    writer.setOutput(System.out, "UTF8");
                }
                catch (UnsupportedEncodingException e) {
                    System.err.println("error: Unable to set output. Exiting.");
                    System.exit(1);
                }
            }
            writer.setCanonical(canonical);
            try {
                Document document = parser.parse(arg);
                writer.write(document);
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
    } 
    private static void printUsage() {
        System.err.println("usage: java dom.Writer (options) uri ...");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name     Select parser by name.");
        System.err.println("  -n | -N     Turn on/off namespace processing.");
        System.err.println("  -v | -V     Turn on/off validation.");
        System.err.println("  -xd | -XD   Turn on/off loading of external DTDs.");
        System.err.println("              NOTE: Always on when -v in use and not supported by all parsers.");
        System.err.println("  -s | -S     Turn on/off Schema validation support.");
        System.err.println("              NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F    Turn on/off Schema full checking.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -hs | -HS   Turn on/off honouring of all schema locations.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -va | -VA   Turn on/off validation of schema annotations.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -ga | -GA   Turn on/off generation of synthetic schema annotations.");
        System.err.println("              NOTE: Requires use of -s and not supported by all parsers.");
        System.err.println("  -dv | -DV   Turn on/off dynamic validation.");
        System.err.println("              NOTE: Not supported by all parsers.");
        System.err.println("  -xi | -XI   Turn on/off XInclude processing.");
        System.err.println("              NOTE: Not supported by all parsers.");
        System.err.println("  -xb | -XB   Turn on/off base URI fixup during XInclude processing.");
        System.err.println("              NOTE: Requires use of -xi and not supported by all parsers.");
        System.err.println("  -xl | -XL   Turn on/off language fixup during XInclude processing.");
        System.err.println("              NOTE: Requires use of -xi and not supported by all parsers.");
        System.err.println("  -c | -C     Turn on/off Canonical XML output.");
        System.err.println("              NOTE: This is not W3C canonical output.");
        System.err.println("  -h          This help screen.");
        System.err.println();
        System.err.println("defaults:");
        System.err.println("  Parser:     "+DEFAULT_PARSER_NAME);
        System.err.print("  Namespaces: ");
        System.err.println(DEFAULT_NAMESPACES ? "on" : "off");
        System.err.print("  Validation: ");
        System.err.println(DEFAULT_VALIDATION ? "on" : "off");
        System.err.print("  Load External DTD: ");
        System.err.println(DEFAULT_LOAD_EXTERNAL_DTD ? "on" : "off");
        System.err.print("  Schema:     ");
        System.err.println(DEFAULT_SCHEMA_VALIDATION ? "on" : "off");
        System.err.print("  Schema full checking:     ");
        System.err.println(DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off");
        System.err.print("  Dynamic:    ");
        System.err.println(DEFAULT_DYNAMIC_VALIDATION ? "on" : "off");
        System.err.print("  Canonical:  ");
        System.err.println(DEFAULT_CANONICAL ? "on" : "off");
        System.err.print("  Honour all schema locations:       ");
        System.err.println(DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS ? "on" : "off");
        System.err.print("  Validate Annotations:              ");
        System.err.println(DEFAULT_VALIDATE_ANNOTATIONS ? "on" : "off");
        System.err.print("  Generate Synthetic Annotations:    ");
        System.err.println(DEFAULT_GENERATE_SYNTHETIC_ANNOTATIONS ? "on" : "off");
        System.err.print("  XInclude:   ");
        System.err.println(DEFAULT_XINCLUDE ? "on" : "off");
        System.err.print("  XInclude base URI fixup:  ");
        System.err.println(DEFAULT_XINCLUDE_FIXUP_BASE_URIS ? "on" : "off");
        System.err.print("  XInclude language fixup:  ");
        System.err.println(DEFAULT_XINCLUDE_FIXUP_LANGUAGE ? "on" : "off");
    } 
}  
