package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.Set;
import java.util.HashSet;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.VectorSet;
public class Ant extends Task {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File dir = null;
    private String antFile = null;
    private String output = null;
    private boolean inheritAll = true;
    private boolean inheritRefs = false;
    private Vector properties = new Vector();
    private Vector references = new Vector();
    private Project newProject;
    private PrintStream out = null;
    private Vector propertySets = new Vector();
    private Vector targets = new Vector();
    private boolean targetAttributeSet = false;
    private boolean useNativeBasedir = false;
    public Ant() {
    }
    public Ant(Task owner) {
        bindToOwner(owner);
    }
    public void setUseNativeBasedir(boolean b) {
        useNativeBasedir = b;
    }
    public void setInheritAll(boolean value) {
        inheritAll = value;
    }
    public void setInheritRefs(boolean value) {
        inheritRefs = value;
    }
    public void init() {
        newProject = getProject().createSubProject();
        newProject.setJavaVersionProperty();
    }
    private void reinit() {
        init();
    }
    private void initializeProject() {
        newProject.setInputHandler(getProject().getInputHandler());
        Iterator iter = getBuildListeners();
        while (iter.hasNext()) {
            newProject.addBuildListener((BuildListener) iter.next());
        }
        if (output != null) {
            File outfile = null;
            if (dir != null) {
                outfile = FILE_UTILS.resolveFile(dir, output);
            } else {
                outfile = getProject().resolveFile(output);
            }
            try {
                out = new PrintStream(new FileOutputStream(outfile));
                DefaultLogger logger = new DefaultLogger();
                logger.setMessageOutputLevel(Project.MSG_INFO);
                logger.setOutputPrintStream(out);
                logger.setErrorPrintStream(out);
                newProject.addBuildListener(logger);
            } catch (IOException ex) {
                log("Ant: Can't set output to " + output);
            }
        }
        if (useNativeBasedir) {
            addAlmostAll(getProject().getUserProperties(), PropertyType.USER);
        } else {
            getProject().copyUserProperties(newProject);
        }
        if (!inheritAll) {
           newProject.initProperties();
        } else {
            addAlmostAll(getProject().getProperties(), PropertyType.PLAIN);
        }
        Enumeration e = propertySets.elements();
        while (e.hasMoreElements()) {
            PropertySet ps = (PropertySet) e.nextElement();
            addAlmostAll(ps.getProperties(), PropertyType.PLAIN);
        }
    }
    public void handleOutput(String outputToHandle) {
        if (newProject != null) {
            newProject.demuxOutput(outputToHandle, false);
        } else {
            super.handleOutput(outputToHandle);
        }
    }
    public int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        if (newProject != null) {
            return newProject.demuxInput(buffer, offset, length);
        }
        return super.handleInput(buffer, offset, length);
    }
    public void handleFlush(String toFlush) {
        if (newProject != null) {
            newProject.demuxFlush(toFlush, false);
        } else {
            super.handleFlush(toFlush);
        }
    }
    public void handleErrorOutput(String errorOutputToHandle) {
        if (newProject != null) {
            newProject.demuxOutput(errorOutputToHandle, true);
        } else {
            super.handleErrorOutput(errorOutputToHandle);
        }
    }
    public void handleErrorFlush(String errorOutputToFlush) {
        if (newProject != null) {
            newProject.demuxFlush(errorOutputToFlush, true);
        } else {
            super.handleErrorFlush(errorOutputToFlush);
        }
    }
    public void execute() throws BuildException {
        File savedDir = dir;
        String savedAntFile = antFile;
        Vector locals = new VectorSet(targets);
        try {
            getNewProject();
            if (dir == null && inheritAll) {
                dir = getProject().getBaseDir();
            }
            initializeProject();
            if (dir != null) {
                if (!useNativeBasedir) {
                    newProject.setBaseDir(dir);
                    if (savedDir != null) {
                        newProject.setInheritedProperty(MagicNames.PROJECT_BASEDIR,
                                                        dir.getAbsolutePath());
                    }
                }
            } else {
                dir = getProject().getBaseDir();
            }
            overrideProperties();
            if (antFile == null) {
                antFile = getDefaultBuildFile();
            }
            File file = FILE_UTILS.resolveFile(dir, antFile);
            antFile = file.getAbsolutePath();
            log("calling target(s) "
                + ((locals.size() > 0) ? locals.toString() : "[default]")
                + " in build file " + antFile, Project.MSG_VERBOSE);
            newProject.setUserProperty(MagicNames.ANT_FILE , antFile);
            String thisAntFile = getProject().getProperty(MagicNames.ANT_FILE);
            if (thisAntFile != null
                && file.equals(getProject().resolveFile(thisAntFile))
                && getOwningTarget() != null) {
                if (getOwningTarget().getName().equals("")) {
                    if (getTaskName().equals("antcall")) {
                        throw new BuildException("antcall must not be used at"
                                                 + " the top level.");
                    }
                    throw new BuildException(getTaskName() + " task at the"
                                + " top level must not invoke"
                                + " its own build file.");
                }
            }
            try {
                ProjectHelper.configureProject(newProject, file);
            } catch (BuildException ex) {
                throw ProjectHelper.addLocationToBuildException(
                    ex, getLocation());
            }
            if (locals.size() == 0) {
                String defaultTarget = newProject.getDefaultTarget();
                if (defaultTarget != null) {
                    locals.add(defaultTarget);
                }
            }
            if (newProject.getProperty(MagicNames.ANT_FILE)
                .equals(getProject().getProperty(MagicNames.ANT_FILE))
                && getOwningTarget() != null) {
                String owningTargetName = getOwningTarget().getName();
                if (locals.contains(owningTargetName)) {
                    throw new BuildException(getTaskName() + " task calling "
                                             + "its own parent target.");
                }
                boolean circular = false;
                for (Iterator it = locals.iterator();
                     !circular && it.hasNext();) {
                    Target other =
                        (Target) (getProject().getTargets().get(it.next()));
                    circular |= (other != null
                                 && other.dependsOn(owningTargetName));
                }
                if (circular) {
                    throw new BuildException(getTaskName()
                                             + " task calling a target"
                                             + " that depends on"
                                             + " its parent target \'"
                                             + owningTargetName
                                             + "\'.");
                }
            }
            addReferences();
            if (locals.size() > 0 && !(locals.size() == 1
                                       && "".equals(locals.get(0)))) {
                BuildException be = null;
                try {
                    log("Entering " + antFile + "...", Project.MSG_VERBOSE);
                    newProject.fireSubBuildStarted();
                    newProject.executeTargets(locals);
                } catch (BuildException ex) {
                    be = ProjectHelper
                        .addLocationToBuildException(ex, getLocation());
                    throw be;
                } finally {
                    log("Exiting " + antFile + ".", Project.MSG_VERBOSE);
                    newProject.fireSubBuildFinished(be);
                }
            }
        } finally {
            newProject = null;
            Enumeration e = properties.elements();
            while (e.hasMoreElements()) {
                Property p = (Property) e.nextElement();
                p.setProject(null);
            }
            if (output != null && out != null) {
                try {
                    out.close();
                } catch (final Exception ex) {
                }
            }
            dir = savedDir;
            antFile = savedAntFile;
        }
    }
    protected String getDefaultBuildFile() {
        return Main.DEFAULT_BUILD_FILENAME;
    }
    private void overrideProperties() throws BuildException {
        Set set = new HashSet();
        for (int i = properties.size() - 1; i >= 0; --i) {
            Property p = (Property) properties.get(i);
            if (p.getName() != null && !p.getName().equals("")) {
                if (set.contains(p.getName())) {
                    properties.remove(i);
                } else {
                    set.add(p.getName());
                }
            }
        }
        Enumeration e = properties.elements();
        while (e.hasMoreElements()) {
            Property p = (Property) e.nextElement();
            p.setProject(newProject);
            p.execute();
        }
        if (useNativeBasedir) {
            addAlmostAll(getProject().getInheritedProperties(),
                         PropertyType.INHERITED);
        } else {
            getProject().copyInheritedProperties(newProject);
        }
    }
    private void addReferences() throws BuildException {
        Hashtable thisReferences
            = (Hashtable) getProject().getReferences().clone();
        Hashtable newReferences = newProject.getReferences();
        Enumeration e;
        if (references.size() > 0) {
            for (e = references.elements(); e.hasMoreElements();) {
                Reference ref = (Reference) e.nextElement();
                String refid = ref.getRefId();
                if (refid == null) {
                    throw new BuildException("the refid attribute is required"
                                             + " for reference elements");
                }
                if (!thisReferences.containsKey(refid)) {
                    log("Parent project doesn't contain any reference '"
                        + refid + "'",
                        Project.MSG_WARN);
                    continue;
                }
                thisReferences.remove(refid);
                String toRefid = ref.getToRefid();
                if (toRefid == null) {
                    toRefid = refid;
                }
                copyReference(refid, toRefid);
            }
        }
        if (inheritRefs) {
            for (e = thisReferences.keys(); e.hasMoreElements();) {
                String key = (String) e.nextElement();
                if (newReferences.containsKey(key)) {
                    continue;
                }
                copyReference(key, key);
                newProject.inheritIDReferences(getProject());
            }
        }
    }
    private void copyReference(String oldKey, String newKey) {
        Object orig = getProject().getReference(oldKey);
        if (orig == null) {
            log("No object referenced by " + oldKey + ". Can't copy to "
                + newKey,
                Project.MSG_WARN);
            return;
        }
        Class c = orig.getClass();
        Object copy = orig;
        try {
            Method cloneM = c.getMethod("clone", new Class[0]);
            if (cloneM != null) {
                copy = cloneM.invoke(orig, new Object[0]);
                log("Adding clone of reference " + oldKey, Project.MSG_DEBUG);
            }
        } catch (Exception e) {
        }
        if (copy instanceof ProjectComponent) {
            ((ProjectComponent) copy).setProject(newProject);
        } else {
            try {
                Method setProjectM =
                    c.getMethod("setProject", new Class[] {Project.class});
                if (setProjectM != null) {
                    setProjectM.invoke(copy, new Object[] {newProject});
                }
            } catch (NoSuchMethodException e) {
            } catch (Exception e2) {
                String msg = "Error setting new project instance for "
                    + "reference with id " + oldKey;
                throw new BuildException(msg, e2, getLocation());
            }
        }
        newProject.addReference(newKey, copy);
    }
    private void addAlmostAll(Hashtable props, PropertyType type) {
        Enumeration e = props.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement().toString();
            if (MagicNames.PROJECT_BASEDIR.equals(key)
                || MagicNames.ANT_FILE.equals(key)) {
                continue;
            }
            String value = props.get(key).toString();
            if (type == PropertyType.PLAIN) {
                if (newProject.getProperty(key) == null) {
                    newProject.setNewProperty(key, value);
                }
            } else if (type == PropertyType.USER) {
                newProject.setUserProperty(key, value);
            } else if (type == PropertyType.INHERITED) {
                newProject.setInheritedProperty(key, value);
            }
        }
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    public void setAntfile(String antFile) {
        this.antFile = antFile;
    }
    public void setTarget(String targetToAdd) {
        if (targetToAdd.equals("")) {
            throw new BuildException("target attribute must not be empty");
        }
        targets.add(targetToAdd);
        targetAttributeSet = true;
    }
    public void setOutput(String outputFile) {
        this.output = outputFile;
    }
    public Property createProperty() {
        Property p = new Property(true, getProject());
        p.setProject(getNewProject());
        p.setTaskName("property");
        properties.addElement(p);
        return p;
    }
    public void addReference(Reference ref) {
        references.addElement(ref);
    }
    public void addConfiguredTarget(TargetElement t) {
        if (targetAttributeSet) {
            throw new BuildException(
                "nested target is incompatible with the target attribute");
        }
        String name = t.getName();
        if (name.equals("")) {
            throw new BuildException("target name must not be empty");
        }
        targets.add(name);
    }
    public void addPropertyset(PropertySet ps) {
        propertySets.addElement(ps);
    }
    protected Project getNewProject() {
        if (newProject == null) {
            reinit();
        }
        return newProject;
    }
    private Iterator getBuildListeners() {
        return getProject().getBuildListeners().iterator();
    }
    public static class Reference
        extends org.apache.tools.ant.types.Reference {
        public Reference() {
                super();
        }
        private String targetid = null;
        public void setToRefid(String targetid) {
            this.targetid = targetid;
        }
        public String getToRefid() {
            return targetid;
        }
    }
    public static class TargetElement {
        private String name;
        public TargetElement() {
        }
        public void setName(String name) {
            this.name = name;
        }
        public String getName() {
            return name;
        }
    }
    private static final class PropertyType {
        private PropertyType() {}
        private static final PropertyType PLAIN = new PropertyType();
        private static final PropertyType INHERITED = new PropertyType();
        private static final PropertyType USER = new PropertyType();
    }
}
