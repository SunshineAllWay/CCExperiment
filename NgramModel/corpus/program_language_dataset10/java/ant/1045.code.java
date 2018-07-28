package org.apache.tools.ant.taskdefs.optional;
import java.io.*;
import org.apache.tools.ant.BuildFileTest;
public class ANTLRTest extends BuildFileTest {
    private final static String TASKDEFS_DIR = "src/etc/testcases/taskdefs/optional/antlr/";
    public ANTLRTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "antlr.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "required argument, target, missing");
    }
    public void test2() {
        expectBuildException("test2", "Invalid output directory");
    }
    public void test3() {
        executeTarget("test3");
    }
    public void test4() {
        executeTarget("test4");
    }
    public void test5() {
        expectBuildException("test5", "ANTLR returned: 1");
    }
    public void test6() {
        executeTarget("test6");
    }
    public void test7() {
        expectBuildException("test7", "Unable to determine generated class");
    }
    public void test8() {
        expectBuildException("test8", "Invalid super grammar file");
    }
    public void test9() {
        executeTarget("test9");
    }
    public void test10() {
        executeTarget("test10");
        File outputDirectory = new File(System.getProperty("root"), TASKDEFS_DIR + "antlr.tmp");
        String[] calcFiles = outputDirectory.list(new HTMLFilter());
        assertTrue(calcFiles.length > 0);
    }
    public void test11() {
        executeTarget("test11");
    }
    public void test12() {
        executeTarget("test12");
    }
    public void test13() {
        executeTarget("test13");
    }
    public void testNoRecompile() {
        executeTarget("test9");
        assertEquals(-1, getFullLog().indexOf("Skipped grammar file."));
        executeTarget("noRecompile");
        assertTrue(-1 != getFullLog().indexOf("Skipped grammar file."));
    }
    public void testNormalRecompile() {
        executeTarget("test9");
        assertEquals(-1, getFullLog().indexOf("Skipped grammar file."));
        executeTarget("normalRecompile");
        assertEquals(-1, getFullLog().indexOf("Skipped grammar file."));
    }
    public void testSupergrammarChangeRecompile() {
        executeTarget("test9");
        assertEquals(-1, getFullLog().indexOf("Skipped grammar file."));
        executeTarget("supergrammarChangeRecompile");
        assertEquals(-1, getFullLog().indexOf("Skipped grammar file."));
    }
}
class CalcFileFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.startsWith("Calc");
    }
}
class HTMLFilter implements FilenameFilter {
    public boolean accept(File dir, String name) {
        return name.endsWith("html");
    }
}
