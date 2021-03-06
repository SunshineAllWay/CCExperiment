package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.KeepAliveOutputStream;
public class FormatterElement {
    private String classname;
    private String extension;
    private OutputStream out = new KeepAliveOutputStream(System.out);
    private File outFile;
    private boolean useFile = true;
    private Object ifCond;
    private Object unlessCond;
    private Project project;
    public static final String XML_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.XMLJUnitResultFormatter";
    public static final String BRIEF_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.BriefJUnitResultFormatter";
    public static final String PLAIN_FORMATTER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.PlainJUnitResultFormatter";
    public static final String FAILURE_RECORDER_CLASS_NAME =
        "org.apache.tools.ant.taskdefs.optional.junit.FailureRecorder";
    public void setType(TypeAttribute type) {
        if ("xml".equals(type.getValue())) {
            setClassname(XML_FORMATTER_CLASS_NAME);
        } else {
            if ("brief".equals(type.getValue())) {
                setClassname(BRIEF_FORMATTER_CLASS_NAME);
            } else {
                if ("failure".equals(type.getValue())) {
                    setClassname(FAILURE_RECORDER_CLASS_NAME);
                } else { 
                    setClassname(PLAIN_FORMATTER_CLASS_NAME);
                }
            }
        }
    }
    public void setClassname(String classname) {
        this.classname = classname;
        if (XML_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".xml");
        } else if (PLAIN_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".txt");
        } else if (BRIEF_FORMATTER_CLASS_NAME.equals(classname)) {
           setExtension(".txt");
        }
    }
    public String getClassname() {
        return classname;
    }
    public void setExtension(String ext) {
        this.extension = ext;
    }
    public String getExtension() {
        return extension;
    }
    void setOutfile(File out) {
        this.outFile = out;
    }
    public void setOutput(OutputStream out) {
        if (out == System.out || out == System.err) {
            out = new KeepAliveOutputStream(out);
        }
        this.out = out;
    }
    public void setUseFile(boolean useFile) {
        this.useFile = useFile;
    }
    boolean getUseFile() {
        return useFile;
    }
    public void setIf(Object ifCond) {
        this.ifCond = ifCond;
    }
    public void setIf(String ifCond) {
        setIf((Object) ifCond);
    }
    public void setUnless(Object unlessCond) {
        this.unlessCond = unlessCond;
    }
    public void setUnless(String unlessCond) {
        setUnless((Object) unlessCond);
    }
    public boolean shouldUse(Task t) {
        PropertyHelper ph = PropertyHelper.getPropertyHelper(t.getProject());
        return ph.testIfCondition(ifCond)
            && ph.testUnlessCondition(unlessCond);
    }
    JUnitTaskMirror.JUnitResultFormatterMirror createFormatter() throws BuildException {
        return createFormatter(null);
    }
    public void setProject(Project project) {
        this.project = project;
    }
    JUnitTaskMirror.JUnitResultFormatterMirror createFormatter(ClassLoader loader)
            throws BuildException {
        if (classname == null) {
            throw new BuildException("you must specify type or classname");
        }
        Class f = null;
        try {
            if (loader == null) {
                f = Class.forName(classname);
            } else {
                f = Class.forName(classname, true, loader);
            }
        } catch (ClassNotFoundException e) {
            throw new BuildException(
                "Using loader " + loader + " on class " + classname
                + ": " + e, e);
        } catch (NoClassDefFoundError e) {
            throw new BuildException(
                "Using loader " + loader + " on class " + classname
                + ": " + e, e);
        }
        Object o = null;
        try {
            o = f.newInstance();
        } catch (InstantiationException e) {
            throw new BuildException(e);
        } catch (IllegalAccessException e) {
            throw new BuildException(e);
        }
        if (!(o instanceof JUnitTaskMirror.JUnitResultFormatterMirror)) {
            throw new BuildException(classname + " is not a JUnitResultFormatter");
        }
        JUnitTaskMirror.JUnitResultFormatterMirror r =
            (JUnitTaskMirror.JUnitResultFormatterMirror) o;
        if (useFile && outFile != null) {
            try {
                out = new BufferedOutputStream(new FileOutputStream(outFile));
            } catch (java.io.IOException e) {
                throw new BuildException("Unable to open file " + outFile, e);
            }
        }
        r.setOutput(out);
        boolean needToSetProjectReference = true;
        try {
            Field field = r.getClass().getField("project");
            Object value = field.get(r);
            if (value instanceof Project) {
                needToSetProjectReference = false;
            }
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
            throw new BuildException(e);
        }
        if (needToSetProjectReference) {
            Method setter;
            try {
                setter = r.getClass().getMethod("setProject", new Class[] {Project.class});
                setter.invoke(r, new Object[] {project});
            } catch (NoSuchMethodException e) {
            } catch (IllegalAccessException e) {
                throw new BuildException(e);
            } catch (InvocationTargetException e) {
                throw new BuildException(e);
            }
        }
        return r;
    }
    public static class TypeAttribute extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"plain", "xml", "brief", "failure"};
        }
    }
}
