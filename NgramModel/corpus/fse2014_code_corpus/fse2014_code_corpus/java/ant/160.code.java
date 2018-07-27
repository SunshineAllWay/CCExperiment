package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.Task;
public class AntlibDefinition extends Task {
    private String uri = "";
    private ClassLoader antlibClassLoader;
    public void setURI(String uri) throws BuildException {
        if (uri.equals(ProjectHelper.ANT_CORE_URI)) {
            uri = "";
        }
        if (uri.startsWith("ant:")) {
            throw new BuildException("Attempt to use a reserved URI " + uri);
        }
        this.uri = uri;
    }
    public String getURI() {
        return uri;
    }
    public void setAntlibClassLoader(ClassLoader classLoader) {
        this.antlibClassLoader = classLoader;
    }
    public ClassLoader getAntlibClassLoader() {
        return antlibClassLoader;
    }
}
