package org.apache.batik.bridge;
public class NoLoadScriptSecurity implements ScriptSecurity {
    public static final String ERROR_NO_SCRIPT_OF_TYPE_ALLOWED
        = "NoLoadScriptSecurity.error.no.script.of.type.allowed";
    protected SecurityException se;
    public void checkLoadScript(){
        throw se;
    }
    public NoLoadScriptSecurity(String scriptType){
        se = new SecurityException
            (Messages.formatMessage(ERROR_NO_SCRIPT_OF_TYPE_ALLOWED,
                                    new Object[]{scriptType}));
    }
}
