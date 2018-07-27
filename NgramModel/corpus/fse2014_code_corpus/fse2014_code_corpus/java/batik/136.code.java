package org.apache.batik.bridge;
import org.apache.batik.util.ParsedURL;
public class EmbededExternalResourceSecurity implements ExternalResourceSecurity {
    public static final String DATA_PROTOCOL = "data";
    public static final String ERROR_EXTERNAL_RESOURCE_NOT_EMBEDED
        = "EmbededExternalResourceSecurity.error.external.resource.not.embeded";
    protected SecurityException se;
    public void checkLoadExternalResource(){
        if (se != null) {
            throw se;
        }
    }
    public EmbededExternalResourceSecurity(ParsedURL externalResourceURL){
        if ( externalResourceURL == null
             ||
             !DATA_PROTOCOL.equals(externalResourceURL.getProtocol()) ) {
            se = new SecurityException
                (Messages.formatMessage(ERROR_EXTERNAL_RESOURCE_NOT_EMBEDED,
                                        new Object[]{externalResourceURL}));
        }
    }
}
