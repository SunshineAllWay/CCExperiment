package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ComponentHelper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.types.resources.URLResource;
public class Antlib extends Task implements TaskContainer {
    public static final String TAG = "antlib";
    public static Antlib createAntlib(Project project, URL antlibUrl,
                                      String uri) {
        try {
            antlibUrl.openConnection().connect();
        } catch (IOException ex) {
            throw new BuildException(
                "Unable to find " + antlibUrl, ex);
        }
        ComponentHelper helper =
            ComponentHelper.getComponentHelper(project);
        helper.enterAntLib(uri);
        URLResource antlibResource = new URLResource(antlibUrl);
        try {
            ProjectHelper parser = null;
            Object p =
                project.getReference(ProjectHelper.PROJECTHELPER_REFERENCE);
            if (p instanceof ProjectHelper) {
                parser = (ProjectHelper) p;
                if (!parser.canParseAntlibDescriptor(antlibResource)) {
                    parser = null;
                }
            }
            if (parser == null) {
                ProjectHelperRepository helperRepository =
                    ProjectHelperRepository.getInstance();
                parser = helperRepository.getProjectHelperForAntlib(antlibResource);
            }
            UnknownElement ue =
                parser.parseAntlibDescriptor(project, antlibResource);
            if (!(ue.getTag().equals(TAG))) {
                throw new BuildException(
                    "Unexpected tag " + ue.getTag() + " expecting "
                    + TAG, ue.getLocation());
            }
            Antlib antlib = new Antlib();
            antlib.setProject(project);
            antlib.setLocation(ue.getLocation());
            antlib.setTaskName("antlib");
            antlib.init();
            ue.configure(antlib);
            return antlib;
        } finally {
            helper.exitAntLib();
        }
    }
    private ClassLoader classLoader;
    private String      uri = "";
    private List  tasks = new ArrayList();
    protected void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
    protected void  setURI(String uri) {
        this.uri = uri;
    }
    private ClassLoader getClassLoader() {
        if (classLoader == null) {
            classLoader = Antlib.class.getClassLoader();
        }
        return classLoader;
    }
    public void addTask(Task nestedTask) {
        tasks.add(nestedTask);
    }
    public void execute() {
        for (Iterator i = tasks.iterator(); i.hasNext();) {
            UnknownElement ue = (UnknownElement) i.next();
            setLocation(ue.getLocation());
            ue.maybeConfigure();
            Object configuredObject = ue.getRealThing();
            if (configuredObject == null) {
                continue;
            }
            if (!(configuredObject instanceof AntlibDefinition)) {
                throw new BuildException(
                    "Invalid task in antlib " + ue.getTag()
                    + " " + configuredObject.getClass() + " does not "
                    + "extend org.apache.tools.ant.taskdefs.AntlibDefinition");
            }
            AntlibDefinition def = (AntlibDefinition) configuredObject;
            def.setURI(uri);
            def.setAntlibClassLoader(getClassLoader());
            def.init();
            def.execute();
        }
    }
}