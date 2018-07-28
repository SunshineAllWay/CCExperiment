package org.apache.tools.ant.util;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
public final class SplitClassLoader extends AntClassLoader {
    private final String[] splitClasses;
    public SplitClassLoader(ClassLoader parent, Path path, Project project,
                            String[] splitClasses) {
        super(parent, project, path, true);
        this.splitClasses = splitClasses;
    }
    protected synchronized Class loadClass(String classname, boolean resolve)
        throws ClassNotFoundException {
        Class theClass = findLoadedClass(classname);
        if (theClass != null) {
            return theClass;
        }
        if (isSplit(classname)) {
            theClass = findClass(classname);
            if (resolve) {
                resolveClass(theClass);
            }
            return theClass;
        } else {
            return super.loadClass(classname, resolve);
        }
    }
    private boolean isSplit(String classname) {
        String simplename = classname.substring(classname.lastIndexOf('.') + 1);
        for (int i = 0; i < splitClasses.length; i++) {
            if (simplename.equals(splitClasses[i])
                || simplename.startsWith(splitClasses[i] + '$')) {
                return true;
            }
        }
        return false;
    }
}
