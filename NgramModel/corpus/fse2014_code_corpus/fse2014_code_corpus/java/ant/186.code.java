package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.types.EnumeratedAttribute;
public abstract class Definer extends DefBase {
    private static final String ANTLIB_XML = "/antlib.xml";
    private static class ResourceStack extends ThreadLocal {
        public Object initialValue() {
            return new HashMap();
        }
        Map getStack() {
            return (Map) get();
        }
    }
    private static ResourceStack resourceStack = new ResourceStack();
    private String name;
    private String classname;
    private File file;
    private String resource;
    private boolean restrict = false;
    private   int    format = Format.PROPERTIES;
    private   boolean definerSet = false;
    private   int         onError = OnError.FAIL;
    private   String      adapter;
    private   String      adaptTo;
    private   Class       adapterClass;
    private   Class       adaptToClass;
    public static class OnError extends EnumeratedAttribute {
        public static final int  FAIL = 0, REPORT = 1, IGNORE = 2, FAIL_ALL = 3;
        public static final String POLICY_FAIL = "fail";
        public static final String POLICY_REPORT = "report";
        public static final String POLICY_IGNORE = "ignore";
        public static final String POLICY_FAILALL = "failall";
        public OnError() {
            super();
        }
        public OnError(String value) {
            setValue(value);
        }
        public String[] getValues() {
            return new String[] {POLICY_FAIL, POLICY_REPORT, POLICY_IGNORE, POLICY_FAILALL};
        }
    }
    public static class Format extends EnumeratedAttribute {
        public static final int PROPERTIES = 0, XML = 1;
        public String[] getValues() {
            return new String[] {"properties", "xml"};
        }
    }
     protected void setRestrict(boolean restrict) {
         this.restrict = restrict;
     }
    public void setOnError(OnError onError) {
        this.onError = onError.getIndex();
    }
    public void setFormat(Format format) {
        this.format = format.getIndex();
    }
    public String getName() {
        return name;
    }
    public File getFile() {
        return file;
    }
    public String getResource() {
        return resource;
    }
    public void execute() throws BuildException {
        ClassLoader al = createLoader();
        if (!definerSet) {
            if (getURI() == null) {
                throw new BuildException(
                        "name, file or resource attribute of "
                                + getTaskName() + " is undefined",
                        getLocation());
            }
            if (getURI().startsWith(MagicNames.ANTLIB_PREFIX)) {
                String uri1 = getURI();
                setResource(makeResourceFromURI(uri1));
            } else {
                throw new BuildException(
                        "Only antlib URIs can be located from the URI alone,"
                                + "not the URI " + getURI());
            }
        }
        if (name != null) {
            if (classname == null) {
                throw new BuildException(
                    "classname attribute of " + getTaskName() + " element "
                    + "is undefined", getLocation());
            }
            addDefinition(al, name, classname);
        } else {
            if (classname != null) {
                String msg = "You must not specify classname "
                    + "together with file or resource.";
                throw new BuildException(msg, getLocation());
            }
            Enumeration urls = null;
            if (file != null) {
                final URL url = fileToURL();
                if (url == null) {
                    return;
                }
                urls = new Enumeration() {
                    private boolean more = true;
                    public boolean hasMoreElements() {
                        return more;
                    }
                    public Object nextElement() throws NoSuchElementException {
                        if (more) {
                            more = false;
                            return url;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }
                };
            } else {
                urls = resourceToURLs(al);
            }
            while (urls.hasMoreElements()) {
                URL url = (URL) urls.nextElement();
                int fmt = this.format;
                if (url.toString().toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
                    fmt = Format.XML;
                }
                if (fmt == Format.PROPERTIES) {
                    loadProperties(al, url);
                    break;
                } else {
                    if (resourceStack.getStack().get(url) != null) {
                        log("Warning: Recursive loading of " + url
                            + " ignored"
                            + " at " + getLocation()
                            + " originally loaded at "
                            + resourceStack.getStack().get(url),
                            Project.MSG_WARN);
                    } else {
                        try {
                            resourceStack.getStack().put(url, getLocation());
                            loadAntlib(al, url);
                        } finally {
                            resourceStack.getStack().remove(url);
                        }
                    }
                }
            }
        }
    }
    public static String makeResourceFromURI(String uri) {
        String path = uri.substring(MagicNames.ANTLIB_PREFIX.length());
        String resource;
        if (path.startsWith("//")) {
            resource = path.substring("//".length());
            if (!resource.endsWith(".xml")) {
                resource = resource + ANTLIB_XML;
            }
        } else {
            resource = path.replace('.', '/') + ANTLIB_XML;
        }
        return resource;
    }
    private URL fileToURL() {
        String message = null;
        if (!(file.exists())) {
            message = "File " + file + " does not exist";
        }
        if (message == null && !(file.isFile())) {
            message = "File " + file + " is not a file";
        }
        if (message == null) {
            try {
                return FileUtils.getFileUtils().getFileURL(file);
            } catch (Exception ex) {
                message =
                    "File " + file + " cannot use as URL: "
                    + ex.toString();
            }
        }
        switch (onError) {
            case OnError.FAIL_ALL:
                throw new BuildException(message);
            case OnError.FAIL:
            case OnError.REPORT:
                log(message, Project.MSG_WARN);
                break;
            case OnError.IGNORE:
                log(message, Project.MSG_VERBOSE);
                break;
            default:
                break;
        }
        return null;
    }
    private Enumeration resourceToURLs(ClassLoader classLoader) {
        Enumeration ret;
        try {
            ret = classLoader.getResources(resource);
        } catch (IOException e) {
            throw new BuildException(
                "Could not fetch resources named " + resource,
                e, getLocation());
        }
        if (!ret.hasMoreElements()) {
            String message = "Could not load definitions from resource "
                + resource + ". It could not be found.";
            switch (onError) {
                case OnError.FAIL_ALL:
                    throw new BuildException(message);
                case OnError.FAIL:
                case OnError.REPORT:
                    log(message, Project.MSG_WARN);
                    break;
                case OnError.IGNORE:
                    log(message, Project.MSG_VERBOSE);
                    break;
                default:
                    break;
            }
        }
        return ret;
    }
    protected void loadProperties(ClassLoader al, URL url) {
        InputStream is = null;
        try {
            is = url.openStream();
            if (is == null) {
                log("Could not load definitions from " + url,
                    Project.MSG_WARN);
                return;
            }
            Properties props = new Properties();
            props.load(is);
            Enumeration keys = props.keys();
            while (keys.hasMoreElements()) {
                name = ((String) keys.nextElement());
                classname = props.getProperty(name);
                addDefinition(al, name, classname);
            }
        } catch (IOException ex) {
            throw new BuildException(ex, getLocation());
        } finally {
            FileUtils.close(is);
        }
    }
    private void loadAntlib(ClassLoader classLoader, URL url) {
        try {
            Antlib antlib = Antlib.createAntlib(getProject(), url, getURI());
            antlib.setClassLoader(classLoader);
            antlib.setURI(getURI());
            antlib.execute();
        } catch (BuildException ex) {
            throw ProjectHelper.addLocationToBuildException(
                ex, getLocation());
        }
    }
    public void setFile(File file) {
        if (definerSet) {
            tooManyDefinitions();
        }
        definerSet = true;
        this.file = file;
    }
    public void setResource(String res) {
        if (definerSet) {
            tooManyDefinitions();
        }
        definerSet = true;
        this.resource = res;
    }
    public void setAntlib(String antlib) {
        if (definerSet) {
            tooManyDefinitions();
        }
        if (!antlib.startsWith("antlib:")) {
            throw new BuildException(
                "Invalid antlib attribute - it must start with antlib:");
        }
        setURI(antlib);
        this.resource = antlib.substring("antlib:".length()).replace('.', '/')
            + "/antlib.xml";
        definerSet = true;
    }
    public void setName(String name) {
        if (definerSet) {
            tooManyDefinitions();
        }
        definerSet = true;
        this.name = name;
    }
    public String getClassname() {
        return classname;
    }
    public void setClassname(String classname) {
        this.classname = classname;
    }
    public void setAdapter(String adapter) {
        this.adapter = adapter;
    }
    protected void setAdapterClass(Class adapterClass) {
        this.adapterClass = adapterClass;
    }
    public void setAdaptTo(String adaptTo) {
        this.adaptTo = adaptTo;
    }
    protected void setAdaptToClass(Class adaptToClass) {
        this.adaptToClass = adaptToClass;
    }
    protected void addDefinition(ClassLoader al, String name, String classname)
        throws BuildException {
        Class cl = null;
        try {
            try {
                name = ProjectHelper.genComponentName(getURI(), name);
                if (onError != OnError.IGNORE) {
                    cl = Class.forName(classname, true, al);
                }
                if (adapter != null) {
                    adapterClass = Class.forName(adapter, true, al);
                }
                if (adaptTo != null) {
                    adaptToClass = Class.forName(adaptTo, true, al);
                }
                AntTypeDefinition def = new AntTypeDefinition();
                def.setName(name);
                def.setClassName(classname);
                def.setClass(cl);
                def.setAdapterClass(adapterClass);
                def.setAdaptToClass(adaptToClass);
                def.setRestrict(restrict);
                def.setClassLoader(al);
                if (cl != null) {
                    def.checkClass(getProject());
                }
                ComponentHelper.getComponentHelper(getProject())
                        .addDataTypeDefinition(def);
            } catch (ClassNotFoundException cnfe) {
                String msg = getTaskName() + " class " + classname
                        + " cannot be found"
                        + "\n using the classloader " + al;
                throw new BuildException(msg, cnfe, getLocation());
            } catch (NoClassDefFoundError ncdfe) {
                String msg = getTaskName() + " A class needed by class "
                        + classname + " cannot be found: " + ncdfe.getMessage()
                        + "\n using the classloader " + al;
                throw new BuildException(msg, ncdfe, getLocation());
            }
        } catch (BuildException ex) {
            switch (onError) {
                case OnError.FAIL_ALL:
                case OnError.FAIL:
                    throw ex;
                case OnError.REPORT:
                    log(ex.getLocation() + "Warning: " + ex.getMessage(),
                        Project.MSG_WARN);
                    break;
                default:
                    log(ex.getLocation() + ex.getMessage(),
                        Project.MSG_DEBUG);
            }
        }
    }
    private void tooManyDefinitions() {
        throw new BuildException(
            "Only one of the attributes name, file and resource"
            + " can be set", getLocation());
    }
}
