package dom.rename;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import dom.ParserWrapper;
import dom.util.Assertion;
public class Test implements UserDataHandler {
    protected static final String NAMESPACES_FEATURE_ID =
        "http://xml.org/sax/features/namespaces";
    protected static final String VALIDATION_FEATURE_ID =
        "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema";
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID =
        "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String DEFERRED_DOM_FEATURE_ID =
        "http://apache.org/xml/features/dom/defer-node-expansion";
    protected static final String DEFAULT_PARSER_NAME = "dom.wrappers.Xerces";
    protected static final boolean DEFAULT_NAMESPACES = true;
    protected static final boolean DEFAULT_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_VALIDATION = false;
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    protected static final boolean DEFAULT_DEFERRED_DOM = true;
    public void test(Document doc) {
        System.out.println("DOM rename Test...");
	NodeList elements = doc.getElementsByTagName("email");
	Element child = (Element) elements.item(0);
	Assertion.verify(child != null);
	Assertion.equals(child.getNodeName(), "email");
	Attr at = child.getAttributeNode("defaultEmailAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultEmailValue");
	Assertion.verify(at.getSpecified() == false);
	child.setUserData("mydata", "yo", this);
	Assertion.equals((String) child.getUserData("mydata"), "yo");
	Element newChild = (Element) doc.renameNode(child, null, "url");
	Assertion.equals(newChild.getNodeName(), "url");
	Assertion.verify(newChild.getNamespaceURI() == null);
	Assertion.verify(newChild.hasAttribute("defaultEmailAttr") == false);
	Assertion.verify(at.getSpecified() == true);
	at = newChild.getAttributeNode("defaultUrlAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultUrlValue");
	Assertion.verify(at.getSpecified() == false);
	Assertion.equals((String) newChild.getUserData("mydata"), "yo");
	if (newChild != child) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == child);
	    Assertion.verify(lastDestination == newChild);
	    resetHandlerData();
	}
	Element newChild2 = (Element) doc.renameNode(newChild, "ns1", "foo");
	Assertion.equals(newChild2.getNodeName(), "foo");
	Assertion.equals(newChild2.getNamespaceURI(), "ns1");
	Assertion.verify(newChild2.hasAttribute("defaultUrlAttr") == false);
	Assertion.equals((String) newChild2.getUserData("mydata"), "yo");
	if (newChild2 != newChild) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == newChild);
	    Assertion.verify(lastDestination == newChild2);
	    resetHandlerData();
	}
	child = (Element) elements.item(1);
	Assertion.verify(child != null);
	Assertion.equals(child.getNodeName(), "email");
	at = child.getAttributeNode("defaultEmailAttr");
	Assertion.verify(at != null);
	Assertion.equals(at.getValue(), "defaultEmailValue");
	Assertion.verify(at.getSpecified() == false);
	at.setUserData("mydata", "yo", this);
	Assertion.equals((String) at.getUserData("mydata"), "yo");
	Attr newAt = (Attr) doc.renameNode(at, null, "foo");
	Assertion.verify(newAt != null);
	Assertion.equals(newAt.getNodeName(), "foo");
	Assertion.equals(newAt.getNamespaceURI(), null);
	Assertion.equals(newAt.getValue(), "defaultEmailValue");
	Assertion.verify(newAt.getSpecified() == true);
	Assertion.verify(child.hasAttribute("foo") == true);
	Assertion.verify(child.hasAttribute("defaultEmailAttr") == true);
	Assertion.equals((String) newAt.getUserData("mydata"), "yo");
	if (newAt != at) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == at);
	    Assertion.verify(lastDestination == newAt);
	    resetHandlerData();
	}
	Attr newAt2 = (Attr) doc.renameNode(newAt, "ns1", "bar");
	Assertion.verify(newAt2 != null);
	Assertion.equals(newAt2.getNodeName(), "bar");
	Assertion.equals(newAt2.getNamespaceURI(), "ns1");
	Assertion.equals(newAt2.getValue(), "defaultEmailValue");
	Assertion.verify(newAt2.getSpecified() == true);
	Assertion.verify(child.hasAttributeNS("ns1", "bar") == true);
	Assertion.equals((String) newAt2.getUserData("mydata"), "yo");
	if (newAt2 != newAt) {
	    Assertion.verify(lastOperation == UserDataHandler.NODE_RENAMED);
	    Assertion.verify(lastKey == "mydata");
	    Assertion.equals((String) lastData, "yo");
	    Assertion.verify(lastSource == newAt);
	    Assertion.verify(lastDestination == newAt2);
	    resetHandlerData();
	}
        System.out.println("done.");
    } 
    short lastOperation = -1;
    String lastKey;
    Object lastData;
    Node lastSource;
    Node lastDestination;
    void resetHandlerData() {
	lastOperation = -1;
	lastKey = null;
	lastData = null;
	lastSource = null;
	lastDestination = null;
    }
    public void handle(short operation, String key, Object data,
		       Node src, Node dst) {
	lastOperation = operation;
	lastKey = key;
	lastData = data;
	lastSource = src;
	lastDestination = dst;
    }
    public static void main(String argv[]) {
        Test test = new Test();
        ParserWrapper parser = null;
        boolean namespaces = DEFAULT_NAMESPACES;
        boolean validation = DEFAULT_VALIDATION;
        boolean schemaValidation = DEFAULT_SCHEMA_VALIDATION;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        boolean deferredDom = DEFAULT_DEFERRED_DOM;
        String inputfile="tests/dom/rename/input.xml";
        for (int i = 0; i < argv.length; i++) {
            String arg = argv[i];
            if (arg.startsWith("-")) {
                String option = arg.substring(1);
                if (option.equals("p")) {
                    if (++i == argv.length) {
                        System.err.println("error: Missing argument to -p"
                                           + " option.");
                    }
                    String parserName = argv[i];
                    try {
                        parser = (ParserWrapper)
                            Class.forName(parserName).newInstance();
                    }
                    catch (Exception e) {
                        parser = null;
                        System.err.println("error: Unable to instantiate "
                                           + "parser (" + parserName + ")");
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
                if (option.equalsIgnoreCase("d")) {
                    deferredDom = option.equals("d");
                    continue;
                }
                if (option.equals("h")) {
                    printUsage();
                    continue;
                }
            }
        }
            if (parser == null) {
                try {
                    parser = (ParserWrapper)
                        Class.forName(DEFAULT_PARSER_NAME).newInstance();
                }
                catch (Exception e) {
                    System.err.println("error: Unable to instantiate parser ("
                                       + DEFAULT_PARSER_NAME + ")");
                    System.exit(1);
                }
            }
            try {
                parser.setFeature(NAMESPACES_FEATURE_ID, namespaces);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + NAMESPACES_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(VALIDATION_FEATURE_ID, validation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + VALIDATION_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(SCHEMA_VALIDATION_FEATURE_ID,
                                  schemaValidation);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + SCHEMA_VALIDATION_FEATURE_ID + ")");
            }
            try {
                parser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID,
                                  schemaFullChecking);
            }
            catch (SAXException e) {
                System.err.println("warning: Parser does not support feature ("
                                   + SCHEMA_FULL_CHECKING_FEATURE_ID + ")");
            }
            if (parser instanceof dom.wrappers.Xerces) {
                try {
                    parser.setFeature(DEFERRED_DOM_FEATURE_ID,
                                      deferredDom);
                }
                catch (SAXException e) {
                    System.err.println("warning: Parser does not support " +
                                       "feature (" +
                                       DEFERRED_DOM_FEATURE_ID + ")");
                }
            }
	    Document document = null;
            try {
                document = parser.parse(inputfile);
            }
            catch (SAXParseException e) {
            }
            catch (Exception e) {
                System.err.println("error: Parse error occurred - " +
                                   e.getMessage());
                Exception se = e;
                if (e instanceof SAXException) {
                    se = ((SAXException)e).getException();
                }
                if (se != null)
                  se.printStackTrace(System.err);
                else
                  e.printStackTrace(System.err);
		return;
            }
	    test.test(document);
    } 
    private static void printUsage() {
        System.err.println("usage: java dom.ids.Test (options) " +
                           "...data/personal.xml");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name    Select parser by name.");
        System.err.println("  -d  | -D   Turn on/off (Xerces) deferred DOM.");
        System.err.println("  -n  | -N   Turn on/off namespace processing.");
        System.err.println("  -v  | -V   Turn on/off validation.");
        System.err.println("  -s  | -S   Turn on/off Schema validation " +
                           "support.");
        System.err.println("             NOTE: Not supported by all parsers.");
        System.err.println("  -f  | -F   Turn on/off Schema full checking.");
        System.err.println("             NOTE: Requires use of -s and not " +
                           "supported by all parsers.");
        System.err.println("  -h         This help screen.");
        System.err.println();
        System.err.println("defaults:");
        System.err.println("  Parser:     " + DEFAULT_PARSER_NAME);
        System.err.println("  Xerces Deferred DOM: " +
                           (DEFAULT_DEFERRED_DOM ? "on" : "off"));
        System.err.println("  Namespaces: " +
                           (DEFAULT_NAMESPACES ? "on" : "off"));
        System.err.println("  Validation: " +
                           (DEFAULT_VALIDATION ? "on" : "off"));
        System.err.println("  Schema:     " +
                           (DEFAULT_SCHEMA_VALIDATION ? "on" : "off"));
        System.err.println("  Schema full checking:     " +
                           (DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off"));
    } 
} 
