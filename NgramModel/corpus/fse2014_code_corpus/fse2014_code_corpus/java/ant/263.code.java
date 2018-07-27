package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.FileList;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.taskdefs.Ant.TargetElement;
public class SubAnt extends Task {
    private Path buildpath;
    private Ant ant = null;
    private String subTarget = null;
    private String antfile = getDefaultBuildFile();
    private File genericantfile = null;
    private boolean verbose = false;
    private boolean inheritAll = false;
    private boolean inheritRefs = false;
    private boolean failOnError = true;
    private String output  = null;
    private Vector properties = new Vector();
    private Vector references = new Vector();
    private Vector propertySets = new Vector();
    private Vector targets = new Vector();
    protected String getDefaultBuildFile() {
        return Main.DEFAULT_BUILD_FILENAME;
    }
    public void handleOutput(String output) {
        if (ant != null) {
            ant.handleOutput(output);
        } else {
            super.handleOutput(output);
        }
    }
    public int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        if (ant != null) {
            return ant.handleInput(buffer, offset, length);
        } else {
            return super.handleInput(buffer, offset, length);
        }
    }
    public void handleFlush(String output) {
        if (ant != null) {
            ant.handleFlush(output);
        } else {
            super.handleFlush(output);
        }
    }
    public void handleErrorOutput(String output) {
        if (ant != null) {
            ant.handleErrorOutput(output);
        } else {
            super.handleErrorOutput(output);
        }
    }
    public void handleErrorFlush(String output) {
        if (ant != null) {
            ant.handleErrorFlush(output);
        } else {
            super.handleErrorFlush(output);
        }
    }
    public void execute() {
        if (buildpath == null) {
            throw new BuildException("No buildpath specified");
        }
        final String[] filenames = buildpath.list();
        final int count = filenames.length;
        if (count < 1) {
            log("No sub-builds to iterate on", Project.MSG_WARN);
            return;
        }
        BuildException buildException = null;
        for (int i = 0; i < count; ++i) {
            File file = null;
            String subdirPath = null;
            Throwable thrownException = null;
            try {
                File directory = null;
                file = new File(filenames[i]);
                if (file.isDirectory()) {
                    if (verbose) {
                        subdirPath = file.getPath();
                        log("Entering directory: " + subdirPath + "\n", Project.MSG_INFO);
                    }
                    if (genericantfile != null) {
                        directory = file;
                        file = genericantfile;
                    } else {
                        file = new File(file, antfile);
                    }
                }
                execute(file, directory);
                if (verbose && subdirPath != null) {
                    log("Leaving directory: " + subdirPath + "\n", Project.MSG_INFO);
                }
            } catch (RuntimeException ex) {
                if (!(getProject().isKeepGoingMode())) {
                    if (verbose && subdirPath != null) {
                        log("Leaving directory: " + subdirPath + "\n", Project.MSG_INFO);
                    }
                    throw ex; 
                }
                thrownException = ex;
            } catch (Throwable ex) {
                if (!(getProject().isKeepGoingMode())) {
                    if (verbose && subdirPath != null) {
                        log("Leaving directory: " + subdirPath + "\n", Project.MSG_INFO);
                    }
                    throw new BuildException(ex);
                }
                thrownException = ex;
            }
            if (thrownException != null) {
                if (thrownException instanceof BuildException) {
                    log("File '" + file
                        + "' failed with message '"
                        + thrownException.getMessage() + "'.", Project.MSG_ERR);
                    if (buildException == null) {
                        buildException = (BuildException) thrownException;
                    }
                } else {
                    log("Target '" + file
                        + "' failed with message '"
                        + thrownException.getMessage() + "'.", Project.MSG_ERR);
                    thrownException.printStackTrace(System.err);
                    if (buildException == null) {
                        buildException =
                            new BuildException(thrownException);
                    }
                }
                if (verbose && subdirPath != null) {
                    log("Leaving directory: " + subdirPath + "\n", Project.MSG_INFO);
                }
            }
        }
        if (buildException != null) {
            throw buildException;
        }
    }
    private void execute(File file, File directory)
                throws BuildException {
        if (!file.exists() || file.isDirectory() || !file.canRead()) {
            String msg = "Invalid file: " + file;
            if (failOnError) {
                throw new BuildException(msg);
            }
            log(msg, Project.MSG_WARN);
            return;
        }
        ant = createAntTask(directory);
        String antfilename = file.getAbsolutePath();
        ant.setAntfile(antfilename);
        for (int i = 0; i < targets.size(); i++) {
            TargetElement targetElement = (TargetElement) targets.get(i);
            ant.addConfiguredTarget(targetElement);
        }
        try {
            ant.execute();
        } catch (BuildException e) {
            if (failOnError || isHardError(e)) {
                throw e;
            }
            log("Failure for target '" + subTarget
               + "' of: " +  antfilename + "\n"
               + e.getMessage(), Project.MSG_WARN);
        } catch (Throwable e) {
            if (failOnError || isHardError(e)) {
                throw new BuildException(e);
            }
            log("Failure for target '" + subTarget
                + "' of: " + antfilename + "\n"
                + e.toString(),
                Project.MSG_WARN);
        } finally {
            ant = null;
        }
    }
    private boolean isHardError(Throwable t) {
        if (t instanceof BuildException) {
            return isHardError(t.getCause());
        } else if (t instanceof OutOfMemoryError) {
            return true;
        } else if (t instanceof ThreadDeath) {
            return true;
        } else { 
            return false;
        }
    }
    public void setAntfile(String antfile) {
        this.antfile = antfile;
    }
    public void setGenericAntfile(File afile) {
        this.genericantfile = afile;
    }
    public void setFailonerror(boolean failOnError) {
        this.failOnError = failOnError;
    }
    public void setTarget(String target) {
        this.subTarget = target;
    }
    public void addConfiguredTarget(TargetElement t) {
        String name = t.getName();
        if ("".equals(name)) {
            throw new BuildException("target name must not be empty");
        }
        targets.add(t);
    }
    public void setVerbose(boolean on) {
        this.verbose = on;
    }
    public void setOutput(String s) {
        this.output = s;
    }
    public void setInheritall(boolean b) {
        this.inheritAll = b;
    }
    public void setInheritrefs(boolean b) {
        this.inheritRefs = b;
    }
    public void addProperty(Property p) {
        properties.addElement(p);
    }
    public void addReference(Ant.Reference r) {
        references.addElement(r);
    }
    public void addPropertyset(PropertySet ps) {
        propertySets.addElement(ps);
    }
    public void addDirset(DirSet set) {
        add(set);
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void addFilelist(FileList list) {
        add(list);
    }
    public void add(ResourceCollection rc) {
        getBuildpath().add(rc);
    }
    public void setBuildpath(Path s) {
        getBuildpath().append(s);
    }
    public Path createBuildpath() {
        return getBuildpath().createPath();
    }
    public Path.PathElement createBuildpathElement() {
        return getBuildpath().createPathElement();
    }
    private Path getBuildpath() {
        if (buildpath == null) {
            buildpath = new Path(getProject());
        }
        return buildpath;
    }
    public void setBuildpathRef(Reference r) {
        createBuildpath().setRefid(r);
    }
    private Ant createAntTask(File directory) {
        Ant antTask = new Ant(this);
        antTask.init();
        if (subTarget != null && subTarget.length() > 0) {
            antTask.setTarget(subTarget);
        }
        if (output != null) {
            antTask.setOutput(output);
        }
        if (directory != null) {
            antTask.setDir(directory);
        } else {
            antTask.setUseNativeBasedir(true);
        }
        antTask.setInheritAll(inheritAll);
        for (Enumeration i = properties.elements(); i.hasMoreElements();) {
            copyProperty(antTask.createProperty(), (Property) i.nextElement());
        }
        for (Enumeration i = propertySets.elements(); i.hasMoreElements();) {
            antTask.addPropertyset((PropertySet) i.nextElement());
        }
        antTask.setInheritRefs(inheritRefs);
        for (Enumeration i = references.elements(); i.hasMoreElements();) {
            antTask.addReference((Ant.Reference) i.nextElement());
        }
        return antTask;
    }
    private static void copyProperty(Property to, Property from) {
        to.setName(from.getName());
        if (from.getValue() != null) {
            to.setValue(from.getValue());
        }
        if (from.getFile() != null) {
            to.setFile(from.getFile());
        }
        if (from.getResource() != null) {
            to.setResource(from.getResource());
        }
        if (from.getPrefix() != null) {
            to.setPrefix(from.getPrefix());
        }
        if (from.getRefid() != null) {
            to.setRefid(from.getRefid());
        }
        if (from.getEnvironment() != null) {
            to.setEnvironment(from.getEnvironment());
        }
        if (from.getClasspath() != null) {
            to.setClasspath(from.getClasspath());
        }
    }
} 
