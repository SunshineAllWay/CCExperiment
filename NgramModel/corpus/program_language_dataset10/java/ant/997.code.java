package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class RenameTest extends BuildFileTest {
    public RenameTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/rename.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument missing");
    }
    public void test2() {
        expectBuildException("test2", "required argument missing");
    }
    public void test3() {
        expectBuildException("test3", "required argument missing");
    }
    public void test6() {
        executeTarget("test6");
    }
}
