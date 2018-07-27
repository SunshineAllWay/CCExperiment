package org.apache.tools.ant;
import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.LoaderUtils;
import org.xml.sax.AttributeList;
public class ProjectHelper {
    public static final String ANT_CORE_URI    = "antlib:org.apache.tools.ant";
    public static final String ANT_CURRENT_URI      = "ant:current";
    public static final String ANTLIB_URI     = "antlib:";
    public static final String ANT_TYPE = "ant-type";
    public static final String HELPER_PROPERTY = MagicNames.PROJECT_HELPER_CLASS;
    public static final String SERVICE_ID = MagicNames.PROJECT_HELPER_SERVICE;
    public static final String PROJECTHELPER_REFERENCE = MagicNames.REFID_PROJECT_HELPER;
    public static void configureProject(Project project, File buildFile) throws BuildException {
        FileResource resource = new FileResource(buildFile);
        ProjectHelper helper = ProjectHelperRepository.getInstance().getProjectHelperForBuildFile(resource);
        project.addReference(PROJECTHELPER_REFERENCE, helper);
        helper.parse(project, buildFile);
    }
    public final static class OnMissingExtensionPoint {
        public static final OnMissingExtensionPoint FAIL = new OnMissingExtensionPoint(
                "fail");
        public static final OnMissingExtensionPoint WARN = new OnMissingExtensionPoint(
                "warn");
        public static final OnMissingExtensionPoint IGNORE = new OnMissingExtensionPoint(
                "ignore");
        private static final OnMissingExtensionPoint[] values = new OnMissingExtensionPoint[] {
                                FAIL, WARN, IGNORE };
        private final String name;
        private OnMissingExtensionPoint(String name) {
            this.name = name;
        }
        public String name() {
            return name;
        }
        public String toString() {
            return name;
        }
        public static OnMissingExtensionPoint valueOf(String name) {
            if (name == null) {
                throw new NullPointerException();
            }
            for (int i = 0; i < values.length; i++) {
                if (name.equals(values[i].name())) {
                    return values[i];
                }
            }
            throw new IllegalArgumentException(
                    "Unknown onMissingExtensionPoint " + name);
        }
    }
    public ProjectHelper() {
    }
    private Vector importStack = new Vector();
    private List extensionStack = new LinkedList();
    public Vector getImportStack() {
        return importStack;
    }
    public List getExtensionStack() {
        return extensionStack;
    }
    private final static ThreadLocal targetPrefix = new ThreadLocal() {
            protected Object initialValue() {
                return (String) null;
            }
        };
    public static String getCurrentTargetPrefix() {
        return (String) targetPrefix.get();
    }
    public static void setCurrentTargetPrefix(String prefix) {
        targetPrefix.set(prefix);
    }
    private final static ThreadLocal prefixSeparator = new ThreadLocal() {
            protected Object initialValue() {
                return ".";
            }
        };
    public static String getCurrentPrefixSeparator() {
        return (String) prefixSeparator.get();
    }
    public static void setCurrentPrefixSeparator(String sep) {
        prefixSeparator.set(sep);
    }
    private final static ThreadLocal inIncludeMode = new ThreadLocal() {
            protected Object initialValue() {
                return Boolean.FALSE;
            }
        };
    public static boolean isInIncludeMode() {
        return inIncludeMode.get() == Boolean.TRUE;
    }
    public static void setInIncludeMode(boolean includeMode) {
        inIncludeMode.set(includeMode ? Boolean.TRUE : Boolean.FALSE);
    }
    public void parse(Project project, Object source) throws BuildException {
        throw new BuildException("ProjectHelper.parse() must be implemented "
            + "in a helper plugin " + this.getClass().getName());
    }
    public static ProjectHelper getProjectHelper() {
        return (ProjectHelper) ProjectHelperRepository.getInstance().getHelpers().next();
    }
    public static ClassLoader getContextClassLoader() {
        return LoaderUtils.isContextLoaderAvailable() ? LoaderUtils.getContextClassLoader() : null;
    }
    public static void configure(Object target, AttributeList attrs,
                                 Project project) throws BuildException {
        if (target instanceof TypeAdapter) {
            target = ((TypeAdapter) target).getProxy();
        }
        IntrospectionHelper ih = IntrospectionHelper.getHelper(project, target.getClass());
        for (int i = 0, length = attrs.getLength(); i < length; i++) {
            String value = replaceProperties(project, attrs.getValue(i), project.getProperties());
            try {
                ih.setAttribute(project, target, attrs.getName(i).toLowerCase(Locale.ENGLISH), value);
            } catch (BuildException be) {
                if (!attrs.getName(i).equals("id")) {
                    throw be;
                }
            }
        }
    }
    public static void addText(Project project, Object target, char[] buf,
        int start, int count) throws BuildException {
        addText(project, target, new String(buf, start, count));
    }
    public static void addText(Project project, Object target, String text)
        throws BuildException {
        if (text == null) {
            return;
        }
        if (target instanceof TypeAdapter) {
            target = ((TypeAdapter) target).getProxy();
        }
        IntrospectionHelper.getHelper(project, target.getClass()).addText(project, target, text);
    }
    public static void storeChild(Project project, Object parent, Object child, String tag) {
        IntrospectionHelper ih = IntrospectionHelper.getHelper(project, parent.getClass());
        ih.storeElement(project, parent, child, tag);
    }
     public static String replaceProperties(Project project, String value) throws BuildException {
         return project.replaceProperties(value);
     }
     public static String replaceProperties(Project project, String value, Hashtable keys)
             throws BuildException {
        PropertyHelper ph = PropertyHelper.getPropertyHelper(project);
        return ph.replaceProperties(null, value, keys);
    }
    public static void parsePropertyString(String value, Vector fragments, Vector propertyRefs)
            throws BuildException {
        PropertyHelper.parsePropertyStringDefault(value, fragments, propertyRefs);
    }
    public static String genComponentName(String uri, String name) {
        if (uri == null || uri.equals("") || uri.equals(ANT_CORE_URI)) {
            return name;
        }
        return uri + ":" + name;
    }
    public static String extractUriFromComponentName(String componentName) {
        if (componentName == null) {
            return "";
        }
        int index = componentName.lastIndexOf(':');
        if (index == -1) {
            return "";
        }
        return componentName.substring(0, index);
    }
    public static String extractNameFromComponentName(String componentName) {
        int index = componentName.lastIndexOf(':');
        if (index == -1) {
            return componentName;
        }
        return componentName.substring(index + 1);
    }
    public static BuildException addLocationToBuildException(
            BuildException ex, Location newLocation) {
        if (ex.getLocation() == null || ex.getMessage() == null) {
            return ex;
        }
        String errorMessage
            = "The following error occurred while executing this line:"
            + System.getProperty("line.separator")
            + ex.getLocation().toString()
            + ex.getMessage();
        if (newLocation == null) {
            return new BuildException(errorMessage, ex);
        }
        return new BuildException(errorMessage, ex, newLocation);
    }
    public boolean canParseAntlibDescriptor(Resource r) {
        return false;
    }
    public UnknownElement parseAntlibDescriptor(Project containingProject,
                                                Resource source) {
        throw new BuildException("can't parse antlib descriptors");
    }
    public boolean canParseBuildFile(Resource buildFile) {
        return true;
    }
    public String getDefaultBuildFile() {
        return Main.DEFAULT_BUILD_FILENAME;
    }
}
