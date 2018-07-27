package xni;
import java.util.Vector;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.parsers.XIncludeAwareParserConfiguration;
import org.apache.xerces.parsers.XMLGrammarPreparser;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public class XMLGrammarBuilder {
    public static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    public static final String GRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String NAMESPACES_FEATURE_ID = "http://xml.org/sax/features/namespaces";
    protected static final String VALIDATION_FEATURE_ID = "http://xml.org/sax/features/validation";
    protected static final String SCHEMA_VALIDATION_FEATURE_ID = "http://apache.org/xml/features/validation/schema";
    protected static final String SCHEMA_FULL_CHECKING_FEATURE_ID = "http://apache.org/xml/features/validation/schema-full-checking";
    protected static final String HONOUR_ALL_SCHEMA_LOCATIONS_ID = "http://apache.org/xml/features/honour-all-schemaLocations";
    public static final int BIG_PRIME = 2039;
    protected static final boolean DEFAULT_SCHEMA_FULL_CHECKING = false;
    protected static final boolean DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS = false;
    public static void main(String argv[]) {
        if (argv.length < 2) {
            printUsage();
            System.exit(1);
        }
        XMLParserConfiguration parserConfiguration = null;
        String arg = null;
        int i = 0;
        arg = argv[i];
        if (arg.equals("-p")) {
            i++;
            String parserName = argv[i];
            try {
                ClassLoader cl = ObjectFactory.findClassLoader();
                parserConfiguration = (XMLParserConfiguration)ObjectFactory.newInstance(parserName, cl, true);
            }
            catch (Exception e) {
                parserConfiguration = null;
                System.err.println("error: Unable to instantiate parser configuration ("+parserName+")");
            }
            i++;
        }
        arg = argv[i];
        Vector externalDTDs = null;
        if (arg.equals("-d")) {
            externalDTDs= new Vector();
            i++;
            while (i < argv.length && !(arg = argv[i]).startsWith("-")) {
                externalDTDs.addElement(arg);
                i++;
            }
            if (externalDTDs.size() == 0) {
                printUsage();
                System.exit(1);
            }
        }
        Vector schemas = null;
        boolean schemaFullChecking = DEFAULT_SCHEMA_FULL_CHECKING;
        boolean honourAllSchemaLocations = DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS;
        if(i < argv.length) {
            arg = argv[i];
            if (arg.equals("-f")) {
                schemaFullChecking = true;
                i++;
                arg = argv[i];
            } 
            else if (arg.equals("-F")) {
                schemaFullChecking = false;
                i++;
                arg = argv[i];
            }
            if (arg.equals("-hs")) {
                honourAllSchemaLocations = true;
                i++;
                arg = argv[i];
            }
            else if (arg.equals("-HS")) {
                honourAllSchemaLocations = false;
                i++;
                arg = argv[i];
            }
            if (arg.equals("-a")) {
                if(externalDTDs != null) {
                    printUsage();
                    System.exit(1);
                }
                schemas= new Vector();
                i++;
                while (i < argv.length && !(arg = argv[i]).startsWith("-")) {
                    schemas.addElement(arg);
                    i++;
                }
                if (schemas.size() == 0) {
                    printUsage();
                    System.exit(1);
                }
            }
        }
        Vector ifiles = null;
        if (i < argv.length) {
            if (!arg.equals("-i")) {
                printUsage();
                System.exit(1);
            }
            i++;
            ifiles = new Vector();
            while (i < argv.length && !(arg = argv[i]).startsWith("-")) {
                ifiles.addElement(arg);
                i++;
            }
            if (ifiles.size() == 0 || i != argv.length) {
                printUsage();
                System.exit(1);
            }
        }
        SymbolTable sym = new SymbolTable(BIG_PRIME);
        XMLGrammarPreparser preparser = new XMLGrammarPreparser(sym);
        XMLGrammarPoolImpl grammarPool = new XMLGrammarPoolImpl();
        boolean isDTD = false;
        if(externalDTDs != null) {
            preparser.registerPreparser(XMLGrammarDescription.XML_DTD, null);
            isDTD = true;
        } else if(schemas != null) {
            preparser.registerPreparser(XMLGrammarDescription.XML_SCHEMA, null);
            isDTD = false;
        } else {
            System.err.println("No schema or DTD specified!");
            System.exit(1);
        }
        preparser.setProperty(GRAMMAR_POOL, grammarPool);
        preparser.setFeature(NAMESPACES_FEATURE_ID, true);
        preparser.setFeature(VALIDATION_FEATURE_ID, true);
        preparser.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
        preparser.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
        preparser.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, honourAllSchemaLocations);
        try {
            if(isDTD) {
                for (i = 0; i < externalDTDs.size(); i++) {
                    Grammar g = preparser.preparseGrammar(XMLGrammarDescription.XML_DTD, stringToXIS((String)externalDTDs.elementAt(i)));
                }
            } else { 
                for (i = 0; i < schemas.size(); i++) {
                    Grammar g = preparser.preparseGrammar(XMLGrammarDescription.XML_SCHEMA, stringToXIS((String)schemas.elementAt(i)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (parserConfiguration == null) {
            parserConfiguration = new XIncludeAwareParserConfiguration(sym, grammarPool);
        } 
        else {
            parserConfiguration.setProperty(SYMBOL_TABLE, sym);
            parserConfiguration.setProperty(GRAMMAR_POOL, grammarPool);
        }
        try{
            parserConfiguration.setFeature(NAMESPACES_FEATURE_ID, true);
            parserConfiguration.setFeature(VALIDATION_FEATURE_ID, true);
            parserConfiguration.setFeature(SCHEMA_VALIDATION_FEATURE_ID, true);
            parserConfiguration.setFeature(SCHEMA_FULL_CHECKING_FEATURE_ID, schemaFullChecking);
            parserConfiguration.setFeature(HONOUR_ALL_SCHEMA_LOCATIONS_ID, honourAllSchemaLocations);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (ifiles != null) {
            try {
                for (i = 0; i < ifiles.size(); i++) {
                    parserConfiguration.parse(stringToXIS((String)ifiles.elementAt(i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
    } 
    private static void printUsage() {
        System.err.println("usage: java xni.XMLGrammarBuilder [-p name] -d uri ... | [-f|-F] [-hs|-HS] -a uri ... [-i uri ...]");
        System.err.println();
        System.err.println("options:");
        System.err.println("  -p name     Select parser configuration by name to use for instance validation");
        System.err.println("  -d          Grammars to preparse are DTD external subsets");
        System.err.println("  -f  | -F    Turn on/off Schema full checking (default "+
                (DEFAULT_SCHEMA_FULL_CHECKING ? "on" : "off)"));
        System.err.println("  -hs | -HS   Turn on/off honouring of all schema locations (default "+
                (DEFAULT_HONOUR_ALL_SCHEMA_LOCATIONS ? "on" : "off)"));
        System.err.println("  -a uri ...  Provide a list of schema documents");
        System.err.println("  -i uri ...  Provide a list of instance documents to validate");
        System.err.println();
        System.err.println("NOTE: Both -d and -a cannot be specified!");
    } 
    private static XMLInputSource stringToXIS(String uri) {
        return new XMLInputSource(null, uri, null);
    }
} 
