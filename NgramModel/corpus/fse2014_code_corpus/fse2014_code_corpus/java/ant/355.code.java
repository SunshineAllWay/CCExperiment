package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.optional.javah.JavahAdapter;
import org.apache.tools.ant.taskdefs.optional.javah.JavahAdapterFactory;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.facade.FacadeTaskHelper;
import org.apache.tools.ant.util.facade.ImplementationSpecificArgument;
public class Javah extends Task {
    private Vector classes = new Vector(2);
    private String cls;
    private File destDir;
    private Path classpath = null;
    private File outputFile = null;
    private boolean verbose = false;
    private boolean force   = false;
    private boolean old     = false;
    private boolean stubs   = false;
    private Path bootclasspath;
    private FacadeTaskHelper facade = null;
    private Vector files = new Vector();
    private JavahAdapter nestedAdapter = null;
    public Javah() {
        facade = new FacadeTaskHelper(JavahAdapterFactory.getDefault());
    }
    public void setClass(String cls) {
        this.cls = cls;
    }
    public ClassArgument createClass() {
        ClassArgument ga = new ClassArgument();
        classes.addElement(ga);
        return ga;
    }
    public class ClassArgument {
        private String name;
        public ClassArgument() {
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    public void addFileSet(FileSet fs) {
        files.add(fs);
    }
    public String[] getClasses() {
        ArrayList al = new ArrayList();
        if (cls != null) {
            StringTokenizer tok = new StringTokenizer(cls, ",", false);
            while (tok.hasMoreTokens()) {
                al.add(tok.nextToken().trim());
            }
        }
        if (files.size() > 0) {
            for (Enumeration e = files.elements(); e.hasMoreElements();) {
                FileSet fs = (FileSet) e.nextElement();
                String[] includedClasses = fs.getDirectoryScanner(
                    getProject()).getIncludedFiles();
                for (int i = 0; i < includedClasses.length; i++) {
                    String className =
                        includedClasses[i].replace('\\', '.').replace('/', '.')
                        .substring(0, includedClasses[i].length() - 6);
                    al.add(className);
                }
            }
        }
        Enumeration e = classes.elements();
        while (e.hasMoreElements()) {
            ClassArgument arg = (ClassArgument) e.nextElement();
            al.add(arg.getName());
        }
        return (String[]) al.toArray(new String[al.size()]);
    }
    public void setDestdir(File destDir) {
        this.destDir = destDir;
    }
    public File getDestdir() {
        return destDir;
    }
    public void setClasspath(Path src) {
        if (classpath == null) {
            classpath = src;
        } else {
            classpath.append(src);
        }
    }
    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }
        return classpath.createPath();
    }
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    public Path getClasspath() {
        return classpath;
    }
    public void setBootclasspath(Path src) {
        if (bootclasspath == null) {
            bootclasspath = src;
        } else {
            bootclasspath.append(src);
        }
    }
    public Path createBootclasspath() {
        if (bootclasspath == null) {
            bootclasspath = new Path(getProject());
        }
        return bootclasspath.createPath();
    }
    public void setBootClasspathRef(Reference r) {
        createBootclasspath().setRefid(r);
    }
    public Path getBootclasspath() {
        return bootclasspath;
    }
    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
    public File getOutputfile() {
        return outputFile;
    }
    public void setForce(boolean force) {
        this.force = force;
    }
    public boolean getForce() {
        return force;
    }
    public void setOld(boolean old) {
        this.old = old;
    }
    public boolean getOld() {
        return old;
    }
    public void setStubs(boolean stubs) {
        this.stubs = stubs;
    }
    public boolean getStubs() {
        return stubs;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public boolean getVerbose() {
        return verbose;
    }
    public void setImplementation(String impl) {
        if ("default".equals(impl)) {
            facade.setImplementation(JavahAdapterFactory.getDefault());
        } else {
            facade.setImplementation(impl);
        }
    }
    public ImplementationSpecificArgument createArg() {
        ImplementationSpecificArgument arg =
            new ImplementationSpecificArgument();
        facade.addImplementationArgument(arg);
        return arg;
    }
    public String[] getCurrentArgs() {
        return facade.getArgs();
    }
    public Path createImplementationClasspath() {
        return facade.getImplementationClasspath(getProject());
    }
    public void add(JavahAdapter adapter) {
        if (nestedAdapter != null) {
            throw new BuildException("Can't have more than one javah"
                                     + " adapter");
        }
        nestedAdapter = adapter;
    }
    public void execute() throws BuildException {
        if ((cls == null) && (classes.size() == 0) && (files.size() == 0)) {
            throw new BuildException("class attribute must be set!",
                getLocation());
        }
        if ((cls != null) && (classes.size() > 0) && (files.size() > 0)) {
            throw new BuildException("set class attribute OR class element OR fileset, "
                + "not 2 or more of them.", getLocation());
        }
        if (destDir != null) {
            if (!destDir.isDirectory()) {
                throw new BuildException("destination directory \"" + destDir
                    + "\" does not exist or is not a directory", getLocation());
            }
            if (outputFile != null) {
                throw new BuildException("destdir and outputFile are mutually "
                    + "exclusive", getLocation());
            }
        }
        if (classpath == null) {
            classpath = (new Path(getProject())).concatSystemClasspath("last");
        } else {
            classpath = classpath.concatSystemClasspath("ignore");
        }
        JavahAdapter ad =
            nestedAdapter != null ? nestedAdapter :
            JavahAdapterFactory.getAdapter(facade.getImplementation(),
                                           this,
                                           createImplementationClasspath());
        if (!ad.compile(this)) {
            throw new BuildException("compilation failed");
        }
    }
    public void logAndAddFiles(Commandline cmd) {
        logAndAddFilesToCompile(cmd);
    }
    protected void logAndAddFilesToCompile(Commandline cmd) {
        log("Compilation " + cmd.describeArguments(),
            Project.MSG_VERBOSE);
        StringBuffer niceClassList = new StringBuffer();
        String[] c = getClasses();
        for (int i = 0; i < c.length; i++) {
            cmd.createArgument().setValue(c[i]);
            niceClassList.append("    ");
            niceClassList.append(c[i]);
            niceClassList.append(StringUtils.LINE_SEP);
        }
        StringBuffer prefix = new StringBuffer("Class");
        if (c.length > 1) {
            prefix.append("es");
        }
        prefix.append(" to be compiled:");
        prefix.append(StringUtils.LINE_SEP);
        log(prefix.toString() + niceClassList.toString(), Project.MSG_VERBOSE);
    }
}