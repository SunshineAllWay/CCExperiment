package org.apache.tools.ant.types.selectors;
import java.io.File;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
public class ExtendSelector extends BaseSelector {
    private String classname = null;
    private FileSelector dynselector = null;
    private Vector paramVec = new Vector();
    private Path classpath = null;
    public ExtendSelector() {
    }
    public void setClassname(String classname) {
        this.classname = classname;
    }
    public void selectorCreate() {
        if (classname != null && classname.length() > 0) {
            try {
                Class c = null;
                if (classpath == null) {
                    c = Class.forName(classname);
                } else {
                    AntClassLoader al
                            = getProject().createClassLoader(classpath);
                    c = Class.forName(classname, true, al);
                }
                dynselector = (FileSelector) c.newInstance();
                final Project p = getProject();
                if (p != null) {
                    p.setProjectReference(dynselector);
                }
            } catch (ClassNotFoundException cnfexcept) {
                setError("Selector " + classname
                    + " not initialized, no such class");
            } catch (InstantiationException iexcept) {
                setError("Selector " + classname
                    + " not initialized, could not create class");
            } catch (IllegalAccessException iaexcept) {
                setError("Selector " + classname
                    + " not initialized, class not accessible");
            }
        } else {
            setError("There is no classname specified");
        }
    }
    public void addParam(Parameter p) {
        paramVec.addElement(p);
    }
    public final void setClasspath(Path classpath) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }
    public final Path createClasspath() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        if (this.classpath == null) {
            this.classpath = new Path(getProject());
        }
        return this.classpath.createPath();
    }
    public final Path getClasspath() {
        return classpath;
    }
    public void setClasspathref(Reference r) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        createClasspath().setRefid(r);
    }
    public void verifySettings() {
        if (dynselector == null) {
            selectorCreate();
        }
        if (classname == null || classname.length() < 1) {
            setError("The classname attribute is required");
        } else if (dynselector == null) {
            setError("Internal Error: The custom selector was not created");
        } else if (!(dynselector instanceof ExtendFileSelector)
                    && (paramVec.size() > 0)) {
            setError("Cannot set parameters on custom selector that does not "
                    + "implement ExtendFileSelector");
        }
    }
    public boolean isSelected(File basedir, String filename, File file)
            throws BuildException {
        validate();
        if (paramVec.size() > 0 && dynselector instanceof ExtendFileSelector) {
            Parameter[] paramArray = new Parameter[paramVec.size()];
            paramVec.copyInto(paramArray);
            ((ExtendFileSelector) dynselector).setParameters(paramArray);
        }
        return dynselector.isSelected(basedir, filename, file);
    }
}
