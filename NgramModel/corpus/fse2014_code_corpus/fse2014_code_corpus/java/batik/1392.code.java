package org.apache.batik.util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
public class PreferenceManager
{
    protected Properties internal = null;
    protected Map defaults = null;
    protected String prefFileName = null;
    protected String fullName = null;
    protected static final String USER_HOME = getSystemProperty("user.home");
    protected static final String USER_DIR  = getSystemProperty("user.dir");
    protected static final String FILE_SEP  = getSystemProperty("file.separator");
    private static String PREF_DIR = null;
    protected static String getSystemProperty(String prop){
        try{
            return System.getProperty(prop);
        }catch(AccessControlException e){
            return "";
        }
    }
    public PreferenceManager(String prefFileName)
    {
        this(prefFileName, null);
    }
    public PreferenceManager(String prefFileName, Map defaults)
    {
        this.prefFileName = prefFileName;
        this.defaults = defaults;
        internal = new Properties();
    }
    public static void setPreferenceDirectory(String dir)
    {
        PREF_DIR = dir;
    }
    public static String getPreferenceDirectory()
    {
        return PREF_DIR;
    }
    public void load()
        throws IOException
    {
        FileInputStream fis = null;
        if (fullName != null)
            try {
                fis = new FileInputStream(fullName);
            } catch (IOException e1) {
                fullName = null;
            }
        if (fullName == null) {
            if (PREF_DIR != null) {
                try {
                    fis =
                        new FileInputStream(fullName =
                                            PREF_DIR+FILE_SEP+prefFileName);
                } catch (IOException e2) {
                    fullName = null;
                }
            }
            if (fullName == null) {
                try {
                    fis =
                        new FileInputStream(fullName =
                                            USER_HOME+FILE_SEP+prefFileName);
                } catch (IOException e3) {
                    try {
                        fis = new FileInputStream(fullName =
                                                  USER_DIR+FILE_SEP+prefFileName);
                    } catch (IOException e4) {
                        fullName = null;
                    }
                }
            }
        }
        if (fullName != null) {
            try {
                internal.load(fis);
            } finally {
                fis.close();
            }
        }
    }
    public void save()
        throws IOException
    {
        FileOutputStream fos = null;
        if (fullName != null)
            try {
                fos = new FileOutputStream(fullName);
            } catch(IOException e1) {
                fullName = null;
            }
        if (fullName == null) {
            if (PREF_DIR != null) {
                try {
                    fos =
                        new FileOutputStream(fullName =
                                             PREF_DIR+FILE_SEP+prefFileName);
                } catch (IOException e2) {
                    fullName = null;
                }
            }
            if (fullName == null) {
                try {
                    fos =
                        new FileOutputStream(fullName =
                                             USER_HOME+FILE_SEP+prefFileName);
                } catch (IOException e3) {
                    fullName = null;
                    throw e3;
                }
            }
        }
        try {
            internal.store(fos, prefFileName);
        } finally {
            fos.close();
        }
    }
    private Object getDefault(String key)
    {
        if (defaults != null)
            return defaults.get(key);
        else
            return null;
    }
    public Rectangle getRectangle(String key)
    {
        Rectangle defaultValue = (Rectangle)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        Rectangle result = new Rectangle();
        try {
            int x, y, w, h;
            String token;
            StringTokenizer st = new StringTokenizer(sp," ", false);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            x = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            y = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            w = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            h = Integer.parseInt(token);
            result.setBounds(x,y,w,h);
            return result;
        } catch (NumberFormatException e) {
            internal.remove(key);
            return defaultValue;
        }
    }
    public Dimension getDimension(String key)
    {
        Dimension defaultValue = (Dimension)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null)
            return defaultValue;
        Dimension result = new Dimension();
        try {
            int w, h;
            String token;
            StringTokenizer st = new StringTokenizer(sp," ", false);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            w = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            h = Integer.parseInt(token);
            result.setSize(w,h);
            return result;
        } catch (NumberFormatException e) {
            internal.remove(key);
            return defaultValue;
        }
    }
    public Point getPoint(String key)
    {
        Point defaultValue = (Point)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        Point result = new Point();
        try {
            int x, y;
            String token;
            StringTokenizer st = new StringTokenizer(sp," ", false);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            x = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            y = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            result.setLocation(x,y);
            return result;
        } catch (NumberFormatException e) {
            internal.remove(key);
            return defaultValue;
        }
    }
    public Color getColor(String key)
    {
        Color defaultValue = (Color)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        try {
            int r, g, b, a;
            String token;
            StringTokenizer st = new StringTokenizer(sp," ", false);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            r = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            g = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            b = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            a = Integer.parseInt(token);
            return new Color(r, g, b, a);
        } catch (NumberFormatException e) {
            internal.remove(key);
            return defaultValue;
        }
    }
    public Font getFont(String key)
    {
        Font defaultValue = (Font)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        try {
            int size, type;
            String name;
            String token;
            StringTokenizer st = new StringTokenizer(sp," ", false);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            name = st.nextToken();
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            size = Integer.parseInt(token);
            if (!st.hasMoreTokens()) {
                internal.remove(key);
                return defaultValue;
            }
            token = st.nextToken();
            type = Integer.parseInt(token);
            return new Font(name, type, size);
        } catch (NumberFormatException e) {
            internal.remove(key);
            return defaultValue;
        }
    }
    public String getString(String key)
    {
        String sp = internal.getProperty(key);
        if (sp == null) {
            sp = (String)getDefault(key);
        }
        return sp;
    }
    public String[] getStrings(String mkey)
    {
        String last;
        int i = 0;
        ArrayList v = new ArrayList();
        while (true) {
            last = getString(mkey+i);
            i++;
            if (last == null)
                break;
            v.add(last);
        }
        if (v.size() != 0) {
            String[] str = new String[v.size()];
            return (String[])v.toArray(str);
        } else {
            return (String[])getDefault(mkey);
        }
    }
    public URL getURL(String key)
    {
        URL defaultValue = (URL)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        URL url = null;
        try {
            url = new URL(sp);
        } catch (MalformedURLException ex) {
            internal.remove(key);
            return defaultValue;
        }
        return url;
    }
    public URL[] getURLs(String mkey)
    {
        URL last;
        int i = 0;
        ArrayList v = new ArrayList();
        while (true) {
            last = getURL(mkey+i);
            i++;
            if (last == null)
                break;
            v.add(last);
        }
        if (v.size() != 0) {
            URL[] path = new URL[v.size()];
            return (URL[])v.toArray(path);
        } else {
            return (URL[])getDefault(mkey);
        }
    }
    public File getFile(String key)
    {
        File defaultValue = (File)getDefault(key);
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        File file = new File(sp);
        if (file.exists())
            return file;
        else {
            internal.remove(key);
            return defaultValue;
        }
    }
    public File[] getFiles(String mkey)
    {
        File last;
        int i = 0;
        ArrayList v = new ArrayList();
        while (true) {
            last = getFile(mkey+i);
            i++;
            if (last == null)
                break;
            v.add(last);
        }
        if (v.size() != 0) {
            File[] path = new File[v.size()];
            return (File[])v.toArray(path);
        } else {
            return (File[])getDefault(mkey);
        }
    }
    public int getInteger(String key)
    {
        int defaultValue = 0;
        if (getDefault(key) != null)
            defaultValue = ((Integer)getDefault(key)).intValue();
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        int value;
        try {
            value = Integer.parseInt(sp);
        } catch (NumberFormatException ex) {
            internal.remove(key);
            return defaultValue;
        }
        return value;
    }
    public float getFloat(String key)
    {
        float defaultValue = 0;
        if (getDefault(key) != null)
            defaultValue = ((Float)getDefault(key)).floatValue();
        String sp = internal.getProperty(key);
        if (sp == null) {
            return defaultValue;
        }
        float value;
        try {
            value = Float.parseFloat(sp);
        } catch (NumberFormatException ex) {
            setFloat(key, defaultValue);
            return defaultValue;
        }
        return value;
    }
    public boolean getBoolean(String key)
    {
        if (internal.getProperty(key) != null)
            return internal.getProperty(key).equals("true");
        else
            if (getDefault(key) != null)
                return ((Boolean)getDefault(key)).booleanValue();
            else
                return false;
    }
    public void setRectangle(String key, Rectangle value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.x+" "+value.y+" "+
                                 value.width+ ' ' +value.height);
        else
            internal.remove(key);
    }
    public void setDimension(String key, Dimension value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.width+" "+value.height);
        else
            internal.remove(key);
    }
    public void setPoint(String key, Point value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.x+" "+value.y);
        else
            internal.remove(key);
    }
    public void setColor(String key, Color value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.getRed()+" "+
                         value.getGreen()+" "+value.getBlue()+" "+
                         value.getAlpha());
        else
            internal.remove(key);
    }
    public void setFont(String key, Font value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.getName()+" "+value.getSize()+" "+
                         value.getStyle());
        else
            internal.remove(key);
    }
    public void setString(String key, String value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value);
        else
            internal.remove(key);
    }
    public void setStrings(String mkey, String[] values)
    {
        int j = 0;
        if (values != null)
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    setString(mkey+j, values[i]);
                    j++;
                }
            }
        String last;
        while (true) {
            last = getString(mkey+j);
            if (last == null)
                break;
            setString(mkey+j, null);
            j++;
        }
    }
    public void setURL(String key, URL value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.toString());
        else
            internal.remove(key);
    }
    public void setURLs(String mkey, URL[] values)
    {
        int j = 0;
        if (values != null)
            for (int i = 0 ; i < values.length; i++) {
                if (values[i] != null) {
                    setURL(mkey+j, values[i]);
                    j++;
                }
            }
        String last;
        while (true) {
            last = getString(mkey+j);
            if (last == null)
                break;
            setString(mkey+j, null);
            j++;
        }
    }
    public void setFile(String key, File value)
    {
        if (value != null && !value.equals(getDefault(key)))
            internal.setProperty(key, value.getAbsolutePath());
        else
            internal.remove(key);
    }
    public void setFiles(String mkey, File[] values)
    {
        int j = 0;
        if (values != null)
            for (int i = 0 ; i < values.length; i++) {
                if (values[i] != null) {
                    setFile(mkey+j, values[i]);
                    j++;
                }
            }
        String last;
        while (true) {
            last = getString(mkey+j);
            if (last == null)
                break;
            setString(mkey+j, null);
            j++;
        }
    }
    public void setInteger(String key, int value)
    {
        if (getDefault(key) != null &&
            ((Integer)getDefault(key)).intValue() != value)
            internal.setProperty(key, Integer.toString(value));
        else
            internal.remove(key);
    }
    public void setFloat(String key, float value)
    {
        if (getDefault(key) != null &&
            ((Float)getDefault(key)).floatValue() != value)
            internal.setProperty(key, Float.toString(value));
        else
            internal.remove(key);
    }
    public void setBoolean(String key, boolean value)
    {
        if (getDefault(key) != null &&
            ((Boolean)getDefault(key)).booleanValue() != value) {
            internal.setProperty(key, value?"true":"false");
        } else {
            internal.remove(key);
        }
    }
}
