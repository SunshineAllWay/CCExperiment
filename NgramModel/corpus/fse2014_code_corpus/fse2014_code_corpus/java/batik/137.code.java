package org.apache.batik.bridge;
import org.apache.batik.util.ParsedURL;
public class EmbededScriptSecurity implements ScriptSecurity {
    public static final String DATA_PROTOCOL = "data";
    public static final String ERROR_CANNOT_ACCESS_DOCUMENT_URL
        = "DefaultScriptSecurity.error.cannot.access.document.url";
    public static final String ERROR_SCRIPT_NOT_EMBEDED
        = "EmbededScriptSecurity.error.script.not.embeded";
    protected SecurityException se;
    public void checkLoadScript(){
        if (se != null) {
            throw se;
        }
    }
    public EmbededScriptSecurity(String scriptType,
                                 ParsedURL scriptURL,
                                 ParsedURL docURL){
        if (docURL == null) {
            se = new SecurityException
                (Messages.formatMessage(ERROR_CANNOT_ACCESS_DOCUMENT_URL,
                                        new Object[]{scriptURL}));
        } else {
            if ( !docURL.equals(scriptURL)
                 &&
                 (scriptURL == null
                  ||
                  !DATA_PROTOCOL.equals(scriptURL.getProtocol()) )) {
                se = new SecurityException
                    (Messages.formatMessage(ERROR_SCRIPT_NOT_EMBEDED,
                                            new Object[]{scriptURL}));
            }
        }
    }
}
