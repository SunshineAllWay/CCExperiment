package org.apache.tools.ant.taskdefs.optional;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.taskdefs.optional.jsp.Jasper41Mangler;
import org.apache.tools.ant.taskdefs.optional.jsp.JspMangler;
import org.apache.tools.ant.taskdefs.optional.jsp.JspNameMangler;
import org.apache.tools.ant.taskdefs.optional.jsp.compilers.JspCompilerAdapter;
import org.apache.tools.ant.taskdefs.optional.jsp.compilers.JspCompilerAdapterFactory;
public class JspcTest extends BuildFileTest {
    private File baseDir;
    private File outDir;
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/";
    public JspcTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "jspc.xml");
        baseDir = new File(System.getProperty("root"), TASKDEFS_DIR);
        outDir = new File(baseDir, "jsp/java");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testSimple() throws Exception {
        executeJspCompile("testSimple", "simple_jsp.java");
    }
    public void testUriroot() throws Exception {
        executeJspCompile("testUriroot", "uriroot_jsp.java");
    }
    public void testXml() throws Exception {
        executeJspCompile("testXml", "xml_jsp.java");
    }
    public void testKeyword() throws Exception {
        executeJspCompile("testKeyword", "default_jsp.java");
    }
    public void testInvalidClassname() throws Exception {
        executeJspCompile("testInvalidClassname",
                "_1nvalid_0002dclassname_jsp.java");
    }
    public void testNoTld() throws Exception {
         expectBuildExceptionContaining("testNoTld",
                 "not found",
                 "Java returned: 9");
    }
    public void testNotAJspFile()  throws Exception {
        executeTarget("testNotAJspFile");
    }
    protected void executeJspCompile(String target, String javafile)
        throws Exception {
        executeTarget(target);
        assertJavaFileCreated(javafile);
    }
    protected void assertJavaFileCreated(String filename)
        throws Exception {
        File file = getOutputFile(filename);
        assertTrue("file " + filename + " not found", file.exists());
        assertTrue("file " + filename + " is empty", file.length() > 0);
    }
    protected File getOutputFile(String subpath) {
        return new File(outDir, subpath);
    }
    public void testJasperNameManglerSelection() {
        JspCompilerAdapter adapter=
                JspCompilerAdapterFactory.getCompiler("jasper", null,null);
        JspMangler mangler=adapter.createMangler();
        assertTrue(mangler instanceof JspNameMangler);
        adapter= JspCompilerAdapterFactory.getCompiler("jasper41", null, null);
        mangler = adapter.createMangler();
        assertTrue(mangler instanceof Jasper41Mangler);
    }
    public void testJasper41() {
        JspMangler mangler = new Jasper41Mangler();
        assertMapped(mangler, "for.jsp", "for_jsp");
        assertMapped(mangler, "0.jsp", "_0_jsp");
        assertMapped(mangler, "_.jsp", "___jsp");
        assertMapped(mangler, "-.jsp", "__0002d_jsp");
        char s = File.separatorChar;
        assertMapped(mangler, "" + s + s + "somewhere" + s + "file" + s + "index.jsp", "index_jsp");
    }
    protected void assertMapped(JspMangler mangler, String filename, String classname) {
        String mappedname = mangler.mapJspToJavaName(new File(filename));
        assertTrue(filename+" should have mapped to "+classname
                    +" but instead mapped to "+mappedname,
                    classname.equals(mappedname));
    }
}
