package org.apache.xerces.jaxp;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.jaxp.validation.XSGrammarPoolContainer;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
final class SchemaValidatorConfiguration implements XMLComponentManager {
    private static final String SCHEMA_VALIDATION =
        Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    private static final String VALIDATION =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String USE_GRAMMAR_POOL_ONLY =
        Constants.XERCES_FEATURE_PREFIX + Constants.USE_GRAMMAR_POOL_ONLY_FEATURE;
    private static final String PARSER_SETTINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    private static final String XMLGRAMMAR_POOL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private final XMLComponentManager fParentComponentManager;
    private final XMLGrammarPool fGrammarPool;
    private final boolean fUseGrammarPoolOnly;
    private final ValidationManager fValidationManager;
    public SchemaValidatorConfiguration(XMLComponentManager parentManager, 
            XSGrammarPoolContainer grammarContainer, ValidationManager validationManager) {
        fParentComponentManager = parentManager;
        fGrammarPool = grammarContainer.getGrammarPool();
        fUseGrammarPoolOnly = grammarContainer.isFullyComposed();
        fValidationManager = validationManager;
        try {
            XMLErrorReporter errorReporter = (XMLErrorReporter) fParentComponentManager.getProperty(ERROR_REPORTER);
            if (errorReporter != null) {
                errorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new XSMessageFormatter());
            }
        }
        catch (XMLConfigurationException exc) {}
    }
    public boolean getFeature(String featureId)
            throws XMLConfigurationException {
        if (PARSER_SETTINGS.equals(featureId)) {
            return fParentComponentManager.getFeature(featureId);
        }
        else if (VALIDATION.equals(featureId) || SCHEMA_VALIDATION.equals(featureId)) {
            return true;
        }
        else if (USE_GRAMMAR_POOL_ONLY.equals(featureId)) {
            return fUseGrammarPoolOnly;
        }
        return fParentComponentManager.getFeature(featureId);
    }
    public Object getProperty(String propertyId)
            throws XMLConfigurationException {
        if (XMLGRAMMAR_POOL.equals(propertyId)) {
            return fGrammarPool;
        }
        else if (VALIDATION_MANAGER.equals(propertyId)) {
            return fValidationManager;
        }
        return fParentComponentManager.getProperty(propertyId);
    }
}
