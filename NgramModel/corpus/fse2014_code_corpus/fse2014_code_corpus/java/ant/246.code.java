package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.property.ResolvePropertyMap;
public class Property extends Task {
    protected String name;
    protected String value;
    protected File file;
    protected URL url;
    protected String resource;
    protected Path classpath;
    protected String env;
    protected Reference ref;
    protected String prefix;
    private Project fallback;
    private Object untypedValue;
    private boolean valueAttributeUsed = false;
    private boolean relative = false;
    private File basedir;
    private boolean prefixValues = false;
    protected boolean userProperty; 
    public Property() {
        this(false);
    }
    protected Property(boolean userProperty) {
        this(userProperty, null);
    }
    protected Property(boolean userProperty, Project fallback) {
        this.userProperty = userProperty;
        this.fallback = fallback;
    }
    public void setRelative(boolean relative) {
        this.relative = relative;
    }
    public void setBasedir(File basedir) {
        this.basedir = basedir;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setLocation(File location) {
        if (relative) {
            internalSetValue(location);
        } else {
            setValue(location.getAbsolutePath());
        }
    }
    public void setValue(Object value) {
        valueAttributeUsed = true;
        internalSetValue(value);
    }
    private void internalSetValue(Object value) {
        this.untypedValue = value;
        this.value = value == null ? null : value.toString();
    }
    public void setValue(String value) {
        setValue((Object) value);
    }
    public void addText(String msg) {
        if (!valueAttributeUsed) {
            msg = getProject().replaceProperties(msg);
            String currentValue = getValue();
            if (currentValue != null) {
                msg = currentValue + msg;
            }
            internalSetValue(msg);
        } else if (msg.trim().length() > 0) {
            throw new BuildException("can't combine nested text with value"
                                     + " attribute");
        }
    }
    public String getValue() {
        return value;
    }
    public void setFile(File file) {
        this.file = file;
    }
    public File getFile() {
        return file;
    }
    public void setUrl(URL url) {
        this.url = url;
    }
    public URL getUrl() {
        return url;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
        if (prefix != null && !prefix.endsWith(".")) {
            this.prefix += ".";
        }
    }
    public String getPrefix() {
        return prefix;
    }
    public void setPrefixValues(boolean b) {
        prefixValues = b;
    }
    public boolean getPrefixValues() {
        return prefixValues;
    }
    public void setRefid(Reference ref) {
        this.ref = ref;
    }
    public Reference getRefid() {
        return ref;
    }
    public void setResource(String resource) {
        this.resource = resource;
    }
    public String getResource() {
        return resource;
    }
    public void setEnvironment(String env) {
        this.env = env;
    }
    public String getEnvironment() {
        return env;
    }
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public Path getClasspath() {
        return classpath;
    }
    public void setUserProperty(boolean userProperty) {
        log("DEPRECATED: Ignoring request to set user property in Property"
            + " task.", Project.MSG_WARN);
    }
    public String toString() {
        return value == null ? "" : value;
    }
    public void execute() throws BuildException {
        if (getProject() == null) {
            throw new IllegalStateException("project has not been set");
        }
        if (name != null) {
            if (untypedValue == null && ref == null) {
                throw new BuildException("You must specify value, location or "
                                         + "refid with the name attribute",
                                         getLocation());
            }
        } else {
            if (url == null && file == null && resource == null && env == null) {
                throw new BuildException("You must specify url, file, resource or "
                                         + "environment when not using the "
                                         + "name attribute", getLocation());
            }
        }
        if (url == null && file == null && resource == null && prefix != null) {
            throw new BuildException("Prefix is only valid when loading from "
                                     + "a url, file or resource", getLocation());
        }
        if (name != null && untypedValue != null) {
            if (relative) {
                try {
                    File from = untypedValue instanceof File ? (File)untypedValue : new File(untypedValue.toString());
                    File to = basedir != null ? basedir : getProject().getBaseDir();
                    String relPath = FileUtils.getRelativePath(to, from);
                    relPath = relPath.replace('/', File.separatorChar);
                    addProperty(name, relPath);
                } catch (Exception e) {
                    throw new BuildException(e, getLocation());
                }
            } else {
                addProperty(name, untypedValue);
            }
        }
        if (file != null) {
            loadFile(file);
        }
        if (url != null) {
            loadUrl(url);
        }
        if (resource != null) {
            loadResource(resource);
        }
        if (env != null) {
            loadEnvironment(env);
        }
        if ((name != null) && (ref != null)) {
            try {
                addProperty(name,
                            ref.getReferencedObject(getProject()).toString());
            } catch (BuildException be) {
                if (fallback != null) {
                    addProperty(name,
                                ref.getReferencedObject(fallback).toString());
                } else {
                    throw be;
                }
            }
        }
    }
    protected void loadUrl(URL url) throws BuildException {
        Properties props = new Properties();
        log("Loading " + url, Project.MSG_VERBOSE);
        try {
            InputStream is = url.openStream();
            try {
                loadProperties(props, is, url.getFile().endsWith(".xml"));
            } finally {
                if (is != null) {
                    is.close();
                }
            }
            addProperties(props);
        } catch (IOException ex) {
            throw new BuildException(ex, getLocation());
        }
    }
    private void loadProperties(
                                Properties props, InputStream is, boolean isXml) throws IOException {
        if (isXml) {
            try {
                Method loadXmlMethod = props.getClass().getMethod("loadFromXML",
                                                                  new Class[] {InputStream.class});
                loadXmlMethod.invoke(props, new Object[] {is});
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                log("Can not load xml based property definition on Java < 5");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            props.load(is);
        }
    }
    protected void loadFile(File file) throws BuildException {
        Properties props = new Properties();
        log("Loading " + file.getAbsolutePath(), Project.MSG_VERBOSE);
        try {
            if (file.exists()) {
                FileInputStream  fis = null;
                try {
                    fis = new FileInputStream(file);
                    loadProperties(props, fis, file.getName().endsWith(".xml"));
                } finally {
                    FileUtils.close(fis);
                }
                addProperties(props);
            } else {
                log("Unable to find property file: " + file.getAbsolutePath(),
                    Project.MSG_VERBOSE);
            }
        } catch (IOException ex) {
            throw new BuildException(ex, getLocation());
        }
    }
    protected void loadResource(String name) {
        Properties props = new Properties();
        log("Resource Loading " + name, Project.MSG_VERBOSE);
        InputStream is = null;
        ClassLoader cL = null;
        boolean cleanup = false;
        try {
            if (classpath != null) {
                cleanup = true;
                cL = getProject().createClassLoader(classpath);
            } else {
                cL = this.getClass().getClassLoader();
            }
            if (cL == null) {
                is = ClassLoader.getSystemResourceAsStream(name);
            } else {
                is = cL.getResourceAsStream(name);
            }
            if (is != null) {
                loadProperties(props, is, name.endsWith(".xml"));
                addProperties(props);
            } else {
                log("Unable to find resource " + name, Project.MSG_WARN);
            }
        } catch (IOException ex) {
            throw new BuildException(ex, getLocation());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
            if (cleanup && cL != null) {
                ((AntClassLoader) cL).cleanup();
            }
        }
    }
    protected void loadEnvironment(String prefix) {
        Properties props = new Properties();
        if (!prefix.endsWith(".")) {
            prefix += ".";
        }
        log("Loading Environment " + prefix, Project.MSG_VERBOSE);
        Map osEnv = Execute.getEnvironmentVariables();
        for (Iterator e = osEnv.entrySet().iterator(); e.hasNext(); ) {
            Map.Entry entry = (Map.Entry) e.next();
            props.put(prefix + entry.getKey(), entry.getValue());
        }
        addProperties(props);
    }
    protected void addProperties(Properties props) {
        HashMap m = new HashMap(props);
        resolveAllProperties(m);
        for (Iterator it = m.keySet().iterator(); it.hasNext();) {
            Object k = it.next();
            if (k instanceof String) {
                String propertyName = (String) k;
                if (prefix != null) {
                    propertyName = prefix + propertyName;
                }
                addProperty(propertyName, m.get(k));
            }
        }
    }
    protected void addProperty(String n, String v) {
        addProperty(n, (Object) v);
    }
    protected void addProperty(String n, Object v) {
        PropertyHelper ph = PropertyHelper.getPropertyHelper(getProject());
        if (userProperty) {
            if (ph.getUserProperty(n) == null) {
                ph.setInheritedProperty(n, v);
            } else {
                log("Override ignored for " + n, Project.MSG_VERBOSE);
            }
        } else {
            ph.setNewProperty(n, v);
        }
    }
    private void resolveAllProperties(Map props) throws BuildException {
        PropertyHelper propertyHelper
            = (PropertyHelper) PropertyHelper.getPropertyHelper(getProject());
        new ResolvePropertyMap(
                               getProject(),
                               propertyHelper,
                               propertyHelper.getExpanders())
            .resolveAllProperties(props, getPrefix(), getPrefixValues());
    }
}
