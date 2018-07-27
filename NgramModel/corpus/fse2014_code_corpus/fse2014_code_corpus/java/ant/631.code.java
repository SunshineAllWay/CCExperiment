package org.apache.tools.ant.types.optional;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.util.ScriptRunnerBase;
import org.apache.tools.ant.util.ScriptRunnerHelper;
import java.io.File;
public abstract class AbstractScriptComponent extends ProjectComponent {
    private ScriptRunnerHelper helper = new ScriptRunnerHelper();
    private ScriptRunnerBase   runner = null;
    public void setProject(Project project) {
        super.setProject(project);
        helper.setProjectComponent(this);
    }
    public ScriptRunnerBase getRunner() {
        initScriptRunner();
        return runner;
    }
    public void setSrc(File file) {
        helper.setSrc(file);
    }
    public void addText(String text) {
        helper.addText(text);
    }
    public void setManager(String manager) {
        helper.setManager(manager);
    }
    public void setLanguage(String language) {
        helper.setLanguage(language);
    }
    protected void initScriptRunner() {
        if (runner != null) {
            return;
        }
        helper.setProjectComponent(this);
        runner = helper.getScriptRunner();
    }
    public void setClasspath(Path classpath) {
        helper.setClasspath(classpath);
    }
    public Path createClasspath() {
        return helper.createClasspath();
    }
    public void setClasspathRef(Reference r) {
        helper.setClasspathRef(r);
    }
    protected void executeScript(String execName) {
        getRunner().executeScript(execName);
    }
    public void setSetBeans(boolean setBeans) {
        helper.setSetBeans(setBeans);
    }
}
