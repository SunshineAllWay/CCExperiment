package org.apache.tools.ant.util.depend.bcel;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.depend.AbstractAnalyzer;
public class AncestorAnalyzer extends AbstractAnalyzer {
    public AncestorAnalyzer() {
        try {
            new ClassParser("force");
        } catch (Exception e) {
            if (!(e instanceof IOException)) {
                throw new BuildException(e);
            }
        }
    }
    protected void determineDependencies(Vector files, Vector classes) {
        Hashtable dependencies = new Hashtable();
        Hashtable containers = new Hashtable();
        Hashtable toAnalyze = new Hashtable();
        Hashtable nextAnalyze = new Hashtable();
        for (Enumeration e = getRootClasses(); e.hasMoreElements();) {
            String classname = (String) e.nextElement();
            toAnalyze.put(classname, classname);
        }
        int count = 0;
        int maxCount = isClosureRequired() ? MAX_LOOPS : 2;
        while (toAnalyze.size() != 0 && count++ < maxCount) {
            nextAnalyze.clear();
            for (Enumeration e = toAnalyze.keys(); e.hasMoreElements();) {
                String classname = (String) e.nextElement();
                dependencies.put(classname, classname);
                try {
                    File container = getClassContainer(classname);
                    if (container == null) {
                        continue;
                    }
                    containers.put(container, container);
                    ClassParser parser = null;
                    if (container.getName().endsWith(".class")) {
                        parser = new ClassParser(container.getPath());
                    } else {
                        parser = new ClassParser(container.getPath(),
                            classname.replace('.', '/') + ".class");
                    }
                    JavaClass javaClass = parser.parse();
                    String[] interfaces = javaClass.getInterfaceNames();
                    for (int i = 0; i < interfaces.length; ++i) {
                        String interfaceName = interfaces[i];
                        if (!dependencies.containsKey(interfaceName)) {
                            nextAnalyze.put(interfaceName, interfaceName);
                        }
                    }
                    if (javaClass.isClass()) {
                        String superClass = javaClass.getSuperclassName();
                        if (!dependencies.containsKey(superClass)) {
                            nextAnalyze.put(superClass, superClass);
                        }
                    }
                } catch (IOException ioe) {
                }
            }
            Hashtable temp = toAnalyze;
            toAnalyze = nextAnalyze;
            nextAnalyze = temp;
        }
        files.removeAllElements();
        for (Enumeration e = containers.keys(); e.hasMoreElements();) {
            files.addElement((File) e.nextElement());
        }
        classes.removeAllElements();
        for (Enumeration e = dependencies.keys(); e.hasMoreElements();) {
            classes.addElement((String) e.nextElement());
        }
    }
    protected boolean supportsFileDependencies() {
        return true;
    }
}
