package org.apache.batik.script.rhino;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
public class Messages {
    protected Messages() { }
    protected static final String RESOURCES =
        "org.apache.batik.script.rhino.resources.messages";
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES, Messages.class.getClassLoader());
    public static void setLocale(Locale l) {
        localizableSupport.setLocale(l);
    }
    public static Locale getLocale() {
        return localizableSupport.getLocale();
    }
    public static String formatMessage(String key, Object[] args)
        throws MissingResourceException {
        return localizableSupport.formatMessage(key, args);
    }
    public static String getString(String key)
        throws MissingResourceException {
        return localizableSupport.getString(key);
    }
    public static int getInteger(String key)
        throws MissingResourceException {
        return localizableSupport.getInteger(key);
    }
    public static int getCharacter(String key)
        throws MissingResourceException {
        return localizableSupport.getCharacter(key);
    }
}
