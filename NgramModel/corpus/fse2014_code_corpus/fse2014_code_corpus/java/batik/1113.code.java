package org.apache.batik.script;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
public class ImportInfo {
    static final String defaultFile = "META-INF/imports/script.txt";
    static String importFile;
    static {
        importFile = defaultFile;
        try {
            importFile = System.getProperty
                ("org.apache.batik.script.imports", defaultFile);
        } catch (SecurityException se) {
        } catch (NumberFormatException nfe) {
        }
    }
    static ImportInfo defaultImports=null;
    static public ImportInfo getImports() {
        if (defaultImports == null) 
            defaultImports = readImports();
        return defaultImports;    
    }
    static ImportInfo readImports() {
        ImportInfo ret = new ImportInfo();
        ClassLoader cl = ImportInfo.class.getClassLoader();
        if (cl == null) return ret;
        Enumeration e;
        try {
            e = cl.getResources(importFile);
        } catch (IOException ioe) {
            return ret;
        }
        while (e.hasMoreElements()) {
            try {
                URL url = (URL)e.nextElement();
                ret.addImports(url);
            } catch (Exception ex) {
            }
        }
        return ret;
    }
    protected Set classes;
    protected Set packages;
    public ImportInfo() {
        classes = new HashSet();
        packages = new HashSet();
    }
    public Iterator getClasses()  { 
        return Collections.unmodifiableSet(classes).iterator(); 
    }
    public Iterator getPackages() { 
        return Collections.unmodifiableSet(packages).iterator(); 
    }
    public void addClass  (String cls) { classes.add(cls); }
    public void addPackage(String pkg) { packages.add(pkg); }
    public boolean removeClass(String cls) { return classes.remove(cls); }
    public boolean removePackage(String pkg) { return packages.remove(pkg); }
    static final String classStr    = "class";
    static final String packageStr  = "package";
    public void addImports(URL src) throws IOException
    {
        InputStream    is = null;
        Reader         r  = null;
        BufferedReader br = null;
        try {
            is = src.openStream();
            r  = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(r);
            String line;
            while ((line = br.readLine()) != null) {
                int idx = line.indexOf('#');
                if (idx != -1)
                    line = line.substring(0, idx);
                line = line.trim();
                if (line.length() == 0) continue;
                idx = line.indexOf(' ');
                if (idx == -1) continue; 
                String prefix = line.substring(0,idx);
                line = line.substring(idx+1);
                boolean isPackage = packageStr.equals(prefix);
                boolean isClass   = classStr.equals(prefix);
                if (!isPackage && !isClass) continue;
                while (line.length() != 0) {
                    idx = line.indexOf(' ');
                    String id;
                    if (idx == -1) {
                        id = line;
                        line = ""; 
                    } else {
                        id   = line.substring(0, idx);
                        line = line.substring(idx+1);
                    }
                    if (id.length() == 0) continue;
                    if (isClass) addClass(id);
                    else         addPackage(id);
                }
            }
        }
        finally {
            if ( is != null ){
                try {
                    is.close();
                } catch ( IOException ignored ){}
                is = null;
            }
            if ( r != null ){
                try{
                    r.close();
                } catch ( IOException ignored ){}
                r = null;
            }
            if ( br == null ){
                try{
                    br.close();
                } catch ( IOException ignored ){}
                br = null;
            }
        }
    }
};
