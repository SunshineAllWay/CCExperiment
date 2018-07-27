package org.apache.tools.ant.taskdefs.optional.script;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.FileSet;
import java.io.File;
public class ScriptDefTest extends BuildFileTest {
    public ScriptDefTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/script/scriptdef.xml");
    }
    public void testSimple() {
        executeTarget("simple");
        Project p = getProject();
        FileSet fileset = (FileSet) p.getReference("testfileset");
        File baseDir = fileset.getDir(p);
        String log = getLog();
        assertTrue("Expecting attribute value printed",
            log.indexOf("Attribute attr1 = test") != -1);
        assertTrue("Expecting nested element value printed",
            log.indexOf("Fileset basedir = " + baseDir.getAbsolutePath()) != -1);
    }
    public void testNoLang() {
        expectBuildExceptionContaining("nolang",
            "Absence of language attribute not detected",
            "requires a language attribute");
    }
    public void testNoName() {
        expectBuildExceptionContaining("noname",
            "Absence of name attribute not detected",
            "scriptdef requires a name attribute");
    }
    public void testNestedByClassName() {
        executeTarget("nestedbyclassname");
        Project p = getProject();
        FileSet fileset = (FileSet) p.getReference("testfileset");
        File baseDir = fileset.getDir(p);
        String log = getLog();
        assertTrue("Expecting attribute value to be printed",
            log.indexOf("Attribute attr1 = test") != -1);
        assertTrue("Expecting nested element value to be printed",
            log.indexOf("Fileset basedir = " + baseDir.getAbsolutePath()) != -1);
    }
    public void testNoElement() {
        expectOutput("noelement", "Attribute attr1 = test");
    }
    public void testException() {
        expectBuildExceptionContaining("exception",
            "Should have thrown an exception in the script",
            "TypeError");
    }
    public void testDoubleDef() {
        executeTarget("doubledef");
        String log = getLog();
        assertTrue("Task1 did not execute",
            log.indexOf("Task1") != -1);
        assertTrue("Task2 did not execute",
            log.indexOf("Task2") != -1);
    }
    public void testDoubleAttribute() {
        expectBuildExceptionContaining("doubleAttributeDef",
            "Should have detected duplicate attribute definition",
            "attr1 attribute more than once");
    }
    public void testProperty() {
        executeTarget("property");
        String log = getLog();
        assertTrue("Expecting property in attribute value replaced",
            log.indexOf("Attribute value = test") != -1);
    }
}
