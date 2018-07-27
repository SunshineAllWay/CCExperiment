package org.apache.xerces.impl.xs.traversers;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.xs.XSSimpleTypeDecl;
import org.apache.xerces.impl.xs.SchemaGrammar;
import org.apache.xerces.impl.xs.SchemaNamespaceSupport;
import org.apache.xerces.impl.xs.SchemaSymbols;
import org.apache.xerces.impl.xs.XMLSchemaException;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XSAttributeDecl;
import org.apache.xerces.impl.xs.XSAttributeGroupDecl;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.impl.xs.XSDDescription;
import org.apache.xerces.impl.xs.XSDeclarationPool;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSGrammarBucket;
import org.apache.xerces.impl.xs.XSGroupDecl;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.impl.xs.XSModelGroupImpl;
import org.apache.xerces.impl.xs.XSNotationDecl;
import org.apache.xerces.impl.xs.XSParticleDecl;
import org.apache.xerces.impl.xs.identity.IdentityConstraint;
import org.apache.xerces.impl.xs.opti.ElementImpl;
import org.apache.xerces.impl.xs.opti.SchemaDOMParser;
import org.apache.xerces.impl.xs.opti.SchemaParsingConfig;
import org.apache.xerces.impl.xs.util.SimpleLocator;
import org.apache.xerces.impl.xs.util.XSInputSource;
import org.apache.xerces.parsers.SAXParser;
import org.apache.xerces.parsers.XML11Configuration;
import org.apache.xerces.util.DOMInputSource;
import org.apache.xerces.util.DOMUtil;
import org.apache.xerces.util.DefaultErrorHandler;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.SAXInputSource;
import org.apache.xerces.util.StAXInputSource;
import org.apache.xerces.util.StAXLocationWrapper;
import org.apache.xerces.util.SymbolHash;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.XMLSchemaDescription;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeGroupDefinition;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSModelGroupDefinition;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSObject;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSSimpleTypeDefinition;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.apache.xerces.xs.datatypes.ObjectList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class XSDHandler {
    protected static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    protected static final String XMLSCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS =
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String STANDARD_URI_CONFORMANT_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.STANDARD_URI_CONFORMANT_FEATURE;
    protected static final String DISALLOW_DOCTYPE =
        Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE;
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected static final String VALIDATE_ANNOTATIONS =
        Constants.XERCES_FEATURE_PREFIX + Constants.VALIDATE_ANNOTATIONS_FEATURE;
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = 
      Constants.XERCES_FEATURE_PREFIX + Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;
    protected static final String NAMESPACE_GROWTH = 
      Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_GROWTH_FEATURE;
    protected static final String TOLERATE_DUPLICATES = 
      Constants.XERCES_FEATURE_PREFIX + Constants.TOLERATE_DUPLICATES_FEATURE;
    private static final String NAMESPACE_PREFIXES =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACE_PREFIXES_FEATURE;
    protected static final String STRING_INTERNING =
        Constants.SAX_FEATURE_PREFIX + Constants.STRING_INTERNING_FEATURE;
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    protected static final String JAXP_SCHEMA_SOURCE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE;
    public static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    public static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    public static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    public static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    protected static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    protected static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    protected static final boolean DEBUG_NODE_POOL = false;
    final static int ATTRIBUTE_TYPE          = 1;
    final static int ATTRIBUTEGROUP_TYPE     = 2;
    final static int ELEMENT_TYPE            = 3;
    final static int GROUP_TYPE              = 4;
    final static int IDENTITYCONSTRAINT_TYPE = 5;
    final static int NOTATION_TYPE           = 6;
    final static int TYPEDECL_TYPE           = 7;
    public final static String REDEF_IDENTIFIER = "_fn3dktizrknc9pi";
    protected Hashtable fNotationRegistry = new Hashtable();
    protected XSDeclarationPool fDeclPool = null;
    private Hashtable fUnparsedAttributeRegistry = new Hashtable();
    private Hashtable fUnparsedAttributeGroupRegistry = new Hashtable();
    private Hashtable fUnparsedElementRegistry = new Hashtable();
    private Hashtable fUnparsedGroupRegistry = new Hashtable();
    private Hashtable fUnparsedIdentityConstraintRegistry = new Hashtable();
    private Hashtable fUnparsedNotationRegistry = new Hashtable();
    private Hashtable fUnparsedTypeRegistry = new Hashtable();
    private Hashtable fUnparsedAttributeRegistrySub = new Hashtable();
    private Hashtable fUnparsedAttributeGroupRegistrySub = new Hashtable();
    private Hashtable fUnparsedElementRegistrySub = new Hashtable();
    private Hashtable fUnparsedGroupRegistrySub = new Hashtable();
    private Hashtable fUnparsedIdentityConstraintRegistrySub = new Hashtable();
    private Hashtable fUnparsedNotationRegistrySub = new Hashtable();
    private Hashtable fUnparsedTypeRegistrySub = new Hashtable();
    private Hashtable fUnparsedRegistriesExt[] = new Hashtable[] {
        null,
        new Hashtable(), 
        new Hashtable(), 
        new Hashtable(), 
        new Hashtable(), 
        new Hashtable(), 
        new Hashtable(), 
        new Hashtable(), 
    };
    private Hashtable fXSDocumentInfoRegistry = new Hashtable();
    private Hashtable fDependencyMap = new Hashtable();
    private Hashtable fImportMap = new Hashtable();
    private Vector fAllTNSs = new Vector();
    private Hashtable fLocationPairs = null;
    private static final Hashtable EMPTY_TABLE = new Hashtable();
    Hashtable fHiddenNodes = null;
    private String null2EmptyString(String ns) {
        return ns == null ? XMLSymbols.EMPTY_STRING : ns;
    }
    private String emptyString2Null(String ns) {
        return ns == XMLSymbols.EMPTY_STRING ? null : ns;
    }
    private String doc2SystemId(Element ele) {
        String documentURI = null;
        if(ele.getOwnerDocument() instanceof org.apache.xerces.impl.xs.opti.SchemaDOM){
            documentURI = ((org.apache.xerces.impl.xs.opti.SchemaDOM) ele.getOwnerDocument()).getDocumentURI();
        }
        return documentURI != null ? documentURI : (String) fDoc2SystemId.get(ele);
    }
    private Hashtable fTraversed = new Hashtable();
    private Hashtable fDoc2SystemId = new Hashtable();
    private XSDocumentInfo fRoot = null;
    private Hashtable fDoc2XSDocumentMap = new Hashtable();
    private Hashtable fRedefine2XSDMap = new Hashtable();
    private Hashtable fRedefine2NSSupport = new Hashtable();
    private Hashtable fRedefinedRestrictedAttributeGroupRegistry = new Hashtable();
    private Hashtable fRedefinedRestrictedGroupRegistry = new Hashtable();
    private boolean fLastSchemaWasDuplicate;
    private boolean fValidateAnnotations = false;
    private boolean fHonourAllSchemaLocations = false;
    boolean fNamespaceGrowth = false;
    boolean fTolerateDuplicates = false;
    private XMLErrorReporter fErrorReporter;
    private XMLEntityResolver fEntityResolver;
    private XSAttributeChecker fAttributeChecker;
    private SymbolTable fSymbolTable;
    private XSGrammarBucket fGrammarBucket;
    private XSDDescription fSchemaGrammarDescription;
    private XMLGrammarPool fGrammarPool;
    XSDAttributeGroupTraverser fAttributeGroupTraverser;
    XSDAttributeTraverser fAttributeTraverser;
    XSDComplexTypeTraverser fComplexTypeTraverser;
    XSDElementTraverser fElementTraverser;
    XSDGroupTraverser fGroupTraverser;
    XSDKeyrefTraverser fKeyrefTraverser;
    XSDNotationTraverser fNotationTraverser;
    XSDSimpleTypeTraverser fSimpleTypeTraverser;
    XSDUniqueOrKeyTraverser fUniqueOrKeyTraverser;
    XSDWildcardTraverser fWildCardTraverser;
    SchemaDVFactory fDVFactory;
    SchemaDOMParser fSchemaParser;
    SchemaContentHandler fXSContentHandler;
    StAXSchemaParser fStAXSchemaParser;
    XML11Configuration fAnnotationValidator;
    XSAnnotationGrammarPool fGrammarBucketAdapter;
    private static final int INIT_STACK_SIZE = 30;
    private static final int INC_STACK_SIZE  = 10;
    private int fLocalElemStackPos = 0;
    private XSParticleDecl[] fParticle = new XSParticleDecl[INIT_STACK_SIZE];
    private Element[] fLocalElementDecl = new Element[INIT_STACK_SIZE];
    private XSDocumentInfo[] fLocalElementDecl_schema = new XSDocumentInfo[INIT_STACK_SIZE]; 
    private int[] fAllContext = new int[INIT_STACK_SIZE];
    private XSObject[] fParent = new XSObject[INIT_STACK_SIZE];
    private String [][] fLocalElemNamespaceContext = new String [INIT_STACK_SIZE][1];
    private static final int INIT_KEYREF_STACK = 2;
    private static final int INC_KEYREF_STACK_AMOUNT = 2;
    private int fKeyrefStackPos = 0;
    private Element [] fKeyrefs = new Element[INIT_KEYREF_STACK];
    private XSDocumentInfo [] fKeyrefsMapXSDocumentInfo = new XSDocumentInfo[INIT_KEYREF_STACK];
    private XSElementDecl [] fKeyrefElems = new XSElementDecl [INIT_KEYREF_STACK];
    private String [][] fKeyrefNamespaceContext = new String[INIT_KEYREF_STACK][1];
    SymbolHash fGlobalAttrDecls = new SymbolHash();
    SymbolHash fGlobalAttrGrpDecls = new SymbolHash();
    SymbolHash fGlobalElemDecls = new SymbolHash();
    SymbolHash fGlobalGroupDecls = new SymbolHash();
    SymbolHash fGlobalNotationDecls = new SymbolHash();
    SymbolHash fGlobalIDConstraintDecls = new SymbolHash();
    SymbolHash fGlobalTypeDecls = new SymbolHash();
    public XSDHandler(){
        fHiddenNodes = new Hashtable();       
        fSchemaParser = new SchemaDOMParser(new SchemaParsingConfig());
    }
    public XSDHandler (XSGrammarBucket gBucket) {
        this();
        fGrammarBucket = gBucket;
        fSchemaGrammarDescription = new XSDDescription();
    } 
    public SchemaGrammar parseSchema(XMLInputSource is, XSDDescription desc,
            Hashtable locationPairs)
    throws IOException {
        fLocationPairs = locationPairs;
        fSchemaParser.resetNodePool();   
        SchemaGrammar grammar = null;
        String schemaNamespace  = null;
        short referType = desc.getContextType();
        if (referType != XSDDescription.CONTEXT_PREPARSE){
            if (fHonourAllSchemaLocations && referType == XSDDescription.CONTEXT_IMPORT && isExistingGrammar(desc, fNamespaceGrowth)) {
                grammar = fGrammarBucket.getGrammar(desc.getTargetNamespace());
            }
            else {
                grammar = findGrammar(desc, fNamespaceGrowth);
            }
            if (grammar != null) {
                if (!fNamespaceGrowth) {
                    return grammar;
                }
                else {
                    try {
                        if (grammar.getDocumentLocations().contains(XMLEntityManager.expandSystemId(is.getSystemId(), is.getBaseSystemId(), false))) {
                            return grammar; 
                        }
                    }
                    catch (MalformedURIException e) {
                    }
                }
            }
            schemaNamespace = desc.getTargetNamespace();
            if (schemaNamespace != null) {
                schemaNamespace = fSymbolTable.addSymbol(schemaNamespace);
            }
        }
        prepareForParse();       
        Element schemaRoot = null;
        if (is instanceof DOMInputSource) {
            schemaRoot = getSchemaDocument(schemaNamespace, (DOMInputSource) is,
                    referType == XSDDescription.CONTEXT_PREPARSE,
                    referType, null); 
        } 
        else if (is instanceof SAXInputSource) {
        	schemaRoot = getSchemaDocument(schemaNamespace, (SAXInputSource) is,
                    referType == XSDDescription.CONTEXT_PREPARSE,
                    referType, null);     
        } 
        else if (is instanceof StAXInputSource) {
            schemaRoot = getSchemaDocument(schemaNamespace, (StAXInputSource) is,
                    referType == XSDDescription.CONTEXT_PREPARSE,
                    referType, null);
        } 
        else if (is instanceof XSInputSource) {
            schemaRoot = getSchemaDocument((XSInputSource) is, desc);
        } 
        else {
        	schemaRoot = getSchemaDocument(schemaNamespace, is,
                  referType == XSDDescription.CONTEXT_PREPARSE,
                  referType, null);
        } 
        if (schemaRoot == null) {
            if (is instanceof XSInputSource) {
                return fGrammarBucket.getGrammar(desc.getTargetNamespace());
            }
            return grammar;
        }
        if (referType == XSDDescription.CONTEXT_PREPARSE) {
        	Element schemaElem = schemaRoot;
            schemaNamespace = DOMUtil.getAttrValue(schemaElem, SchemaSymbols.ATT_TARGETNAMESPACE);
            if(schemaNamespace != null && schemaNamespace.length() > 0) {
                schemaNamespace = fSymbolTable.addSymbol(schemaNamespace);
                desc.setTargetNamespace(schemaNamespace);
            }
            else {
                schemaNamespace = null;
            }
            grammar = findGrammar(desc, fNamespaceGrowth);
            String schemaId = XMLEntityManager.expandSystemId(is.getSystemId(), is.getBaseSystemId(), false);
            if (grammar != null) {
                if (!fNamespaceGrowth || (schemaId != null && grammar.getDocumentLocations().contains(schemaId))) {
                    return grammar; 
                }
            }
            XSDKey key = new XSDKey(schemaId, referType, schemaNamespace);
            fTraversed.put(key, schemaRoot);
            if (schemaId != null) {
            	fDoc2SystemId.put(schemaRoot, schemaId);
            }
        }
        prepareForTraverse();
        fRoot = constructTrees(schemaRoot, is.getSystemId(), desc, grammar != null);
        if (fRoot == null) {
            return null;
        }
        buildGlobalNameRegistries();
        ArrayList annotationInfo = fValidateAnnotations ? new ArrayList() : null;
        traverseSchemas(annotationInfo);
        traverseLocalElements();
        resolveKeyRefs();
        for (int i = fAllTNSs.size() - 1; i >= 0; i--) {
            String tns = (String)fAllTNSs.elementAt(i);
            Vector ins = (Vector)fImportMap.get(tns);
            SchemaGrammar sg = fGrammarBucket.getGrammar(emptyString2Null(tns));
            if (sg == null)
                continue;
            SchemaGrammar isg;
            int count = 0;
            for (int j = 0; j < ins.size(); j++) {
                isg = fGrammarBucket.getGrammar((String)ins.elementAt(j));
                if (isg != null)
                    ins.setElementAt(isg, count++);
            }
            ins.setSize(count);
            sg.setImportedGrammars(ins);
        }
        if (fValidateAnnotations && annotationInfo.size() > 0) {
            validateAnnotations(annotationInfo);
        }
        return fGrammarBucket.getGrammar(fRoot.fTargetNamespace);
    } 
    private void validateAnnotations(ArrayList annotationInfo) {
        if (fAnnotationValidator == null) {
            createAnnotationValidator();
        }
        final int size = annotationInfo.size();
        final XMLInputSource src = new XMLInputSource(null, null, null);
        fGrammarBucketAdapter.refreshGrammars(fGrammarBucket);
        for (int i = 0; i < size; i += 2) {
            src.setSystemId((String) annotationInfo.get(i));
            XSAnnotationInfo annotation = (XSAnnotationInfo) annotationInfo.get(i+1);
            while (annotation != null) {
                src.setCharacterStream(new StringReader(annotation.fAnnotation));
                try {
                    fAnnotationValidator.parse(src);
                }
                catch (IOException exc) {}
                annotation = annotation.next;
            }
        }
    }
    private void createAnnotationValidator() {
        fAnnotationValidator = new XML11Configuration();
        fGrammarBucketAdapter = new XSAnnotationGrammarPool();
        fAnnotationValidator.setFeature(VALIDATION, true);
        fAnnotationValidator.setFeature(XMLSCHEMA_VALIDATION, true);
        fAnnotationValidator.setProperty(XMLGRAMMAR_POOL, fGrammarBucketAdapter);
        XMLErrorHandler errorHandler = fErrorReporter.getErrorHandler();
        fAnnotationValidator.setProperty(ERROR_HANDLER, (errorHandler != null) ? errorHandler : new DefaultErrorHandler());
        Locale locale = fErrorReporter.getLocale();
        fAnnotationValidator.setProperty(LOCALE, locale);
    }
    SchemaGrammar getGrammar(String tns) {
        return fGrammarBucket.getGrammar(tns);
    }
    protected SchemaGrammar findGrammar(XSDDescription desc, boolean ignoreConflict) {
        SchemaGrammar sg = fGrammarBucket.getGrammar(desc.getTargetNamespace());
        if (sg == null) {
            if (fGrammarPool != null) {
                sg = (SchemaGrammar)fGrammarPool.retrieveGrammar(desc);
                if (sg != null) {
                    if (!fGrammarBucket.putGrammar(sg, true, ignoreConflict)) {
                        reportSchemaWarning("GrammarConflict", null, null);
                        sg = null;
                    }
                }
            }
        }
        return sg;
    }
    private static final String[][] NS_ERROR_CODES = {
            {"src-include.2.1", "src-include.2.1"},
            {"src-redefine.3.1", "src-redefine.3.1"},
            {"src-import.3.1", "src-import.3.2"},
            null,
            {"TargetNamespace.1", "TargetNamespace.2"},
            {"TargetNamespace.1", "TargetNamespace.2"},
            {"TargetNamespace.1", "TargetNamespace.2"},
            {"TargetNamespace.1", "TargetNamespace.2"}
    };
    private static final String[] ELE_ERROR_CODES = {
            "src-include.1", "src-redefine.2", "src-import.2", "schema_reference.4",
            "schema_reference.4", "schema_reference.4", "schema_reference.4", "schema_reference.4"
    };
    protected XSDocumentInfo constructTrees(Element schemaRoot, String locationHint, XSDDescription desc, boolean nsCollision) {
        if (schemaRoot == null) return null;
        String callerTNS = desc.getTargetNamespace();
        short referType = desc.getContextType();
        XSDocumentInfo currSchemaInfo = null;
        try {
            currSchemaInfo = new XSDocumentInfo(schemaRoot, fAttributeChecker, fSymbolTable);
        } catch (XMLSchemaException se) {
            reportSchemaError(ELE_ERROR_CODES[referType],
                    new Object[]{locationHint},
					  schemaRoot);
            return null;
        }
        if (currSchemaInfo.fTargetNamespace != null &&
                currSchemaInfo.fTargetNamespace.length() == 0) {
            reportSchemaWarning("EmptyTargetNamespace",
                    new Object[]{locationHint},
					schemaRoot);
            currSchemaInfo.fTargetNamespace = null;
        }
        if (callerTNS != null) {
            int secondIdx = 0;
            if (referType == XSDDescription.CONTEXT_INCLUDE ||
                    referType == XSDDescription.CONTEXT_REDEFINE) {
                if (currSchemaInfo.fTargetNamespace == null) {
                    currSchemaInfo.fTargetNamespace = callerTNS;
                    currSchemaInfo.fIsChameleonSchema = true;
                }
                else if (callerTNS != currSchemaInfo.fTargetNamespace) {
                    reportSchemaError(NS_ERROR_CODES[referType][secondIdx],
                            new Object [] {callerTNS, currSchemaInfo.fTargetNamespace},
							schemaRoot);
                    return null;
                }
            }
            else if (referType != XSDDescription.CONTEXT_PREPARSE && callerTNS != currSchemaInfo.fTargetNamespace) {
                reportSchemaError(NS_ERROR_CODES[referType][secondIdx],
                        new Object [] {callerTNS, currSchemaInfo.fTargetNamespace},
						schemaRoot);
                return null;
            }
        }
        else if (currSchemaInfo.fTargetNamespace != null) {
            if (referType == XSDDescription.CONTEXT_PREPARSE) {
                desc.setTargetNamespace(currSchemaInfo.fTargetNamespace);
                callerTNS = currSchemaInfo.fTargetNamespace;
            }
            else {
                int secondIdx = 1;
                reportSchemaError(NS_ERROR_CODES[referType][secondIdx],
                        new Object [] {callerTNS, currSchemaInfo.fTargetNamespace},
						schemaRoot);
                return null;
            }
        }
        currSchemaInfo.addAllowedNS(currSchemaInfo.fTargetNamespace);
        SchemaGrammar sg = null;
        if (nsCollision) {
            SchemaGrammar sg2 = fGrammarBucket.getGrammar(currSchemaInfo.fTargetNamespace);
            if (sg2.isImmutable()) {
                sg = new SchemaGrammar(sg2);
                fGrammarBucket.putGrammar(sg);
                updateImportListWith(sg);
            }
            else {
                sg = sg2;
            }
            updateImportListFor(sg);
        }
        else if (referType == XSDDescription.CONTEXT_INCLUDE ||
                referType == XSDDescription.CONTEXT_REDEFINE) {
            sg = fGrammarBucket.getGrammar(currSchemaInfo.fTargetNamespace);
        }
        else if(fHonourAllSchemaLocations && referType == XSDDescription.CONTEXT_IMPORT) {
            sg = findGrammar(desc, false);
            if(sg == null) {
                sg = new SchemaGrammar(currSchemaInfo.fTargetNamespace, desc.makeClone(), fSymbolTable);
                fGrammarBucket.putGrammar(sg);
            }
        }
        else {
            sg = new SchemaGrammar(currSchemaInfo.fTargetNamespace, desc.makeClone(), fSymbolTable);
            fGrammarBucket.putGrammar(sg);
        }
        sg.addDocument(null, (String)fDoc2SystemId.get(currSchemaInfo.fSchemaElement));
        fDoc2XSDocumentMap.put(schemaRoot, currSchemaInfo);
        Vector dependencies = new Vector();
        Element rootNode = schemaRoot;
        Element newSchemaRoot = null;
        for (Element child = DOMUtil.getFirstChildElement(rootNode);
        child != null;
        child = DOMUtil.getNextSiblingElement(child)) {
            String schemaNamespace=null;
            String schemaHint=null;
            String localName = DOMUtil.getLocalName(child);
            short refType = -1;
            boolean importCollision = false;
            if (localName.equals(SchemaSymbols.ELT_ANNOTATION))
                continue;
            else if (localName.equals(SchemaSymbols.ELT_IMPORT)) {
                refType = XSDDescription.CONTEXT_IMPORT;
                Object[] importAttrs = fAttributeChecker.checkAttributes(child, true, currSchemaInfo);
                schemaHint = (String)importAttrs[XSAttributeChecker.ATTIDX_SCHEMALOCATION];
                schemaNamespace = (String)importAttrs[XSAttributeChecker.ATTIDX_NAMESPACE];
                if (schemaNamespace != null)
                    schemaNamespace = fSymbolTable.addSymbol(schemaNamespace);
                Element importChild = DOMUtil.getFirstChildElement(child);
                if(importChild != null ) {
                    String importComponentType = DOMUtil.getLocalName(importChild);
                    if (importComponentType.equals(SchemaSymbols.ELT_ANNOTATION)) {
                        sg.addAnnotation(
                                fElementTraverser.traverseAnnotationDecl(importChild, importAttrs, true, currSchemaInfo));
                    } else {
                        reportSchemaError("s4s-elt-must-match.1", new Object [] {localName, "annotation?", importComponentType}, child);
                    }
                    if(DOMUtil.getNextSiblingElement(importChild) != null) {
                        reportSchemaError("s4s-elt-must-match.1", new Object [] {localName, "annotation?", DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(importChild))}, child);
                    }
                }
                else {
                    String text = DOMUtil.getSyntheticAnnotation(child);
                    if (text != null) {
                        sg.addAnnotation(fElementTraverser.traverseSyntheticAnnotation(child, text, importAttrs, true, currSchemaInfo));
                    }
                }
                fAttributeChecker.returnAttrArray(importAttrs, currSchemaInfo);
                if (schemaNamespace == currSchemaInfo.fTargetNamespace) {
                    reportSchemaError(schemaNamespace != null ? 
                            "src-import.1.1" : "src-import.1.2", new Object [] {schemaNamespace}, child);
                    continue;
                }
                if(currSchemaInfo.isAllowedNS(schemaNamespace)) {
                    if(!fHonourAllSchemaLocations && !fNamespaceGrowth)
                        continue;
                }
                else  {
                    currSchemaInfo.addAllowedNS(schemaNamespace);
                }
                String tns = null2EmptyString(currSchemaInfo.fTargetNamespace);
                Vector ins = (Vector)fImportMap.get(tns);
                if (ins == null) {
                    fAllTNSs.addElement(tns);
                    ins = new Vector();
                    fImportMap.put(tns, ins);
                    ins.addElement(schemaNamespace);
                }
                else if (!ins.contains(schemaNamespace)){
                    ins.addElement(schemaNamespace);
                }
                fSchemaGrammarDescription.reset();
                fSchemaGrammarDescription.setContextType(XSDDescription.CONTEXT_IMPORT);
                fSchemaGrammarDescription.setBaseSystemId(doc2SystemId(schemaRoot));
                fSchemaGrammarDescription.setLiteralSystemId(schemaHint);
                fSchemaGrammarDescription.setLocationHints(new String[]{schemaHint});
                fSchemaGrammarDescription.setTargetNamespace(schemaNamespace);
                SchemaGrammar isg = findGrammar(fSchemaGrammarDescription, fNamespaceGrowth);
                if (isg != null) {
                    if (fNamespaceGrowth) {
                        try {
                            if (isg.getDocumentLocations().contains(XMLEntityManager.expandSystemId(schemaHint, fSchemaGrammarDescription.getBaseSystemId(), false))) {
                                continue;
                            }
                            else {
                                importCollision = true;
                            }
                        } 
                        catch (MalformedURIException e) {
                        }
                    }
                    else if (!fHonourAllSchemaLocations || isExistingGrammar(fSchemaGrammarDescription, false)) {
                        continue;
                    }
                }
                newSchemaRoot = resolveSchema(fSchemaGrammarDescription, false, child, isg == null);
            }
            else if ((localName.equals(SchemaSymbols.ELT_INCLUDE)) ||
                    (localName.equals(SchemaSymbols.ELT_REDEFINE))) {
                Object[] includeAttrs = fAttributeChecker.checkAttributes(child, true, currSchemaInfo);
                schemaHint = (String)includeAttrs[XSAttributeChecker.ATTIDX_SCHEMALOCATION];
                if (localName.equals(SchemaSymbols.ELT_REDEFINE)) {
                    fRedefine2NSSupport.put(child, new SchemaNamespaceSupport(currSchemaInfo.fNamespaceSupport));
                }
                if(localName.equals(SchemaSymbols.ELT_INCLUDE)) {
                    Element includeChild = DOMUtil.getFirstChildElement(child);
                    if(includeChild != null ) {
                        String includeComponentType = DOMUtil.getLocalName(includeChild);
                        if (includeComponentType.equals(SchemaSymbols.ELT_ANNOTATION)) {
                            sg.addAnnotation(
                                    fElementTraverser.traverseAnnotationDecl(includeChild, includeAttrs, true, currSchemaInfo));
                        } else {
                            reportSchemaError("s4s-elt-must-match.1", new Object [] {localName, "annotation?", includeComponentType}, child);
                        }
                        if(DOMUtil.getNextSiblingElement(includeChild) != null) {
                            reportSchemaError("s4s-elt-must-match.1", new Object [] {localName, "annotation?", DOMUtil.getLocalName(DOMUtil.getNextSiblingElement(includeChild))}, child);
                        }
                    }
                    else {
                        String text = DOMUtil.getSyntheticAnnotation(child);
                        if (text != null) {
                            sg.addAnnotation(fElementTraverser.traverseSyntheticAnnotation(child, text, includeAttrs, true, currSchemaInfo));
                        }
                    }
                }
                else {
                    for (Element redefinedChild = DOMUtil.getFirstChildElement(child);
                    redefinedChild != null;
                    redefinedChild = DOMUtil.getNextSiblingElement(redefinedChild)) {
                        String redefinedComponentType = DOMUtil.getLocalName(redefinedChild);
                        if (redefinedComponentType.equals(SchemaSymbols.ELT_ANNOTATION)) {
                            sg.addAnnotation(
                                    fElementTraverser.traverseAnnotationDecl(redefinedChild, includeAttrs, true, currSchemaInfo));
                            DOMUtil.setHidden(redefinedChild, fHiddenNodes);
                        }
                        else {
                            String text = DOMUtil.getSyntheticAnnotation(child);
                            if (text != null) {
                                sg.addAnnotation(fElementTraverser.traverseSyntheticAnnotation(child, text, includeAttrs, true, currSchemaInfo));
                            }
                        }
                    }
                }
                fAttributeChecker.returnAttrArray(includeAttrs, currSchemaInfo);
                if (schemaHint == null) {
                    reportSchemaError("s4s-att-must-appear", new Object [] {
                            "<include> or <redefine>", "schemaLocation"},
                            child);
                }
                boolean mustResolve = false;
                refType = XSDDescription.CONTEXT_INCLUDE;
                if(localName.equals(SchemaSymbols.ELT_REDEFINE)) {
                    mustResolve = nonAnnotationContent(child);
                    refType = XSDDescription.CONTEXT_REDEFINE;
                }
                fSchemaGrammarDescription.reset();
                fSchemaGrammarDescription.setContextType(refType);
                fSchemaGrammarDescription.setBaseSystemId(doc2SystemId(schemaRoot));
                fSchemaGrammarDescription.setLocationHints(new String[]{schemaHint});
                fSchemaGrammarDescription.setTargetNamespace(callerTNS);
                boolean alreadyTraversed = false;
                XMLInputSource schemaSource = resolveSchemaSource(fSchemaGrammarDescription, mustResolve, child, true);
                if (fNamespaceGrowth && refType == XSDDescription.CONTEXT_INCLUDE) {
                    try {
                        final String schemaId = XMLEntityManager.expandSystemId(schemaSource.getSystemId(), schemaSource.getBaseSystemId(), false);
                        alreadyTraversed = sg.getDocumentLocations().contains(schemaId);
                    }
                    catch(MalformedURIException e) {
                    }
                }
                if (!alreadyTraversed) {
                    newSchemaRoot = resolveSchema(schemaSource, fSchemaGrammarDescription, mustResolve, child);
                    schemaNamespace = currSchemaInfo.fTargetNamespace;
                }
                else {
                    fLastSchemaWasDuplicate = true;
                }
            }
            else {
                break;
            }
            XSDocumentInfo newSchemaInfo = null;
            if (fLastSchemaWasDuplicate) {
                newSchemaInfo = newSchemaRoot == null ? null : (XSDocumentInfo)fDoc2XSDocumentMap.get(newSchemaRoot);
            }
            else {
               	newSchemaInfo = constructTrees(newSchemaRoot, schemaHint, fSchemaGrammarDescription, importCollision);
            }
            if (localName.equals(SchemaSymbols.ELT_REDEFINE) &&
                    newSchemaInfo != null) {
                fRedefine2XSDMap.put(child, newSchemaInfo);
            }
            if (newSchemaRoot != null) {
                if (newSchemaInfo != null)
                    dependencies.addElement(newSchemaInfo);
                newSchemaRoot = null;
            }
        }
        fDependencyMap.put(currSchemaInfo, dependencies);
        return currSchemaInfo;
    } 
    private boolean isExistingGrammar(XSDDescription desc, boolean ignoreConflict) {
        SchemaGrammar sg = fGrammarBucket.getGrammar(desc.getTargetNamespace());
        if (sg == null) {
            return findGrammar(desc, ignoreConflict) != null;
        }
        else if (sg.isImmutable()) {
            return true;
        }
        else {
            try {
                return sg.getDocumentLocations().contains(XMLEntityManager.expandSystemId(desc.getLiteralSystemId(), desc.getBaseSystemId(), false));
            } 
            catch (MalformedURIException e) {
                return false;
            }
        }
    }
    private void updateImportListFor(SchemaGrammar grammar) {
        Vector importedGrammars = grammar.getImportedGrammars();
        if (importedGrammars != null) {
            for (int i=0; i<importedGrammars.size(); i++) {
                SchemaGrammar isg1 = (SchemaGrammar) importedGrammars.elementAt(i);
                SchemaGrammar isg2 = fGrammarBucket.getGrammar(isg1.getTargetNamespace());
                if (isg2 != null && isg1 != isg2) {
                    importedGrammars.set(i, isg2);
                }
            }
        }
    }
    private void updateImportListWith(SchemaGrammar newGrammar) {
        SchemaGrammar[] schemaGrammars = fGrammarBucket.getGrammars();
        for (int i = 0; i < schemaGrammars.length; ++i) {
            SchemaGrammar sg = schemaGrammars[i];
            if (sg != newGrammar) {
                Vector importedGrammars = sg.getImportedGrammars();
                if (importedGrammars != null) {
                    for (int j=0; j<importedGrammars.size(); j++) {
                        SchemaGrammar isg = (SchemaGrammar) importedGrammars.elementAt(j);
                        if (null2EmptyString(isg.getTargetNamespace()).equals(null2EmptyString(newGrammar.getTargetNamespace()))) {
                            if (isg != newGrammar) {
                                importedGrammars.set(j, newGrammar);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
    protected void buildGlobalNameRegistries() {
        Stack schemasToProcess = new Stack();
        schemasToProcess.push(fRoot);
        while (!schemasToProcess.empty()) {            
            XSDocumentInfo currSchemaDoc =
                (XSDocumentInfo)schemasToProcess.pop();
            Element currDoc = currSchemaDoc.fSchemaElement; 
            if(DOMUtil.isHidden(currDoc, fHiddenNodes)){
                continue;
            }
            Element currRoot = currDoc;
            boolean dependenciesCanOccur = true;
            for (Element globalComp =
                DOMUtil.getFirstChildElement(currRoot);
            globalComp != null;
            globalComp = DOMUtil.getNextSiblingElement(globalComp)) {
                if (DOMUtil.getLocalName(globalComp).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    continue;
                }
                else if (DOMUtil.getLocalName(globalComp).equals(SchemaSymbols.ELT_INCLUDE) ||
                        DOMUtil.getLocalName(globalComp).equals(SchemaSymbols.ELT_IMPORT)) {
                    if (!dependenciesCanOccur) {
                        reportSchemaError("s4s-elt-invalid-content.3", new Object [] {DOMUtil.getLocalName(globalComp)}, globalComp);
                    }
                    DOMUtil.setHidden(globalComp, fHiddenNodes);
                }
                else if (DOMUtil.getLocalName(globalComp).equals(SchemaSymbols.ELT_REDEFINE)) {
                    if (!dependenciesCanOccur) {
                        reportSchemaError("s4s-elt-invalid-content.3", new Object [] {DOMUtil.getLocalName(globalComp)}, globalComp);
                    }
                    for (Element redefineComp = DOMUtil.getFirstChildElement(globalComp);
                    redefineComp != null;
                    redefineComp = DOMUtil.getNextSiblingElement(redefineComp)) {
                        String lName = DOMUtil.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME);
                        if (lName.length() == 0) 
                            continue;
                        String qName = currSchemaDoc.fTargetNamespace == null ?
                                ","+lName:
                                    currSchemaDoc.fTargetNamespace +","+lName;
                        String componentType = DOMUtil.getLocalName(redefineComp);
                        if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                            checkForDuplicateNames(qName, ATTRIBUTEGROUP_TYPE, fUnparsedAttributeGroupRegistry, fUnparsedAttributeGroupRegistrySub, redefineComp, currSchemaDoc);
                            String targetLName = DOMUtil.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME)+REDEF_IDENTIFIER;
                            renameRedefiningComponents(currSchemaDoc, redefineComp, SchemaSymbols.ELT_ATTRIBUTEGROUP,
                                    lName, targetLName);
                        }
                        else if ((componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) ||
                                (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE))) {
                            checkForDuplicateNames(qName, TYPEDECL_TYPE, fUnparsedTypeRegistry, fUnparsedTypeRegistrySub, redefineComp, currSchemaDoc);
                            String targetLName = DOMUtil.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME) + REDEF_IDENTIFIER;
                            if (componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                                renameRedefiningComponents(currSchemaDoc, redefineComp, SchemaSymbols.ELT_COMPLEXTYPE,
                                        lName, targetLName);
                            }
                            else { 
                                renameRedefiningComponents(currSchemaDoc, redefineComp, SchemaSymbols.ELT_SIMPLETYPE,
                                        lName, targetLName);
                            }
                        }
                        else if (componentType.equals(SchemaSymbols.ELT_GROUP)) {
                            checkForDuplicateNames(qName, GROUP_TYPE, fUnparsedGroupRegistry, fUnparsedGroupRegistrySub, redefineComp, currSchemaDoc);
                            String targetLName = DOMUtil.getAttrValue(redefineComp, SchemaSymbols.ATT_NAME)+REDEF_IDENTIFIER;
                            renameRedefiningComponents(currSchemaDoc, redefineComp, SchemaSymbols.ELT_GROUP,
                                    lName, targetLName);
                        }
                    } 
                }
                else {
                    dependenciesCanOccur = false;
                    String lName = DOMUtil.getAttrValue(globalComp, SchemaSymbols.ATT_NAME);
                    if (lName.length() == 0) 
                        continue;
                    String qName = currSchemaDoc.fTargetNamespace == null?
                            ","+lName:
                                currSchemaDoc.fTargetNamespace +","+lName;
                    String componentType = DOMUtil.getLocalName(globalComp);
                    if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                        checkForDuplicateNames(qName, ATTRIBUTE_TYPE, fUnparsedAttributeRegistry, fUnparsedAttributeRegistrySub, globalComp, currSchemaDoc);
                    }
                    else if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                        checkForDuplicateNames(qName, ATTRIBUTEGROUP_TYPE, fUnparsedAttributeGroupRegistry, fUnparsedAttributeGroupRegistrySub, globalComp, currSchemaDoc);
                    }
                    else if ((componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) ||
                            (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE))) {
                        checkForDuplicateNames(qName, TYPEDECL_TYPE, fUnparsedTypeRegistry, fUnparsedTypeRegistrySub, globalComp, currSchemaDoc);
                    }
                    else if (componentType.equals(SchemaSymbols.ELT_ELEMENT)) {
                        checkForDuplicateNames(qName, ELEMENT_TYPE, fUnparsedElementRegistry, fUnparsedElementRegistrySub, globalComp, currSchemaDoc);
                    }
                    else if (componentType.equals(SchemaSymbols.ELT_GROUP)) {
                        checkForDuplicateNames(qName, GROUP_TYPE, fUnparsedGroupRegistry, fUnparsedGroupRegistrySub, globalComp, currSchemaDoc);
                    }
                    else if (componentType.equals(SchemaSymbols.ELT_NOTATION)) {
                        checkForDuplicateNames(qName, NOTATION_TYPE, fUnparsedNotationRegistry, fUnparsedNotationRegistrySub, globalComp, currSchemaDoc);
                    }
                }
            } 
           	DOMUtil.setHidden(currDoc, fHiddenNodes);
            Vector currSchemaDepends = (Vector)fDependencyMap.get(currSchemaDoc);
            for (int i = 0; i < currSchemaDepends.size(); i++) {
                schemasToProcess.push(currSchemaDepends.elementAt(i));
            }
        } 
    } 
    protected void traverseSchemas(ArrayList annotationInfo) {
        setSchemasVisible(fRoot);
        Stack schemasToProcess = new Stack();
        schemasToProcess.push(fRoot);
        while (!schemasToProcess.empty()) {
            XSDocumentInfo currSchemaDoc =
                (XSDocumentInfo)schemasToProcess.pop();
            Element currDoc = currSchemaDoc.fSchemaElement;
            SchemaGrammar currSG = fGrammarBucket.getGrammar(currSchemaDoc.fTargetNamespace);
            if(DOMUtil.isHidden(currDoc, fHiddenNodes)) {
                continue;
            }
            Element currRoot = currDoc;
            boolean sawAnnotation = false;
            for (Element globalComp =
                DOMUtil.getFirstVisibleChildElement(currRoot, fHiddenNodes);
            globalComp != null;
            globalComp = DOMUtil.getNextVisibleSiblingElement(globalComp, fHiddenNodes)) {
                DOMUtil.setHidden(globalComp, fHiddenNodes); 
                String componentType = DOMUtil.getLocalName(globalComp);
                if (DOMUtil.getLocalName(globalComp).equals(SchemaSymbols.ELT_REDEFINE)) {
                    currSchemaDoc.backupNSSupport((SchemaNamespaceSupport)fRedefine2NSSupport.get(globalComp));
                    for (Element redefinedComp = DOMUtil.getFirstVisibleChildElement(globalComp, fHiddenNodes);
                    redefinedComp != null;
                    redefinedComp = DOMUtil.getNextVisibleSiblingElement(redefinedComp, fHiddenNodes)) {
                        String redefinedComponentType = DOMUtil.getLocalName(redefinedComp);
                        DOMUtil.setHidden(redefinedComp, fHiddenNodes);
                        if (redefinedComponentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                            fAttributeGroupTraverser.traverseGlobal(redefinedComp, currSchemaDoc, currSG);
                        }
                        else if (redefinedComponentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                            fComplexTypeTraverser.traverseGlobal(redefinedComp, currSchemaDoc, currSG);
                        }
                        else if (redefinedComponentType.equals(SchemaSymbols.ELT_GROUP)) {
                            fGroupTraverser.traverseGlobal(redefinedComp, currSchemaDoc, currSG);
                        }
                        else if (redefinedComponentType.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                            fSimpleTypeTraverser.traverseGlobal(redefinedComp, currSchemaDoc, currSG);
                        }
                        else {
                            reportSchemaError("s4s-elt-must-match.1", new Object [] {DOMUtil.getLocalName(globalComp), "(annotation | (simpleType | complexType | group | attributeGroup))*", redefinedComponentType}, redefinedComp);
                        }
                    } 
                    currSchemaDoc.restoreNSSupport();
                }
                else if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                    fAttributeTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
                    fAttributeGroupTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                    fComplexTypeTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_ELEMENT)) {
                    fElementTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_GROUP)) {
                    fGroupTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_NOTATION)) {
                    fNotationTraverser.traverse(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
                    fSimpleTypeTraverser.traverseGlobal(globalComp, currSchemaDoc, currSG);
                }
                else if (componentType.equals(SchemaSymbols.ELT_ANNOTATION)) {
                    currSG.addAnnotation(fElementTraverser.traverseAnnotationDecl(globalComp, currSchemaDoc.getSchemaAttrs(), true, currSchemaDoc));
                    sawAnnotation = true;
                }
                else {
                    reportSchemaError("s4s-elt-invalid-content.1", new Object [] {SchemaSymbols.ELT_SCHEMA, DOMUtil.getLocalName(globalComp)}, globalComp);
                }
            } 
            if (!sawAnnotation) {
                String text = DOMUtil.getSyntheticAnnotation(currRoot);
                if (text != null) {
                    currSG.addAnnotation(fElementTraverser.traverseSyntheticAnnotation(currRoot, text, currSchemaDoc.getSchemaAttrs(), true, currSchemaDoc));
                }
            }
            if (annotationInfo != null) {
                XSAnnotationInfo info = currSchemaDoc.getAnnotations();
                if (info != null) {
                    annotationInfo.add(doc2SystemId(currDoc));
                    annotationInfo.add(info);
                }
            }
            currSchemaDoc.returnSchemaAttrs();
            DOMUtil.setHidden(currDoc, fHiddenNodes);
            Vector currSchemaDepends = (Vector)fDependencyMap.get(currSchemaDoc);
            for (int i = 0; i < currSchemaDepends.size(); i++) {
                schemasToProcess.push(currSchemaDepends.elementAt(i));
            }
        } 
    } 
    private Vector fReportedTNS = null;
    private final boolean needReportTNSError(String uri) {
        if (fReportedTNS == null)
            fReportedTNS = new Vector();
        else if (fReportedTNS.contains(uri))
            return false;
        fReportedTNS.addElement(uri);
        return true;
    }
    private static final String[] COMP_TYPE = {
            null,               
            "attribute declaration",
            "attribute group",
            "element declaration",
            "group",
            "identity constraint",
            "notation",
            "type definition",
    };
    private static final String[] CIRCULAR_CODES = {
            "Internal-Error",
            "Internal-Error",
            "src-attribute_group.3",
            "e-props-correct.6",
            "mg-props-correct.2",
            "Internal-Error",
            "Internal-Error",
            "st-props-correct.2",       
    };
    void addGlobalAttributeDecl(XSAttributeDecl decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalAttrDecls.get(declKey) == null) {
            fGlobalAttrDecls.put(declKey, decl);
        }
    }
    void addGlobalAttributeGroupDecl(XSAttributeGroupDecl decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalAttrGrpDecls.get(declKey) == null) {
            fGlobalAttrGrpDecls.put(declKey, decl);
        }
    }
    void addGlobalElementDecl(XSElementDecl decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalElemDecls.get(declKey) == null) {
            fGlobalElemDecls.put(declKey, decl);
        }
    }
    void addGlobalGroupDecl(XSGroupDecl decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalGroupDecls.get(declKey) == null) {
            fGlobalGroupDecls.put(declKey, decl);
        }
    }
    void addGlobalNotationDecl(XSNotationDecl decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalNotationDecls.get(declKey) == null) {
            fGlobalNotationDecls.put(declKey, decl);
        }
    }
    void addGlobalTypeDecl(XSTypeDefinition decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getName() : namespace + "," + decl.getName();
        if (fGlobalTypeDecls.get(declKey) == null) {
            fGlobalTypeDecls.put(declKey, decl);
        }
    }
    void addIDConstraintDecl(IdentityConstraint decl) {
        final String namespace = decl.getNamespace();
        final String declKey = (namespace == null || namespace.length() == 0)
            ? "," + decl.getIdentityConstraintName() : namespace + "," + decl.getIdentityConstraintName();
        if (fGlobalIDConstraintDecls.get(declKey) == null) {
            fGlobalIDConstraintDecls.put(declKey, decl);
        }
    }
    private XSAttributeDecl getGlobalAttributeDecl(String declKey) {
        return (XSAttributeDecl)fGlobalAttrDecls.get(declKey);
    }
    private XSAttributeGroupDecl getGlobalAttributeGroupDecl(String declKey) {
        return (XSAttributeGroupDecl)fGlobalAttrGrpDecls.get(declKey);
    }
    private XSElementDecl getGlobalElementDecl(String declKey) {
        return (XSElementDecl)fGlobalElemDecls.get(declKey);
    }
    private XSGroupDecl getGlobalGroupDecl(String declKey) {
        return (XSGroupDecl)fGlobalGroupDecls.get(declKey);
    }
    private XSNotationDecl getGlobalNotationDecl(String declKey) {
        return (XSNotationDecl)fGlobalNotationDecls.get(declKey);
    }
    private XSTypeDefinition getGlobalTypeDecl(String declKey) {
        return (XSTypeDefinition)fGlobalTypeDecls.get(declKey);
    }
    private IdentityConstraint getIDConstraintDecl(String declKey) {
        return (IdentityConstraint)fGlobalIDConstraintDecls.get(declKey);
    }
    protected Object getGlobalDecl(XSDocumentInfo currSchema,
            int declType,
            QName declToTraverse,
            Element elmNode) {
        if (DEBUG_NODE_POOL) {
            System.out.println("TRAVERSE_GL: "+declToTraverse.toString());
        }
        if (declToTraverse.uri != null &&
                declToTraverse.uri == SchemaSymbols.URI_SCHEMAFORSCHEMA) {
            if (declType == TYPEDECL_TYPE) {
                Object retObj = SchemaGrammar.SG_SchemaNS.getGlobalTypeDecl(declToTraverse.localpart);
                if (retObj != null)
                    return retObj;
            }
        }
        if (!currSchema.isAllowedNS(declToTraverse.uri)) {
            if (currSchema.needReportTNSError(declToTraverse.uri)) {
                String code = declToTraverse.uri == null ? "src-resolve.4.1" : "src-resolve.4.2";
                reportSchemaError(code, new Object[]{fDoc2SystemId.get(currSchema.fSchemaElement), declToTraverse.uri, declToTraverse.rawname}, elmNode);
            }
        }
        SchemaGrammar sGrammar = fGrammarBucket.getGrammar(declToTraverse.uri);
        if (sGrammar == null) {
            if (needReportTNSError(declToTraverse.uri))
                reportSchemaError("src-resolve", new Object[]{declToTraverse.rawname, COMP_TYPE[declType]}, elmNode);
            return null;
        }
        Object retObj = getGlobalDeclFromGrammar(sGrammar, declType, declToTraverse.localpart);
        String declKey = declToTraverse.uri == null? ","+declToTraverse.localpart:
            declToTraverse.uri+","+declToTraverse.localpart;
        if (!fTolerateDuplicates) {
            if (retObj != null) {
                return retObj;
            }
        }
        else {
            Object retObj2 = getGlobalDecl(declKey, declType);
            if (retObj2 != null) {
                return retObj2;
            }
        }
        XSDocumentInfo schemaWithDecl = null;
        Element decl = null;
        XSDocumentInfo declDoc = null;
        switch (declType) {
        case ATTRIBUTE_TYPE :
            decl = (Element)fUnparsedAttributeRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedAttributeRegistrySub.get(declKey);
            break;
        case ATTRIBUTEGROUP_TYPE :
            decl = (Element)fUnparsedAttributeGroupRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedAttributeGroupRegistrySub.get(declKey);
            break;
        case ELEMENT_TYPE :
            decl = (Element)fUnparsedElementRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedElementRegistrySub.get(declKey);
            break;
        case GROUP_TYPE :
            decl = (Element)fUnparsedGroupRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedGroupRegistrySub.get(declKey);
            break;
        case IDENTITYCONSTRAINT_TYPE :
            decl = (Element)fUnparsedIdentityConstraintRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedIdentityConstraintRegistrySub.get(declKey);
            break;
        case NOTATION_TYPE :
            decl = (Element)fUnparsedNotationRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedNotationRegistrySub.get(declKey);
            break;
        case TYPEDECL_TYPE :
            decl = (Element)fUnparsedTypeRegistry.get(declKey);
            declDoc = (XSDocumentInfo)fUnparsedTypeRegistrySub.get(declKey);
            break;
        default:
            reportSchemaError("Internal-Error", new Object [] {"XSDHandler asked to locate component of type " + declType + "; it does not recognize this type!"}, elmNode);
        }
        if (decl == null) {
            if (retObj == null) {
                reportSchemaError("src-resolve", new Object[]{declToTraverse.rawname, COMP_TYPE[declType]}, elmNode);
            }
            return retObj;
        }
        schemaWithDecl = findXSDocumentForDecl(currSchema, decl, declDoc);
        if (schemaWithDecl == null) {
            if (retObj == null) {
                String code = declToTraverse.uri == null ? "src-resolve.4.1" : "src-resolve.4.2";
                reportSchemaError(code, new Object[]{fDoc2SystemId.get(currSchema.fSchemaElement), declToTraverse.uri, declToTraverse.rawname}, elmNode);
            }
            return retObj;
        }
        if (DOMUtil.isHidden(decl, fHiddenNodes)) {
            if (retObj == null) {
                String code = CIRCULAR_CODES[declType];
                if (declType == TYPEDECL_TYPE) {
                    if (SchemaSymbols.ELT_COMPLEXTYPE.equals(DOMUtil.getLocalName(decl))) {
                        code = "ct-props-correct.3";
                    }
                }
                reportSchemaError(code, new Object [] {declToTraverse.prefix+":"+declToTraverse.localpart}, elmNode);
            }
            return retObj;
        }
        return traverseGlobalDecl(declType, decl, schemaWithDecl, sGrammar);
    } 
    protected Object getGlobalDecl(String declKey, int declType) {
        Object retObj = null;
        switch (declType) {
        case ATTRIBUTE_TYPE :
            retObj = getGlobalAttributeDecl(declKey);
            break;
        case ATTRIBUTEGROUP_TYPE :
            retObj = getGlobalAttributeGroupDecl(declKey);
            break;
        case ELEMENT_TYPE :
            retObj = getGlobalElementDecl(declKey);
            break;
        case GROUP_TYPE :
            retObj = getGlobalGroupDecl(declKey);
            break;
        case IDENTITYCONSTRAINT_TYPE :
            retObj = getIDConstraintDecl(declKey);
            break;
        case NOTATION_TYPE :
            retObj = getGlobalNotationDecl(declKey);
            break;
        case TYPEDECL_TYPE :
            retObj = getGlobalTypeDecl(declKey);
            break;
        }
        return retObj;
    }
    protected Object getGlobalDeclFromGrammar(SchemaGrammar sGrammar, int declType, String localpart) {
        Object retObj = null;
        switch (declType) {
        case ATTRIBUTE_TYPE :
            retObj = sGrammar.getGlobalAttributeDecl(localpart);
            break;
        case ATTRIBUTEGROUP_TYPE :
            retObj = sGrammar.getGlobalAttributeGroupDecl(localpart);
            break;
        case ELEMENT_TYPE :
            retObj = sGrammar.getGlobalElementDecl(localpart);
            break;
        case GROUP_TYPE :
            retObj = sGrammar.getGlobalGroupDecl(localpart);
            break;
        case IDENTITYCONSTRAINT_TYPE :
            retObj = sGrammar.getIDConstraintDecl(localpart);
            break;
        case NOTATION_TYPE :
            retObj = sGrammar.getGlobalNotationDecl(localpart);
            break;
        case TYPEDECL_TYPE :
            retObj = sGrammar.getGlobalTypeDecl(localpart);
            break;
        }
        return retObj;
    }
    protected Object getGlobalDeclFromGrammar(SchemaGrammar sGrammar, int declType, String localpart, String schemaLoc) {
        Object retObj = null;
        switch (declType) {
        case ATTRIBUTE_TYPE :
            retObj = sGrammar.getGlobalAttributeDecl(localpart, schemaLoc);
            break;
        case ATTRIBUTEGROUP_TYPE :
            retObj = sGrammar.getGlobalAttributeGroupDecl(localpart, schemaLoc);
            break;
        case ELEMENT_TYPE :
            retObj = sGrammar.getGlobalElementDecl(localpart, schemaLoc);
            break;
        case GROUP_TYPE :
            retObj = sGrammar.getGlobalGroupDecl(localpart, schemaLoc);
            break;
        case IDENTITYCONSTRAINT_TYPE :
            retObj = sGrammar.getIDConstraintDecl(localpart, schemaLoc);
            break;
        case NOTATION_TYPE :
            retObj = sGrammar.getGlobalNotationDecl(localpart, schemaLoc);
            break;
        case TYPEDECL_TYPE :
            retObj = sGrammar.getGlobalTypeDecl(localpart, schemaLoc);
            break;
        }
        return retObj;
    }
    protected Object traverseGlobalDecl(int declType, Element decl, XSDocumentInfo schemaDoc, SchemaGrammar grammar) {
        Object retObj = null;
        DOMUtil.setHidden(decl, fHiddenNodes);
        SchemaNamespaceSupport nsSupport = null;
        Element parent = DOMUtil.getParent(decl);
        if (DOMUtil.getLocalName(parent).equals(SchemaSymbols.ELT_REDEFINE))
            nsSupport = (SchemaNamespaceSupport)fRedefine2NSSupport.get(parent);
        schemaDoc.backupNSSupport(nsSupport);
        switch (declType) {
        case TYPEDECL_TYPE :
            if (DOMUtil.getLocalName(decl).equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
                retObj = fComplexTypeTraverser.traverseGlobal(decl, schemaDoc, grammar);
            }
            else {
                retObj = fSimpleTypeTraverser.traverseGlobal(decl, schemaDoc, grammar);
            }
            break;
        case ATTRIBUTE_TYPE :
            retObj = fAttributeTraverser.traverseGlobal(decl, schemaDoc, grammar);
            break;            
        case ELEMENT_TYPE :
            retObj = fElementTraverser.traverseGlobal(decl, schemaDoc, grammar);
            break;
        case ATTRIBUTEGROUP_TYPE :
            retObj = fAttributeGroupTraverser.traverseGlobal(decl, schemaDoc, grammar);
            break;
        case GROUP_TYPE :
            retObj = fGroupTraverser.traverseGlobal(decl, schemaDoc, grammar);
            break;
        case NOTATION_TYPE :
            retObj = fNotationTraverser.traverse(decl, schemaDoc, grammar);
            break;
        case IDENTITYCONSTRAINT_TYPE :
            break;            
        }
        schemaDoc.restoreNSSupport();
        return retObj;
    }
    public String schemaDocument2SystemId(XSDocumentInfo schemaDoc) {
        return (String)fDoc2SystemId.get(schemaDoc.fSchemaElement);
    }
    Object getGrpOrAttrGrpRedefinedByRestriction(int type, QName name, XSDocumentInfo currSchema, Element elmNode) {
        String realName = name.uri != null?name.uri+","+name.localpart:
            ","+name.localpart;
        String nameToFind = null;
        switch (type) {
        case ATTRIBUTEGROUP_TYPE:
            nameToFind = (String)fRedefinedRestrictedAttributeGroupRegistry.get(realName);
            break;
        case GROUP_TYPE:
            nameToFind = (String)fRedefinedRestrictedGroupRegistry.get(realName);
            break;
        default:
            return null;
        }
        if (nameToFind == null) return null;
        int commaPos = nameToFind.indexOf(",");
        QName qNameToFind = new QName(XMLSymbols.EMPTY_STRING, nameToFind.substring(commaPos+1),
                nameToFind.substring(commaPos), (commaPos == 0)? null : nameToFind.substring(0, commaPos));
        Object retObj = getGlobalDecl(currSchema, type, qNameToFind, elmNode);
        if(retObj == null) {
            switch (type) {
            case ATTRIBUTEGROUP_TYPE:
                reportSchemaError("src-redefine.7.2.1", new Object []{name.localpart}, elmNode);
                break;
            case GROUP_TYPE:
                reportSchemaError("src-redefine.6.2.1", new Object []{name.localpart}, elmNode);
                break;
            }
            return null;
        }
        return retObj;
    } 
    protected void resolveKeyRefs() {
        for (int i=0; i<fKeyrefStackPos; i++) {
            XSDocumentInfo keyrefSchemaDoc = fKeyrefsMapXSDocumentInfo[i];
            keyrefSchemaDoc.fNamespaceSupport.makeGlobal();
            keyrefSchemaDoc.fNamespaceSupport.setEffectiveContext( fKeyrefNamespaceContext[i] );
            SchemaGrammar keyrefGrammar = fGrammarBucket.getGrammar(keyrefSchemaDoc.fTargetNamespace);
           	DOMUtil.setHidden(fKeyrefs[i], fHiddenNodes);
            fKeyrefTraverser.traverse(fKeyrefs[i], fKeyrefElems[i], keyrefSchemaDoc, keyrefGrammar);
        }
    } 
    protected Hashtable getIDRegistry() {
        return fUnparsedIdentityConstraintRegistry;
    }
    protected Hashtable getIDRegistry_sub() {
        return fUnparsedIdentityConstraintRegistrySub;
    }
    protected void storeKeyRef (Element keyrefToStore, XSDocumentInfo schemaDoc,
            XSElementDecl currElemDecl) {
        String keyrefName = DOMUtil.getAttrValue(keyrefToStore, SchemaSymbols.ATT_NAME);
        if (keyrefName.length() != 0) {
            String keyrefQName = schemaDoc.fTargetNamespace == null?
                    "," + keyrefName: schemaDoc.fTargetNamespace+","+keyrefName;
            checkForDuplicateNames(keyrefQName, IDENTITYCONSTRAINT_TYPE, fUnparsedIdentityConstraintRegistry, fUnparsedIdentityConstraintRegistrySub, keyrefToStore, schemaDoc);
        }
        if (fKeyrefStackPos == fKeyrefs.length) {
            Element [] elemArray = new Element [fKeyrefStackPos + INC_KEYREF_STACK_AMOUNT];
            System.arraycopy(fKeyrefs, 0, elemArray, 0, fKeyrefStackPos);
            fKeyrefs = elemArray;
            XSElementDecl [] declArray = new XSElementDecl [fKeyrefStackPos + INC_KEYREF_STACK_AMOUNT];
            System.arraycopy(fKeyrefElems, 0, declArray, 0, fKeyrefStackPos);
            fKeyrefElems = declArray;
            String[][] stringArray = new String [fKeyrefStackPos + INC_KEYREF_STACK_AMOUNT][];
            System.arraycopy(fKeyrefNamespaceContext, 0, stringArray, 0, fKeyrefStackPos);
            fKeyrefNamespaceContext = stringArray;
            XSDocumentInfo [] xsDocumentInfo = new XSDocumentInfo [fKeyrefStackPos + INC_KEYREF_STACK_AMOUNT];
            System.arraycopy(fKeyrefsMapXSDocumentInfo, 0, xsDocumentInfo, 0, fKeyrefStackPos);
            fKeyrefsMapXSDocumentInfo = xsDocumentInfo;
        }
        fKeyrefs[fKeyrefStackPos] = keyrefToStore;
        fKeyrefElems[fKeyrefStackPos] = currElemDecl;
        fKeyrefNamespaceContext[fKeyrefStackPos] = schemaDoc.fNamespaceSupport.getEffectiveLocalContext();
        fKeyrefsMapXSDocumentInfo[fKeyrefStackPos++] = schemaDoc; 
    } 
    private Element resolveSchema(XSDDescription desc, boolean mustResolve,
                                  Element referElement, boolean usePairs) {
        XMLInputSource schemaSource = null;
        try {
            Hashtable pairs = usePairs ? fLocationPairs : EMPTY_TABLE;
            schemaSource = XMLSchemaLoader.resolveDocument(desc, pairs, fEntityResolver);
        }
        catch (IOException ex) {
            if (mustResolve) {
                reportSchemaError("schema_reference.4",
                        new Object[]{desc.getLocationHints()[0]},
                        referElement);
            }
            else {
                reportSchemaWarning("schema_reference.4",
                        new Object[]{desc.getLocationHints()[0]},
                        referElement);
            }
        }
        if (schemaSource instanceof DOMInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (DOMInputSource) schemaSource, mustResolve, desc.getContextType(), referElement);    
        } 
        else if (schemaSource instanceof SAXInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (SAXInputSource) schemaSource, mustResolve, desc.getContextType(), referElement);         
        } 
        else if (schemaSource instanceof StAXInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (StAXInputSource) schemaSource, mustResolve, desc.getContextType(), referElement); 
        } 
        else if (schemaSource instanceof XSInputSource) {
            return getSchemaDocument((XSInputSource) schemaSource, desc);
        } 
        return getSchemaDocument(desc.getTargetNamespace(), schemaSource, mustResolve, desc.getContextType(), referElement);
    } 
    private Element resolveSchema(XMLInputSource schemaSource, XSDDescription desc,
            boolean mustResolve, Element referElement) {
        if (schemaSource instanceof DOMInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (DOMInputSource) schemaSource, mustResolve, desc.getContextType(), referElement);    
        } 
        else if (schemaSource instanceof SAXInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (SAXInputSource) schemaSource, mustResolve, desc.getContextType(), referElement);         
        } 
        else if (schemaSource instanceof StAXInputSource) {
            return getSchemaDocument(desc.getTargetNamespace(), (StAXInputSource) schemaSource, mustResolve, desc.getContextType(), referElement); 
        } 
        else if (schemaSource instanceof XSInputSource) {
            return getSchemaDocument((XSInputSource) schemaSource, desc);
        } 
        return getSchemaDocument(desc.getTargetNamespace(), schemaSource, mustResolve, desc.getContextType(), referElement);
    }
    private XMLInputSource resolveSchemaSource(XSDDescription desc, boolean mustResolve,
            Element referElement, boolean usePairs) {
        XMLInputSource schemaSource = null;
        try {
            Hashtable pairs = usePairs ? fLocationPairs : EMPTY_TABLE;
            schemaSource = XMLSchemaLoader.resolveDocument(desc, pairs, fEntityResolver);
        }
        catch (IOException ex) {
            if (mustResolve) {
                reportSchemaError("schema_reference.4",
                        new Object[]{desc.getLocationHints()[0]},
                        referElement);
            }
            else {
                reportSchemaWarning("schema_reference.4",
                        new Object[]{desc.getLocationHints()[0]},
                        referElement);
            }
        }
        return schemaSource;
    }
    private Element getSchemaDocument(String schemaNamespace, XMLInputSource schemaSource,
            boolean mustResolve, short referType, Element referElement) {
        boolean hasInput = true;
        IOException exception = null;
        Element schemaElement = null;
        try {
            if (schemaSource != null &&
                    (schemaSource.getSystemId() != null ||
                            schemaSource.getByteStream() != null ||
                            schemaSource.getCharacterStream() != null)) {
                XSDKey key = null;
                String schemaId = null;
                if (referType != XSDDescription.CONTEXT_PREPARSE){
                    schemaId = XMLEntityManager.expandSystemId(schemaSource.getSystemId(), schemaSource.getBaseSystemId(), false);
                    key = new XSDKey(schemaId, referType, schemaNamespace);
                    if((schemaElement = (Element)fTraversed.get(key)) != null) {
                        fLastSchemaWasDuplicate = true;
                        return schemaElement;
                    }
                }
                fSchemaParser.parse(schemaSource);
                Document schemaDocument = fSchemaParser.getDocument();
                schemaElement = schemaDocument != null ? DOMUtil.getRoot(schemaDocument) : null;
                return getSchemaDocument0(key, schemaId, schemaElement);
            }
            else {
                hasInput = false;
            }
        }
        catch (IOException ex) {
            exception = ex;
        }
        return getSchemaDocument1(mustResolve, hasInput, schemaSource, referElement, exception);
    } 
    private Element getSchemaDocument(String schemaNamespace, SAXInputSource schemaSource,
            boolean mustResolve, short referType, Element referElement) {
        XMLReader parser = schemaSource.getXMLReader();
        InputSource inputSource = schemaSource.getInputSource();
        boolean hasInput = true;
        IOException exception = null;
        Element schemaElement = null;
        try {
            if (inputSource != null &&
                    (inputSource.getSystemId() != null ||
                     inputSource.getByteStream() != null ||
                     inputSource.getCharacterStream() != null)) {
                XSDKey key = null;
                String schemaId = null;
                if (referType != XSDDescription.CONTEXT_PREPARSE) {
                    schemaId = XMLEntityManager.expandSystemId(inputSource.getSystemId(), schemaSource.getBaseSystemId(), false);
                    key = new XSDKey(schemaId, referType, schemaNamespace);
                    if ((schemaElement = (Element) fTraversed.get(key)) != null) {
                        fLastSchemaWasDuplicate = true;
                        return schemaElement;
                    }
                }
                boolean namespacePrefixes = false;
                if (parser != null) {
                    try {
                        namespacePrefixes = parser.getFeature(NAMESPACE_PREFIXES);
                    }
                    catch (SAXException se) {}
                }
                else {
                    try {
                        parser = XMLReaderFactory.createXMLReader();
                    }
                    catch (SAXException se) {
                        parser = new SAXParser();
                    }
                    try {
                        parser.setFeature(NAMESPACE_PREFIXES, true);
                        namespacePrefixes = true;
                        if (parser instanceof SAXParser) {
                            Object securityManager = fSchemaParser.getProperty(SECURITY_MANAGER);
                            if (securityManager != null) {
                                parser.setProperty(SECURITY_MANAGER, securityManager);
                            }
                        }
                    }
                    catch (SAXException se) {}
                }
                boolean stringsInternalized = false;
                try {
                    stringsInternalized = parser.getFeature(STRING_INTERNING);
                }
                catch (SAXException exc) {
                }
                if (fXSContentHandler == null) {
                    fXSContentHandler = new SchemaContentHandler();
                }
                fXSContentHandler.reset(fSchemaParser, fSymbolTable, 
                        namespacePrefixes, stringsInternalized);
                parser.setContentHandler(fXSContentHandler);
                parser.setErrorHandler(fErrorReporter.getSAXErrorHandler());
                parser.parse(inputSource);
                try {
                    parser.setContentHandler(null);
                    parser.setErrorHandler(null);
                }
                catch (Exception e) {}
                Document schemaDocument = fXSContentHandler.getDocument();
                schemaElement = schemaDocument != null ? DOMUtil.getRoot(schemaDocument) : null;
                return getSchemaDocument0(key, schemaId, schemaElement);
            }
            else {
                hasInput = false;
            }
        }
        catch (SAXParseException spe) {
            throw SAX2XNIUtil.createXMLParseException0(spe);
        }
        catch (SAXException se) {
            throw SAX2XNIUtil.createXNIException0(se);
        }
        catch (IOException ioe) {
            exception = ioe;
        }
        return getSchemaDocument1(mustResolve, hasInput, schemaSource, referElement, exception);
    } 
    private Element getSchemaDocument(String schemaNamespace, DOMInputSource schemaSource,
            boolean mustResolve, short referType, Element referElement) {
        boolean hasInput = true;
        IOException exception = null;
        Element schemaElement = null;
        Element schemaRootElement = null;
        final Node node = schemaSource.getNode();
        short nodeType = -1;
        if (node != null) {
            nodeType = node.getNodeType();
            if (nodeType == Node.DOCUMENT_NODE) {
                schemaRootElement = DOMUtil.getRoot((Document) node);
            }
            else if (nodeType == Node.ELEMENT_NODE) {
                schemaRootElement = (Element) node;
            }
        }
        try {
            if (schemaRootElement != null) {
                XSDKey key = null;
                String schemaId = null;
                if (referType != XSDDescription.CONTEXT_PREPARSE) {
                    schemaId = XMLEntityManager.expandSystemId(schemaSource.getSystemId(), schemaSource.getBaseSystemId(), false);
                    boolean isDocument = (nodeType == Node.DOCUMENT_NODE);
                    if (!isDocument) {
                        Node parent = schemaRootElement.getParentNode();
                        if (parent != null) {
                            isDocument = (parent.getNodeType() == Node.DOCUMENT_NODE);
                        }
                    }
                    if (isDocument) {
                        key = new XSDKey(schemaId, referType, schemaNamespace);
                        if ((schemaElement = (Element) fTraversed.get(key)) != null) {
                            fLastSchemaWasDuplicate = true;
                            return schemaElement;
                        }
                    }
                }
                schemaElement = schemaRootElement;
                return getSchemaDocument0(key, schemaId, schemaElement);
            }
            else {
                hasInput = false;
            }
        }
        catch (IOException ioe) {
            exception = ioe;
        }
        return getSchemaDocument1(mustResolve, hasInput, schemaSource, referElement, exception);
    } 
    private Element getSchemaDocument(String schemaNamespace, StAXInputSource schemaSource,
            boolean mustResolve, short referType, Element referElement) {
        IOException exception = null;
        Element schemaElement = null;
        try {
            final boolean consumeRemainingContent = schemaSource.shouldConsumeRemainingContent();
            final XMLStreamReader streamReader = schemaSource.getXMLStreamReader();
            final XMLEventReader eventReader = schemaSource.getXMLEventReader();
            XSDKey key = null;
            String schemaId = null;
            if (referType != XSDDescription.CONTEXT_PREPARSE) {
                schemaId = XMLEntityManager.expandSystemId(schemaSource.getSystemId(), schemaSource.getBaseSystemId(), false);
                boolean isDocument = consumeRemainingContent;
                if (!isDocument) {
                    if (streamReader != null) {
                        isDocument = (streamReader.getEventType() == XMLStreamReader.START_DOCUMENT);
                    }
                    else {
                        isDocument = eventReader.peek().isStartDocument();
                    }
                }
                if (isDocument) {
                    key = new XSDKey(schemaId, referType, schemaNamespace);
                    if ((schemaElement = (Element) fTraversed.get(key)) != null) {
                        fLastSchemaWasDuplicate = true;
                        return schemaElement;
                    }
                }
            }
            if (fStAXSchemaParser == null) {
                fStAXSchemaParser = new StAXSchemaParser();
            }
            fStAXSchemaParser.reset(fSchemaParser, fSymbolTable);
            if (streamReader != null) {
                fStAXSchemaParser.parse(streamReader);
                if (consumeRemainingContent) {
                    while (streamReader.hasNext()) {
                        streamReader.next();
                    }
                }
            }
            else {
                fStAXSchemaParser.parse(eventReader);
                if (consumeRemainingContent) {
                    while (eventReader.hasNext()) {
                        eventReader.nextEvent();
                    }
                }
            }
            Document schemaDocument = fStAXSchemaParser.getDocument();
            schemaElement = schemaDocument != null ? DOMUtil.getRoot(schemaDocument) : null;
            return getSchemaDocument0(key, schemaId, schemaElement);
        }
        catch (XMLStreamException e) {
            Throwable t = e.getNestedException();
            if (t instanceof IOException) {
                exception = (IOException) t;
            }
            else {
                StAXLocationWrapper slw = new StAXLocationWrapper();
                slw.setLocation(e.getLocation());
                throw new XMLParseException(slw, e.getMessage(), e);
            }
        }
        catch (IOException e) {
            exception = e;
        }
        return getSchemaDocument1(mustResolve, true, schemaSource, referElement, exception);
    } 
    private Element getSchemaDocument0(XSDKey key, String schemaId, Element schemaElement) {
        if (key != null) {
            fTraversed.put(key, schemaElement);
        }
        if (schemaId != null) {
            fDoc2SystemId.put(schemaElement, schemaId);
        }
        fLastSchemaWasDuplicate = false;
        return schemaElement;
    } 
    private Element getSchemaDocument1(boolean mustResolve, boolean hasInput, 
            XMLInputSource schemaSource, Element referElement, IOException ioe) {
        if (mustResolve) {
            if (hasInput) {
                reportSchemaError("schema_reference.4",
                        new Object[]{schemaSource.getSystemId()},
                        referElement, ioe);
            }
            else {
                reportSchemaError("schema_reference.4",
                        new Object[]{schemaSource == null ? "" : schemaSource.getSystemId()},
                        referElement, ioe);
            }
        }
        else if (hasInput) {
            reportSchemaWarning("schema_reference.4",
                    new Object[]{schemaSource.getSystemId()},
                    referElement, ioe);
        }
        fLastSchemaWasDuplicate = false;
        return null;
    } 
    private Element getSchemaDocument(XSInputSource schemaSource, XSDDescription desc) {
        SchemaGrammar[] grammars = schemaSource.getGrammars();
        short referType = desc.getContextType();
        if (grammars != null && grammars.length > 0) {
            Vector expandedGrammars = expandGrammars(grammars);
            if (fNamespaceGrowth || !existingGrammars(expandedGrammars)) {
                addGrammars(expandedGrammars);
                if (referType == XSDDescription.CONTEXT_PREPARSE) {
                    desc.setTargetNamespace(grammars[0].getTargetNamespace());
                }
            }
        }
        else {
            XSObject[] components = schemaSource.getComponents();
            if (components != null && components.length > 0) {
                Hashtable importDependencies = new Hashtable();
                Vector expandedComponents = expandComponents(components, importDependencies);
                if (fNamespaceGrowth || canAddComponents(expandedComponents)) {
                    addGlobalComponents(expandedComponents, importDependencies);
                    if (referType == XSDDescription.CONTEXT_PREPARSE) {
                        desc.setTargetNamespace(components[0].getNamespace());
                    }
                }
            }
        }
        return null;
    } 
    private Vector expandGrammars(SchemaGrammar[] grammars) {
        Vector currGrammars = new Vector();
        for (int i=0; i<grammars.length; i++) {
            if (!currGrammars.contains(grammars[i])) {
                currGrammars.add(grammars[i]);
            }
        }
        SchemaGrammar sg1, sg2;
        Vector gs;
        for (int i = 0; i < currGrammars.size(); i++) {
            sg1 = (SchemaGrammar)currGrammars.elementAt(i);
            gs = sg1.getImportedGrammars();
            if (gs == null) {
                continue;
            }
            for (int j = gs.size() - 1; j >= 0; j--) {
                sg2 = (SchemaGrammar)gs.elementAt(j);
                if (!currGrammars.contains(sg2)) {
                    currGrammars.addElement(sg2);
                }
            }
        }
        return currGrammars;
    }
    private boolean existingGrammars(Vector grammars) {
        int length = grammars.size();
        final XSDDescription desc = new XSDDescription();
        for (int i=0; i < length; i++) {
            final SchemaGrammar sg1 = (SchemaGrammar)grammars.elementAt(i);
            desc.setNamespace(sg1.getTargetNamespace());
            final SchemaGrammar sg2 = findGrammar(desc, false);
            if (sg2 != null) {
                return true;
            }
        }
        return false;
    }
    private boolean canAddComponents(Vector components) {
        final int size = components.size();
        final XSDDescription desc = new XSDDescription(); 
        for (int i=0; i<size; i++) {
            XSObject component = (XSObject) components.elementAt(i);
            if (!canAddComponent(component, desc)) {
                return false;
            }
        }
        return true;
    }
    private boolean canAddComponent(XSObject component, XSDDescription desc) {
        desc.setNamespace(component.getNamespace());
        final SchemaGrammar sg = findGrammar(desc, false);
        if (sg == null) {
            return true;
        }
        else if (sg.isImmutable()) {
            return false;
        }
        short componentType = component.getType();
        final String name = component.getName();
        switch (componentType) {
        case XSConstants.TYPE_DEFINITION :
            if (sg.getGlobalTypeDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.ATTRIBUTE_DECLARATION :
            if (sg.getGlobalAttributeDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.ATTRIBUTE_GROUP :
            if (sg.getGlobalAttributeDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.ELEMENT_DECLARATION :
            if (sg.getGlobalElementDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.MODEL_GROUP_DEFINITION :
            if (sg.getGlobalGroupDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.NOTATION_DECLARATION :
            if (sg.getGlobalNotationDecl(name) == component) {
                return true;
            }
            break;
        case XSConstants.IDENTITY_CONSTRAINT :
        case XSConstants.ATTRIBUTE_USE :
        default :
            return true;
        }
        return false;
    }
    private void addGrammars(Vector grammars) {
        int length = grammars.size();
        XSDDescription desc = new XSDDescription();
        for (int i=0; i < length; i++) {
            final SchemaGrammar sg1 = (SchemaGrammar)grammars.elementAt(i);
            desc.setNamespace(sg1.getTargetNamespace());
            final SchemaGrammar sg2 = findGrammar(desc, fNamespaceGrowth);
            if (sg1 != sg2) {
                addGrammarComponents(sg1, sg2);
            }
        }
    }
    private void addGrammarComponents(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        if (dstGrammar == null) {
            createGrammarFrom(srcGrammar);
            return;
        }
        SchemaGrammar tmpGrammar = dstGrammar;
        if (tmpGrammar.isImmutable()) {
            tmpGrammar = createGrammarFrom(dstGrammar);
        }
        addNewGrammarLocations(srcGrammar, tmpGrammar);
        addNewImportedGrammars(srcGrammar, tmpGrammar);
        addNewGrammarComponents(srcGrammar, tmpGrammar);
    }
    private SchemaGrammar createGrammarFrom(SchemaGrammar grammar) {
        SchemaGrammar newGrammar = new SchemaGrammar(grammar);
        fGrammarBucket.putGrammar(newGrammar);
        updateImportListWith(newGrammar);
        updateImportListFor(newGrammar);
        return newGrammar;
    }
    private void addNewGrammarLocations(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        final StringList locations = srcGrammar.getDocumentLocations();
        final int locSize = locations.size();
        final StringList locations2 = dstGrammar.getDocumentLocations();
        for (int i=0; i<locSize; i++) {
            String loc = locations.item(i);
            if (!locations2.contains(loc)) {
                dstGrammar.addDocument(null, loc);
            }
        }
    }
    private void addNewImportedGrammars(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        final Vector igs1 = srcGrammar.getImportedGrammars();
        if (igs1 != null) {
            Vector igs2 = dstGrammar.getImportedGrammars();
            if (igs2 == null) {
                igs2 = ((Vector) igs1.clone());
                dstGrammar.setImportedGrammars(igs2);
            }
            else {
                updateImportList(igs1, igs2);
            }
        }
    }
    private void updateImportList(Vector importedSrc, Vector importedDst)
    {
        final int size = importedSrc.size();
        for (int i=0; i<size; i++) {
            final SchemaGrammar sg = (SchemaGrammar) importedSrc.elementAt(i);
            if (!containedImportedGrammar(importedDst, sg)) {
                importedDst.add(sg);
            }
        }
    }
    private void addNewGrammarComponents(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        dstGrammar.resetComponents();
        addGlobalElementDecls(srcGrammar, dstGrammar);
        addGlobalAttributeDecls(srcGrammar, dstGrammar);
        addGlobalAttributeGroupDecls(srcGrammar, dstGrammar);
        addGlobalGroupDecls(srcGrammar, dstGrammar);
        addGlobalTypeDecls(srcGrammar, dstGrammar);
        addGlobalNotationDecls(srcGrammar, dstGrammar);
    }
    private void addGlobalElementDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.ELEMENT_DECLARATION);
        int len = components.getLength();
        XSElementDecl srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSElementDecl) components.item(i);
            dstDecl = dstGrammar.getGlobalElementDecl(srcDecl.getName()); 
            if (dstDecl == null) {
                dstGrammar.addGlobalElementDecl(srcDecl);
            }
            else if (dstDecl != srcDecl){
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.ELEMENT_DECLARATION);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSElementDecl)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalElementDecl(name, location); 
            if ( dstDecl == null) {
                dstGrammar.addGlobalElementDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl){
            }
        }
    }
    private void addGlobalAttributeDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.ATTRIBUTE_DECLARATION);
        int len = components.getLength();
        XSAttributeDecl srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSAttributeDecl) components.item(i);
            dstDecl = dstGrammar.getGlobalAttributeDecl(srcDecl.getName()); 
            if (dstDecl == null) {
                dstGrammar.addGlobalAttributeDecl(srcDecl);
            }
            else if (dstDecl != srcDecl && !fTolerateDuplicates) {
                reportSharingError(srcDecl.getNamespace(), srcDecl.getName());
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.ATTRIBUTE_DECLARATION);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSAttributeDecl)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalAttributeDecl(name, location); 
            if (dstDecl == null) {
                dstGrammar.addGlobalAttributeDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl) {
            }
        }
    }
    private void addGlobalAttributeGroupDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.ATTRIBUTE_GROUP);
        int len = components.getLength();
        XSAttributeGroupDecl srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSAttributeGroupDecl) components.item(i);
            dstDecl = dstGrammar.getGlobalAttributeGroupDecl(srcDecl.getName());
            if (dstDecl == null) {
                dstGrammar.addGlobalAttributeGroupDecl(srcDecl);
            }
            else if (dstDecl != srcDecl && !fTolerateDuplicates) {
                reportSharingError(srcDecl.getNamespace(), srcDecl.getName());
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.ATTRIBUTE_GROUP);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSAttributeGroupDecl)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalAttributeGroupDecl(name, location);
            if (dstDecl == null) {
                dstGrammar.addGlobalAttributeGroupDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl) {
            }
        }
    }
    private void addGlobalNotationDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.NOTATION_DECLARATION);
        int len = components.getLength();
        XSNotationDecl srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSNotationDecl) components.item(i);
            dstDecl = dstGrammar.getGlobalNotationDecl(srcDecl.getName());
            if (dstDecl == null) {
                dstGrammar.addGlobalNotationDecl(srcDecl);
            }
            else if (dstDecl != srcDecl && !fTolerateDuplicates) {
                reportSharingError(srcDecl.getNamespace(), srcDecl.getName());
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.NOTATION_DECLARATION);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSNotationDecl)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalNotationDecl(name, location);
            if (dstDecl == null) {
                dstGrammar.addGlobalNotationDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl) {
            }
        }
    }
    private void addGlobalGroupDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.MODEL_GROUP_DEFINITION);
        int len = components.getLength();
        XSGroupDecl srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSGroupDecl) components.item(i);
            dstDecl = dstGrammar.getGlobalGroupDecl(srcDecl.getName());
            if (dstDecl == null) {
                dstGrammar.addGlobalGroupDecl(srcDecl);
            }
            else if (srcDecl != dstDecl && !fTolerateDuplicates) {
                reportSharingError(srcDecl.getNamespace(), srcDecl.getName());
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.MODEL_GROUP_DEFINITION);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSGroupDecl)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalGroupDecl(name, location);
            if (dstDecl == null) {
                dstGrammar.addGlobalGroupDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl) {
            }
        }
    }
    private void addGlobalTypeDecls(SchemaGrammar srcGrammar, SchemaGrammar dstGrammar) {
        XSNamedMap components = srcGrammar.getComponents(XSConstants.TYPE_DEFINITION);
        int len = components.getLength();
        XSTypeDefinition srcDecl, dstDecl;
        for (int i=0; i<len; i++) {
            srcDecl = (XSTypeDefinition) components.item(i);
            dstDecl = dstGrammar.getGlobalTypeDecl(srcDecl.getName());
            if (dstDecl == null) {
                dstGrammar.addGlobalTypeDecl(srcDecl);
            }
            else if (dstDecl != srcDecl && !fTolerateDuplicates) {
                reportSharingError(srcDecl.getNamespace(), srcDecl.getName());
            }
        }
        ObjectList componentsExt = srcGrammar.getComponentsExt(XSConstants.TYPE_DEFINITION);
        len = componentsExt.getLength();
        for (int i=0; i<len; i+= 2) {
            final String key = (String) componentsExt.item(i);
            final int index = key.indexOf(',');
            final String location = key.substring(0, index);
            final String name = key.substring(index + 1, key.length());
            srcDecl = (XSTypeDefinition)componentsExt.item(i+1);
            dstDecl = dstGrammar.getGlobalTypeDecl(name, location);
            if (dstDecl == null) {
                dstGrammar.addGlobalTypeDecl(srcDecl, location);
            }
            else if (dstDecl != srcDecl) {
            }
        }
    }
    private Vector expandComponents(XSObject[] components, Hashtable dependencies) {
        Vector newComponents = new Vector();
        for (int i=0; i<components.length; i++) {
            if (!newComponents.contains(components[i])) {
                newComponents.add(components[i]);
            }
        }
        for (int i=0; i<newComponents.size(); i++) {
            final XSObject component = (XSObject) newComponents.elementAt(i);
            expandRelatedComponents(component, newComponents, dependencies);
        }
        return newComponents;
    }
    private void expandRelatedComponents(XSObject component, Vector componentList, Hashtable dependencies) {
        short componentType = component.getType();
        switch (componentType) {
        case XSConstants.TYPE_DEFINITION :
            expandRelatedTypeComponents((XSTypeDefinition) component, componentList, component.getNamespace(), dependencies);
            break;
        case XSConstants.ATTRIBUTE_DECLARATION :
            expandRelatedAttributeComponents((XSAttributeDeclaration) component, componentList, component.getNamespace(), dependencies);
            break;
        case XSConstants.ATTRIBUTE_GROUP :
            expandRelatedAttributeGroupComponents((XSAttributeGroupDefinition) component, componentList, component.getNamespace(), dependencies);
        case XSConstants.ELEMENT_DECLARATION :
            expandRelatedElementComponents((XSElementDeclaration) component, componentList, component.getNamespace(), dependencies);
            break;
        case XSConstants.MODEL_GROUP_DEFINITION :
            expandRelatedModelGroupDefinitionComponents((XSModelGroupDefinition) component, componentList, component.getNamespace(), dependencies);
        case XSConstants.ATTRIBUTE_USE :
        case XSConstants.NOTATION_DECLARATION :
        case XSConstants.IDENTITY_CONSTRAINT :
        default :
            break;
        }
    }
    private void expandRelatedAttributeComponents(XSAttributeDeclaration decl, Vector componentList, String namespace, Hashtable dependencies) {
        addRelatedType(decl.getTypeDefinition(), componentList, namespace, dependencies);
    }
    private void expandRelatedElementComponents(XSElementDeclaration decl, Vector componentList, String namespace, Hashtable dependencies) {
        addRelatedType(decl.getTypeDefinition(), componentList, namespace, dependencies);
        final XSElementDeclaration subElemDecl = decl.getSubstitutionGroupAffiliation();
        if (subElemDecl != null) {
            addRelatedElement(subElemDecl, componentList, namespace, dependencies);
        }
    }
    private void expandRelatedTypeComponents(XSTypeDefinition type, Vector componentList, String namespace, Hashtable dependencies) {
        if (type instanceof XSComplexTypeDecl) {
            expandRelatedComplexTypeComponents((XSComplexTypeDecl) type, componentList, namespace, dependencies);
        }
        else if (type instanceof XSSimpleTypeDecl) {
            expandRelatedSimpleTypeComponents((XSSimpleTypeDefinition) type, componentList, namespace, dependencies);
        }
    }
    private void expandRelatedModelGroupDefinitionComponents(XSModelGroupDefinition modelGroupDef, Vector componentList,
            String namespace, Hashtable dependencies) {
        expandRelatedModelGroupComponents(modelGroupDef.getModelGroup(), componentList, namespace, dependencies);
    }
    private void expandRelatedAttributeGroupComponents(XSAttributeGroupDefinition attrGroup, Vector componentList
            , String namespace, Hashtable dependencies) {
        expandRelatedAttributeUsesComponents(attrGroup.getAttributeUses(), componentList, namespace, dependencies);
    }
    private void expandRelatedComplexTypeComponents(XSComplexTypeDecl type, Vector componentList, String namespace, Hashtable dependencies) {
        addRelatedType(type.getBaseType(), componentList, namespace, dependencies);
        expandRelatedAttributeUsesComponents(type.getAttributeUses(), componentList, namespace, dependencies);
        final XSParticle particle = type.getParticle();
        if (particle != null) {
            expandRelatedParticleComponents(particle, componentList, namespace, dependencies);
        }
    }
    private void expandRelatedSimpleTypeComponents(XSSimpleTypeDefinition type, Vector componentList, String namespace, Hashtable dependencies) {
        final XSTypeDefinition baseType = type.getBaseType();
        if (baseType != null) {
            addRelatedType(baseType, componentList, namespace, dependencies);
        }
        final XSTypeDefinition itemType = type.getItemType();
        if (itemType != null) {
            addRelatedType(itemType, componentList, namespace, dependencies);
        }
        final XSTypeDefinition primitiveType = type.getPrimitiveType();
        if (primitiveType != null) {
            addRelatedType(primitiveType, componentList, namespace, dependencies);
        }
        final XSObjectList memberTypes = type.getMemberTypes();
        if (memberTypes.size() > 0) {
            for (int i=0; i<memberTypes.size(); i++) {
                addRelatedType((XSTypeDefinition)memberTypes.item(i), componentList, namespace, dependencies);
            }
        }
    }
    private void expandRelatedAttributeUsesComponents(XSObjectList attrUses, Vector componentList,
            String namespace, Hashtable dependencies) {
        final int attrUseSize = (attrUses == null) ? 0 : attrUses.size();
        for (int i=0; i<attrUseSize; i++) {
            expandRelatedAttributeUseComponents((XSAttributeUse)attrUses.item(i), componentList, namespace, dependencies);
        }
    }
    private void expandRelatedAttributeUseComponents(XSAttributeUse component, Vector componentList,
            String namespace, Hashtable dependencies) {
        addRelatedAttribute(component.getAttrDeclaration(), componentList, namespace, dependencies);
    }
    private void expandRelatedParticleComponents(XSParticle component, Vector componentList,
            String namespace, Hashtable dependencies) {
        XSTerm term = component.getTerm();
        switch (term.getType()) {
        case XSConstants.ELEMENT_DECLARATION :
            addRelatedElement((XSElementDeclaration) term, componentList, namespace, dependencies);
            break;
        case XSConstants.MODEL_GROUP :
            expandRelatedModelGroupComponents((XSModelGroup) term, componentList, namespace, dependencies);
            break;
        default:
            break;
        }
    }
    private void expandRelatedModelGroupComponents(XSModelGroup modelGroup, Vector componentList,
            String namespace, Hashtable dependencies) {
        XSObjectList particles = modelGroup.getParticles();
        final int length = (particles == null) ? 0 : particles.getLength();
        for (int i=0; i<length; i++) {
            expandRelatedParticleComponents((XSParticle)particles.item(i), componentList, namespace, dependencies);
        }
    }
    private void addRelatedType(XSTypeDefinition type, Vector componentList, String namespace, Hashtable dependencies) {
        if (!type.getAnonymous()) {
            if (!type.getNamespace().equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) { 
                if (!componentList.contains(type)) {
                    final Vector importedNamespaces = findDependentNamespaces(namespace, dependencies);
                    addNamespaceDependency(namespace, type.getNamespace(), importedNamespaces);
                    componentList.add(type);
                }
            }
        }
        else {
            expandRelatedTypeComponents(type, componentList, namespace, dependencies);
        }
    }
    private void addRelatedElement(XSElementDeclaration decl, Vector componentList, String namespace, Hashtable dependencies) {
        if (decl.getScope() == XSConstants.SCOPE_GLOBAL) {
            if (!componentList.contains(decl)) {
                Vector importedNamespaces = findDependentNamespaces(namespace, dependencies);
                addNamespaceDependency(namespace, decl.getNamespace(), importedNamespaces);
                componentList.add(decl);
            }
        }
        else {
            expandRelatedElementComponents(decl, componentList, namespace, dependencies);
        }
    }
    private void addRelatedAttribute(XSAttributeDeclaration decl, Vector componentList, String namespace, Hashtable dependencies) {
        if (decl.getScope() == XSConstants.SCOPE_GLOBAL) {
            if (!componentList.contains(decl)) {
                Vector importedNamespaces = findDependentNamespaces(namespace, dependencies);
                addNamespaceDependency(namespace, decl.getNamespace(), importedNamespaces);
                componentList.add(decl);
            }
        }
        else {
            expandRelatedAttributeComponents(decl, componentList, namespace, dependencies);
        }
    }
    private void addGlobalComponents(Vector components, Hashtable importDependencies) {
        final XSDDescription desc = new XSDDescription();
        final int size = components.size();
        for (int i=0; i<size; i++) {
            addGlobalComponent((XSObject) components.elementAt(i), desc);
        }
        updateImportDependencies(importDependencies);
    }
    private void addGlobalComponent(XSObject component, XSDDescription desc) {
        final String namespace = component.getNamespace();
        desc.setNamespace(namespace);
        final SchemaGrammar sg = getSchemaGrammar(desc);
        short componentType = component.getType();
        final String name = component.getName();
        switch (componentType) {
        case XSConstants.TYPE_DEFINITION :
            if (!((XSTypeDefinition) component).getAnonymous()) {
                if (sg.getGlobalTypeDecl(name) == null) {
                    sg.addGlobalTypeDecl((XSTypeDefinition) component);
                }
                if (sg.getGlobalTypeDecl(name, "") == null) {
                    sg.addGlobalTypeDecl((XSTypeDefinition) component, "");
                }
            }
            break;
        case XSConstants.ATTRIBUTE_DECLARATION :
            if (((XSAttributeDecl) component).getScope() == XSAttributeDecl.SCOPE_GLOBAL) {
                if (sg.getGlobalAttributeDecl(name) == null) {
                    sg.addGlobalAttributeDecl((XSAttributeDecl) component);
                }
                if (sg.getGlobalAttributeDecl(name, "") == null) {
                    sg.addGlobalAttributeDecl((XSAttributeDecl) component, "");
                }
            }
            break;
        case XSConstants.ATTRIBUTE_GROUP :
            if (sg.getGlobalAttributeDecl(name) == null) {
                sg.addGlobalAttributeGroupDecl((XSAttributeGroupDecl) component);
            }
            if (sg.getGlobalAttributeDecl(name, "") == null) {
                sg.addGlobalAttributeGroupDecl((XSAttributeGroupDecl) component, "");
            }
            break;
        case XSConstants.ELEMENT_DECLARATION :
            if (((XSElementDecl) component).getScope() == XSElementDecl.SCOPE_GLOBAL) {
                sg.addGlobalElementDeclAll((XSElementDecl) component);
                if (sg.getGlobalElementDecl(name) == null) {
                    sg.addGlobalElementDecl((XSElementDecl) component);
                }
                if (sg.getGlobalElementDecl(name, "") == null) {
                    sg.addGlobalElementDecl((XSElementDecl) component, "");
                }
            }
            break;
        case XSConstants.MODEL_GROUP_DEFINITION :
            if (sg.getGlobalGroupDecl(name) == null) {
                sg.addGlobalGroupDecl((XSGroupDecl) component);
            }
            if (sg.getGlobalGroupDecl(name, "") == null) {
                sg.addGlobalGroupDecl((XSGroupDecl) component, "");
            }
            break;
        case XSConstants.NOTATION_DECLARATION :
            if (sg.getGlobalNotationDecl(name) == null) {
                sg.addGlobalNotationDecl((XSNotationDecl) component);
            }
            if (sg.getGlobalNotationDecl(name, "") == null) {
                sg.addGlobalNotationDecl((XSNotationDecl) component, "");
            }
            break;
        case XSConstants.IDENTITY_CONSTRAINT :
        case XSConstants.ATTRIBUTE_USE :
        default :
            break;
        }
    }
    private void updateImportDependencies(Hashtable table) {
        Enumeration keys = table.keys();
        String namespace;
        Vector importList;
        while (keys.hasMoreElements()) {
            namespace = (String) keys.nextElement();
            importList = (Vector) table.get(null2EmptyString(namespace));
            if (importList.size() > 0) {
                expandImportList(namespace, importList);
            }
        }
    }
    private void expandImportList(String namespace, Vector namespaceList) {
        SchemaGrammar sg = fGrammarBucket.getGrammar(namespace);
        if (sg != null) {
            Vector isgs = sg.getImportedGrammars();
            if (isgs == null) {
                isgs = new Vector();
                addImportList(sg, isgs, namespaceList);
                sg.setImportedGrammars(isgs);
            }
            else {
                updateImportList(sg, isgs, namespaceList);
            }
        }
    }
    private void addImportList(SchemaGrammar sg, Vector importedGrammars, Vector namespaceList) {
        final int size = namespaceList.size();
        SchemaGrammar isg;
        for (int i=0; i<size; i++) {
            isg = fGrammarBucket.getGrammar((String)namespaceList.elementAt(i));
            if (isg != null) {
                importedGrammars.add(isg);
            }
            else {
            }
        }
    }
    private void updateImportList(SchemaGrammar sg, Vector importedGrammars, Vector namespaceList) {
        final int size = namespaceList.size();
        SchemaGrammar isg;
        for (int i=0; i<size; i++) {
            isg = fGrammarBucket.getGrammar((String)namespaceList.elementAt(i));
            if (isg != null) {
                if (!containedImportedGrammar(importedGrammars, isg)) {
                    importedGrammars.add(isg);
                }
            }
            else {
            }
        }
    }
    private boolean containedImportedGrammar(Vector importedGrammar, SchemaGrammar grammar) {
        final int size = importedGrammar.size();
        SchemaGrammar sg;
        for (int i=0; i<size; i++) {
            sg = (SchemaGrammar) importedGrammar.elementAt(i);
            if (null2EmptyString(sg.getTargetNamespace()).equals(null2EmptyString(grammar.getTargetNamespace()))) {
                return true;
            }
        }
        return false;
    }
    private SchemaGrammar getSchemaGrammar(XSDDescription desc) {
        SchemaGrammar sg = findGrammar(desc, fNamespaceGrowth);
        if (sg == null) {
            sg = new SchemaGrammar(desc.getNamespace(), desc.makeClone(), fSymbolTable);
            fGrammarBucket.putGrammar(sg);
        }
        else if (sg.isImmutable()){
            sg = createGrammarFrom(sg);
        }
        return sg;
    }
    private Vector findDependentNamespaces(String namespace, Hashtable table) {
        final String ns = null2EmptyString(namespace);
        Vector namespaceList = (Vector) table.get(ns);
        if (namespaceList == null) {
            namespaceList = new Vector();
            table.put(ns, namespaceList);
        }
        return namespaceList;
    }
    private void addNamespaceDependency(String namespace1, String namespace2, Vector list) {
        final String ns1 = null2EmptyString(namespace1);
        final String ns2 = null2EmptyString(namespace2);
        if (!ns1.equals(ns2)) {
            if (!list.contains(ns2)) {
                list.add(ns2);
            }
        }
    }
    private void reportSharingError(String namespace, String name) {
        final String qName = (namespace == null)
            ? "," + name : namespace + "," + name;
        reportSchemaError("sch-props-correct.2", new Object [] {qName}, null);
    }
    private void createTraversers() {
        fAttributeChecker = new XSAttributeChecker(this);
        fAttributeGroupTraverser = new XSDAttributeGroupTraverser(this, fAttributeChecker);
        fAttributeTraverser = new XSDAttributeTraverser(this, fAttributeChecker);
        fComplexTypeTraverser = new XSDComplexTypeTraverser(this, fAttributeChecker);
        fElementTraverser = new XSDElementTraverser(this, fAttributeChecker);
        fGroupTraverser = new XSDGroupTraverser(this, fAttributeChecker);
        fKeyrefTraverser = new XSDKeyrefTraverser(this, fAttributeChecker);
        fNotationTraverser = new XSDNotationTraverser(this, fAttributeChecker);
        fSimpleTypeTraverser = new XSDSimpleTypeTraverser(this, fAttributeChecker);
        fUniqueOrKeyTraverser = new XSDUniqueOrKeyTraverser(this, fAttributeChecker);
        fWildCardTraverser = new XSDWildcardTraverser(this, fAttributeChecker);
    } 
    void prepareForParse() {
        fTraversed.clear();
        fDoc2SystemId.clear();
        fHiddenNodes.clear();
        fLastSchemaWasDuplicate = false;
    }
    void prepareForTraverse() {
        fUnparsedAttributeRegistry.clear();
        fUnparsedAttributeGroupRegistry.clear();
        fUnparsedElementRegistry.clear();
        fUnparsedGroupRegistry.clear();
        fUnparsedIdentityConstraintRegistry.clear();
        fUnparsedNotationRegistry.clear();
        fUnparsedTypeRegistry.clear();
        fUnparsedAttributeRegistrySub.clear();
        fUnparsedAttributeGroupRegistrySub.clear();
        fUnparsedElementRegistrySub.clear();
        fUnparsedGroupRegistrySub.clear();
        fUnparsedIdentityConstraintRegistrySub.clear();
        fUnparsedNotationRegistrySub.clear();
        fUnparsedTypeRegistrySub.clear();
        for (int i=1; i<= TYPEDECL_TYPE; i++) {
            fUnparsedRegistriesExt[i].clear();
        }
        fXSDocumentInfoRegistry.clear();
        fDependencyMap.clear();
        fDoc2XSDocumentMap.clear();
        fRedefine2XSDMap.clear();
        fRedefine2NSSupport.clear();
        fAllTNSs.removeAllElements();
        fImportMap.clear();
        fRoot = null;
        for (int i = 0; i < fLocalElemStackPos; i++) {
            fParticle[i] = null;
            fLocalElementDecl[i] = null;
            fLocalElementDecl_schema[i] = null;
            fLocalElemNamespaceContext[i] = null;
        }
        fLocalElemStackPos = 0;
        for (int i = 0; i < fKeyrefStackPos; i++) {
            fKeyrefs[i] = null;
            fKeyrefElems[i] = null;
            fKeyrefNamespaceContext[i] = null;
            fKeyrefsMapXSDocumentInfo[i] = null;
        }
        fKeyrefStackPos = 0;
        if (fAttributeChecker == null) {
            createTraversers();
        }
        Locale locale = fErrorReporter.getLocale();
        fAttributeChecker.reset(fSymbolTable);
        fAttributeGroupTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fAttributeTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fComplexTypeTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fElementTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fGroupTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fKeyrefTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fNotationTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fSimpleTypeTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fUniqueOrKeyTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fWildCardTraverser.reset(fSymbolTable, fValidateAnnotations, locale);
        fRedefinedRestrictedAttributeGroupRegistry.clear();
        fRedefinedRestrictedGroupRegistry.clear();
        fGlobalAttrDecls.clear();
        fGlobalAttrGrpDecls.clear();
        fGlobalElemDecls.clear();
        fGlobalGroupDecls.clear();
        fGlobalNotationDecls.clear();
        fGlobalIDConstraintDecls.clear();
        fGlobalTypeDecls.clear();
    }
    public void setDeclPool (XSDeclarationPool declPool){
        fDeclPool = declPool;
    }
    public void setDVFactory(SchemaDVFactory dvFactory){
        fDVFactory = dvFactory;
    }
    public void reset(XMLComponentManager componentManager) {
        fSymbolTable = (SymbolTable) componentManager.getProperty(SYMBOL_TABLE);
        fEntityResolver = (XMLEntityResolver) componentManager.getProperty(ENTITY_MANAGER);
        XMLEntityResolver er = (XMLEntityResolver)componentManager.getProperty(ENTITY_RESOLVER);
        if (er != null)
            fSchemaParser.setEntityResolver(er);
        fErrorReporter =
            (XMLErrorReporter) componentManager.getProperty(ERROR_REPORTER);
        try {
            XMLErrorHandler currErrorHandler = fErrorReporter.getErrorHandler();
            if (currErrorHandler != fSchemaParser.getProperty(ERROR_HANDLER)) {
                fSchemaParser.setProperty(ERROR_HANDLER, (currErrorHandler != null) ? currErrorHandler : new DefaultErrorHandler());
                if (fAnnotationValidator != null) {
                    fAnnotationValidator.setProperty(ERROR_HANDLER, (currErrorHandler != null) ? currErrorHandler : new DefaultErrorHandler());
            	}
            }
            Locale currentLocale = fErrorReporter.getLocale();
            if (currentLocale != fSchemaParser.getProperty(LOCALE)) {
                fSchemaParser.setProperty(LOCALE, currentLocale);
                if (fAnnotationValidator != null) {
                    fAnnotationValidator.setProperty(LOCALE, currentLocale);
                }
            }
        } 
        catch (XMLConfigurationException e) {}
        try {
            fValidateAnnotations = componentManager.getFeature(VALIDATE_ANNOTATIONS);
        } catch (XMLConfigurationException e) {
            fValidateAnnotations = false;
        }
        try {
            fHonourAllSchemaLocations = componentManager.getFeature(HONOUR_ALL_SCHEMALOCATIONS);
        } catch (XMLConfigurationException e) {
            fHonourAllSchemaLocations = false;
        }
        try {
            fNamespaceGrowth = componentManager.getFeature(NAMESPACE_GROWTH);
        } catch (XMLConfigurationException e) {
            fNamespaceGrowth = false;
        }
        try {
            fTolerateDuplicates = componentManager.getFeature(TOLERATE_DUPLICATES);
        } catch (XMLConfigurationException e) {
            fTolerateDuplicates = false;
        }
        try {
            fSchemaParser.setFeature(
                    CONTINUE_AFTER_FATAL_ERROR,
                    fErrorReporter.getFeature(CONTINUE_AFTER_FATAL_ERROR));
        } catch (XMLConfigurationException e) {
        }
        try {
            fSchemaParser.setFeature(
                    ALLOW_JAVA_ENCODINGS,
                    componentManager.getFeature(ALLOW_JAVA_ENCODINGS));
        } catch (XMLConfigurationException e) {
        }
        try {
            fSchemaParser.setFeature(
                    STANDARD_URI_CONFORMANT_FEATURE,
                    componentManager.getFeature(STANDARD_URI_CONFORMANT_FEATURE));
        } catch (XMLConfigurationException e) {
        }
        try {
            fGrammarPool =
                (XMLGrammarPool) componentManager.getProperty(XMLGRAMMAR_POOL);
        } catch (XMLConfigurationException e) {
            fGrammarPool = null;
        }
        try {
            fSchemaParser.setFeature( DISALLOW_DOCTYPE,
                    componentManager.getFeature(DISALLOW_DOCTYPE));
        } catch (XMLConfigurationException e) {
        }
        try {
            Object security = componentManager.getProperty(SECURITY_MANAGER);
            if (security != null){
                fSchemaParser.setProperty(SECURITY_MANAGER, security);
            }
        } catch (XMLConfigurationException e) {
        }
    } 
    void traverseLocalElements() {
        fElementTraverser.fDeferTraversingLocalElements = false;
        for (int i = 0; i < fLocalElemStackPos; i++) {
            Element currElem = fLocalElementDecl[i];
            XSDocumentInfo currSchema = fLocalElementDecl_schema[i];
            SchemaGrammar currGrammar = fGrammarBucket.getGrammar(currSchema.fTargetNamespace);
            fElementTraverser.traverseLocal (fParticle[i], currElem, currSchema, currGrammar, fAllContext[i], fParent[i], fLocalElemNamespaceContext[i]);
            if (fParticle[i].fType == XSParticleDecl.PARTICLE_EMPTY) {
                XSModelGroupImpl group = null;
                if (fParent[i] instanceof XSComplexTypeDecl) {
                    XSParticle p = ((XSComplexTypeDecl)fParent[i]).getParticle();
                    if (p != null)
                        group = (XSModelGroupImpl)p.getTerm();
                }
                else {
                    group = ((XSGroupDecl)fParent[i]).fModelGroup;
                }
                if (group != null)
                    removeParticle(group, fParticle[i]);
            }
        }
    }
    private boolean removeParticle(XSModelGroupImpl group, XSParticleDecl particle) {
        XSParticleDecl member;
        for (int i = 0; i < group.fParticleCount; i++) {
            member = group.fParticles[i];
            if (member == particle) {
                for (int j = i; j < group.fParticleCount-1; j++)
                    group.fParticles[j] = group.fParticles[j+1];
                group.fParticleCount--;
                return true;
            }
            if (member.fType == XSParticleDecl.PARTICLE_MODELGROUP) {
                if (removeParticle((XSModelGroupImpl)member.fValue, particle))
                    return true;
            }
        }
        return false;
    }
    void fillInLocalElemInfo(Element elmDecl,
            XSDocumentInfo schemaDoc,
            int allContextFlags,
            XSObject parent,
            XSParticleDecl particle) {
        if (fParticle.length == fLocalElemStackPos) {
            XSParticleDecl[] newStackP = new XSParticleDecl[fLocalElemStackPos+INC_STACK_SIZE];
            System.arraycopy(fParticle, 0, newStackP, 0, fLocalElemStackPos);
            fParticle = newStackP;
            Element[] newStackE = new Element[fLocalElemStackPos+INC_STACK_SIZE];
            System.arraycopy(fLocalElementDecl, 0, newStackE, 0, fLocalElemStackPos);
            fLocalElementDecl = newStackE;
            XSDocumentInfo [] newStackE_schema = new XSDocumentInfo[fLocalElemStackPos+INC_STACK_SIZE];
            System.arraycopy(fLocalElementDecl_schema, 0, newStackE_schema, 0, fLocalElemStackPos);
            fLocalElementDecl_schema = newStackE_schema;
            int[] newStackI = new int[fLocalElemStackPos+INC_STACK_SIZE];
            System.arraycopy(fAllContext, 0, newStackI, 0, fLocalElemStackPos);
            fAllContext = newStackI;
            XSObject[] newStackC = new XSObject[fLocalElemStackPos+INC_STACK_SIZE];
            System.arraycopy(fParent, 0, newStackC, 0, fLocalElemStackPos);
            fParent = newStackC;
            String [][] newStackN = new String [fLocalElemStackPos+INC_STACK_SIZE][];
            System.arraycopy(fLocalElemNamespaceContext, 0, newStackN, 0, fLocalElemStackPos);
            fLocalElemNamespaceContext = newStackN;
        }
        fParticle[fLocalElemStackPos] = particle;
        fLocalElementDecl[fLocalElemStackPos] = elmDecl;
        fLocalElementDecl_schema[fLocalElemStackPos] = schemaDoc;
        fAllContext[fLocalElemStackPos] = allContextFlags;
        fParent[fLocalElemStackPos] = parent;
        fLocalElemNamespaceContext[fLocalElemStackPos++] = schemaDoc.fNamespaceSupport.getEffectiveLocalContext();
    } 
    void checkForDuplicateNames(String qName, int declType,
            Hashtable registry, Hashtable registry_sub, Element currComp,
            XSDocumentInfo currSchema) {
        Object objElem = null;
        if ((objElem = registry.get(qName)) == null) {
            if (fNamespaceGrowth && !fTolerateDuplicates) {
                checkForDuplicateNames(qName, declType, currComp);
            }
            registry.put(qName, currComp);
            registry_sub.put(qName, currSchema);
        }
        else {
            Element collidingElem = (Element)objElem;
            XSDocumentInfo collidingElemSchema = (XSDocumentInfo)registry_sub.get(qName);
            if (collidingElem == currComp) return;
            Element elemParent = null;
            XSDocumentInfo redefinedSchema = null;
            boolean collidedWithRedefine = true;
            if ((DOMUtil.getLocalName((elemParent = DOMUtil.getParent(collidingElem))).equals(SchemaSymbols.ELT_REDEFINE))) {
                redefinedSchema = (XSDocumentInfo)(fRedefine2XSDMap.get(elemParent));
            }
            else if ((DOMUtil.getLocalName(DOMUtil.getParent(currComp)).equals(SchemaSymbols.ELT_REDEFINE))) {
                redefinedSchema = collidingElemSchema;
                collidedWithRedefine = false;
            }
            if (redefinedSchema != null) { 
                if(collidingElemSchema == currSchema){
                    reportSchemaError("sch-props-correct.2", new Object[]{qName}, currComp);
                    return;
                }
                String newName = qName.substring(qName.lastIndexOf(',')+1)+REDEF_IDENTIFIER;
                if (redefinedSchema == currSchema) { 
                    currComp.setAttribute(SchemaSymbols.ATT_NAME, newName);
                    if (currSchema.fTargetNamespace == null){
                        registry.put(","+newName, currComp);
                        registry_sub.put(","+newName, currSchema);
                    }
                    else{
                        registry.put(currSchema.fTargetNamespace+","+newName, currComp);
                        registry_sub.put(currSchema.fTargetNamespace+","+newName, currSchema);
                    }
                    if (currSchema.fTargetNamespace == null)
                        checkForDuplicateNames(","+newName, declType, registry, registry_sub, currComp, currSchema);
                    else
                        checkForDuplicateNames(currSchema.fTargetNamespace+","+newName, declType, registry, registry_sub, currComp, currSchema);
                }
                else { 
                    if (collidedWithRedefine) {
                        if (currSchema.fTargetNamespace == null)
                            checkForDuplicateNames(","+newName, declType, registry, registry_sub, currComp, currSchema);
                        else
                            checkForDuplicateNames(currSchema.fTargetNamespace+","+newName, declType, registry, registry_sub, currComp, currSchema);
                    }
                    else {
                        reportSchemaError("sch-props-correct.2", new Object [] {qName}, currComp);
                    }
                }
            }
            else {
                if (!fTolerateDuplicates || fUnparsedRegistriesExt[declType].get(qName) == currSchema) {
                    reportSchemaError("sch-props-correct.2", new Object []{qName}, currComp);
                }
            }
        }
        if (fTolerateDuplicates) {
            fUnparsedRegistriesExt[declType].put(qName, currSchema);
        }
    } 
    void checkForDuplicateNames(String qName, int declType, Element currComp) {
        int namespaceEnd = qName.indexOf(',');
        String namespace = qName.substring(0, namespaceEnd);
        SchemaGrammar grammar = fGrammarBucket.getGrammar(emptyString2Null(namespace));
        if (grammar != null) {
            Object obj = getGlobalDeclFromGrammar(grammar, declType, qName.substring(namespaceEnd + 1));
            if (obj != null) {
                reportSchemaError("sch-props-correct.2", new Object []{qName}, currComp);
            }
        }
    }
    private void renameRedefiningComponents(XSDocumentInfo currSchema,
            Element child, String componentType,
            String oldName, String newName) {
        if (componentType.equals(SchemaSymbols.ELT_SIMPLETYPE)) {
            Element grandKid = DOMUtil.getFirstChildElement(child);
            if (grandKid == null) {
                reportSchemaError("src-redefine.5.a.a", null, child);
            }
            else {
                String grandKidName = DOMUtil.getLocalName(grandKid);
                if (grandKidName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                    grandKid = DOMUtil.getNextSiblingElement(grandKid);
                }
                if (grandKid == null) {
                    reportSchemaError("src-redefine.5.a.a", null, child);
                }
                else {
                    grandKidName = DOMUtil.getLocalName(grandKid);
                    if (!grandKidName.equals(SchemaSymbols.ELT_RESTRICTION)) {
                        reportSchemaError("src-redefine.5.a.b", new Object[]{grandKidName}, child);
                    }
                    else {
                        Object[] attrs = fAttributeChecker.checkAttributes(grandKid, false, currSchema);
                        QName derivedBase = (QName)attrs[XSAttributeChecker.ATTIDX_BASE];
                        if (derivedBase == null ||
                                derivedBase.uri != currSchema.fTargetNamespace ||
                                !derivedBase.localpart.equals(oldName)) {
                            reportSchemaError("src-redefine.5.a.c",
                                    new Object[]{grandKidName,
                                    (currSchema.fTargetNamespace==null?"":currSchema.fTargetNamespace)
                                    + "," + oldName},
                                    child);
                        }
                        else {
                            if (derivedBase.prefix != null && derivedBase.prefix.length() > 0)
                                grandKid.setAttribute( SchemaSymbols.ATT_BASE,
                                        derivedBase.prefix + ":" + newName );
                            else
                                grandKid.setAttribute( SchemaSymbols.ATT_BASE, newName );
                        }
                        fAttributeChecker.returnAttrArray(attrs, currSchema);
                    }
                }
            }
        }
        else if (componentType.equals(SchemaSymbols.ELT_COMPLEXTYPE)) {
            Element grandKid = DOMUtil.getFirstChildElement(child);
            if (grandKid == null) {
                reportSchemaError("src-redefine.5.b.a", null, child);
            }
            else {
                if (DOMUtil.getLocalName(grandKid).equals(SchemaSymbols.ELT_ANNOTATION)) {
                    grandKid = DOMUtil.getNextSiblingElement(grandKid);
                }
                if (grandKid == null) {
                    reportSchemaError("src-redefine.5.b.a", null, child);
                }
                else {
                    Element greatGrandKid = DOMUtil.getFirstChildElement(grandKid);
                    if (greatGrandKid == null) {
                        reportSchemaError("src-redefine.5.b.b", null, grandKid);
                    }
                    else {
                        String greatGrandKidName = DOMUtil.getLocalName(greatGrandKid);
                        if (greatGrandKidName.equals(SchemaSymbols.ELT_ANNOTATION)) {
                            greatGrandKid = DOMUtil.getNextSiblingElement(greatGrandKid);
                        }
                        if (greatGrandKid == null) {
                            reportSchemaError("src-redefine.5.b.b", null, grandKid);
                        }
                        else {
                            greatGrandKidName = DOMUtil.getLocalName(greatGrandKid);
                            if (!greatGrandKidName.equals(SchemaSymbols.ELT_RESTRICTION) &&
                                    !greatGrandKidName.equals(SchemaSymbols.ELT_EXTENSION)) {
                                reportSchemaError("src-redefine.5.b.c", new Object[]{greatGrandKidName}, greatGrandKid);
                            }
                            else {
                                Object[] attrs = fAttributeChecker.checkAttributes(greatGrandKid, false, currSchema);
                                QName derivedBase = (QName)attrs[XSAttributeChecker.ATTIDX_BASE];
                                if (derivedBase == null ||
                                        derivedBase.uri != currSchema.fTargetNamespace ||
                                        !derivedBase.localpart.equals(oldName)) {
                                    reportSchemaError("src-redefine.5.b.d",
                                            new Object[]{greatGrandKidName,
                                            (currSchema.fTargetNamespace==null?"":currSchema.fTargetNamespace)
                                            + "," + oldName},
                                            greatGrandKid);
                                }
                                else {
                                    if (derivedBase.prefix != null && derivedBase.prefix.length() > 0)
                                        greatGrandKid.setAttribute( SchemaSymbols.ATT_BASE,
                                                derivedBase.prefix + ":" + newName );
                                    else
                                        greatGrandKid.setAttribute( SchemaSymbols.ATT_BASE,
                                                newName );
                                }
                            }
                        }
                    }
                }
            }
        }
        else if (componentType.equals(SchemaSymbols.ELT_ATTRIBUTEGROUP)) {
            String processedBaseName = (currSchema.fTargetNamespace == null)?
                    ","+oldName:currSchema.fTargetNamespace+","+oldName;
            int attGroupRefsCount = changeRedefineGroup(processedBaseName, componentType, newName, child, currSchema);
            if (attGroupRefsCount > 1) {
                reportSchemaError("src-redefine.7.1", new Object []{new Integer(attGroupRefsCount)}, child);
            }
            else if (attGroupRefsCount == 1) {
            }
            else
                if (currSchema.fTargetNamespace == null)
                    fRedefinedRestrictedAttributeGroupRegistry.put(processedBaseName, ","+newName);
                else
                    fRedefinedRestrictedAttributeGroupRegistry.put(processedBaseName, currSchema.fTargetNamespace+","+newName);
        }
        else if (componentType.equals(SchemaSymbols.ELT_GROUP)) {
            String processedBaseName = (currSchema.fTargetNamespace == null)?
                    ","+oldName:currSchema.fTargetNamespace+","+oldName;
            int groupRefsCount = changeRedefineGroup(processedBaseName, componentType, newName, child, currSchema);
            if (groupRefsCount > 1) {
                reportSchemaError("src-redefine.6.1.1", new Object []{new Integer(groupRefsCount)}, child);
            }
            else if (groupRefsCount == 1) {
            }
            else {
                if (currSchema.fTargetNamespace == null)
                    fRedefinedRestrictedGroupRegistry.put(processedBaseName, ","+newName);
                else
                    fRedefinedRestrictedGroupRegistry.put(processedBaseName, currSchema.fTargetNamespace+","+newName);
            }
        }
        else {
            reportSchemaError("Internal-Error", new Object [] {"could not handle this particular <redefine>; please submit your schemas and instance document in a bug report!"}, child);
        }
    } 
    private String findQName(String name, XSDocumentInfo schemaDoc) {
        SchemaNamespaceSupport currNSMap = schemaDoc.fNamespaceSupport;
        int colonPtr = name.indexOf(':');
        String prefix = XMLSymbols.EMPTY_STRING;
        if (colonPtr > 0)
            prefix = name.substring(0, colonPtr);
        String uri = currNSMap.getURI(fSymbolTable.addSymbol(prefix));
        String localpart = (colonPtr == 0)?name:name.substring(colonPtr+1);
        if (prefix == XMLSymbols.EMPTY_STRING && uri == null && schemaDoc.fIsChameleonSchema)
            uri = schemaDoc.fTargetNamespace;
        if (uri == null)
            return ","+localpart;
        return uri+","+localpart;
    } 
    private int changeRedefineGroup(String originalQName, String elementSought,
            String newName, Element curr, XSDocumentInfo schemaDoc) {
        int result = 0;
        for (Element child = DOMUtil.getFirstChildElement(curr);
        child != null; child = DOMUtil.getNextSiblingElement(child)) {
            String name = DOMUtil.getLocalName(child);
            if (!name.equals(elementSought))
                result += changeRedefineGroup(originalQName, elementSought, newName, child, schemaDoc);
            else {
                String ref = child.getAttribute( SchemaSymbols.ATT_REF );
                if (ref.length() != 0) {
                    String processedRef = findQName(ref, schemaDoc);
                    if (originalQName.equals(processedRef)) {
                        String prefix = XMLSymbols.EMPTY_STRING;
                        int colonptr = ref.indexOf(":");
                        if (colonptr > 0) {
                            prefix = ref.substring(0,colonptr);
                            child.setAttribute(SchemaSymbols.ATT_REF, prefix + ":" + newName);
                        }
                        else
                            child.setAttribute(SchemaSymbols.ATT_REF, newName);
                        result++;
                        if (elementSought.equals(SchemaSymbols.ELT_GROUP)) {
                            String minOccurs = child.getAttribute( SchemaSymbols.ATT_MINOCCURS );
                            String maxOccurs = child.getAttribute( SchemaSymbols.ATT_MAXOCCURS );
                            if (!((maxOccurs.length() == 0 || maxOccurs.equals("1"))
                                    && (minOccurs.length() == 0 || minOccurs.equals("1")))) {
                                reportSchemaError("src-redefine.6.1.2", new Object [] {ref}, child);
                            }
                        }
                    }
                } 
            }
        }
        return result;
    } 
    private XSDocumentInfo findXSDocumentForDecl(XSDocumentInfo currSchema,
            Element decl, XSDocumentInfo decl_Doc) {
        if (DEBUG_NODE_POOL) {
            System.out.println("DOCUMENT NS:"+ currSchema.fTargetNamespace+" hashcode:"+ ((Object)currSchema.fSchemaElement).hashCode());
        }
        Object temp = decl_Doc;
        if (temp == null) {
            return null;
        }
        XSDocumentInfo declDocInfo = (XSDocumentInfo)temp;
        return declDocInfo;
    } 
    private boolean nonAnnotationContent(Element elem) {
        for(Element child = DOMUtil.getFirstChildElement(elem); child != null; child = DOMUtil.getNextSiblingElement(child)) {
            if(!(DOMUtil.getLocalName(child).equals(SchemaSymbols.ELT_ANNOTATION))) return true;
        }
        return false;
    } 
    private void setSchemasVisible(XSDocumentInfo startSchema) {
        if (DOMUtil.isHidden(startSchema.fSchemaElement, fHiddenNodes)) {
            DOMUtil.setVisible(startSchema.fSchemaElement, fHiddenNodes);           
            Vector dependingSchemas = (Vector)fDependencyMap.get(startSchema);
            for (int i = 0; i < dependingSchemas.size(); i++) {
                setSchemasVisible((XSDocumentInfo)dependingSchemas.elementAt(i));
            }
        }
    } 
    private SimpleLocator xl = new SimpleLocator();
    public SimpleLocator element2Locator(Element e) {
        if (!( e instanceof ElementImpl))
            return null;
        SimpleLocator l = new SimpleLocator();
        return element2Locator(e, l) ? l : null;
    }
    public boolean element2Locator(Element e, SimpleLocator l) {
        if (l == null)
            return false;
        if (e instanceof ElementImpl) {
            ElementImpl ele = (ElementImpl)e;
            Document doc = ele.getOwnerDocument();
            String sid = (String)fDoc2SystemId.get(DOMUtil.getRoot(doc));
            int line = ele.getLineNumber();
            int column = ele.getColumnNumber();
            l.setValues(sid, sid, line, column, ele.getCharacterOffset());
            return true;
        }
        return false;
    }
    void reportSchemaError(String key, Object[] args, Element ele) {
        reportSchemaError(key, args, ele, null);
    }
    void reportSchemaError(String key, Object[] args, Element ele, Exception exception) {
        if (element2Locator(ele, xl)) {
            fErrorReporter.reportError(xl, XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_ERROR, exception);
        }
        else {
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_ERROR, exception);
        }
    }
    void reportSchemaWarning(String key, Object[] args, Element ele) {
        reportSchemaWarning(key, args, ele, null);
    }
    void reportSchemaWarning(String key, Object[] args, Element ele, Exception exception) {
        if (element2Locator(ele, xl)) {
            fErrorReporter.reportError(xl, XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_WARNING, exception);
        }
        else {
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                    key, args, XMLErrorReporter.SEVERITY_WARNING, exception);
        }
    }
    private static class XSAnnotationGrammarPool implements XMLGrammarPool {
        private XSGrammarBucket fGrammarBucket;
        private Grammar [] fInitialGrammarSet;
        public Grammar[] retrieveInitialGrammarSet(String grammarType) {
            if (grammarType == XMLGrammarDescription.XML_SCHEMA) {
                if (fInitialGrammarSet == null) {
                    if (fGrammarBucket == null) {
                        fInitialGrammarSet = new Grammar [] {SchemaGrammar.Schema4Annotations.INSTANCE};
                    }
                    else {
                        SchemaGrammar [] schemaGrammars = fGrammarBucket.getGrammars();
                        for (int i = 0; i < schemaGrammars.length; ++i) {
                            if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(schemaGrammars[i].getTargetNamespace())) {
                                fInitialGrammarSet = schemaGrammars;
                                return fInitialGrammarSet;
                            }
                        }
                        Grammar [] grammars = new Grammar[schemaGrammars.length + 1];
                        System.arraycopy(schemaGrammars, 0, grammars, 0, schemaGrammars.length);
                        grammars[grammars.length - 1] = SchemaGrammar.Schema4Annotations.INSTANCE;
                        fInitialGrammarSet = grammars;
                    }
                }
                return fInitialGrammarSet;
            }
            return new Grammar[0];
        }
        public void cacheGrammars(String grammarType, Grammar[] grammars) {
        }
        public Grammar retrieveGrammar(XMLGrammarDescription desc) {
            if (desc.getGrammarType() == XMLGrammarDescription.XML_SCHEMA) {
                final String tns = ((XMLSchemaDescription) desc).getTargetNamespace();
                if (fGrammarBucket != null) {
                    Grammar grammar = fGrammarBucket.getGrammar(tns);
                    if (grammar != null) {
                        return grammar;
                    }
                }
                if (SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(tns)) {
                    return SchemaGrammar.Schema4Annotations.INSTANCE;
                }
            }
            return null;
        }
        public void refreshGrammars(XSGrammarBucket gBucket) {
            fGrammarBucket = gBucket;
            fInitialGrammarSet = null;
        }
        public void lockPool() {}
        public void unlockPool() {}
        public void clear() {}
    }
    private static class XSDKey {
        String systemId;
        short  referType;
        String referNS;
        XSDKey(String systemId, short referType, String referNS) {
            this.systemId = systemId;
            this.referType = referType;
            this.referNS = referNS;
        }
        public int hashCode() {
            return referNS == null ? 0 : referNS.hashCode();
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof XSDKey)) {
                return false;
            }
            XSDKey key = (XSDKey)obj;
            if (referNS != key.referNS)
                return false;
            if (systemId == null || !systemId.equals(key.systemId)) {
                return false;
            }
            return true;
        }
    }
    private static final class SAX2XNIUtil extends ErrorHandlerWrapper {
        public static XMLParseException createXMLParseException0(SAXParseException exception) {
            return createXMLParseException(exception);
        }
        public static XNIException createXNIException0(SAXException exception) {
            return createXNIException(exception);
        }
    }
    public void setGenerateSyntheticAnnotations(boolean state) {
        fSchemaParser.setFeature(GENERATE_SYNTHETIC_ANNOTATIONS, state);
    }
} 
