package org.apache.batik.bridge;
import org.apache.batik.util.ParsedURL;
public class DefaultExternalResourceSecurity implements ExternalResourceSecurity {
    public static final String DATA_PROTOCOL = "data";
    public static final String ERROR_CANNOT_ACCESS_DOCUMENT_URL
        = "DefaultExternalResourceSecurity.error.cannot.access.document.url";
    public static final String ERROR_EXTERNAL_RESOURCE_FROM_DIFFERENT_URL
        = "DefaultExternalResourceSecurity.error.external.resource.from.different.url";
    protected SecurityException se;
    public void checkLoadExternalResource(){
        if (se != null) {
            se.fillInStackTrace();
            throw se;
        }
    }
    public DefaultExternalResourceSecurity(ParsedURL externalResourceURL,
                                           ParsedURL docURL){
        if (docURL == null) {
            se = new SecurityException
                (Messages.formatMessage(ERROR_CANNOT_ACCESS_DOCUMENT_URL,
                                        new Object[]{externalResourceURL}));
        } else {
            String docHost    = docURL.getHost();
            String externalResourceHost = externalResourceURL.getHost();
            if ((docHost != externalResourceHost) &&
                ((docHost == null) || (!docHost.equals(externalResourceHost)))){
                if ( externalResourceURL == null
                     ||
                     !DATA_PROTOCOL.equals(externalResourceURL.getProtocol()) ) {
                se = new SecurityException
                    (Messages.formatMessage(ERROR_EXTERNAL_RESOURCE_FROM_DIFFERENT_URL,
                                            new Object[]{externalResourceURL}));
                }
            }
        }
    }
}
