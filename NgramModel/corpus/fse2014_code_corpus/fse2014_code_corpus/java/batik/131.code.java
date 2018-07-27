package org.apache.batik.bridge;
import org.apache.batik.util.ParsedURL;
public class DefaultScriptSecurity implements ScriptSecurity {
    public static final String DATA_PROTOCOL = "data";
    public static final String ERROR_CANNOT_ACCESS_DOCUMENT_URL
        = "DefaultScriptSecurity.error.cannot.access.document.url";
    public static final String ERROR_SCRIPT_FROM_DIFFERENT_URL
        = "DefaultScriptSecurity.error.script.from.different.url";
    protected SecurityException se;
    public void checkLoadScript(){
        if (se != null) {
            throw se;
        }
    }
    public DefaultScriptSecurity(String scriptType,
                                 ParsedURL scriptURL,
                                 ParsedURL docURL){
        if (docURL == null) {
            se = new SecurityException
                (Messages.formatMessage(ERROR_CANNOT_ACCESS_DOCUMENT_URL,
                                        new Object[]{scriptURL}));
        } else {
            String docHost    = docURL.getHost();
            String scriptHost = scriptURL.getHost();
            if ((docHost != scriptHost) &&
                ((docHost == null) || (!docHost.equals(scriptHost)))) {
                if ( !docURL.equals(scriptURL)
                     &&
                     (scriptURL == null
                      ||
                      !DATA_PROTOCOL.equals(scriptURL.getProtocol()) )) {
                    se = new SecurityException
                        (Messages.formatMessage(ERROR_SCRIPT_FROM_DIFFERENT_URL,
                                                new Object[]{scriptURL}));
                }
            }
        }
    }
}
