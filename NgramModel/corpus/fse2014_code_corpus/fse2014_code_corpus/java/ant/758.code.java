package org.apache.tools.ant.types.selectors.modifiedselector;
import java.util.Iterator;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;
import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
public class PropertiesfileCache implements Cache {
    private File cachefile = null;
    private Properties cache = new Properties();
    private boolean cacheLoaded = false;
    private boolean cacheDirty  = true;
    public PropertiesfileCache() {
    }
    public PropertiesfileCache(File cachefile) {
        this.cachefile = cachefile;
    }
    public void setCachefile(File file) {
        cachefile = file;
    }
    public File getCachefile() {
        return cachefile;
    }
    public boolean isValid() {
        return (cachefile != null);
    }
    public void load() {
        if ((cachefile != null) && cachefile.isFile() && cachefile.canRead()) {
            try {
                BufferedInputStream bis = new BufferedInputStream(
                    new FileInputStream(cachefile));
                cache.load(bis);
                bis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheLoaded = true;
        cacheDirty  = false;
    }
    public void save() {
        if (!cacheDirty) {
            return;
        }
        if ((cachefile != null) && cache.propertyNames().hasMoreElements()) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(
                      new FileOutputStream(cachefile));
                cache.store(bos, null);
                bos.flush();
                bos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cacheDirty = false;
    }
    public void delete() {
        cache = new Properties();
        cachefile.delete();
        cacheLoaded = true;
        cacheDirty = false;
    }
    public Object get(Object key) {
        if (!cacheLoaded) {
            load();
        }
        try {
            return cache.getProperty(String.valueOf(key));
        } catch (ClassCastException e) {
            return null;
        }
    }
    public void put(Object key, Object value) {
        cache.put(String.valueOf(key), String.valueOf(value));
        cacheDirty = true;
    }
    public Iterator iterator() {
        Vector v = new java.util.Vector();
        Enumeration en = cache.propertyNames();
        while (en.hasMoreElements()) {
            v.add(en.nextElement());
        }
        return v.iterator();
    }
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("<PropertiesfileCache:");
        buf.append("cachefile=").append(cachefile);
        buf.append(";noOfEntries=").append(cache.size());
        buf.append(">");
        return buf.toString();
    }
}
