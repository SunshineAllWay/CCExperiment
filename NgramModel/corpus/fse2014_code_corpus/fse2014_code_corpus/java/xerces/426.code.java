package org.apache.xerces.impl.xs;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import org.apache.xerces.dom.DOMErrorImpl;
import org.apache.xerces.dom.DOMMessageFormatter;
import org.apache.xerces.dom.DOMStringListImpl;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dv.InvalidDatatypeValueException;
import org.apache.xerces.impl.dv.SchemaDVFactory;
import org.apache.xerces.impl.dv.xs.SchemaDVFactoryImpl;
import org.apache.xerces.impl.xs.models.CMBuilder;
import org.apache.xerces.impl.xs.models.CMNodeFactory;
import org.apache.xerces.impl.xs.traversers.XSDHandler;
import org.apache.xerces.util.DOMEntityResolverWrapper;
import org.apache.xerces.util.DOMErrorHandlerWrapper;
import org.apache.xerces.util.DefaultErrorHandler;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.util.URI.MalformedURIException;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.grammars.XSGrammar;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xs.LSInputList;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMStringList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
public class XMLSchemaLoader implements XMLGrammarLoader, XMLComponent, XSElementDeclHelper,
XSLoader, DOMConfiguration {
    protected static final String SCHEMA_FULL_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
    protected static final String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS =
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String STANDARD_URI_CONFORMANT_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.STANDARD_URI_CONFORMANT_FEATURE;
    protected static final String VALIDATE_ANNOTATIONS =
        Constants.XERCES_FEATURE_PREFIX + Constants.VALIDATE_ANNOTATIONS_FEATURE;
    protected static final String DISALLOW_DOCTYPE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE;
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;
    protected static final String AUGMENT_PSVI = 
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;
    protected static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    protected static final String NAMESPACE_GROWTH = 
        Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_GROWTH_FEATURE;
    protected static final String TOLERATE_DUPLICATES = 
        Constants.XERCES_FEATURE_PREFIX + Constants.TOLERATE_DUPLICATES_FEATURE;
    protected static final String SCHEMA_DV_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_DV_FACTORY_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        SCHEMA_FULL_CHECKING,
        AUGMENT_PSVI,
        CONTINUE_AFTER_FATAL_ERROR,
        ALLOW_JAVA_ENCODINGS,
        STANDARD_URI_CONFORMANT_FEATURE, 
        DISALLOW_DOCTYPE,
        GENERATE_SYNTHETIC_ANNOTATIONS,
        VALIDATE_ANNOTATIONS,
        HONOUR_ALL_SCHEMALOCATIONS,
        NAMESPACE_GROWTH,
        TOLERATE_DUPLICATES
    };
    public static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    public static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    public static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    public static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String SCHEMA_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION;
    protected static final String SCHEMA_NONS_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_NONS_LOCATION;
    protected static final String JAXP_SCHEMA_SOURCE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE;
    protected static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    protected static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    protected static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;   
    private static final String [] RECOGNIZED_PROPERTIES = {
        ENTITY_MANAGER,
        SYMBOL_TABLE,
        ERROR_REPORTER,
        ERROR_HANDLER,
        ENTITY_RESOLVER,
        XMLGRAMMAR_POOL,
        SCHEMA_LOCATION,
        SCHEMA_NONS_LOCATION,
        JAXP_SCHEMA_SOURCE,
        SECURITY_MANAGER,
        LOCALE,
        SCHEMA_DV_FACTORY
    };
    private final ParserConfigurationSettings fLoaderConfig = new ParserConfigurationSettings();
    private XMLErrorReporter fErrorReporter = new XMLErrorReporter ();
    private XMLEntityManager fEntityManager = null;
    private XMLEntityResolver fUserEntityResolver = null;
    private XMLGrammarPool fGrammarPool = null;
    private String fExternalSchemas = null;
    private String fExternalNoNSSchema = null;
    private Object fJAXPSource = null;
    private boolean fIsCheckedFully = false;
    private boolean fJAXPProcessed = false;
    private boolean fSettingsChanged = true;
    private XSDHandler fSchemaHandler;
    private XSGrammarBucket fGrammarBucket;
    private XSDeclarationPool fDeclPool = null;
    private SubstitutionGroupHandler fSubGroupHandler;
    private CMBuilder fCMBuilder;
    private XSDDescription fXSDDescription = new XSDDescription();
    private SchemaDVFactory fDefaultSchemaDVFactory;
    private WeakHashMap fJAXPCache;
    private Locale fLocale = Locale.getDefault();
    private DOMStringList fRecognizedParameters = null;
    private DOMErrorHandlerWrapper fErrorHandler = null;
    private DOMEntityResolverWrapper fResourceResolver = null;
    public XMLSchemaLoader() {
        this( new SymbolTable(), null, new XMLEntityManager(), null, null, null);
    }
    public XMLSchemaLoader(SymbolTable symbolTable) {
        this( symbolTable, null, new XMLEntityManager(), null, null, null);
    }
    XMLSchemaLoader(XMLErrorReporter errorReporter,
            XSGrammarBucket grammarBucket,
            SubstitutionGroupHandler sHandler, CMBuilder builder) {
        this(null, errorReporter, null, grammarBucket, sHandler, builder);
    }
    XMLSchemaLoader(SymbolTable symbolTable,
            XMLErrorReporter errorReporter,
            XMLEntityManager entityResolver,
            XSGrammarBucket grammarBucket,
            SubstitutionGroupHandler sHandler,
            CMBuilder builder) {
        fLoaderConfig.addRecognizedFeatures(RECOGNIZED_FEATURES);
        fLoaderConfig.addRecognizedProperties(RECOGNIZED_PROPERTIES); 
        if (symbolTable != null){ 
            fLoaderConfig.setProperty(SYMBOL_TABLE, symbolTable);       
        }
        if(errorReporter == null) {
            errorReporter = new XMLErrorReporter ();
            errorReporter.setLocale(fLocale);
            errorReporter.setProperty(ERROR_HANDLER, new DefaultErrorHandler());
        }
        fErrorReporter = errorReporter;
        if(fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
            fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
        }
        fLoaderConfig.setProperty(ERROR_REPORTER, fErrorReporter);
        fEntityManager = entityResolver;   
        if (fEntityManager != null){   
            fLoaderConfig.setProperty(ENTITY_MANAGER, fEntityManager);
        }
        fLoaderConfig.setFeature(AUGMENT_PSVI, true);
        if(grammarBucket == null ) {
            grammarBucket = new XSGrammarBucket();
        }
        fGrammarBucket = grammarBucket;
        if (sHandler == null) {
            sHandler = new SubstitutionGroupHandler(this);
        }
        fSubGroupHandler = sHandler;
        CMNodeFactory nodeFactory = new CMNodeFactory() ;
        if(builder == null) {
            builder = new CMBuilder(nodeFactory);
        }
        fCMBuilder = builder;
        fSchemaHandler = new XSDHandler(fGrammarBucket);
        fJAXPCache = new WeakHashMap();
        fSettingsChanged = true;
    }
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
    } 
    public boolean getFeature(String featureId)
    throws XMLConfigurationException {                
        return fLoaderConfig.getFeature(featureId);        
    } 
    public void setFeature(String featureId,
            boolean state) throws XMLConfigurationException {
        fSettingsChanged = true; 
        if(featureId.equals(CONTINUE_AFTER_FATAL_ERROR)) {
            fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, state);
        } 
        else if(featureId.equals(GENERATE_SYNTHETIC_ANNOTATIONS)) {
            fSchemaHandler.setGenerateSyntheticAnnotations(state);
        }
        fLoaderConfig.setFeature(featureId, state);
    } 
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } 
    public Object getProperty(String propertyId)
    throws XMLConfigurationException {
        return fLoaderConfig.getProperty(propertyId);
    } 
    public void setProperty(String propertyId,
            Object state) throws XMLConfigurationException {                   
        fSettingsChanged = true;
        fLoaderConfig.setProperty(propertyId, state);    
        if (propertyId.equals(JAXP_SCHEMA_SOURCE)) {
            fJAXPSource = state;
            fJAXPProcessed = false;
        }  
        else if (propertyId.equals(XMLGRAMMAR_POOL)) {
            fGrammarPool = (XMLGrammarPool)state;
        } 
        else if (propertyId.equals(SCHEMA_LOCATION)) {
            fExternalSchemas = (String)state;
        }
        else if (propertyId.equals(SCHEMA_NONS_LOCATION)) {
            fExternalNoNSSchema = (String) state;
        }
        else if (propertyId.equals(LOCALE)) {
            setLocale((Locale) state);
        }
        else if (propertyId.equals(ENTITY_RESOLVER)) {
            fEntityManager.setProperty(ENTITY_RESOLVER, state);
        }
        else if (propertyId.equals(ERROR_REPORTER)) {
            fErrorReporter = (XMLErrorReporter)state;
            if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
            }
        }
    } 
    public void setLocale(Locale locale) {
        fLocale = locale;
        fErrorReporter.setLocale(locale);
    } 
    public Locale getLocale() {
        return fLocale;
    } 
    public void setErrorHandler(XMLErrorHandler errorHandler) {
        fErrorReporter.setProperty(ERROR_HANDLER, errorHandler);
    } 
    public XMLErrorHandler getErrorHandler() {
        return fErrorReporter.getErrorHandler();
    } 
    public void setEntityResolver(XMLEntityResolver entityResolver) {
        fUserEntityResolver = entityResolver;
        fLoaderConfig.setProperty(ENTITY_RESOLVER, entityResolver);
        fEntityManager.setProperty(ENTITY_RESOLVER, entityResolver);
    } 
    public XMLEntityResolver getEntityResolver() {
        return fUserEntityResolver;
    } 
    public void loadGrammar(XMLInputSource source[]) 
    throws IOException, XNIException {
        int numSource = source.length;
        for (int i = 0; i < numSource; ++i) {
            loadGrammar(source[i]);
        }   
    }
    public Grammar loadGrammar(XMLInputSource source)
    throws IOException, XNIException {
        reset(fLoaderConfig);
        fSettingsChanged = false;
        XSDDescription desc = new XSDDescription();
        desc.fContextType = XSDDescription.CONTEXT_PREPARSE;
        desc.setBaseSystemId(source.getBaseSystemId());
        desc.setLiteralSystemId( source.getSystemId());
        Hashtable locationPairs = new Hashtable();
        processExternalHints(fExternalSchemas, fExternalNoNSSchema,
                locationPairs, fErrorReporter);
        SchemaGrammar grammar = loadSchema(desc, source, locationPairs);
        if(grammar != null && fGrammarPool != null) {
            fGrammarPool.cacheGrammars(XMLGrammarDescription.XML_SCHEMA, fGrammarBucket.getGrammars());
            if(fIsCheckedFully && fJAXPCache.get(grammar) != grammar) {
                XSConstraints.fullSchemaChecking(fGrammarBucket, fSubGroupHandler, fCMBuilder, fErrorReporter);
            }
        }
        return grammar;
    } 
    SchemaGrammar loadSchema(XSDDescription desc,
            XMLInputSource source,
            Hashtable locationPairs) throws IOException, XNIException {
        if(!fJAXPProcessed) {
            processJAXPSchemaSource(locationPairs);
        }
        SchemaGrammar grammar = fSchemaHandler.parseSchema(source, desc, locationPairs);
        return grammar;
    } 
    public static XMLInputSource resolveDocument(XSDDescription desc, Hashtable locationPairs,
            XMLEntityResolver entityResolver) throws IOException {
        String loc = null;
        if (desc.getContextType() == XSDDescription.CONTEXT_IMPORT ||
                desc.fromInstance()) {
            String namespace = desc.getTargetNamespace();
            String ns = namespace == null ? XMLSymbols.EMPTY_STRING : namespace;
            LocationArray tempLA = (LocationArray)locationPairs.get(ns);
            if(tempLA != null)
                loc = tempLA.getFirstLocation();
        }
        if (loc == null) {
            String[] hints = desc.getLocationHints();
            if (hints != null && hints.length > 0)
                loc = hints[0];
        }
        String expandedLoc = XMLEntityManager.expandSystemId(loc, desc.getBaseSystemId(), false);
        desc.setLiteralSystemId(loc);
        desc.setExpandedSystemId(expandedLoc);
        return entityResolver.resolveEntity(desc);
    }
    public static void processExternalHints(String sl, String nsl,
            Hashtable locations,
            XMLErrorReporter er) {
        if (sl != null) {
            try {
                XSAttributeDecl attrDecl = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_SCHEMALOCATION);
                attrDecl.fType.validate(sl, null, null);
                if (!tokenizeSchemaLocationStr(sl, locations, null)) {
                    er.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                            "SchemaLocation",
                            new Object[]{sl},
                            XMLErrorReporter.SEVERITY_WARNING);
                }
            }
            catch (InvalidDatatypeValueException ex) {
                er.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                        ex.getKey(), ex.getArgs(),
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
        if (nsl != null) {
            try {
                XSAttributeDecl attrDecl = SchemaGrammar.SG_XSI.getGlobalAttributeDecl(SchemaSymbols.XSI_NONAMESPACESCHEMALOCATION);
                attrDecl.fType.validate(nsl, null, null);
                LocationArray la = ((LocationArray)locations.get(XMLSymbols.EMPTY_STRING));
                if(la == null) {
                    la = new LocationArray();
                    locations.put(XMLSymbols.EMPTY_STRING, la);
                }
                la.addLocation(nsl);
            }
            catch (InvalidDatatypeValueException ex) {
                er.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                        ex.getKey(), ex.getArgs(),
                        XMLErrorReporter.SEVERITY_WARNING);
            }
        }
    }
    public static boolean tokenizeSchemaLocationStr(String schemaStr, Hashtable locations, String base) {
        if (schemaStr!= null) {
            StringTokenizer t = new StringTokenizer(schemaStr, " \n\t\r");
            String namespace, location;
            while (t.hasMoreTokens()) {
                namespace = t.nextToken ();
                if (!t.hasMoreTokens()) {
                    return false; 
                }
                location = t.nextToken();
                LocationArray la = ((LocationArray)locations.get(namespace));
                if(la == null) {
                    la = new LocationArray();
                    locations.put(namespace, la);
                }
                if (base != null) {
                    try {
                        location = XMLEntityManager.expandSystemId(location, base, false);
                    } catch (MalformedURIException e) {
                    }
                }
                la.addLocation(location);
            }
        }
        return true;
    } 
    private void processJAXPSchemaSource(Hashtable locationPairs) throws IOException {
        fJAXPProcessed = true;
        if (fJAXPSource == null) {
            return;
        }
        Class componentType = fJAXPSource.getClass().getComponentType();
        XMLInputSource xis = null;
        String sid = null;
        if (componentType == null) {
            if (fJAXPSource instanceof InputStream ||
                    fJAXPSource instanceof InputSource) {
                SchemaGrammar g = (SchemaGrammar)fJAXPCache.get(fJAXPSource);
                if (g != null) {
                    fGrammarBucket.putGrammar(g);
                    return;
                }
            }
            fXSDDescription.reset();
            xis = xsdToXMLInputSource(fJAXPSource);
            sid = xis.getSystemId();
            fXSDDescription.fContextType = XSDDescription.CONTEXT_PREPARSE;
            if (sid != null) {
                fXSDDescription.setBaseSystemId(xis.getBaseSystemId());
                fXSDDescription.setLiteralSystemId(sid);
                fXSDDescription.setExpandedSystemId(sid);
                fXSDDescription.fLocationHints = new String[]{sid};
            }
            SchemaGrammar g = loadSchema(fXSDDescription, xis, locationPairs);
            if (g != null) {
                if (fJAXPSource instanceof InputStream ||
                        fJAXPSource instanceof InputSource) {
                    fJAXPCache.put(fJAXPSource, g);
                    if (fIsCheckedFully) {
                        XSConstraints.fullSchemaChecking(fGrammarBucket, fSubGroupHandler, fCMBuilder, fErrorReporter);
                    }
                }
                fGrammarBucket.putGrammar(g);
            }
            return;
        } 
        else if ( (componentType != Object.class) &&
                (componentType != String.class) &&
                (componentType != File.class) &&
                (componentType != InputStream.class) &&
                (componentType != InputSource.class) &&
                !File.class.isAssignableFrom(componentType) &&
                !InputStream.class.isAssignableFrom(componentType) &&
                !InputSource.class.isAssignableFrom(componentType) &&
                !componentType.isInterface()
        ) {
            MessageFormatter mf = fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN);
            throw new XMLConfigurationException(
                    XMLConfigurationException.NOT_SUPPORTED, 
                    mf.formatMessage(fErrorReporter.getLocale(), "jaxp12-schema-source-type.2",
                    new Object [] {componentType.getName()}));
        }
        Object[] objArr = (Object[]) fJAXPSource;
        ArrayList jaxpSchemaSourceNamespaces = new ArrayList();
        for (int i = 0; i < objArr.length; i++) {
            if (objArr[i] instanceof InputStream ||
                    objArr[i] instanceof InputSource) {
                SchemaGrammar g = (SchemaGrammar)fJAXPCache.get(objArr[i]);
                if (g != null) {
                    fGrammarBucket.putGrammar(g);
                    continue;
                }
            }
            fXSDDescription.reset();
            xis = xsdToXMLInputSource(objArr[i]);
            sid = xis.getSystemId();
            fXSDDescription.fContextType = XSDDescription.CONTEXT_PREPARSE;
            if (sid != null) {
                fXSDDescription.setBaseSystemId(xis.getBaseSystemId());
                fXSDDescription.setLiteralSystemId(sid);
                fXSDDescription.setExpandedSystemId(sid);
                fXSDDescription.fLocationHints = new String[]{sid};
            }
            String targetNamespace = null ;
            SchemaGrammar grammar = fSchemaHandler.parseSchema(xis,fXSDDescription, locationPairs);
            if (fIsCheckedFully) {
                XSConstraints.fullSchemaChecking(fGrammarBucket, fSubGroupHandler, fCMBuilder, fErrorReporter);
            }                                   
            if (grammar != null) {
                targetNamespace = grammar.getTargetNamespace();
                if (jaxpSchemaSourceNamespaces.contains(targetNamespace)) {
                    MessageFormatter mf = fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN);
                    throw new java.lang.IllegalArgumentException(mf.formatMessage(fErrorReporter.getLocale(), 
                            "jaxp12-schema-source-ns", null));
                }
                else {
                    jaxpSchemaSourceNamespaces.add(targetNamespace) ;
                }
                if (objArr[i] instanceof InputStream ||
                        objArr[i] instanceof InputSource) {
                    fJAXPCache.put(objArr[i], grammar);
                }
                fGrammarBucket.putGrammar(grammar);
            }
            else {
            }
        }
    }
    private XMLInputSource xsdToXMLInputSource(Object val) {
        if (val instanceof String) {
            String loc = (String) val;          
            fXSDDescription.reset();
            fXSDDescription.setValues(null, loc, null, null);
            XMLInputSource xis = null;
            try {
                xis = fEntityManager.resolveEntity(fXSDDescription);
            } 
            catch (IOException ex) {
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                        "schema_reference.4",
                        new Object[] { loc }, XMLErrorReporter.SEVERITY_ERROR);
            }
            if (xis == null) {
                return new XMLInputSource(null, loc, null);
            }
            return xis;
        } 
        else if (val instanceof InputSource) {
            return saxToXMLInputSource((InputSource) val);
        } 
        else if (val instanceof InputStream) {
            return new XMLInputSource(null, null, null,
                    (InputStream) val, null);
        } 
        else if (val instanceof File) {
            File file = (File) val;
            String escapedURI = FilePathToURI.filepath2URI(file.getAbsolutePath());
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
            } 
            catch (FileNotFoundException ex) {
                fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                        "schema_reference.4", new Object[] { file.toString() },
                        XMLErrorReporter.SEVERITY_ERROR);
            }
            return new XMLInputSource(null, escapedURI, null, is, null);
        }
        MessageFormatter mf = fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN);
        throw new XMLConfigurationException(
                XMLConfigurationException.NOT_SUPPORTED, 
                mf.formatMessage(fErrorReporter.getLocale(), "jaxp12-schema-source-type.1",
                new Object [] {val != null ? val.getClass().getName() : "null"}));
    }
    private static XMLInputSource saxToXMLInputSource(InputSource sis) {
        String publicId = sis.getPublicId();
        String systemId = sis.getSystemId();
        Reader charStream = sis.getCharacterStream();
        if (charStream != null) {
            return new XMLInputSource(publicId, systemId, null, charStream,
                    null);
        }
        InputStream byteStream = sis.getByteStream();
        if (byteStream != null) {
            return new XMLInputSource(publicId, systemId, null, byteStream,
                    sis.getEncoding());
        }
        return new XMLInputSource(publicId, systemId, null);
    }
    static class LocationArray{
        int length ;
        String [] locations = new String[2];
        public void resize(int oldLength , int newLength){
            String [] temp = new String[newLength] ;
            System.arraycopy(locations, 0, temp, 0, Math.min(oldLength, newLength));
            locations = temp ;
            length = Math.min(oldLength, newLength);
        }
        public void addLocation(String location){
            if(length >= locations.length ){
                resize(length, Math.max(1, length*2));
            }
            locations[length++] = location;
        }
        public String [] getLocationArray(){
            if(length < locations.length ){
                resize(locations.length, length);
            }
            return locations;
        }
        public String getFirstLocation(){
            return length > 0 ? locations[0] : null;
        }
        public int getLength(){
            return length ;
        }
    } 
    public Boolean getFeatureDefault(String featureId) {
        if (featureId.equals(AUGMENT_PSVI)){
            return Boolean.TRUE;
        }
        return null;
    }
    public Object getPropertyDefault(String propertyId) {
        return null;
    }
    public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
        fGrammarBucket.reset();
        fSubGroupHandler.reset();		
        if (!fSettingsChanged || !parserSettingsUpdated(componentManager)) {
            fJAXPProcessed = false;
            initGrammarBucket();
            if (fDeclPool != null) {
                fDeclPool.reset();
            }
            return;           
        } 
        fEntityManager = (XMLEntityManager)componentManager.getProperty(ENTITY_MANAGER);      
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        SchemaDVFactory dvFactory = null;
        try {
            dvFactory = (SchemaDVFactory)componentManager.getProperty(SCHEMA_DV_FACTORY);
        } catch (XMLConfigurationException e) {
        }
        if (dvFactory == null) {
            if (fDefaultSchemaDVFactory == null) {
                fDefaultSchemaDVFactory = SchemaDVFactory.getInstance();
            }
            dvFactory = fDefaultSchemaDVFactory;
        }
        fSchemaHandler.setDVFactory(dvFactory);
        try {
            fExternalSchemas = (String) componentManager.getProperty(SCHEMA_LOCATION);
            fExternalNoNSSchema =
                (String) componentManager.getProperty(SCHEMA_NONS_LOCATION);
        } catch (XMLConfigurationException e) {
            fExternalSchemas = null;
            fExternalNoNSSchema = null;
        }
        try {
            fJAXPSource = componentManager.getProperty(JAXP_SCHEMA_SOURCE);
            fJAXPProcessed = false;
        } catch (XMLConfigurationException e) {
            fJAXPSource = null;
            fJAXPProcessed = false;
        }
        try {
            fGrammarPool = (XMLGrammarPool) componentManager.getProperty(XMLGRAMMAR_POOL);
        } catch (XMLConfigurationException e) {
            fGrammarPool = null;
        }
        initGrammarBucket();
        boolean psvi = true;
        try {
            psvi = componentManager.getFeature(AUGMENT_PSVI);
        } catch (XMLConfigurationException e) {
            psvi = false;
        }
        if (!psvi && fGrammarPool == null && false) {
            if (fDeclPool != null) {
                fDeclPool.reset();
            }
            else {
                fDeclPool = new XSDeclarationPool();
            }
            fCMBuilder.setDeclPool(fDeclPool);
            fSchemaHandler.setDeclPool(fDeclPool);
            if (dvFactory instanceof SchemaDVFactoryImpl) {
                fDeclPool.setDVFactory((SchemaDVFactoryImpl)dvFactory);
                ((SchemaDVFactoryImpl)dvFactory).setDeclPool(fDeclPool);
            }
        } else {
            fCMBuilder.setDeclPool(null);
            fSchemaHandler.setDeclPool(null);
            if (dvFactory instanceof SchemaDVFactoryImpl) {
                ((SchemaDVFactoryImpl)dvFactory).setDeclPool(null);
            }
        }
        try {
            boolean fatalError = componentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR);
            fErrorReporter.setFeature(CONTINUE_AFTER_FATAL_ERROR, fatalError);
        } catch (XMLConfigurationException e) {
        }
        try {
            fIsCheckedFully = componentManager.getFeature(SCHEMA_FULL_CHECKING);
        }
        catch (XMLConfigurationException e){
            fIsCheckedFully = false;
        }
        try {
            fSchemaHandler.setGenerateSyntheticAnnotations(componentManager.getFeature(GENERATE_SYNTHETIC_ANNOTATIONS));
        }
        catch (XMLConfigurationException e) {
            fSchemaHandler.setGenerateSyntheticAnnotations(false);
        }
        fSchemaHandler.reset(componentManager);		 
    }
    private boolean parserSettingsUpdated(XMLComponentManager componentManager) {
        if (componentManager != fLoaderConfig) {
            try {
                return componentManager.getFeature(PARSER_SETTINGS);     
            }
            catch (XMLConfigurationException e) {}
        }
        return true;
    }
    private void initGrammarBucket(){
        if(fGrammarPool != null) {
            Grammar [] initialGrammars = fGrammarPool.retrieveInitialGrammarSet(XMLGrammarDescription.XML_SCHEMA);
            final int length = (initialGrammars != null) ? initialGrammars.length : 0;
            for (int i = 0; i < length; ++i) {
                if (!fGrammarBucket.putGrammar((SchemaGrammar)(initialGrammars[i]), true)) {
                    fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN,
                            "GrammarConflict", null,
                            XMLErrorReporter.SEVERITY_WARNING);
                }
            }
        }
    }
    public DOMConfiguration getConfig() {
        return this;
    }
    public XSModel load(LSInput is) {
        try {
            Grammar g = loadGrammar(dom2xmlInputSource(is));
            return ((XSGrammar) g).toXSModel();
        } catch (Exception e) {
            reportDOMFatalError(e);
            return null;
        }
    }
    public XSModel loadInputList(LSInputList is) {
        int length = is.getLength();
        SchemaGrammar[] gs = new SchemaGrammar[length];
        for (int i = 0; i < length; i++) {
            try {
                gs[i] = (SchemaGrammar) loadGrammar(dom2xmlInputSource(is.item(i)));
            } catch (Exception e) {
                reportDOMFatalError(e);
                return null;
            }
        }
        return new XSModelImpl(gs);
    }
    public XSModel loadURI(String uri) {
        try {
            Grammar g = loadGrammar(new XMLInputSource(null, uri, null));
            return ((XSGrammar)g).toXSModel();
        }
        catch (Exception e){
            reportDOMFatalError(e);
            return null;
        }
    }
    public XSModel loadURIList(StringList uriList) {
        int length = uriList.getLength();
        SchemaGrammar[] gs = new SchemaGrammar[length];
        for (int i = 0; i < length; i++) {
            try {
                gs[i] =
                    (SchemaGrammar) loadGrammar(new XMLInputSource(null, uriList.item(i), null));
            } catch (Exception e) {
                reportDOMFatalError(e);
                return null;
            }
        }
        return new XSModelImpl(gs);
    }
    void reportDOMFatalError(Exception e) {
                if (fErrorHandler != null) {
                    DOMErrorImpl error = new DOMErrorImpl();
                    error.fException = e;
                    error.fMessage = e.getMessage();
                    error.fSeverity = DOMError.SEVERITY_FATAL_ERROR;
                    fErrorHandler.getErrorHandler().handleError(error);
                }
            }
    public boolean canSetParameter(String name, Object value) {
        if(value instanceof Boolean){
            if (name.equals(Constants.DOM_VALIDATE) ||
                name.equals(SCHEMA_FULL_CHECKING) ||
                name.equals(VALIDATE_ANNOTATIONS) ||
                name.equals(CONTINUE_AFTER_FATAL_ERROR) ||
                name.equals(ALLOW_JAVA_ENCODINGS) ||
                name.equals(STANDARD_URI_CONFORMANT_FEATURE) ||
                name.equals(GENERATE_SYNTHETIC_ANNOTATIONS) ||
                name.equals(HONOUR_ALL_SCHEMALOCATIONS) ||
                name.equals(NAMESPACE_GROWTH) ||
                name.equals(TOLERATE_DUPLICATES)) {
                return true;
            }
            return false;			
        }
        if (name.equals(Constants.DOM_ERROR_HANDLER) ||
            name.equals(Constants.DOM_RESOURCE_RESOLVER) ||
            name.equals(SYMBOL_TABLE) ||
            name.equals(ERROR_REPORTER) ||
            name.equals(ERROR_HANDLER) ||
            name.equals(ENTITY_RESOLVER) ||
            name.equals(XMLGRAMMAR_POOL) ||
            name.equals(SCHEMA_LOCATION) ||
            name.equals(SCHEMA_NONS_LOCATION) ||
            name.equals(JAXP_SCHEMA_SOURCE) ||
            name.equals(SCHEMA_DV_FACTORY)) {
            return true;
        }
        return false;
    }
    public Object getParameter(String name) throws DOMException {
        if (name.equals(Constants.DOM_ERROR_HANDLER)){
            return (fErrorHandler != null) ? fErrorHandler.getErrorHandler() : null;
        }
        else if (name.equals(Constants.DOM_RESOURCE_RESOLVER)) {
            return (fResourceResolver != null) ? fResourceResolver.getEntityResolver() : null;
        }
        try {
            boolean feature = getFeature(name);
            return (feature) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Exception e) {
            Object property;
            try {
                property = getProperty(name);
                return property;
            } catch (Exception ex) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
        }
    }
    public DOMStringList getParameterNames() {
        if (fRecognizedParameters == null){
            ArrayList v = new ArrayList();
            v.add(Constants.DOM_VALIDATE);
            v.add(Constants.DOM_ERROR_HANDLER);
            v.add(Constants.DOM_RESOURCE_RESOLVER);
            v.add(SYMBOL_TABLE);
            v.add(ERROR_REPORTER);
            v.add(ERROR_HANDLER);
            v.add(ENTITY_RESOLVER);
            v.add(XMLGRAMMAR_POOL);
            v.add(SCHEMA_LOCATION);
            v.add(SCHEMA_NONS_LOCATION);
            v.add(JAXP_SCHEMA_SOURCE);
            v.add(SCHEMA_FULL_CHECKING);
            v.add(CONTINUE_AFTER_FATAL_ERROR);
            v.add(ALLOW_JAVA_ENCODINGS);
            v.add(STANDARD_URI_CONFORMANT_FEATURE);
            v.add(VALIDATE_ANNOTATIONS);
            v.add(GENERATE_SYNTHETIC_ANNOTATIONS);
            v.add(HONOUR_ALL_SCHEMALOCATIONS);
            v.add(NAMESPACE_GROWTH);
            v.add(TOLERATE_DUPLICATES);
            fRecognizedParameters = new DOMStringListImpl(v);      	
        }
        return fRecognizedParameters;
    }
    public void setParameter(String name, Object value) throws DOMException {
        if (value instanceof Boolean) {
            boolean state = ((Boolean) value).booleanValue();
            if (name.equals("validate") && state) {
                return;
            }
            try {
                setFeature(name, state);
            } catch (Exception e) {
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
            return;
        }
        if (name.equals(Constants.DOM_ERROR_HANDLER)) {
            if (value instanceof DOMErrorHandler) {
                try {
                    fErrorHandler = new DOMErrorHandlerWrapper((DOMErrorHandler) value);
                    setErrorHandler(fErrorHandler);
                } catch (XMLConfigurationException e) {
                }
            } else {
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
            return;
        }
        if (name.equals(Constants.DOM_RESOURCE_RESOLVER)) {
            if (value instanceof LSResourceResolver) {
                try {
                    fResourceResolver = new DOMEntityResolverWrapper((LSResourceResolver) value);
                    setEntityResolver(fResourceResolver);
                } 
                catch (XMLConfigurationException e) {}
            } else {
                String msg =
                    DOMMessageFormatter.formatMessage(
                            DOMMessageFormatter.DOM_DOMAIN,
                            "FEATURE_NOT_SUPPORTED",
                            new Object[] { name });
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
            }
            return;
        }
        try {
            setProperty(name, value);
        } catch (Exception ex) {
            String msg =
                DOMMessageFormatter.formatMessage(
                        DOMMessageFormatter.DOM_DOMAIN,
                        "FEATURE_NOT_SUPPORTED",
                        new Object[] { name });
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, msg);
        }
    }
	XMLInputSource dom2xmlInputSource(LSInput is) {
        XMLInputSource xis = null;
        if (is.getCharacterStream() != null) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                    is.getBaseURI(), is.getCharacterStream(),
            "UTF-16");
        }
        else if (is.getByteStream() != null) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                    is.getBaseURI(), is.getByteStream(),
                    is.getEncoding());
        }
        else if (is.getStringData() != null && is.getStringData().length() != 0) {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                    is.getBaseURI(), new StringReader(is.getStringData()),
            "UTF-16");
        }
        else {
            xis = new XMLInputSource(is.getPublicId(), is.getSystemId(),
                    is.getBaseURI());
        }
        return xis;
    }
    public XSElementDecl getGlobalElementDecl(QName element) {
        SchemaGrammar sGrammar = fGrammarBucket.getGrammar(element.uri);
        if (sGrammar != null) {
            return sGrammar.getGlobalElementDecl(element.localpart);
        }
        return null;
    }
} 
