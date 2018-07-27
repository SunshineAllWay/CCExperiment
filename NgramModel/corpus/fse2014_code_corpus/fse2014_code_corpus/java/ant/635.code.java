package org.apache.tools.ant.types.optional;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.selectors.BaseSelector;
import org.apache.tools.ant.util.ScriptRunnerBase;
import org.apache.tools.ant.util.ScriptRunnerHelper;
public class ScriptSelector extends BaseSelector {
    private ScriptRunnerHelper helper = new ScriptRunnerHelper();
    private ScriptRunnerBase runner;
    private File basedir;
    private String filename;
    private File file;
    private boolean selected;
    public void setProject(Project project) {
        super.setProject(project);
        helper.setProjectComponent(this);
    }
    public void setManager(String manager) {
        helper.setManager(manager);
    }
    public void setLanguage(String language) {
        helper.setLanguage(language);
    }
    private void init() throws BuildException {
        if (runner != null) {
            return;
        }
        runner = helper.getScriptRunner();
    }
    public void setSrc(File file) {
        helper.setSrc(file);
    }
    public void addText(String text) {
        helper.addText(text);
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
    public void setSetBeans(boolean setBeans) {
        helper.setSetBeans(setBeans);
    }
    public boolean isSelected(File basedir, String filename, File file) {
        init();
        setSelected(true);
        this.file = file;
        this.basedir = basedir;
        this.filename = filename;
        runner.addBean("basedir", basedir);
        runner.addBean("filename", filename);
        runner.addBean("file", file);
        runner.executeScript("ant_selector");
        return isSelected();
    }
    public File getBasedir() {
        return basedir;
    }
    public String getFilename() {
        return filename;
    }
    public File getFile() {
        return file;
    }
    public boolean isSelected() {
        return selected;
    }
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
