package org.apache.tools.ant.loader;
import java.util.Enumeration;
import java.io.Closeable;
import java.io.IOException;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
public class AntClassLoader5 extends AntClassLoader implements Closeable {
    public AntClassLoader5(ClassLoader parent, Project project,
                           Path classpath, boolean parentFirst) {
        super(parent, project, classpath, parentFirst);
    }
    public Enumeration getResources(String name) throws IOException {
        return getNamedResources(name);
    }
    public void close() {
        cleanup();
    }
}
