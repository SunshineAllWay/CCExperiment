package org.apache.batik.i18n;
import java.util.Locale;
import java.util.ResourceBundle;
public interface ExtendedLocalizable extends Localizable {
    void setLocaleGroup(LocaleGroup lg);
    LocaleGroup getLocaleGroup();
    void setDefaultLocale(Locale l);
    Locale getDefaultLocale();
    ResourceBundle getResourceBundle();
}
