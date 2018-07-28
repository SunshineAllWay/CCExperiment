package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.Resources;
public final class BatchTest extends BaseTest {
    private Project project;
    private Resources resources = new Resources();
    public BatchTest(Project project) {
        this.project = project;
        resources.setCache(true);
    }
    public void addFileSet(FileSet fs) {
        add(fs);
        if (fs.getProject() == null) {
            fs.setProject(project);
        }
    }
    public void add(ResourceCollection rc) {
        resources.add(rc);
    }
    public Enumeration elements() {
        JUnitTest[] tests = createAllJUnitTest();
        return Enumerations.fromArray(tests);
    }
    void addTestsTo(Vector v) {
        JUnitTest[] tests = createAllJUnitTest();
        v.ensureCapacity(v.size() + tests.length);
        for (int i = 0; i < tests.length; i++) {
            v.addElement(tests[i]);
        }
    }
    private JUnitTest[] createAllJUnitTest() {
        String[] filenames = getFilenames();
        JUnitTest[] tests = new JUnitTest[filenames.length];
        for (int i = 0; i < tests.length; i++) {
            String classname = javaToClass(filenames[i]);
            tests[i] = createJUnitTest(classname);
        }
        return tests;
    }
    private String[] getFilenames() {
        Vector v = new Vector();
        Iterator iter = resources.iterator();
        while (iter.hasNext()) {
            Resource r = (Resource) iter.next();
            if (r.isExists()) {
                String pathname = r.getName();
                if (pathname.endsWith(".java")) {
                    v.addElement(pathname.substring(0, pathname.length() - ".java".length()));
                } else if (pathname.endsWith(".class")) {
                    v.addElement(pathname.substring(0, pathname.length() - ".class".length()));
                }
            }
        }
        String[] files = new String[v.size()];
        v.copyInto(files);
        return files;
    }
    public static String javaToClass(String filename) {
        return filename.replace(File.separatorChar, '.').replace('/', '.')
            .replace('\\', '.');
    }
    private JUnitTest createJUnitTest(String classname) {
        JUnitTest test = new JUnitTest();
        test.setName(classname);
        test.setHaltonerror(this.haltOnError);
        test.setHaltonfailure(this.haltOnFail);
        test.setFiltertrace(this.filtertrace);
        test.setFork(this.fork);
        test.setIf(getIfCondition());
        test.setUnless(getUnlessCondition());
        test.setTodir(this.destDir);
        test.setFailureProperty(failureProperty);
        test.setErrorProperty(errorProperty);
        Enumeration list = this.formatters.elements();
        while (list.hasMoreElements()) {
            test.addFormatter((FormatterElement) list.nextElement());
        }
        return test;
    }
}
