package org.apache.tools.ant;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import org.apache.tools.ant.taskdefs.PreSetDef;
public class UnknownElement extends Task {
    private final String elementName;
    private String namespace = "";
    private String qname;
    private Object realThing;
    private List children = null;
    private boolean presetDefed = false;
    public UnknownElement(String elementName) {
        this.elementName = elementName;
    }
    public List getChildren() {
        return children;
    }
    public String getTag() {
        return elementName;
    }
    public String getNamespace() {
        return namespace;
    }
    public void setNamespace(String namespace) {
        if (namespace.equals(ProjectHelper.ANT_CURRENT_URI)) {
            ComponentHelper helper = ComponentHelper.getComponentHelper(
                getProject());
            namespace = helper.getCurrentAntlibUri();
        }
        this.namespace = namespace == null ? "" : namespace;
    }
    public String getQName() {
        return qname;
    }
    public void setQName(String qname) {
        this.qname = qname;
    }
    public RuntimeConfigurable getWrapper() {
        return super.getWrapper();
    }
    public void maybeConfigure() throws BuildException {
        if (realThing != null) {
            return;
        }
        configure(makeObject(this, getWrapper()));
    }
    public void configure(Object realObject) {
        realThing = realObject;
        getWrapper().setProxy(realThing);
        Task task = null;
        if (realThing instanceof Task) {
            task = (Task) realThing;
            task.setRuntimeConfigurableWrapper(getWrapper());
            if (getWrapper().getId() != null) {
                this.getOwningTarget().replaceChild(this, (Task) realThing);
            }
       }
        if (task != null) {
            task.maybeConfigure();
        } else {
            getWrapper().maybeConfigure(getProject());
        }
        handleChildren(realThing, getWrapper());
    }
    protected void handleOutput(String output) {
        if (realThing instanceof Task) {
            ((Task) realThing).handleOutput(output);
        } else {
            super.handleOutput(output);
        }
    }
    protected int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        if (realThing instanceof Task) {
            return ((Task) realThing).handleInput(buffer, offset, length);
        } else {
            return super.handleInput(buffer, offset, length);
        }
    }
    protected void handleFlush(String output) {
        if (realThing instanceof Task) {
            ((Task) realThing).handleFlush(output);
        } else {
            super.handleFlush(output);
        }
    }
    protected void handleErrorOutput(String output) {
        if (realThing instanceof Task) {
            ((Task) realThing).handleErrorOutput(output);
        } else {
            super.handleErrorOutput(output);
        }
    }
    protected void handleErrorFlush(String output) {
        if (realThing instanceof Task) {
            ((Task) realThing).handleErrorFlush(output);
        } else {
            super.handleErrorFlush(output);
        }
    }
    public void execute() {
        if (realThing == null) {
            throw new BuildException("Could not create task of type: "
                                     + elementName, getLocation());
        }
        try {
            if (realThing instanceof Task) {
                ((Task) realThing).execute();
            }
        } finally {
            if (getWrapper().getId() == null) {
                realThing = null;
                getWrapper().setProxy(null);
            }
        }
    }
    public void addChild(UnknownElement child) {
        if (children == null) {
            children = new ArrayList();
        }
        children.add(child);
    }
    protected void handleChildren(
        Object parent,
        RuntimeConfigurable parentWrapper)
        throws BuildException {
        if (parent instanceof TypeAdapter) {
            parent = ((TypeAdapter) parent).getProxy();
        }
        String parentUri = getNamespace();
        Class parentClass = parent.getClass();
        IntrospectionHelper ih = IntrospectionHelper.getHelper(getProject(), parentClass);
        if (children != null) {
            Iterator it = children.iterator();
            for (int i = 0; it.hasNext(); i++) {
                RuntimeConfigurable childWrapper = parentWrapper.getChild(i);
                UnknownElement child = (UnknownElement) it.next();
                try {
                    if (!handleChild(
                            parentUri, ih, parent, child, childWrapper)) {
                        if (!(parent instanceof TaskContainer)) {
                            ih.throwNotSupported(getProject(), parent,
                                                 child.getTag());
                        } else {
                            TaskContainer container = (TaskContainer) parent;
                            container.addTask(child);
                        }
                    }
                } catch (UnsupportedElementException ex) {
                    throw new BuildException(
                        parentWrapper.getElementTag()
                        + " doesn't support the nested \"" + ex.getElement()
                        + "\" element.", ex);
                }
            }
        }
    }
    protected String getComponentName() {
        return ProjectHelper.genComponentName(getNamespace(), getTag());
    }
    public void applyPreSet(UnknownElement u) {
        if (presetDefed) {
            return;
        }
        getWrapper().applyPreSet(u.getWrapper());
        if (u.children != null) {
            List newChildren = new ArrayList();
            newChildren.addAll(u.children);
            if (children != null) {
                newChildren.addAll(children);
            }
            children = newChildren;
        }
        presetDefed = true;
    }
    protected Object makeObject(UnknownElement ue, RuntimeConfigurable w) {
        ComponentHelper helper = ComponentHelper.getComponentHelper(
            getProject());
        String name = ue.getComponentName();
        Object o = helper.createComponent(ue, ue.getNamespace(), name);
        if (o == null) {
            throw getNotFoundException("task or type", name);
        }
        if (o instanceof PreSetDef.PreSetDefinition) {
            PreSetDef.PreSetDefinition def = (PreSetDef.PreSetDefinition) o;
            o = def.createObject(ue.getProject());
            if (o == null) {
                throw getNotFoundException(
                    "preset " + name,
                    def.getPreSets().getComponentName());
            }
            ue.applyPreSet(def.getPreSets());
            if (o instanceof Task) {
                Task task = (Task) o;
                task.setTaskType(ue.getTaskType());
                task.setTaskName(ue.getTaskName());
                task.init();
            }
        }
        if (o instanceof UnknownElement) {
            o = ((UnknownElement) o).makeObject((UnknownElement) o, w);
        }
        if (o instanceof Task) {
            ((Task) o).setOwningTarget(getOwningTarget());
        }
        if (o instanceof ProjectComponent) {
            ((ProjectComponent) o).setLocation(getLocation());
        }
        return o;
    }
    protected Task makeTask(UnknownElement ue, RuntimeConfigurable w) {
        Task task = getProject().createTask(ue.getTag());
        if (task != null) {
            task.setLocation(getLocation());
            task.setOwningTarget(getOwningTarget());
            task.init();
        }
        return task;
    }
    protected BuildException getNotFoundException(String what,
                                                  String name) {
        ComponentHelper helper = ComponentHelper.getComponentHelper(getProject());
        String msg = helper.diagnoseCreationFailure(name, what);
        return new BuildException(msg, getLocation());
    }
    public String getTaskName() {
        return realThing == null
            || !(realThing instanceof Task) ? super.getTaskName()
                                            : ((Task) realThing).getTaskName();
    }
    public Task getTask() {
        if (realThing instanceof Task) {
            return (Task) realThing;
        }
        return null;
    }
    public Object getRealThing() {
        return realThing;
    }
    public void setRealThing(Object realThing) {
        this.realThing = realThing;
    }
    private boolean handleChild(
        String parentUri,
        IntrospectionHelper ih,
        Object parent, UnknownElement child,
        RuntimeConfigurable childWrapper) {
        String childName = ProjectHelper.genComponentName(
            child.getNamespace(), child.getTag());
        if (ih.supportsNestedElement(parentUri, childName, getProject(),
                                     parent)) {
            IntrospectionHelper.Creator creator = null;
            try {
                creator = ih.getElementCreator(getProject(), parentUri,
                                               parent, childName, child);
            } catch (UnsupportedElementException use) {
                if (!ih.isDynamic()) {
                    throw use;
                }
                return false;
            }
            creator.setPolyType(childWrapper.getPolyType());
            Object realChild = creator.create();
            if (realChild instanceof PreSetDef.PreSetDefinition) {
                PreSetDef.PreSetDefinition def =
                    (PreSetDef.PreSetDefinition) realChild;
                realChild = creator.getRealObject();
                child.applyPreSet(def.getPreSets());
            }
            childWrapper.setCreator(creator);
            childWrapper.setProxy(realChild);
            if (realChild instanceof Task) {
                Task childTask = (Task) realChild;
                childTask.setRuntimeConfigurableWrapper(childWrapper);
                childTask.setTaskName(childName);
                childTask.setTaskType(childName);
            }
            if (realChild instanceof ProjectComponent) {
                ((ProjectComponent) realChild).setLocation(child.getLocation());
            }
            childWrapper.maybeConfigure(getProject());
            child.handleChildren(realChild, childWrapper);
            creator.store();
            return true;
        }
        return false;
    }
    public boolean similar(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!getClass().getName().equals(obj.getClass().getName())) {
            return false;
        }
        UnknownElement other = (UnknownElement) obj;
        if (!equalsString(elementName, other.elementName)) {
            return false;
        }
        if (!namespace.equals(other.namespace)) {
            return false;
        }
        if (!qname.equals(other.qname)) {
            return false;
        }
        if (!getWrapper().getAttributeMap().equals(
                other.getWrapper().getAttributeMap())) {
            return false;
        }
        if (!getWrapper().getText().toString().equals(
                other.getWrapper().getText().toString())) {
            return false;
        }
        if (children == null || children.size() == 0) {
            return other.children == null || other.children.size() == 0;
        }
        if (other.children == null) {
            return false;
        }
        if (children.size() != other.children.size()) {
            return false;
        }
        for (int i = 0; i < children.size(); ++i) {
            UnknownElement child = (UnknownElement) children.get(i);
            if (!child.similar(other.children.get(i))) {
                return false;
            }
        }
        return true;
    }
    private static boolean equalsString(String a, String b) {
        return (a == null) ? (b == null) : a.equals(b);
    }
    public UnknownElement copy(Project newProject) {
        UnknownElement ret = new UnknownElement(getTag());
        ret.setNamespace(getNamespace());
        ret.setProject(newProject);
        ret.setQName(getQName());
        ret.setTaskType(getTaskType());
        ret.setTaskName(getTaskName());
        ret.setLocation(getLocation());
        if (getOwningTarget() == null) {
            Target t = new Target();
            t.setProject(getProject());
            ret.setOwningTarget(t);
        } else {
            ret.setOwningTarget(getOwningTarget());
        }
        RuntimeConfigurable copyRC = new RuntimeConfigurable(
            ret, getTaskName());
        copyRC.setPolyType(getWrapper().getPolyType());
        Map m = getWrapper().getAttributeMap();
        for (Iterator i = m.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            copyRC.setAttribute(
                (String) entry.getKey(), (String) entry.getValue());
        }
        copyRC.addText(getWrapper().getText().toString());
        for (Enumeration e = getWrapper().getChildren(); e.hasMoreElements();) {
            RuntimeConfigurable r = (RuntimeConfigurable) e.nextElement();
            UnknownElement ueChild = (UnknownElement) r.getProxy();
            UnknownElement copyChild = ueChild.copy(newProject);
            copyRC.addChild(copyChild.getWrapper());
            ret.addChild(copyChild);
        }
        return ret;
    }
}
