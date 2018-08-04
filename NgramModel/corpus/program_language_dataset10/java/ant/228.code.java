package org.apache.tools.ant.taskdefs;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.tools.ant.AntTypeDefinition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
public class MacroDef extends AntlibDefinition  {
    private NestedSequential nestedSequential;
    private String     name;
    private boolean    backTrace = true;
    private List       attributes = new ArrayList();
    private Map        elements   = new HashMap();
    private String     textName   = null;
    private Text       text       = null;
    private boolean    hasImplicitElement = false;
     public void setName(String name) {
        this.name = name;
    }
    public void addConfiguredText(Text text) {
        if (this.text != null) {
            throw new BuildException(
                "Only one nested text element allowed");
        }
        if (text.getName() == null) {
            throw new BuildException(
                "the text nested element needed a \"name\" attribute");
        }
        for (Iterator i = attributes.iterator(); i.hasNext();) {
            Attribute attribute = (Attribute) i.next();
            if (text.getName().equals(attribute.getName())) {
                throw new BuildException(
                    "the name \"" + text.getName()
                    + "\" is already used as an attribute");
            }
        }
        this.text = text;
        this.textName = text.getName();
    }
    public Text getText() {
        return text;
    }
    public void setBackTrace(boolean backTrace) {
        this.backTrace = backTrace;
    }
    public boolean getBackTrace() {
        return backTrace;
    }
    public NestedSequential createSequential() {
        if (this.nestedSequential != null) {
            throw new BuildException("Only one sequential allowed");
        }
        this.nestedSequential = new NestedSequential();
        return this.nestedSequential;
    }
    public static class NestedSequential implements TaskContainer {
        private List nested = new ArrayList();
        public void addTask(Task task) {
            nested.add(task);
        }
        public List getNested() {
            return nested;
        }
        public boolean similar(NestedSequential other) {
            if (nested.size() != other.nested.size()) {
                return false;
            }
            for (int i = 0; i < nested.size(); ++i) {
                UnknownElement me = (UnknownElement) nested.get(i);
                UnknownElement o = (UnknownElement) other.nested.get(i);
                if (!me.similar(o)) {
                    return false;
                }
            }
            return true;
        }
    }
    public UnknownElement getNestedTask() {
        UnknownElement ret = new UnknownElement("sequential");
        ret.setTaskName("sequential");
        ret.setNamespace("");
        ret.setQName("sequential");
        new RuntimeConfigurable(ret, "sequential");
        for (int i = 0; i < nestedSequential.getNested().size(); ++i) {
            UnknownElement e =
                (UnknownElement) nestedSequential.getNested().get(i);
            ret.addChild(e);
            ret.getWrapper().addChild(e.getWrapper());
        }
        return ret;
    }
    public List getAttributes() {
        return attributes;
    }
    public Map getElements() {
        return elements;
    }
    public static boolean isValidNameCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '-';
    }
    private static boolean isValidName(String name) {
        if (name.length() == 0) {
            return false;
        }
        for (int i = 0; i < name.length(); ++i) {
            if (!isValidNameCharacter(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public void addConfiguredAttribute(Attribute attribute) {
        if (attribute.getName() == null) {
            throw new BuildException(
                "the attribute nested element needed a \"name\" attribute");
        }
        if (attribute.getName().equals(textName)) {
            throw new BuildException(
                "the name \"" + attribute.getName()
                + "\" has already been used by the text element");
        }
        for (int i = 0; i < attributes.size(); ++i) {
            Attribute att = (Attribute) attributes.get(i);
            if (att.getName().equals(attribute.getName())) {
                throw new BuildException(
                    "the name \"" + attribute.getName()
                        + "\" has already been used in "
                        + "another attribute element");
            }
        }
        attributes.add(attribute);
    }
    public void addConfiguredElement(TemplateElement element) {
        if (element.getName() == null) {
            throw new BuildException(
                "the element nested element needed a \"name\" attribute");
        }
        if (elements.get(element.getName()) != null) {
            throw new BuildException(
                "the element " + element.getName()
                + " has already been specified");
        }
        if (hasImplicitElement
            || (element.isImplicit() && elements.size() != 0)) {
            throw new BuildException(
                "Only one element allowed when using implicit elements");
        }
        hasImplicitElement = element.isImplicit();
        elements.put(element.getName(), element);
    }
    public void execute() {
        if (nestedSequential == null) {
            throw new BuildException("Missing sequential element");
        }
        if (name == null) {
            throw new BuildException("Name not specified");
        }
        name = ProjectHelper.genComponentName(getURI(), name);
        MyAntTypeDefinition def = new MyAntTypeDefinition(this);
        def.setName(name);
        def.setClass(MacroInstance.class);
        ComponentHelper helper = ComponentHelper.getComponentHelper(
            getProject());
        helper.addDataTypeDefinition(def);
        log("creating macro  " + name, Project.MSG_VERBOSE);
    }
    public static class Attribute {
        private String name;
        private String defaultValue;
        private String description;
        public void setName(String name) {
            if (!isValidName(name)) {
                throw new BuildException(
                    "Illegal name [" + name + "] for attribute");
            }
            this.name = name.toLowerCase(Locale.ENGLISH);
        }
        public String getName() {
            return name;
        }
        public void setDefault(String defaultValue) {
            this.defaultValue = defaultValue;
        }
        public String getDefault() {
            return defaultValue;
        }
        public void setDescription(String desc) {
            description = desc;
        }
        public String getDescription() {
            return description;
        }
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Attribute other = (Attribute) obj;
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            if (defaultValue == null) {
                if (other.defaultValue != null) {
                    return false;
                }
            } else if (!defaultValue.equals(other.defaultValue)) {
                return false;
            }
            return true;
        }
        public int hashCode() {
            return objectHashCode(defaultValue) + objectHashCode(name);
        }
    }
    public static class Text {
        private String  name;
        private boolean optional;
        private boolean trim;
        private String  description;
        private String  defaultString;
        public void setName(String name) {
            if (!isValidName(name)) {
                throw new BuildException(
                    "Illegal name [" + name + "] for attribute");
            }
            this.name = name.toLowerCase(Locale.ENGLISH);
        }
        public String getName() {
            return name;
        }
        public void setOptional(boolean optional) {
            this.optional = optional;
        }
        public boolean getOptional() {
            return optional;
        }
        public void setTrim(boolean trim) {
            this.trim = trim;
        }
        public boolean getTrim() {
            return trim;
        }
        public void setDescription(String desc) {
            description = desc;
        }
        public String getDescription() {
            return description;
        }
        public void setDefault(String defaultString) {
            this.defaultString = defaultString;
        }
        public String getDefault() {
            return defaultString;
        }
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            Text other = (Text) obj;
            return safeCompare(name, other.name)
                && optional == other.optional
                && trim == other.trim
                && safeCompare(defaultString, other.defaultString);
        }
        public int hashCode() {
            return objectHashCode(name);
        }
    }
    private static boolean safeCompare(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
    public static class TemplateElement {
        private String name;
        private String description;
        private boolean optional = false;
        private boolean implicit = false;
        public void setName(String name) {
            if (!isValidName(name)) {
                throw new BuildException(
                    "Illegal name [" + name + "] for macro element");
            }
            this.name = name.toLowerCase(Locale.ENGLISH);
        }
        public String getName() {
            return name;
        }
        public void setDescription(String desc) {
            description = desc;
        }
        public String getDescription() {
            return description;
        }
        public void setOptional(boolean optional) {
            this.optional = optional;
        }
        public boolean isOptional() {
            return optional;
        }
        public void setImplicit(boolean implicit) {
            this.implicit = implicit;
        }
        public boolean isImplicit() {
            return implicit;
        }
        public boolean equals(Object obj) {
            if (obj == this) {
              return true;
            }
            if (obj == null || !obj.getClass().equals(getClass())) {
                return false;
            }
            TemplateElement t = (TemplateElement) obj;
            return
                (name == null ? t.name == null : name.equals(t.name))
                && optional == t.optional
                && implicit == t.implicit;
        }
        public int hashCode() {
            return objectHashCode(name)
                + (optional ? 1 : 0) + (implicit ? 1 : 0);
        }
    } 
    private boolean sameOrSimilar(Object obj, boolean same) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!obj.getClass().equals(getClass())) {
            return false;
        }
        MacroDef other = (MacroDef) obj;
        if (name == null) {
            return other.name == null;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (other.getLocation() != null
            && other.getLocation().equals(getLocation())
            && !same) {
            return true;
        }
        if (text == null) {
            if (other.text != null) {
                return false;
            }
        } else {
            if (!text.equals(other.text)) {
                return false;
            }
        }
        if (getURI() == null || getURI().equals("")
            || getURI().equals(ProjectHelper.ANT_CORE_URI)) {
            if (!(other.getURI() == null || other.getURI().equals("")
                  || other.getURI().equals(ProjectHelper.ANT_CORE_URI))) {
                return false;
            }
        } else {
            if (!getURI().equals(other.getURI())) {
                return false;
            }
        }
        if (!nestedSequential.similar(other.nestedSequential)) {
            return false;
        }
        if (!attributes.equals(other.attributes)) {
            return false;
        }
        if (!elements.equals(other.elements)) {
            return false;
        }
        return true;
    }
    public boolean similar(Object obj) {
        return sameOrSimilar(obj, false);
    }
    public boolean sameDefinition(Object obj) {
        return sameOrSimilar(obj, true);
    }
    private static class MyAntTypeDefinition extends AntTypeDefinition {
        private MacroDef macroDef;
        public MyAntTypeDefinition(MacroDef macroDef) {
            this.macroDef = macroDef;
        }
        public Object create(Project project) {
            Object o = super.create(project);
            if (o == null) {
                return null;
            }
            ((MacroInstance) o).setMacroDef(macroDef);
            return o;
        }
        public boolean sameDefinition(AntTypeDefinition other, Project project) {
            if (!super.sameDefinition(other, project)) {
                return false;
            }
            MyAntTypeDefinition otherDef = (MyAntTypeDefinition) other;
            return macroDef.sameDefinition(otherDef.macroDef);
        }
        public boolean similarDefinition(
            AntTypeDefinition other, Project project) {
            if (!super.similarDefinition(other, project)) {
                return false;
            }
            MyAntTypeDefinition otherDef = (MyAntTypeDefinition) other;
            return macroDef.similar(otherDef.macroDef);
        }
    }
    private static int objectHashCode(Object o) {
        if (o == null) {
            return 0;
        } else {
            return o.hashCode();
        }
    }
}