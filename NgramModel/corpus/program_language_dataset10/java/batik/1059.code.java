package org.apache.batik.i18n;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
public class LocalizableSupport implements Localizable {
    protected LocaleGroup localeGroup = LocaleGroup.DEFAULT;
    protected String bundleName;
    protected ClassLoader classLoader;
    protected Locale locale;
    protected Locale usedLocale;
    List resourceBundles = new ArrayList();
    Class lastResourceClass;
    Class cls;
    public LocalizableSupport(String s, Class cls) {
        this(s, cls, null);
    }
    public LocalizableSupport(String s, Class cls, ClassLoader cl) {
        bundleName = s;
        this.cls = cls;
        classLoader = cl;
    }
    public LocalizableSupport(String s) {
        this(s, (ClassLoader)null);
    }
    public LocalizableSupport(String s, ClassLoader cl) {
        bundleName = s;
        classLoader = cl;
    }
    public void setLocale(Locale l) {
        if (locale != l) {
            locale = l;
            resourceBundles.clear();
            lastResourceClass = null;
        }
    }
    public Locale getLocale() {
        return locale;
    }
    public void setLocaleGroup(LocaleGroup lg) {
        localeGroup = lg;
    }
    public LocaleGroup getLocaleGroup() {
        return localeGroup;
    }
    public void setDefaultLocale(Locale l) {
        localeGroup.setLocale(l);
    }
    public Locale getDefaultLocale() {
        return localeGroup.getLocale();
    }
    public String formatMessage(String key, Object[] args) {
        return MessageFormat.format(getString(key), args);
    }
    protected Locale getCurrentLocale() {
        if (locale != null) return locale;
        Locale l = localeGroup.getLocale();
        if (l != null) return l;
        return Locale.getDefault();
    }
    protected boolean setUsedLocale() {
        Locale l = getCurrentLocale();
        if (usedLocale == l) return false;
        usedLocale = l;
        resourceBundles.clear();
        lastResourceClass = null;
        return true;
    }
    public ResourceBundle getResourceBundle() {
        return getResourceBundle(0);
    }
    protected boolean hasNextResourceBundle(int i) {
        if (i == 0) return true;
        if (i < resourceBundles.size()) return true;
        if (lastResourceClass == null) return false;
        if (lastResourceClass == Object.class) return false;
        return true;
    }
    protected ResourceBundle lookupResourceBundle(String bundle,
                                                  Class theClass){
        ClassLoader cl = classLoader;
        ResourceBundle rb=null;
        if (cl != null) {
            try {
                rb = ResourceBundle.getBundle(bundle, usedLocale, cl);
            } catch (MissingResourceException mre) {
            }
            if (rb != null)
                return rb;
        }
        if (theClass != null) {
            try {
                cl = theClass.getClassLoader();
            } catch (SecurityException se) {
            }
        }
        if (cl == null)
            cl = getClass().getClassLoader();
        try {
            rb = ResourceBundle.getBundle(bundle, usedLocale, cl);
        } catch (MissingResourceException mre) {
        }
        return rb;
    }
    protected ResourceBundle getResourceBundle(int i) {
        setUsedLocale();
        ResourceBundle rb=null;
        if (cls == null) {
            if (resourceBundles.size() == 0) {
                rb = lookupResourceBundle(bundleName, null);
                resourceBundles.add(rb);
            }
            return (ResourceBundle)resourceBundles.get(0);
        }
        while (i >= resourceBundles.size()) {
            if (lastResourceClass == Object.class)
                return null;
            if (lastResourceClass == null)
                lastResourceClass = cls;
            else
                lastResourceClass = lastResourceClass.getSuperclass();
            Class cl = lastResourceClass;
            String bundle = (cl.getPackage().getName() + "." + bundleName);
            resourceBundles.add(lookupResourceBundle(bundle, cl));
        }
        return (ResourceBundle)resourceBundles.get(i);
    }
    public String getString(String key) throws MissingResourceException {
        setUsedLocale();
        for (int i=0; hasNextResourceBundle(i); i++) {
            ResourceBundle rb = getResourceBundle(i);
            if (rb == null) continue;
            try {
                String ret = rb.getString(key);
                if (ret != null) return ret;
            } catch (MissingResourceException mre) {
            }
        }
        String classStr = (cls != null)?cls.toString():bundleName;
        throw new MissingResourceException("Unable to find resource: " + key,
                                           classStr, key);
    }
    public int getInteger(String key)
        throws MissingResourceException {
        String i = getString(key);
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            throw new MissingResourceException
                ("Malformed integer", bundleName, key);
        }
    }
    public int getCharacter(String key)
        throws MissingResourceException {
        String s = getString(key);
        if(s == null || s.length() == 0){
            throw new MissingResourceException
                ("Malformed character", bundleName, key);
        }
        return s.charAt(0);
    }
}
