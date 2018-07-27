package org.apache.tools.ant.types;
import java.util.Stack;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.util.IdentityStack;
public abstract class DataType extends ProjectComponent implements Cloneable {
    protected Reference ref;
    protected boolean checked = true;
    public boolean isReference() {
        return ref != null;
    }
    public void setRefid(final Reference ref) {
        this.ref = ref;
        checked = false;
    }
    protected String getDataTypeName() {
        return ComponentHelper.getElementName(getProject(), this, true);
    }
    protected void dieOnCircularReference() {
        dieOnCircularReference(getProject());
    }
    protected void dieOnCircularReference(Project p) {
        if (checked || !isReference()) {
            return;
        }
        dieOnCircularReference(new IdentityStack(this), p);
    }
    protected void dieOnCircularReference(final Stack stack,
                                          final Project project)
        throws BuildException {
        if (checked || !isReference()) {
            return;
        }
        Object o = ref.getReferencedObject(project);
        if (o instanceof DataType) {
            IdentityStack id = IdentityStack.getInstance(stack);
            if (id.contains(o)) {
                throw circularReference();
            } else {
                id.push(o);
                ((DataType) o).dieOnCircularReference(id, project);
                id.pop();
            }
        }
        checked = true;
    }
    public static void invokeCircularReferenceCheck(DataType dt, Stack stk,
                                                    Project p) {
        dt.dieOnCircularReference(stk, p);
    }
    public static void pushAndInvokeCircularReferenceCheck(DataType dt,
                                                           Stack stk,
                                                           Project p) {
        stk.push(dt);
        dt.dieOnCircularReference(stk, p);
        stk.pop();
    }
    protected Object getCheckedRef() {
        return getCheckedRef(getProject());
    }
    protected Object getCheckedRef(Project p) {
        return getCheckedRef(getClass(), getDataTypeName(), p);
    }
    protected Object getCheckedRef(final Class requiredClass,
                                   final String dataTypeName) {
        return getCheckedRef(requiredClass, dataTypeName, getProject());
    }
    protected Object getCheckedRef(final Class requiredClass,
                                   final String dataTypeName, final Project project) {
        if (project == null) {
            throw new BuildException("No Project specified");
        }
        dieOnCircularReference(project);
        Object o = ref.getReferencedObject(project);
        if (!(requiredClass.isAssignableFrom(o.getClass()))) {
            log("Class " + o.getClass() + " is not a subclass of " + requiredClass,
                    Project.MSG_VERBOSE);
            String msg = ref.getRefId() + " doesn\'t denote a " + dataTypeName;
            throw new BuildException(msg);
        }
        return o;
    }
    protected BuildException tooManyAttributes() {
        return new BuildException("You must not specify more than one "
            + "attribute when using refid");
    }
    protected BuildException noChildrenAllowed() {
        return new BuildException("You must not specify nested elements "
            + "when using refid");
    }
    protected BuildException circularReference() {
        return new BuildException("This data type contains a circular "
            + "reference.");
    }
    protected boolean isChecked() {
        return checked;
    }
    protected void setChecked(final boolean checked) {
        this.checked = checked;
    }
    public Reference getRefid() {
        return ref;
    }
    protected void checkAttributesAllowed() {
        if (isReference()) {
            throw tooManyAttributes();
        }
    }
    protected void checkChildrenAllowed() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
    }
    public String toString() {
        String d = getDescription();
        return d == null ? getDataTypeName() : getDataTypeName() + " " + d;
    }
    public Object clone() throws CloneNotSupportedException {
        DataType dt = (DataType) super.clone();
        dt.setDescription(getDescription());
        if (getRefid() != null) {
            dt.setRefid(getRefid());
        }
        dt.setChecked(isChecked());
        return dt;
    }
}
