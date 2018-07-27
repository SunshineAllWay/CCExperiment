package org.apache.batik.ext.awt.geom;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.i18n.LocalizableSupport;
public class Messages {
    protected Messages() { }
    protected static final String RESOURCES =
        "org.apache.batik.ext.awt.geom.resources.TestMessages";
    protected static LocalizableSupport localizableSupport =
        new LocalizableSupport(RESOURCES);
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
}
