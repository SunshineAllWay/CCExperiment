package org.apache.xerces.util;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
public class ParserConfigurationSettings
    implements XMLComponentManager {
	protected static final String PARSER_SETTINGS = 
			Constants.XERCES_FEATURE_PREFIX + Constants.PARSER_SETTINGS;	
    protected ArrayList fRecognizedProperties;
    protected HashMap fProperties;
    protected ArrayList fRecognizedFeatures;
    protected HashMap fFeatures;
    protected XMLComponentManager fParentSettings;
    public ParserConfigurationSettings() {
        this(null);
    } 
    public ParserConfigurationSettings(XMLComponentManager parent) {
        fRecognizedFeatures = new ArrayList();
        fRecognizedProperties = new ArrayList();
        fFeatures = new HashMap();
        fProperties = new HashMap();
        fParentSettings = parent;
    } 
    public void addRecognizedFeatures(String[] featureIds) {
        int featureIdsCount = featureIds != null ? featureIds.length : 0;
        for (int i = 0; i < featureIdsCount; i++) {
            String featureId = featureIds[i];
            if (!fRecognizedFeatures.contains(featureId)) {
                fRecognizedFeatures.add(featureId);
            }
        }
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        checkFeature(featureId);
        fFeatures.put(featureId, state ? Boolean.TRUE : Boolean.FALSE);
    } 
    public void addRecognizedProperties(String[] propertyIds) {
        int propertyIdsCount = propertyIds != null ? propertyIds.length : 0;
        for (int i = 0; i < propertyIdsCount; i++) {
            String propertyId = propertyIds[i];
            if (!fRecognizedProperties.contains(propertyId)) {
                fRecognizedProperties.add(propertyId);
            }
        }
    } 
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        checkProperty(propertyId);
        fProperties.put(propertyId, value);
    } 
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
        Boolean state = (Boolean) fFeatures.get(featureId);
        if (state == null) {
            checkFeature(featureId);
            return false;
        }
        return state.booleanValue();
    } 
    public Object getProperty(String propertyId)
        throws XMLConfigurationException {
        Object propertyValue = fProperties.get(propertyId);
        if (propertyValue == null) {
            checkProperty(propertyId);
        }
        return propertyValue;
    } 
    protected void checkFeature(String featureId)
        throws XMLConfigurationException {
        if (!fRecognizedFeatures.contains(featureId)) {
            if (fParentSettings != null) {
                fParentSettings.getFeature(featureId);
            }
            else {
                short type = XMLConfigurationException.NOT_RECOGNIZED;
                throw new XMLConfigurationException(type, featureId);
            }
        }
    } 
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {
        if (!fRecognizedProperties.contains(propertyId)) {
            if (fParentSettings != null) {
                fParentSettings.getProperty(propertyId);
            }
            else {
                short type = XMLConfigurationException.NOT_RECOGNIZED;
                throw new XMLConfigurationException(type, propertyId);
            }
        }
    } 
} 
