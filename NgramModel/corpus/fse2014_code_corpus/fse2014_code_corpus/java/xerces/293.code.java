package org.apache.xerces.impl;
import java.util.Hashtable;
import java.util.Locale;
import org.apache.xerces.util.DefaultErrorHandler;
import org.apache.xerces.util.ErrorHandlerProxy;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
public class XMLErrorReporter
    implements XMLComponent {
    public static final short SEVERITY_WARNING = 0;
    public static final short SEVERITY_ERROR = 1;
    public static final short SEVERITY_FATAL_ERROR = 2;
    protected static final String CONTINUE_AFTER_FATAL_ERROR =
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String ERROR_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_HANDLER_PROPERTY;
    private static final String[] RECOGNIZED_FEATURES = {
        CONTINUE_AFTER_FATAL_ERROR,
    };
    private static final Boolean[] FEATURE_DEFAULTS = {
        null,
    };
    private static final String[] RECOGNIZED_PROPERTIES = {
        ERROR_HANDLER,
    };
    private static final Object[] PROPERTY_DEFAULTS = {
        null,
    };
    protected Locale fLocale;
    protected Hashtable fMessageFormatters;
    protected XMLErrorHandler fErrorHandler;
    protected XMLLocator fLocator;
    protected boolean fContinueAfterFatalError;
    protected XMLErrorHandler fDefaultErrorHandler;
    private ErrorHandler fSaxProxy = null;
    public XMLErrorReporter() {
        fMessageFormatters = new Hashtable();
    } 
    public void setLocale(Locale locale) {
        fLocale = locale;
    } 
    public Locale getLocale() {
        return fLocale ;
    } 
    public void setDocumentLocator(XMLLocator locator) {
        fLocator = locator;
    } 
    public void putMessageFormatter(String domain, 
                                    MessageFormatter messageFormatter) {
        fMessageFormatters.put(domain, messageFormatter);
    } 
    public MessageFormatter getMessageFormatter(String domain) {
        return (MessageFormatter)fMessageFormatters.get(domain);
    } 
    public MessageFormatter removeMessageFormatter(String domain) {
        return (MessageFormatter) fMessageFormatters.remove(domain);
    } 
    public String reportError(String domain, String key, Object[] arguments, 
                            short severity) throws XNIException {
        return reportError(fLocator, domain, key, arguments, severity);
    } 
    public String reportError(String domain, String key, Object[] arguments, 
            short severity, Exception exception) throws XNIException {
        return reportError(fLocator, domain, key, arguments, severity, exception);
    } 
    public String reportError(XMLLocator location,
            String domain, String key, Object[] arguments, 
            short severity) throws XNIException {
        return reportError(location, domain, key, arguments, severity, null);
    } 
    public String reportError(XMLLocator location,
                            String domain, String key, Object[] arguments, 
                            short severity, Exception exception) throws XNIException {
        MessageFormatter messageFormatter = getMessageFormatter(domain);
        String message;
        if (messageFormatter != null) {
            message = messageFormatter.formatMessage(fLocale, key, arguments);
        }
        else {
            StringBuffer str = new StringBuffer();
            str.append(domain);
            str.append('#');
            str.append(key);
            int argCount = arguments != null ? arguments.length : 0;
            if (argCount > 0) {
                str.append('?');
                for (int i = 0; i < argCount; i++) {
                    str.append(arguments[i]);
                    if (i < argCount -1) {
                        str.append('&');
                    }
                }
            }
            message = str.toString();
        }
        XMLParseException parseException = (exception != null) ?
            new XMLParseException(location, message, exception) :
            new XMLParseException(location, message);
        XMLErrorHandler errorHandler = fErrorHandler;
        if (errorHandler == null) {
            if (fDefaultErrorHandler == null) {
                fDefaultErrorHandler = new DefaultErrorHandler();
            }
            errorHandler = fDefaultErrorHandler;
        }
        switch (severity) {
            case SEVERITY_WARNING: {
                errorHandler.warning(domain, key, parseException);
                break;
            }
            case SEVERITY_ERROR: {
                errorHandler.error(domain, key, parseException);
                break;
            }
            case SEVERITY_FATAL_ERROR: {
                errorHandler.fatalError(domain, key, parseException);
                if (!fContinueAfterFatalError) {
                    throw parseException;
                }
                break;
            }
        }
        return message;
    } 
    public void reset(XMLComponentManager componentManager)
        throws XNIException {
        try {
            fContinueAfterFatalError = componentManager.getFeature(CONTINUE_AFTER_FATAL_ERROR);
        }
        catch (XNIException e) {
            fContinueAfterFatalError = false;
        }
        fErrorHandler = (XMLErrorHandler)componentManager.getProperty(ERROR_HANDLER);
    } 
    public String[] getRecognizedFeatures() {
        return (String[])(RECOGNIZED_FEATURES.clone());
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE.length() && 
                featureId.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)) {
                fContinueAfterFatalError = state;
            }
        }
    } 
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
        	final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE.length() && 
                featureId.endsWith(Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE)) {
                return fContinueAfterFatalError ;
            }
        }
        return false;
    } 
    public String[] getRecognizedProperties() {
        return (String[])(RECOGNIZED_PROPERTIES.clone());
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.ERROR_HANDLER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ERROR_HANDLER_PROPERTY)) {
                fErrorHandler = (XMLErrorHandler)value;
            }
        }
    } 
    public Boolean getFeatureDefault(String featureId) {
        for (int i = 0; i < RECOGNIZED_FEATURES.length; i++) {
            if (RECOGNIZED_FEATURES[i].equals(featureId)) {
                return FEATURE_DEFAULTS[i];
            }
        }
        return null;
    } 
    public Object getPropertyDefault(String propertyId) {
        for (int i = 0; i < RECOGNIZED_PROPERTIES.length; i++) {
            if (RECOGNIZED_PROPERTIES[i].equals(propertyId)) {
                return PROPERTY_DEFAULTS[i];
            }
        }
        return null;
    } 
    public XMLErrorHandler getErrorHandler() {
        return fErrorHandler;
    }
    public ErrorHandler getSAXErrorHandler() {
        if (fSaxProxy == null) {
            fSaxProxy = new ErrorHandlerProxy() {
                protected XMLErrorHandler getErrorHandler() {
                    return fErrorHandler;
                }
            };
        }
        return fSaxProxy;
    }
} 
