package org.apache.xerces.xni.grammars;
import java.io.IOException;
import java.util.Locale;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
public interface XMLGrammarLoader {
    public String[] getRecognizedFeatures();
    public boolean getFeature(String featureId) 
            throws XMLConfigurationException;
    public void setFeature(String featureId,
                boolean state) throws XMLConfigurationException;
    public String[] getRecognizedProperties();
    public Object getProperty(String propertyId) 
            throws XMLConfigurationException;
    public void setProperty(String propertyId,
                Object state) throws XMLConfigurationException;
    public void setLocale(Locale locale); 
    public Locale getLocale();
    public void setErrorHandler(XMLErrorHandler errorHandler);
    public XMLErrorHandler getErrorHandler();
    public void setEntityResolver(XMLEntityResolver entityResolver);
    public XMLEntityResolver getEntityResolver();
    public Grammar loadGrammar(XMLInputSource source)
        throws IOException, XNIException;
} 
