package org.apache.tools.ant.types;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public final class AntFilterReader
    extends DataType implements Cloneable {
    private String className;
    private final Vector parameters = new Vector();
    private Path classpath;
    public void setClassName(final String className) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.className = className;
    }
    public String getClassName() {
        if (isReference()) {
            return ((AntFilterReader) getCheckedRef()).getClassName();
        }
        dieOnCircularReference();
        return className;
    }
    public void addParam(final Parameter param) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        parameters.addElement(param);
    }
    public void setClasspath(Path classpath) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
        setChecked(false);
    }
    public Path createClasspath() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        setChecked(false);
        return this.classpath.createPath();
    }
    public Path getClasspath() {
        if (isReference()) {
            ((AntFilterReader) getCheckedRef()).getClasspath();
        }
        dieOnCircularReference();
        return classpath;
    }
    public void setClasspathRef(Reference r) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        createClasspath().setRefid(r);
    }
    public Parameter[] getParams() {
        if (isReference()) {
            ((AntFilterReader) getCheckedRef()).getParams();
        }
        dieOnCircularReference();
        Parameter[] params = new Parameter[parameters.size()];
        parameters.copyInto(params);
        return params;
    }
    public void setRefid(Reference r) throws BuildException {
        if (!parameters.isEmpty() || className != null
                || classpath != null) {
            throw tooManyAttributes();
        }
        super.setRefid(r);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            if (classpath != null) {
                pushAndInvokeCircularReferenceCheck(classpath, stk, p);
            }
            setChecked(true);
        }
    }
}
