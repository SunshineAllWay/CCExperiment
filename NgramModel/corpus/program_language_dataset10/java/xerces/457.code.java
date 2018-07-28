package org.apache.xerces.impl.xs.models;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.dtd.models.CMNode;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.SecurityManager;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
public class CMNodeFactory {
    private static final String ERROR_REPORTER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    private static final String SECURITY_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY;
    private static final boolean DEBUG = false ;
    private static final int MULTIPLICITY = 1 ;
    private int nodeCount = 0;
    private int maxNodeLimit ;
    private XMLErrorReporter fErrorReporter;
    private SecurityManager fSecurityManager = null;
    public CMNodeFactory() {
    }
    public void reset(XMLComponentManager componentManager) {
        fErrorReporter = (XMLErrorReporter)componentManager.getProperty(ERROR_REPORTER);
        try {
            fSecurityManager = (SecurityManager)componentManager.getProperty(SECURITY_MANAGER);
            reset();
        }
        catch (XMLConfigurationException e) {
            fSecurityManager = null;
        }
    }
    public void reset() {
        if (fSecurityManager != null) {
            maxNodeLimit = fSecurityManager.getMaxOccurNodeLimit() * MULTIPLICITY;
        }
    }
    public CMNode getCMLeafNode(int type, Object leaf, int id, int position) {
        nodeCountCheck();
        return new XSCMLeaf(type, leaf, id, position) ;
    }
    public CMNode getCMRepeatingLeafNode(int type, Object leaf, 
            int minOccurs, int maxOccurs, int id, int position) {
        nodeCountCheck();
        return new XSCMRepeatingLeaf(type, leaf, minOccurs, maxOccurs, id, position);
    }
    public CMNode getCMUniOpNode(int type, CMNode childNode) {
        nodeCountCheck();
        return new XSCMUniOp(type, childNode) ;
    }
    public CMNode getCMBinOpNode(int type, CMNode leftNode, CMNode rightNode) {
        nodeCountCheck() ;
        return new XSCMBinOp(type, leftNode, rightNode) ;
    }
    public void nodeCountCheck(){
        if( fSecurityManager != null && nodeCount++ > maxNodeLimit){
            if(DEBUG){
                System.out.println("nodeCount = " + nodeCount ) ;
                System.out.println("nodeLimit = " + maxNodeLimit ) ;
            }
            fErrorReporter.reportError(XSMessageFormatter.SCHEMA_DOMAIN, "maxOccurLimit", new Object[]{ new Integer(maxNodeLimit) }, XMLErrorReporter.SEVERITY_FATAL_ERROR);
            nodeCount = 0;
        }
    }
    public void resetNodeCount(){
        nodeCount = 0 ;
    }
    public void setProperty(String propertyId, Object value)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
        	final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SECURITY_MANAGER_PROPERTY.length() && 
                propertyId.endsWith(Constants.SECURITY_MANAGER_PROPERTY)) {
                fSecurityManager = (SecurityManager)value;                
                maxNodeLimit = (fSecurityManager != null) ? fSecurityManager.getMaxOccurNodeLimit() * MULTIPLICITY : 0 ;
                return;
            }
            if (suffixLength == Constants.ERROR_REPORTER_PROPERTY.length() && 
                propertyId.endsWith(Constants.ERROR_REPORTER_PROPERTY)) {
                fErrorReporter = (XMLErrorReporter)value;
                return;
            }
        }
    } 
}
