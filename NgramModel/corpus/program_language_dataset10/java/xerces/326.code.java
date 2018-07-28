package org.apache.xerces.impl.dv;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
public class DatatypeException extends Exception {
    static final long serialVersionUID = 1940805832730465578L;
    protected final String key;
    protected final Object[] args;
    public DatatypeException(String key, Object[] args) {
        super(key);
        this.key = key;
        this.args = args;
    }
    public String getKey() {
        return key;
    }
    public Object[] getArgs() {
        return args;
    }
    public String getMessage() {
        ResourceBundle resourceBundle = null;
        resourceBundle = ResourceBundle.getBundle("org.apache.xerces.impl.msg.XMLSchemaMessages");
        if (resourceBundle == null)
            throw new MissingResourceException("Property file not found!", "org.apache.xerces.impl.msg.XMLSchemaMessages", key);
        String msg = resourceBundle.getString(key);
        if (msg == null) {
            msg = resourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(msg, "org.apache.xerces.impl.msg.XMLSchemaMessages", key);
        }
        if (args != null) {
            try {
                msg = java.text.MessageFormat.format(msg, args);
            } catch (Exception e) {
                msg = resourceBundle.getString("FormatFailed");
                msg += " " + resourceBundle.getString(key);
            }
        } 
        return msg;
    }
}
