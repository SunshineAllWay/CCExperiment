package org.apache.tools.ant.types.optional.depend;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.util.StringUtils;
public class ClassfileSet extends FileSet {
    private Vector rootClasses = new Vector();
    private Vector rootFileSets = new Vector();
    public static class ClassRoot {
        private String rootClass;
        public void setClassname(String name) {
            this.rootClass = name;
        }
        public String getClassname() {
            return rootClass;
        }
    }
    public ClassfileSet() {
    }
    public void addRootFileset(FileSet rootFileSet) {
        rootFileSets.addElement(rootFileSet);
        setChecked(false);
    }
    protected ClassfileSet(ClassfileSet s) {
        super(s);
        rootClasses = (Vector) s.rootClasses.clone();
    }
    public void setRootClass(String rootClass) {
        rootClasses.addElement(rootClass);
    }
    public DirectoryScanner getDirectoryScanner(Project p) {
        if (isReference()) {
            return getRef(p).getDirectoryScanner(p);
        }
        dieOnCircularReference(p);
        DirectoryScanner parentScanner = super.getDirectoryScanner(p);
        DependScanner scanner = new DependScanner(parentScanner);
        Vector allRootClasses = (Vector) rootClasses.clone();
        for (Enumeration e = rootFileSets.elements(); e.hasMoreElements();) {
            FileSet additionalRootSet = (FileSet) e.nextElement();
            DirectoryScanner additionalScanner
                = additionalRootSet.getDirectoryScanner(p);
            String[] files = additionalScanner.getIncludedFiles();
            for (int i = 0; i < files.length; ++i) {
                if (files[i].endsWith(".class")) {
                    String classFilePath = StringUtils.removeSuffix(files[i], ".class");
                    String className
                        = classFilePath.replace('/', '.').replace('\\', '.');
                    allRootClasses.addElement(className);
                }
            }
            scanner.addBasedir(additionalRootSet.getDir(p));
        }
        scanner.setBasedir(getDir(p));
        scanner.setRootClasses(allRootClasses);
        scanner.scan();
        return scanner;
    }
    public void addConfiguredRoot(ClassRoot root) {
        rootClasses.addElement(root.getClassname());
    }
    public Object clone() {
        return new ClassfileSet(isReference()
            ? (ClassfileSet) (getRef(getProject())) : this);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p) {
        if (isChecked()) {
            return;
        }
        super.dieOnCircularReference(stk, p);
        if (!isReference()) {
            for (Enumeration e = rootFileSets.elements();
                 e.hasMoreElements();) {
                FileSet additionalRootSet = (FileSet) e.nextElement();
                pushAndInvokeCircularReferenceCheck(additionalRootSet, stk, p);
            }
            setChecked(true);
        }
    }
}
