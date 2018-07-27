package org.apache.batik.util.resources;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
public class ResourceManager {
    protected ResourceBundle bundle;
    public ResourceManager(ResourceBundle rb) {
        bundle = rb;
    }
    public String getString(String key)
        throws MissingResourceException {
        return bundle.getString(key);
    }
    public List getStringList(String key)
        throws MissingResourceException {
        return getStringList(key, " \t\n\r\f", false);
    }
    public List getStringList(String key, String delim)
        throws MissingResourceException {
        return getStringList(key, delim, false);
    }
    public List getStringList(String key, String delim, boolean returnDelims) 
        throws MissingResourceException {
        List            result = new ArrayList();
        StringTokenizer st     = new StringTokenizer(getString(key),
                                                     delim,
                                                     returnDelims);
        while (st.hasMoreTokens()) {
            result.add(st.nextToken());
        }
        return result;
    }
    public boolean getBoolean(String key)
        throws MissingResourceException, ResourceFormatException {
        String b = getString(key);
        if (b.equals("true")) {
            return true;
        } else if (b.equals("false")) {
            return false;
        } else {
            throw new ResourceFormatException("Malformed boolean",
                                              bundle.getClass().getName(),
                                              key);
        }
    }
    public int getInteger(String key)
        throws MissingResourceException, ResourceFormatException {
        String i = getString(key);
        try {
            return Integer.parseInt(i);
        } catch (NumberFormatException e) {
            throw new ResourceFormatException("Malformed integer",
                                              bundle.getClass().getName(),
                                              key);
        }
    }
    public int getCharacter(String key)
        throws MissingResourceException, ResourceFormatException {
        String s = getString(key);
        if(s == null || s.length() == 0){
            throw new ResourceFormatException("Malformed character",
                                              bundle.getClass().getName(),
                                              key);
        }
        return s.charAt(0);
    }
}
