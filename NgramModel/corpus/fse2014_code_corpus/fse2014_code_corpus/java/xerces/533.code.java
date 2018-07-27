package org.apache.xerces.jaxp.validation;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
final class JAXPValidationMessageFormatter {
    public static String formatMessage(Locale locale, 
        String key, Object[] arguments)
        throws MissingResourceException {
        if (locale == null) {
            locale = Locale.getDefault();
        }
        final ResourceBundle resourceBundle = 
            ResourceBundle.getBundle("org.apache.xerces.impl.msg.JAXPValidationMessages", locale);
        String msg;
        try {
            msg = resourceBundle.getString(key);
            if (arguments != null) {
                try {
                    msg = java.text.MessageFormat.format(msg, arguments);
                } 
                catch (Exception e) {
                    msg = resourceBundle.getString("FormatFailed");
                    msg += " " + resourceBundle.getString(key);
                }
            } 
        }
        catch (MissingResourceException e) {
            msg = resourceBundle.getString("BadMessageKey");
            throw new MissingResourceException(key, msg, key);
        }
        if (msg == null) {
            msg = key;
            if (arguments.length > 0) {
                StringBuffer str = new StringBuffer(msg);
                str.append('?');
                for (int i = 0; i < arguments.length; i++) {
                    if (i > 0) {
                        str.append('&');
                    }
                    str.append(String.valueOf(arguments[i]));
                }
            }
        }
        return msg;
    }
}
