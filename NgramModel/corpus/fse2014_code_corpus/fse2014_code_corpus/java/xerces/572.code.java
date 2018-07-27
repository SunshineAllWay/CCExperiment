package org.apache.xerces.parsers;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XML11DTDScannerImpl;
import org.apache.xerces.impl.XML11DocumentScannerImpl;
import org.apache.xerces.impl.XML11NSDocumentScannerImpl;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLDocumentScannerImpl;
import org.apache.xerces.impl.XMLEntityHandler;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.XMLVersionDetector;
import org.apache.xerces.impl.dtd.XML11DTDProcessor;
import org.apache.xerces.impl.dtd.XML11DTDValidator;
import org.apache.xerces.impl.dtd.XML11NSDTDValidator;
import org.apache.xerces.impl.dtd.XMLDTDProcessor;
import org.apache.xerces.impl.dtd.XMLDTDValidator;
import org.apache.xerces.impl.dtd.XMLNSDTDValidator;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLDocumentSource;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
public class XML11Configuration extends ParserConfigurationSettings
    implements XMLPullParserConfiguration, XML11Configurable {
    protected final static String XML11_DATATYPE_VALIDATOR_FACTORY =
        "org.apache.xerces.impl.dv.dtd.XML11DTDDVFactoryImpl";
    protected static final String WARN_ON_DUPLICATE_ATTDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS =
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
    protected static final String NORMALIZE_DATA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    protected static final String SCHEMA_AUGMENT_PSVI =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;
    protected static final String XMLSCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String XMLSCHEMA_FULL_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
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
    protected static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
	protected static final String VALIDATION =
		Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
	protected static final String NAMESPACES =
		Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
	protected static final String EXTERNAL_GENERAL_ENTITIES =
		Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE;
	protected static final String EXTERNAL_PARAMETER_ENTITIES =
		Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE;
	protected static final String IGNORE_XSI_TYPE =
	    Constants.XERCES_FEATURE_PREFIX + Constants.IGNORE_XSI_TYPE_FEATURE;
	protected static final String ID_IDREF_CHECKING =
	    Constants.XERCES_FEATURE_PREFIX + Constants.ID_IDREF_CHECKING_FEATURE;
	protected static final String UNPARSED_ENTITY_CHECKING =
	    Constants.XERCES_FEATURE_PREFIX + Constants.UNPARSED_ENTITY_CHECKING_FEATURE;
	protected static final String IDENTITY_CONSTRAINT_CHECKING =
	    Constants.XERCES_FEATURE_PREFIX + Constants.IDC_CHECKING_FEATURE;
	protected static final String XML_STRING = 
		Constants.SAX_PROPERTY_PREFIX + Constants.XML_STRING_PROPERTY;
	protected static final String SYMBOL_TABLE = 
		Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
	protected static final String ERROR_HANDLER = 
		Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
	protected static final String ENTITY_RESOLVER = 
		Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String SCHEMA_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION;
    protected static final String SCHEMA_NONS_LOCATION =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_NONS_LOCATION;
    protected static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String DOCUMENT_SCANNER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;
    protected static final String DTD_SCANNER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
    protected static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String DTD_PROCESSOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_PROCESSOR_PROPERTY;
    protected static final String DTD_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;
    protected static final String NAMESPACE_BINDER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_BINDER_PROPERTY;
    protected static final String DATATYPE_VALIDATOR_FACTORY =
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String JAXP_SCHEMA_LANGUAGE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_LANGUAGE;
    protected static final String JAXP_SCHEMA_SOURCE =
        Constants.JAXP_PROPERTY_PREFIX + Constants.SCHEMA_SOURCE;
    protected static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    protected static final String ROOT_TYPE_DEF =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_TYPE_DEFINITION_PROPERTY;
    protected static final String ROOT_ELEMENT_DECL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_ELEMENT_DECLARATION_PROPERTY;
    protected static final String SCHEMA_DV_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_DV_FACTORY_PROPERTY;
    protected static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
	protected SymbolTable fSymbolTable;
    protected XMLInputSource fInputSource;
    protected final ValidationManager fValidationManager;
	protected final XMLVersionDetector fVersionDetector;
	protected Locale fLocale;
	protected final ArrayList fComponents;
	protected final ArrayList fXML11Components;
	protected final ArrayList fCommonComponents;
	protected XMLDocumentHandler fDocumentHandler;
	protected XMLDTDHandler fDTDHandler;
	protected XMLDTDContentModelHandler fDTDContentModelHandler;
	protected XMLDocumentSource fLastComponent;
    protected boolean fParseInProgress = false;
	protected boolean fConfigUpdated = false;
    protected final DTDDVFactory fDatatypeValidatorFactory;
    protected final XMLNSDocumentScannerImpl fNamespaceScanner;
    protected XMLDocumentScannerImpl fNonNSScanner;
    protected final XMLDTDValidator fDTDValidator;
    protected XMLDTDValidator fNonNSDTDValidator;
    protected final XMLDTDScanner fDTDScanner;
    protected final XMLDTDProcessor fDTDProcessor;
    protected DTDDVFactory fXML11DatatypeFactory = null;
    protected XML11NSDocumentScannerImpl fXML11NSDocScanner = null;
    protected XML11DocumentScannerImpl fXML11DocScanner = null;
    protected XML11NSDTDValidator fXML11NSDTDValidator = null;
    protected XML11DTDValidator fXML11DTDValidator = null;
    protected XML11DTDScannerImpl fXML11DTDScanner = null;
    protected XML11DTDProcessor fXML11DTDProcessor = null;
    protected XMLGrammarPool fGrammarPool;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLSchemaValidator fSchemaValidator;
    protected XMLDocumentScanner fCurrentScanner;
    protected DTDDVFactory fCurrentDVFactory;
    protected XMLDTDScanner fCurrentDTDScanner;
    private boolean f11Initialized = false;
    public XML11Configuration() {
        this(null, null, null);
    } 
    public XML11Configuration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public XML11Configuration(SymbolTable symbolTable, XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public XML11Configuration(
        SymbolTable symbolTable,
        XMLGrammarPool grammarPool,
        XMLComponentManager parentSettings) {
		super(parentSettings);
		fComponents = new ArrayList();
		fXML11Components = new ArrayList();
		fCommonComponents = new ArrayList();
		fRecognizedFeatures = new ArrayList();
		fRecognizedProperties = new ArrayList();
		fFeatures = new HashMap();
		fProperties = new HashMap();
		final String[] recognizedFeatures =
		{   
		        CONTINUE_AFTER_FATAL_ERROR, LOAD_EXTERNAL_DTD, 
		        VALIDATION,                 
		        NAMESPACES,
		        NORMALIZE_DATA, SCHEMA_ELEMENT_DEFAULT, SCHEMA_AUGMENT_PSVI,
		        GENERATE_SYNTHETIC_ANNOTATIONS, VALIDATE_ANNOTATIONS,
		        HONOUR_ALL_SCHEMALOCATIONS, NAMESPACE_GROWTH,
		        TOLERATE_DUPLICATES, IGNORE_XSI_TYPE,
		        ID_IDREF_CHECKING, IDENTITY_CONSTRAINT_CHECKING,
		        UNPARSED_ENTITY_CHECKING, USE_GRAMMAR_POOL_ONLY,
		        XMLSCHEMA_VALIDATION, XMLSCHEMA_FULL_CHECKING, 			
		        EXTERNAL_GENERAL_ENTITIES,  
		        EXTERNAL_PARAMETER_ENTITIES,
		        PARSER_SETTINGS,
		};
        addRecognizedFeatures(recognizedFeatures);
        fFeatures.put(VALIDATION, Boolean.FALSE);
        fFeatures.put(NAMESPACES, Boolean.TRUE);
        fFeatures.put(EXTERNAL_GENERAL_ENTITIES, Boolean.TRUE);
        fFeatures.put(EXTERNAL_PARAMETER_ENTITIES, Boolean.TRUE);
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
        fFeatures.put(LOAD_EXTERNAL_DTD, Boolean.TRUE);
        fFeatures.put(SCHEMA_ELEMENT_DEFAULT, Boolean.TRUE);
        fFeatures.put(NORMALIZE_DATA, Boolean.TRUE);
        fFeatures.put(SCHEMA_AUGMENT_PSVI, Boolean.TRUE);
        fFeatures.put(GENERATE_SYNTHETIC_ANNOTATIONS, Boolean.FALSE);
        fFeatures.put(VALIDATE_ANNOTATIONS, Boolean.FALSE);
        fFeatures.put(HONOUR_ALL_SCHEMALOCATIONS, Boolean.FALSE);
        fFeatures.put(NAMESPACE_GROWTH, Boolean.FALSE);
        fFeatures.put(TOLERATE_DUPLICATES, Boolean.FALSE);
        fFeatures.put(IGNORE_XSI_TYPE, Boolean.FALSE);
        fFeatures.put(ID_IDREF_CHECKING, Boolean.TRUE);
        fFeatures.put(IDENTITY_CONSTRAINT_CHECKING, Boolean.TRUE);
        fFeatures.put(UNPARSED_ENTITY_CHECKING, Boolean.TRUE);
        fFeatures.put(USE_GRAMMAR_POOL_ONLY, Boolean.FALSE);
        fFeatures.put(PARSER_SETTINGS, Boolean.TRUE);
        final String[] recognizedProperties =
        {				     
                SYMBOL_TABLE,
                ERROR_HANDLER,  
                ENTITY_RESOLVER,
                ERROR_REPORTER,
                ENTITY_MANAGER,
                DOCUMENT_SCANNER,
                DTD_SCANNER,
                DTD_PROCESSOR,
                DTD_VALIDATOR,
                DATATYPE_VALIDATOR_FACTORY,
                VALIDATION_MANAGER,
                SCHEMA_VALIDATOR,
                XML_STRING,
                XMLGRAMMAR_POOL, 
                JAXP_SCHEMA_SOURCE,
                JAXP_SCHEMA_LANGUAGE,                
                SCHEMA_LOCATION,
                SCHEMA_NONS_LOCATION,
                LOCALE,
                ROOT_TYPE_DEF,
                ROOT_ELEMENT_DECL,
                SCHEMA_DV_FACTORY,
        };
        addRecognizedProperties(recognizedProperties);
		if (symbolTable == null) {
			symbolTable = new SymbolTable();
		}
		fSymbolTable = symbolTable;
		fProperties.put(SYMBOL_TABLE, fSymbolTable);
        fGrammarPool = grammarPool;
        if (fGrammarPool != null) {
			fProperties.put(XMLGRAMMAR_POOL, fGrammarPool);
        }
        fEntityManager = new XMLEntityManager();
		fProperties.put(ENTITY_MANAGER, fEntityManager);
        addCommonComponent(fEntityManager);
        fErrorReporter = new XMLErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
		fProperties.put(ERROR_REPORTER, fErrorReporter);
        addCommonComponent(fErrorReporter);
        fNamespaceScanner = new XMLNSDocumentScannerImpl();
		fProperties.put(DOCUMENT_SCANNER, fNamespaceScanner);
        addComponent((XMLComponent) fNamespaceScanner);
        fDTDScanner = new XMLDTDScannerImpl();
		fProperties.put(DTD_SCANNER, fDTDScanner);
        addComponent((XMLComponent) fDTDScanner);
        fDTDProcessor = new XMLDTDProcessor();
		fProperties.put(DTD_PROCESSOR, fDTDProcessor);
        addComponent(fDTDProcessor);
        fDTDValidator = new XMLNSDTDValidator();
		fProperties.put(DTD_VALIDATOR, fDTDValidator);
        addComponent(fDTDValidator);
        fDatatypeValidatorFactory = DTDDVFactory.getInstance();
		fProperties.put(DATATYPE_VALIDATOR_FACTORY, fDatatypeValidatorFactory);
        fValidationManager = new ValidationManager();
		fProperties.put(VALIDATION_MANAGER, fValidationManager);
        fVersionDetector = new XMLVersionDetector();
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
        try {
            setLocale(Locale.getDefault());
        } catch (XNIException e) {
        }
		fConfigUpdated = false;
    } 
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        fInputSource = inputSource;
    } 
    public void setLocale(Locale locale) throws XNIException {
        fLocale = locale;
        fErrorReporter.setLocale(locale);
    } 
	public void setDocumentHandler(XMLDocumentHandler documentHandler) {
		fDocumentHandler = documentHandler;
		if (fLastComponent != null) {
			fLastComponent.setDocumentHandler(fDocumentHandler);
			if (fDocumentHandler !=null){
				fDocumentHandler.setDocumentSource(fLastComponent);
			}
		}
	} 
	public XMLDocumentHandler getDocumentHandler() {
		return fDocumentHandler;
	} 
	public void setDTDHandler(XMLDTDHandler dtdHandler) {
		fDTDHandler = dtdHandler;
	} 
	public XMLDTDHandler getDTDHandler() {
		return fDTDHandler;
	} 
	public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
		fDTDContentModelHandler = handler;
	} 
	public XMLDTDContentModelHandler getDTDContentModelHandler() {
		return fDTDContentModelHandler;
	} 
	public void setEntityResolver(XMLEntityResolver resolver) {
		fProperties.put(ENTITY_RESOLVER, resolver);
	} 
	public XMLEntityResolver getEntityResolver() {
		return (XMLEntityResolver)fProperties.get(ENTITY_RESOLVER);
	} 
	public void setErrorHandler(XMLErrorHandler errorHandler) {
		fProperties.put(ERROR_HANDLER, errorHandler);
	} 
	public XMLErrorHandler getErrorHandler() {
		return (XMLErrorHandler)fProperties.get(ERROR_HANDLER);
	} 
    public void cleanup() {
        fEntityManager.closeReaders();
    }
    public void parse(XMLInputSource source) throws XNIException, IOException {
        if (fParseInProgress) {
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress = true;
        try {
            setInputSource(source);
            parse(true);
        } catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        } finally {
            fParseInProgress = false;
            this.cleanup();
        }
    } 
    public boolean parse(boolean complete) throws XNIException, IOException {
        if (fInputSource != null) {
            try {
				fValidationManager.reset();
                fVersionDetector.reset(this);
                resetCommon();
                short version = fVersionDetector.determineDocVersion(fInputSource);
                if (version == Constants.XML_VERSION_1_0) {
                    configurePipeline();
                    reset();
                }
                else if (version == Constants.XML_VERSION_1_1) {
                    initXML11Components();
                    configureXML11Pipeline();
                    resetXML11();
                }
                else {
                   return false;
                }
                fConfigUpdated = false;
                fVersionDetector.startDocumentParsing((XMLEntityHandler) fCurrentScanner, version);
                fInputSource = null;
            } catch (XNIException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (IOException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (RuntimeException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } catch (Exception ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }
        try {
            return fCurrentScanner.scanDocument(complete);
        } catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
    } 
	public boolean getFeature(String featureId)
		throws XMLConfigurationException {
        if (featureId.equals(PARSER_SETTINGS)){
        	return fConfigUpdated;
        }
        return super.getFeature(featureId);
	} 
	public void setFeature(String featureId, boolean state)
		throws XMLConfigurationException {
		fConfigUpdated = true;
		int count = fComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fComponents.get(i);
			c.setFeature(featureId, state);
		}
		count = fCommonComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fCommonComponents.get(i);
			c.setFeature(featureId, state);
		}
		count = fXML11Components.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fXML11Components.get(i);
			try{            
				c.setFeature(featureId, state);
			}
			catch (Exception e){
			}
		}
		super.setFeature(featureId, state);
	} 
    public Object getProperty(String propertyId)
        throws XMLConfigurationException {
        if (LOCALE.equals(propertyId)) {
            return getLocale();
        }
        return super.getProperty(propertyId);
    }
	public void setProperty(String propertyId, Object value)
		throws XMLConfigurationException {
		fConfigUpdated = true;
		if (LOCALE.equals(propertyId)) {
		    setLocale((Locale) value);
		}
		int count = fComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fComponents.get(i);
			c.setProperty(propertyId, value);
		}
		count = fCommonComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fCommonComponents.get(i);
			c.setProperty(propertyId, value);
		}
		count = fXML11Components.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fXML11Components.get(i);
			try{			
				c.setProperty(propertyId, value);
			}
			catch (Exception e){
			}
		}
		super.setProperty(propertyId, value);
	} 
	public Locale getLocale() {
		return fLocale;
	} 
	protected void reset() throws XNIException {
		int count = fComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fComponents.get(i);
			c.reset(this);
		}
	} 
	protected void resetCommon() throws XNIException {
		int count = fCommonComponents.size();
		for (int i = 0; i < count; i++) {
			XMLComponent c = (XMLComponent) fCommonComponents.get(i);
			c.reset(this);
		}
	} 
	protected void resetXML11() throws XNIException {
		int count = fXML11Components.size();
		for (int i = 0; i < count; i++) {			
			XMLComponent c = (XMLComponent) fXML11Components.get(i);
			c.reset(this);
		}
	} 
    protected void configureXML11Pipeline() {
        if (fCurrentDVFactory != fXML11DatatypeFactory) {
            fCurrentDVFactory = fXML11DatatypeFactory;
            setProperty(DATATYPE_VALIDATOR_FACTORY, fCurrentDVFactory);
        }
        if (fCurrentDTDScanner != fXML11DTDScanner) {
            fCurrentDTDScanner = fXML11DTDScanner;
            setProperty(DTD_SCANNER, fCurrentDTDScanner);
			setProperty(DTD_PROCESSOR, fXML11DTDProcessor);
        }
        fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fXML11DTDProcessor);
        }
        fXML11DTDScanner.setDTDContentModelHandler(fXML11DTDProcessor);
        fXML11DTDProcessor.setDTDContentModelSource(fXML11DTDScanner);
        fXML11DTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.setDTDContentModelSource(fXML11DTDProcessor);
        }
        if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            if (fCurrentScanner != fXML11NSDocScanner) {
                fCurrentScanner = fXML11NSDocScanner;
                setProperty(DOCUMENT_SCANNER, fXML11NSDocScanner);
                setProperty(DTD_VALIDATOR, fXML11NSDTDValidator);
            }
            fXML11NSDocScanner.setDTDValidator(fXML11NSDTDValidator);
            fXML11NSDocScanner.setDocumentHandler(fXML11NSDTDValidator);
            fXML11NSDTDValidator.setDocumentSource(fXML11NSDocScanner);
            fXML11NSDTDValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fXML11NSDTDValidator);
            }
            fLastComponent = fXML11NSDTDValidator;
        } else {
			  if (fXML11DocScanner == null) {
					fXML11DocScanner = new XML11DocumentScannerImpl();
					addXML11Component(fXML11DocScanner);
					fXML11DTDValidator = new XML11DTDValidator();
					addXML11Component(fXML11DTDValidator);
			  }
            if (fCurrentScanner != fXML11DocScanner) {
                fCurrentScanner = fXML11DocScanner;
                setProperty(DOCUMENT_SCANNER, fXML11DocScanner);
                setProperty(DTD_VALIDATOR, fXML11DTDValidator);
            }
            fXML11DocScanner.setDocumentHandler(fXML11DTDValidator);
            fXML11DTDValidator.setDocumentSource(fXML11DocScanner);
            fXML11DTDValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fXML11DTDValidator);
            }
            fLastComponent = fXML11DTDValidator;
        }
        if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            if (fSchemaValidator == null) {
                fSchemaValidator = new XMLSchemaValidator();
                setProperty(SCHEMA_VALIDATOR, fSchemaValidator);
				addCommonComponent(fSchemaValidator);
				fSchemaValidator.reset(this);
                if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
                }
            }
            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent = fSchemaValidator;
        }
    } 
    protected void configurePipeline() {
        if (fCurrentDVFactory != fDatatypeValidatorFactory) {
            fCurrentDVFactory = fDatatypeValidatorFactory;
            setProperty(DATATYPE_VALIDATOR_FACTORY, fCurrentDVFactory);
        }
        if (fCurrentDTDScanner != fDTDScanner) {
            fCurrentDTDScanner = fDTDScanner;
            setProperty(DTD_SCANNER, fCurrentDTDScanner);
            setProperty(DTD_PROCESSOR, fDTDProcessor);
        }
        fDTDScanner.setDTDHandler(fDTDProcessor);
        fDTDProcessor.setDTDSource(fDTDScanner);
        fDTDProcessor.setDTDHandler(fDTDHandler);
        if (fDTDHandler != null) {
            fDTDHandler.setDTDSource(fDTDProcessor);
        }
        fDTDScanner.setDTDContentModelHandler(fDTDProcessor);
        fDTDProcessor.setDTDContentModelSource(fDTDScanner);
        fDTDProcessor.setDTDContentModelHandler(fDTDContentModelHandler);
        if (fDTDContentModelHandler != null) {
            fDTDContentModelHandler.setDTDContentModelSource(fDTDProcessor);
        }
        if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            if (fCurrentScanner != fNamespaceScanner) {
                fCurrentScanner = fNamespaceScanner;
                setProperty(DOCUMENT_SCANNER, fNamespaceScanner);
                setProperty(DTD_VALIDATOR, fDTDValidator);
            }
            fNamespaceScanner.setDTDValidator(fDTDValidator);
            fNamespaceScanner.setDocumentHandler(fDTDValidator);
            fDTDValidator.setDocumentSource(fNamespaceScanner);
            fDTDValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fDTDValidator);
            }
            fLastComponent = fDTDValidator;
        } else {
            if (fNonNSScanner == null) {
                fNonNSScanner = new XMLDocumentScannerImpl();
                fNonNSDTDValidator = new XMLDTDValidator();
                addComponent((XMLComponent) fNonNSScanner);
                addComponent((XMLComponent) fNonNSDTDValidator);
            }
            if (fCurrentScanner != fNonNSScanner) {
                fCurrentScanner = fNonNSScanner;
                setProperty(DOCUMENT_SCANNER, fNonNSScanner);
                setProperty(DTD_VALIDATOR, fNonNSDTDValidator);
            }
            fNonNSScanner.setDocumentHandler(fNonNSDTDValidator);
            fNonNSDTDValidator.setDocumentSource(fNonNSScanner);
            fNonNSDTDValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fNonNSDTDValidator);
            }
            fLastComponent = fNonNSDTDValidator;
        }
        if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
            if (fSchemaValidator == null) {
                fSchemaValidator = new XMLSchemaValidator();
                setProperty(SCHEMA_VALIDATOR, fSchemaValidator);
                addCommonComponent(fSchemaValidator);
                fSchemaValidator.reset(this);
                if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
                }
            }
            fLastComponent.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentSource(fLastComponent);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            if (fDocumentHandler != null) {
                fDocumentHandler.setDocumentSource(fSchemaValidator);
            }
            fLastComponent = fSchemaValidator;
        }
    } 
    protected void checkFeature(String featureId) throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.DYNAMIC_VALIDATION_FEATURE.length() && 
                featureId.endsWith(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE.length() &&
                featureId.endsWith(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.VALIDATE_CONTENT_MODELS_FEATURE.length() && 
                featureId.endsWith(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.LOAD_DTD_GRAMMAR_FEATURE.length() && 
                featureId.endsWith(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.LOAD_EXTERNAL_DTD_FEATURE.length() && 
                featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.VALIDATE_DATATYPES_FEATURE.length() && 
                featureId.endsWith(Constants.VALIDATE_DATATYPES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.SCHEMA_VALIDATION_FEATURE.length() && 
                featureId.endsWith(Constants.SCHEMA_VALIDATION_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_FULL_CHECKING.length() && 
                featureId.endsWith(Constants.SCHEMA_FULL_CHECKING)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_NORMALIZED_VALUE.length() && 
                featureId.endsWith(Constants.SCHEMA_NORMALIZED_VALUE)) {
                return;
            } 
            if (suffixLength == Constants.SCHEMA_ELEMENT_DEFAULT.length() && 
                featureId.endsWith(Constants.SCHEMA_ELEMENT_DEFAULT)) {
                return;
            }
            if (suffixLength == Constants.PARSER_SETTINGS.length() && 
                featureId.endsWith(Constants.PARSER_SETTINGS)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
        }
        super.checkFeature(featureId);
    } 
    protected void checkProperty(String propertyId) throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.DTD_SCANNER_PROPERTY.length() && 
                propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_LOCATION.length() && 
                propertyId.endsWith(Constants.SCHEMA_LOCATION)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_NONS_LOCATION.length() && 
                propertyId.endsWith(Constants.SCHEMA_NONS_LOCATION)) {
                return;
            }
        }
        if (propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.JAXP_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SCHEMA_SOURCE.length() && 
                propertyId.endsWith(Constants.SCHEMA_SOURCE)) {
                return;
            }
        }
        if (propertyId.startsWith(Constants.SAX_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.SAX_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.XML_STRING_PROPERTY.length() && 
                propertyId.endsWith(Constants.XML_STRING_PROPERTY)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, propertyId);
            }
        }
        super.checkProperty(propertyId);
    } 
    protected void addComponent(XMLComponent component) {
        if (fComponents.contains(component)) {
            return;
        }
        fComponents.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } 
    protected void addCommonComponent(XMLComponent component) {
        if (fCommonComponents.contains(component)) {
            return;
        }
        fCommonComponents.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } 
    protected void addXML11Component(XMLComponent component) {
        if (fXML11Components.contains(component)) {
            return;
        }
        fXML11Components.add(component);
        addRecognizedParamsAndSetDefaults(component);
    } 
    protected void addRecognizedParamsAndSetDefaults(XMLComponent component) {
        String[] recognizedFeatures = component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        String[] recognizedProperties = component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        if (recognizedFeatures != null) {
            for (int i = 0; i < recognizedFeatures.length; ++i) {
                String featureId = recognizedFeatures[i];
                Boolean state = component.getFeatureDefault(featureId);
                if (state != null) {
                    if (!fFeatures.containsKey(featureId)) {
                        fFeatures.put(featureId, state);
                        fConfigUpdated = true;
                    }
                }
            }
        }
        if (recognizedProperties != null) {
            for (int i = 0; i < recognizedProperties.length; ++i) {
                String propertyId = recognizedProperties[i];
                Object value = component.getPropertyDefault(propertyId);
                if (value != null) {
                    if (!fProperties.containsKey(propertyId)) {
                        fProperties.put(propertyId, value);
                        fConfigUpdated = true;
                    }
                }
            }
        }
    } 
    private void initXML11Components() {
        if (!f11Initialized) {
            fXML11DatatypeFactory = DTDDVFactory.getInstance(XML11_DATATYPE_VALIDATOR_FACTORY);
            fXML11DTDScanner = new XML11DTDScannerImpl();
            addXML11Component(fXML11DTDScanner);
            fXML11DTDProcessor = new XML11DTDProcessor();
            addXML11Component(fXML11DTDProcessor);
            fXML11NSDocScanner = new XML11NSDocumentScannerImpl();
            addXML11Component(fXML11NSDocScanner);
            fXML11NSDTDValidator = new XML11NSDTDValidator();
            addXML11Component(fXML11NSDTDValidator);
            f11Initialized = true;
        }
    }
    boolean getFeature0(String featureId)
        throws XMLConfigurationException {
        return super.getFeature(featureId);
    }
} 
