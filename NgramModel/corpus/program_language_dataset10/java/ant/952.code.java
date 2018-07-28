package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class CopydirTest extends BuildFileTest {
    public CopydirTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/copydir.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        expectBuildException("test2", "required argument not specified");
    }
    public void test3() {
        expectBuildException("test3", "required argument not specified");
    }
    public void test4() {
        expectLog("test4", "DEPRECATED - The copydir task is deprecated.  Use copy instead.Warning: src == dest");
    }
    public void test5() {
        executeTarget("test5");
        java.io.File f = new java.io.File(getProjectDir(), "../taskdefs.tmp");
        if (!f.exists() || !f.isDirectory()) {
            fail("Copy failed");
        }
    }
    public void test6() {
        expectBuildException("test6", "target is file");
    }
}
