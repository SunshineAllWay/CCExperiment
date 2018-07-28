package org.apache.tools.ant;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Collection;
import org.apache.tools.ant.property.NullReturn;
import org.apache.tools.ant.property.GetProperty;
import org.apache.tools.ant.property.ParseNextProperty;
import org.apache.tools.ant.property.PropertyExpander;
import org.apache.tools.ant.property.ParseProperties;
public class PropertyHelper implements GetProperty {
    public interface Delegate {
    }
    public interface PropertyEvaluator extends Delegate {
        Object evaluate(String property, PropertyHelper propertyHelper);
    }
    public interface PropertySetter extends Delegate {
        boolean setNew(
            String property, Object value, PropertyHelper propertyHelper);
        boolean set(
            String property, Object value, PropertyHelper propertyHelper);
    }
    private static final PropertyEvaluator TO_STRING = new PropertyEvaluator() {
        private final String PREFIX = "toString:";
        private final int PREFIX_LEN = PREFIX.length();
        public Object evaluate(String property, PropertyHelper propertyHelper) {
            Object o = null;
            if (property.startsWith(PREFIX) && propertyHelper.getProject() != null) {
                o = propertyHelper.getProject().getReference(property.substring(PREFIX_LEN));
            }
            return o == null ? null : o.toString();
        }
    };
    private static final PropertyExpander DEFAULT_EXPANDER = new PropertyExpander() {
        public String parsePropertyName(
            String s, ParsePosition pos, ParseNextProperty notUsed) {
            int index = pos.getIndex();
            if (s.length() - index >= 3
                    && '$' == s.charAt(index) && '{' == s.charAt(index + 1)) {
                int start = index + 2;
                int end = s.indexOf('}', start);
                if (end < 0) {
                    throw new BuildException("Syntax error in property: "
                            + s.substring(index));
                }
                pos.setIndex(end + 1);
                return start == end ? "" :  s.substring(start, end);
            }
            return null;
        }
    };
    private static final PropertyExpander SKIP_DOUBLE_DOLLAR
        = new PropertyExpander() {
            public String parsePropertyName(
                String s, ParsePosition pos, ParseNextProperty notUsed) {
                int index = pos.getIndex();
                if (s.length() - index >= 2) {
                    if ('$' == s.charAt(index) && '$' == s.charAt(++index)) {
                        pos.setIndex(index);
                    }
                }
                return null;
            }
        };
    private static final PropertyEvaluator FROM_REF = new PropertyEvaluator() {
        private final String PREFIX = "ant.refid:";
        private final int PREFIX_LEN = PREFIX.length();
        public Object evaluate(String prop, PropertyHelper helper) {
            return prop.startsWith(PREFIX) && helper.getProject() != null
                ? helper.getProject().getReference(prop.substring(PREFIX_LEN))
                : null;
        }
    };
    private Project project;
    private PropertyHelper next;
    private Hashtable delegates = new Hashtable();
    private Hashtable properties = new Hashtable();
    private Hashtable userProperties = new Hashtable();
    private Hashtable inheritedProperties = new Hashtable();
    protected PropertyHelper() {
        add(FROM_REF);
        add(TO_STRING);
        add(SKIP_DOUBLE_DOLLAR);
        add(DEFAULT_EXPANDER);
    }
    public static Object getProperty(Project project, String name) {
        return PropertyHelper.getPropertyHelper(project)
            .getProperty(name);
    }
    public static void setProperty(Project project, String name, Object value) {
        PropertyHelper.getPropertyHelper(project)
            .setProperty(name, value, true);
    }
    public static void setNewProperty(
        Project project, String name, Object value) {
        PropertyHelper.getPropertyHelper(project)
            .setNewProperty(name, value);
    }
    public void setProject(Project p) {
        this.project = p;
    }
    public Project getProject() {
        return project;
    }
    public void setNext(PropertyHelper next) {
        this.next = next;
    }
    public PropertyHelper getNext() {
        return next;
    }
    public static synchronized PropertyHelper getPropertyHelper(Project project) {
        PropertyHelper helper = null;
        if (project != null) {
            helper = (PropertyHelper) project.getReference(MagicNames
                                                           .REFID_PROPERTY_HELPER);
        }
        if (helper != null) {
            return helper;
        }
        helper = new PropertyHelper();
        helper.setProject(project);
        if (project != null) {
            project.addReference(MagicNames.REFID_PROPERTY_HELPER, helper);
        }
        return helper;
    }
    public Collection getExpanders() {
        return getDelegates(PropertyExpander.class);
    }
    public boolean setPropertyHook(String ns, String name,
                                   Object value,
                                   boolean inherited, boolean user,
                                   boolean isNew) {
        if (getNext() != null) {
            boolean subst = getNext().setPropertyHook(ns, name, value, inherited, user, isNew);
            if (subst) {
                return true;
            }
        }
        return false;
    }
    public Object getPropertyHook(String ns, String name, boolean user) {
        if (getNext() != null) {
            Object o = getNext().getPropertyHook(ns, name, user);
            if (o != null) {
                return o;
            }
        }
        if (project != null && name.startsWith("toString:")) {
            name = name.substring("toString:".length());
            Object v = project.getReference(name);
            return (v == null) ? null : v.toString();
        }
        return null;
    }
    public void parsePropertyString(String value, Vector fragments,
                                    Vector propertyRefs) throws BuildException {
        parsePropertyStringDefault(value, fragments, propertyRefs);
    }
    public String replaceProperties(String ns, String value, Hashtable keys) throws BuildException {
        return replaceProperties(value);
    }
    public String replaceProperties(String value) throws BuildException {
        Object o = parseProperties(value);
        return o == null || o instanceof String ? (String) o : o.toString();
    }
    public Object parseProperties(String value) throws BuildException {
        return new ParseProperties(getProject(), getExpanders(), this)
            .parseProperties(value);
    }
    public boolean containsProperties(String value) {
        return new ParseProperties(getProject(), getExpanders(), this)
            .containsProperties(value);
    }
    public boolean setProperty(String ns, String name, Object value, boolean verbose) {
        return setProperty(name, value, verbose);
    }
    public boolean setProperty(String name, Object value, boolean verbose) {
        for (Iterator iter = getDelegates(PropertySetter.class).iterator(); iter.hasNext();) {
            PropertySetter setter = (PropertySetter) iter.next();
            if (setter.set(name, value, this)) {
                return true;
            }
        }
        synchronized (this) {
            if (userProperties.containsKey(name)) {
                if (project != null && verbose) {
                    project.log("Override ignored for user property \""
                                + name + "\"", Project.MSG_VERBOSE);
                }
                return false;
            }
            if (project != null && verbose) {
                if (properties.containsKey(name)) {
                    project.log("Overriding previous definition of property \""
                                + name + "\"", Project.MSG_VERBOSE);
                }
                project.log("Setting project property: " + name + " -> "
                            + value, Project.MSG_DEBUG);
            }
            if (name != null && value != null) {
                properties.put(name, value);
            }
            return true;
        }
    }
    public void setNewProperty(String ns, String name, Object value) {
        setNewProperty(name, value);
    }
    public void setNewProperty(String name, Object value) {
        for (Iterator iter = getDelegates(PropertySetter.class).iterator();
             iter.hasNext();) {
            PropertySetter setter = (PropertySetter) iter.next();
            if (setter.setNew(name, value, this)) {
                return;
            }
        }
        synchronized (this) {
            if (project != null && properties.containsKey(name)) {
                project.log("Override ignored for property \"" + name
                            + "\"", Project.MSG_VERBOSE);
                return;
            }
            if (project != null) {
                project.log("Setting project property: " + name
                            + " -> " + value, Project.MSG_DEBUG);
            }
            if (name != null && value != null) {
                properties.put(name, value);
            }
        }
    }
    public void setUserProperty(String ns, String name, Object value) {
        setUserProperty(name, value);
    }
    public void setUserProperty(String name, Object value) {
        if (project != null) {
            project.log("Setting ro project property: "
                        + name + " -> " + value, Project.MSG_DEBUG);
        }
        synchronized (this) {
            userProperties.put(name, value);
            properties.put(name, value);
        }
    }
    public void setInheritedProperty(String ns, String name, Object value) {
        setInheritedProperty(name, value);
    }
    public void setInheritedProperty(String name, Object value) {
        if (project != null) {
            project.log("Setting ro project property: " + name + " -> "
                        + value, Project.MSG_DEBUG);
        }
        synchronized (this) {
            inheritedProperties.put(name, value);
            userProperties.put(name, value);
            properties.put(name, value);
        }
    }
    public Object getProperty(String ns, String name) {
        return getProperty(name);
    }
    public Object getProperty(String name) {
        if (name == null) {
            return null;
        }
        for (Iterator iter = getDelegates(PropertyEvaluator.class).iterator(); iter.hasNext();) {
            Object o = ((PropertyEvaluator) iter.next()).evaluate(name, this);
            if (o != null) {
                if (o instanceof NullReturn) {
                    return null;
                }
                return o;
            }
        }
        return properties.get(name);
    }
    public Object getUserProperty(String ns, String name) {
        return getUserProperty(name);
    }
    public Object getUserProperty(String name) {
        if (name == null) {
            return null;
        }
        return userProperties.get(name);
    }
    public Hashtable getProperties() {
        synchronized (properties) {
            return new Hashtable(properties);
        }
    }
    public Hashtable getUserProperties() {
        synchronized (userProperties) {
            return new Hashtable(userProperties);
        }
    }
    public Hashtable getInheritedProperties() {
        synchronized (inheritedProperties) {
            return new Hashtable(inheritedProperties);
        }
    }
    protected Hashtable getInternalProperties() {
        return properties;
    }
    protected Hashtable getInternalUserProperties() {
        return userProperties;
    }
    protected Hashtable getInternalInheritedProperties() {
        return inheritedProperties;
    }
    public void copyInheritedProperties(Project other) {
        synchronized (inheritedProperties) {
            Enumeration e = inheritedProperties.keys();
            while (e.hasMoreElements()) {
                String arg = e.nextElement().toString();
                if (other.getUserProperty(arg) != null) {
                    continue;
                }
                Object value = inheritedProperties.get(arg);
                other.setInheritedProperty(arg, value.toString());
            }
        }
    }
    public void copyUserProperties(Project other) {
        synchronized (userProperties) {
            Enumeration e = userProperties.keys();
            while (e.hasMoreElements()) {
                Object arg = e.nextElement();
                if (inheritedProperties.containsKey(arg)) {
                    continue;
                }
                Object value = userProperties.get(arg);
                other.setUserProperty(arg.toString(), value.toString());
            }
        }
    }
    static void parsePropertyStringDefault(String value, Vector fragments, Vector propertyRefs)
            throws BuildException {
        int prev = 0;
        int pos;
        while ((pos = value.indexOf("$", prev)) >= 0) {
            if (pos > 0) {
                fragments.addElement(value.substring(prev, pos));
            }
            if (pos == (value.length() - 1)) {
                fragments.addElement("$");
                prev = pos + 1;
            } else if (value.charAt(pos + 1) != '{') {
                if (value.charAt(pos + 1) == '$') {
                    fragments.addElement("$");
                    prev = pos + 2;
                } else {
                    fragments.addElement(value.substring(pos, pos + 2));
                    prev = pos + 2;
                }
            } else {
                int endName = value.indexOf('}', pos);
                if (endName < 0) {
                    throw new BuildException("Syntax error in property: " + value);
                }
                String propertyName = value.substring(pos + 2, endName);
                fragments.addElement(null);
                propertyRefs.addElement(propertyName);
                prev = endName + 1;
            }
        }
        if (prev < value.length()) {
            fragments.addElement(value.substring(prev));
        }
    }
    public void add(Delegate delegate) {
        synchronized (delegates) {
            for (Iterator iter = getDelegateInterfaces(delegate).iterator(); iter.hasNext();) {
                Object key = iter.next();
                List list = (List) delegates.get(key);
                if (list == null) {
                    list = new ArrayList();
                } else {
                    list = new ArrayList(list);
                    list.remove(delegate);
                }
                list.add(0, delegate);
                delegates.put(key, Collections.unmodifiableList(list));
            }
        }
    }
    protected List getDelegates(Class type) {
        List r = (List) delegates.get(type);
        return r == null ? Collections.EMPTY_LIST : r;
    }
    protected static Set getDelegateInterfaces(Delegate d) {
        HashSet result = new HashSet();
        Class c = d.getClass();
        while (c != null) {
            Class[] ifs = c.getInterfaces();
            for (int i = 0; i < ifs.length; i++) {
                if (Delegate.class.isAssignableFrom(ifs[i])) {
                    result.add(ifs[i]);
                }
            }
            c = c.getSuperclass();
        }
        result.remove(Delegate.class);
        return result;
    }
    public static Boolean toBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            String s = (String) value;
            if (Project.toBoolean(s)) {
                return Boolean.TRUE;
            }
            if ("off".equalsIgnoreCase(s)
                || "false".equalsIgnoreCase(s)
                || "no".equalsIgnoreCase(s)) {
                return Boolean.FALSE;
            }
        }
        return null;
    }
    private static boolean nullOrEmpty(Object value) {
        return value == null || "".equals(value);
    }
    private boolean evalAsBooleanOrPropertyName(Object value) {
        Boolean b = toBoolean(value);
        if (b != null) {
            return b.booleanValue();
        }
        return getProperty(String.valueOf(value)) != null;
    }
    public boolean testIfCondition(Object value) {
        return nullOrEmpty(value) || evalAsBooleanOrPropertyName(value);
    }
    public boolean testUnlessCondition(Object value) {
        return nullOrEmpty(value) || !evalAsBooleanOrPropertyName(value);
    }
}
