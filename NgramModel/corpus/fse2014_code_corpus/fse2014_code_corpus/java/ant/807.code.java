package org.apache.tools.ant.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
public abstract class ScriptRunnerBase {
    private boolean keepEngine = false;
    private String language;
    private String script = "";
    private Project project;
    private ClassLoader scriptLoader;
    private Map beans = new HashMap();
    public void addBeans(Map dictionary) {
        for (Iterator i = dictionary.keySet().iterator(); i.hasNext();) {
            String key = (String) i.next();
            try {
                Object val = dictionary.get(key);
                addBean(key, val);
            } catch (BuildException ex) {
            }
        }
    }
    public void addBean(String key, Object bean) {
        boolean isValid = key.length() > 0
            && Character.isJavaIdentifierStart(key.charAt(0));
        for (int i = 1; isValid && i < key.length(); i++) {
            isValid = Character.isJavaIdentifierPart(key.charAt(i));
        }
        if (isValid) {
            beans.put(key, bean);
        }
    }
    protected Map getBeans() {
        return beans;
    }
    public abstract void executeScript(String execName);
    public abstract Object evaluateScript(String execName);
    public abstract boolean supportsLanguage();
    public abstract String getManagerName();
    public void setLanguage(String language) {
        this.language = language;
    }
    public String getLanguage() {
        return language;
    }
    public void setScriptClassLoader(ClassLoader classLoader) {
        this.scriptLoader = classLoader;
    }
    protected ClassLoader getScriptClassLoader() {
        return scriptLoader;
    }
    public void setKeepEngine(boolean keepEngine) {
        this.keepEngine = keepEngine;
    }
    public boolean getKeepEngine() {
        return keepEngine;
    }
    public void setSrc(File file) {
        String filename = file.getPath();
        if (!file.exists()) {
            throw new BuildException("file " + filename + " not found.");
        }
        try {
            readSource(new FileReader(file), filename);
        } catch (FileNotFoundException e) {
            throw new BuildException("file " + filename + " not found.");
        }
    }
    private void readSource(Reader reader, String name) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(reader);
            script += FileUtils.safeReadFully(in);
        } catch (IOException ex) {
            throw new BuildException("Failed to read " + name, ex);
        } finally {
            FileUtils.close(in);
        }
    }
    public void loadResource(Resource sourceResource) {
        String name = sourceResource.toLongString();
        InputStream in = null;
        try {
            in = sourceResource.getInputStream();
        } catch (IOException e) {
            throw new BuildException("Failed to open " + name, e);
        } catch (UnsupportedOperationException e) {
            throw new BuildException(
                "Failed to open " + name + " -it is not readable", e);
        }
        readSource(new InputStreamReader(in), name);
    }
    public void loadResources(ResourceCollection collection) {
        Iterator resources = collection.iterator();
        while (resources.hasNext()) {
            Resource resource = (Resource) resources.next();
            loadResource(resource);
        }
    }
    public void addText(String text) {
        script += text;
    }
    public String getScript() {
        return script;
    }
    public void clearScript() {
        this.script = "";
    }
    public void setProject(Project project) {
        this.project = project;
    }
    public Project getProject() {
        return project;
    }
    public void bindToComponent(ProjectComponent component) {
        project = component.getProject();
        addBeans(project.getProperties());
        addBeans(project.getUserProperties());
        addBeans(project.getCopyOfTargets());
        addBeans(project.getCopyOfReferences());
        addBean("project", project);
        addBean("self", component);
    }
    public void bindToComponentMinimum(ProjectComponent component) {
        project = component.getProject();
        addBean("project", project);
        addBean("self", component);
    }
    protected void checkLanguage() {
        if (language == null) {
            throw new BuildException(
                "script language must be specified");
        }
    }
    protected ClassLoader replaceContextLoader() {
        ClassLoader origContextClassLoader =
            Thread.currentThread().getContextClassLoader();
        if (getScriptClassLoader() == null) {
            setScriptClassLoader(getClass().getClassLoader());
        }
        Thread.currentThread().setContextClassLoader(getScriptClassLoader());
        return origContextClassLoader;
    }
    protected void restoreContextLoader(ClassLoader origLoader) {
        Thread.currentThread().setContextClassLoader(
                 origLoader);
    }
}
