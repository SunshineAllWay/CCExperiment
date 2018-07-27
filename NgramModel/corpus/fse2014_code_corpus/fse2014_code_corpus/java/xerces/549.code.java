package org.apache.xerces.jaxp.validation;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.xml.XMLConstants;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.DOMEntityResolverWrapper;
import org.apache.xerces.util.ErrorHandlerWrapper;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.ParserConfigurationSettings;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
final class XMLSchemaValidatorComponentManager extends ParserConfigurationSettings implements
        XMLComponentManager {
    private static final String SCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    private static final String IGNORE_XSI_TYPE =
        Constants.XERCES_FEATURE_PREFIX + Constants.IGNORE_XSI_TYPE_FEATURE;
    private static final String ID_IDREF_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.ID_IDREF_CHECKING_FEATURE;
    private static final String UNPARSED_ENTITY_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.UNPARSED_ENTITY_CHECKING_FEATURE;
    private static final String IDENTITY_CONSTRAINT_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.IDC_CHECKING_FEATURE;
    private static final String DISALLOW_DOCTYPE_DECL_FEATURE =
        Constants.XERCES_FEATURE_PREFIX + Constants.DISALLOW_DOCTYPE_DECL_FEATURE;
    private static final String NORMALIZE_DATA =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
    private static final String SCHEMA_ELEMENT_DEFAULT =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    private static final String SCHEMA_AUGMENT_PSVI =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;
    private static final String ENTITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    private static final String ENTITY_RESOLVER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_RESOLVER_PROPERTY;
    private static final String ERROR_HANDLER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    private static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private static final String SYMBOL_TABLE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SYMBOL_TABLE_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    private boolean fConfigUpdated = true;
    private boolean fUseGrammarPoolOnly;
    private final HashMap fComponents = new HashMap();
    private final XMLEntityManager fEntityManager;
    private final XMLErrorReporter fErrorReporter;
    private final NamespaceContext fNamespaceContext;
    private final XMLSchemaValidator fSchemaValidator;
    private final ValidationManager fValidationManager;
    private final HashMap fInitFeatures = new HashMap();
    private final HashMap fInitProperties = new HashMap();
    private final SecurityManager fInitSecurityManager;
    private ErrorHandler fErrorHandler = null;
    private LSResourceResolver fResourceResolver = null;
    private Locale fLocale = null;
    public XMLSchemaValidatorComponentManager(XSGrammarPoolContainer grammarContainer) {
        fEntityManager = new XMLEntityManager();
        fComponents.put(ENTITY_MANAGER, fEntityManager);
        fErrorReporter = new XMLErrorReporter();
        fComponents.put(ERROR_REPORTER, fErrorReporter);
        fNamespaceContext = new NamespaceSupport();
        fComponents.put(NAMESPACE_CONTEXT, fNamespaceContext);
        fSchemaValidator = new XMLSchemaValidator();
        fComponents.put(SCHEMA_VALIDATOR, fSchemaValidator);
        fValidationManager = new ValidationManager();
        fComponents.put(VALIDATION_MANAGER, fValidationManager);
        fComponents.put(ENTITY_RESOLVER, null);
        fComponents.put(ERROR_HANDLER, null);
        fComponents.put(SECURITY_MANAGER, null);
        fComponents.put(SYMBOL_TABLE, new SymbolTable());
        fComponents.put(XMLGRAMMAR_POOL, grammarContainer.getGrammarPool());
        fUseGrammarPoolOnly = grammarContainer.isFullyComposed();
        fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
        final String [] recognizedFeatures = {
                DISALLOW_DOCTYPE_DECL_FEATURE,
                NORMALIZE_DATA,
                SCHEMA_ELEMENT_DEFAULT,
                SCHEMA_AUGMENT_PSVI
        };
        addRecognizedFeatures(recognizedFeatures);
        fFeatures.put(DISALLOW_DOCTYPE_DECL_FEATURE, Boolean.FALSE);
        fFeatures.put(NORMALIZE_DATA, Boolean.FALSE);
        fFeatures.put(SCHEMA_ELEMENT_DEFAULT, Boolean.FALSE);
        fFeatures.put(SCHEMA_AUGMENT_PSVI, Boolean.TRUE);
        addRecognizedParamsAndSetDefaults(fEntityManager, grammarContainer);
        addRecognizedParamsAndSetDefaults(fErrorReporter, grammarContainer);
        addRecognizedParamsAndSetDefaults(fSchemaValidator, grammarContainer);
        Boolean secureProcessing = grammarContainer.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING);
        if (Boolean.TRUE.equals(secureProcessing)) {
            fInitSecurityManager = new SecurityManager();
        }
        else {
            fInitSecurityManager = null;
        }
        fComponents.put(SECURITY_MANAGER, fInitSecurityManager);
        fFeatures.put(IGNORE_XSI_TYPE, Boolean.FALSE);
        fFeatures.put(ID_IDREF_CHECKING, Boolean.TRUE);
        fFeatures.put(IDENTITY_CONSTRAINT_CHECKING, Boolean.TRUE);
        fFeatures.put(UNPARSED_ENTITY_CHECKING, Boolean.TRUE);
    }
    public boolean getFeature(String featureId)
            throws XMLConfigurationException {
        if (PARSER_SETTINGS.equals(featureId)) {
            return fConfigUpdated;
        }
        else if (VALIDATION.equals(featureId) || SCHEMA_VALIDATION.equals(featureId)) {
            return true;
        }
        else if (USE_GRAMMAR_POOL_ONLY.equals(featureId)) {
            return fUseGrammarPoolOnly;
        }
        else if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(featureId)) {
            return getProperty(SECURITY_MANAGER) != null;
        }
        return super.getFeature(featureId);
    }
    public void setFeature(String featureId, boolean value) throws XMLConfigurationException {
        if (PARSER_SETTINGS.equals(featureId)) {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_SUPPORTED, featureId);
        }
        else if (value == false && (VALIDATION.equals(featureId) || SCHEMA_VALIDATION.equals(featureId))) {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_SUPPORTED, featureId);
        }
        else if (USE_GRAMMAR_POOL_ONLY.equals(featureId) && value != fUseGrammarPoolOnly) {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_SUPPORTED, featureId);
        }
        if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(featureId)) {
            setProperty(SECURITY_MANAGER, value ? new SecurityManager() : null);
            return;
        }
        fConfigUpdated = true;
        fEntityManager.setFeature(featureId, value);
        fErrorReporter.setFeature(featureId, value);
        fSchemaValidator.setFeature(featureId, value);
        if (!fInitFeatures.containsKey(featureId)) {
            boolean current = super.getFeature(featureId);
            fInitFeatures.put(featureId, current ? Boolean.TRUE : Boolean.FALSE); 
        }
        super.setFeature(featureId, value);
    }
    public Object getProperty(String propertyId)
            throws XMLConfigurationException {
        if (LOCALE.equals(propertyId)) {
            return getLocale();
        }
        final Object component = fComponents.get(propertyId);
        if (component != null) {
            return component;
        }
        else if (fComponents.containsKey(propertyId)) {
            return null;
        }
        return super.getProperty(propertyId);
    }
    public void setProperty(String propertyId, Object value) throws XMLConfigurationException {
        if ( ENTITY_MANAGER.equals(propertyId) || ERROR_REPORTER.equals(propertyId) ||
             NAMESPACE_CONTEXT.equals(propertyId) || SCHEMA_VALIDATOR.equals(propertyId) ||
             SYMBOL_TABLE.equals(propertyId) || VALIDATION_MANAGER.equals(propertyId) ||
             XMLGRAMMAR_POOL.equals(propertyId)) {
            throw new XMLConfigurationException(XMLConfigurationException.NOT_SUPPORTED, propertyId);
        }
        fConfigUpdated = true;
        fEntityManager.setProperty(propertyId, value);
        fErrorReporter.setProperty(propertyId, value);
        fSchemaValidator.setProperty(propertyId, value);
        if (ENTITY_RESOLVER.equals(propertyId) || ERROR_HANDLER.equals(propertyId) || 
                SECURITY_MANAGER.equals(propertyId)) {
            fComponents.put(propertyId, value);
            return;
        }
        else if (LOCALE.equals(propertyId)) {
            setLocale((Locale) value);
            fComponents.put(propertyId, value);
            return;
        }
        if (!fInitProperties.containsKey(propertyId)) {
            fInitProperties.put(propertyId, super.getProperty(propertyId));
        }
        super.setProperty(propertyId, value);
    }
    public void addRecognizedParamsAndSetDefaults(XMLComponent component, XSGrammarPoolContainer grammarContainer) {
        final String[] recognizedFeatures = component.getRecognizedFeatures();
        addRecognizedFeatures(recognizedFeatures);
        final String[] recognizedProperties = component.getRecognizedProperties();
        addRecognizedProperties(recognizedProperties);
        setFeatureDefaults(component, recognizedFeatures, grammarContainer);
        setPropertyDefaults(component, recognizedProperties);
    }
    public void reset() throws XNIException {
        fNamespaceContext.reset();
        fValidationManager.reset();
        fEntityManager.reset(this);
        fErrorReporter.reset(this);
        fSchemaValidator.reset(this);
        fConfigUpdated = false;
    }
    void setErrorHandler(ErrorHandler errorHandler) {
        fErrorHandler = errorHandler;
        setProperty(ERROR_HANDLER, (errorHandler != null) ? new ErrorHandlerWrapper(errorHandler) : 
                new ErrorHandlerWrapper(DraconianErrorHandler.getInstance()));
    }
    ErrorHandler getErrorHandler() {
        return fErrorHandler;
    }
    void setResourceResolver(LSResourceResolver resourceResolver) {
        fResourceResolver = resourceResolver;
        setProperty(ENTITY_RESOLVER, new DOMEntityResolverWrapper(resourceResolver));
    }
    LSResourceResolver getResourceResolver() {
        return fResourceResolver;
    }
    void setLocale(Locale locale) {
        fLocale = locale;
        fErrorReporter.setLocale(locale);
    }
    Locale getLocale() {
        return fLocale;
    }
    void restoreInitialState() {
        fConfigUpdated = true;
        fComponents.put(ENTITY_RESOLVER, null);
        fComponents.put(ERROR_HANDLER, null);
        fComponents.put(SECURITY_MANAGER, fInitSecurityManager);
        setLocale(null);
        fComponents.put(LOCALE, null);
        if (!fInitFeatures.isEmpty()) {
            Iterator iter = fInitFeatures.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                boolean value = ((Boolean) entry.getValue()).booleanValue();
                super.setFeature(name, value);
            }
            fInitFeatures.clear();
        }
        if (!fInitProperties.isEmpty()) {
            Iterator iter = fInitProperties.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                super.setProperty(name, value);
            }
            fInitProperties.clear();
        }
    }
    private void setFeatureDefaults(final XMLComponent component, 
            final String [] recognizedFeatures, XSGrammarPoolContainer grammarContainer) {
        if (recognizedFeatures != null) {
            for (int i = 0; i < recognizedFeatures.length; ++i) {
                String featureId = recognizedFeatures[i];
                Boolean state = grammarContainer.getFeature(featureId);
                if (state == null) {
                    state = component.getFeatureDefault(featureId);
                }
                if (state != null) {
                    if (!fFeatures.containsKey(featureId)) {
                        fFeatures.put(featureId, state);
                        fConfigUpdated = true;
                    }
                }
            }
        }
    }
    private void setPropertyDefaults(final XMLComponent component, final String [] recognizedProperties) {
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
} 
