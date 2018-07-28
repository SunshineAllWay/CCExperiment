package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class XorTest extends BuildFileTest {
    public XorTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/xor.xml");
    }
    public void testEmpty() {
        executeTarget("testEmpty");
    }
    public void test0() {
        executeTarget("test0");
    }
    public void test1() {
        executeTarget("test1");
    }
    public void test00() {
        executeTarget("test00");
    }
    public void test10() {
        executeTarget("test10");
    }
    public void test01() {
        executeTarget("test01");
    }
    public void test11() {
        executeTarget("test11");
    }
}
