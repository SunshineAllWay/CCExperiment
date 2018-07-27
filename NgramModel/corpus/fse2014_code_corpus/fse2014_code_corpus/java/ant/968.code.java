package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
public class FailTest extends BuildFileTest {
    public FailTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/fail.xml");
    }
    public void test1() {
        expectBuildExceptionContaining("test1",
                "it is required to fail :-)",
                "No message");
    }
    public void test2() {
        expectSpecificBuildException("test2",
            "it is required to fail :-)",
            "test2");
    }
    public void testText() {
        expectSpecificBuildException("testText",
            "it is required to fail :-)",
            "testText");
    }
    public void testIf() {
        try {
            executeTarget("testIf");
        } catch (BuildException be) {
            fail("foo has not been defined, testIf must not fail");
        }
        project.setProperty("foo", "");
        expectBuildException("testIf", "testIf must fail if foo has been set");
    }
    public void testUnless() {
        expectBuildException("testUnless",
                             "testUnless must fail unless foo has been set");
        project.setProperty("foo", "");
        try {
            executeTarget("testUnless");
        } catch (BuildException be) {
            fail("foo has been defined, testUnless must not fail");
        }
    }
    public void testIfAndUnless() {
        executeTarget("testIfAndUnless");
        project.setProperty("if", "");
        expectBuildExceptionContaining("testIfAndUnless",
                "expect fail on defined(if)",
                "if=if and unless=unless");
        project.setProperty("unless", "");
        executeTarget("testIfAndUnless");
    }
    public void testIfAndUnless2() {
        project.setProperty("unless", "");
        try {
            executeTarget("testIfAndUnless");
        } catch (BuildException be) {
            fail("defined(if) && !defined(unless); testIfAndUnless must not fail");
        }
    }
    public void testNested1() {
        expectSpecificBuildException("testNested1",
            "it is required to fail :-)",
            "condition satisfied");
    }
    public void testNested2() {
        try {
            executeTarget("testNested2");
        } catch (BuildException be) {
            fail("condition not satisfied; testNested2 must not fail");
        }
    }
    public void testNested3() {
        expectSpecificBuildException("testNested3",
            "it is required to fail :-)",
            "testNested3");
    }
    public void testNested4() {
        String specificMessage = "Nested conditions "
          + "not permitted in conjunction with if/unless attributes";
        char[] c = {'a', 'b', 'c'};
        StringBuffer target = new StringBuffer("testNested4x");
        for (int i = 0; i < c.length; i++) {
            target.setCharAt(target.length() - 1, c[i]);
            expectSpecificBuildException(target.toString(),
                "it is required to fail :-)", specificMessage);
        }
    }
    public void testNested5() {
        expectSpecificBuildException("testNested5",
            "it is required to fail :-)",
            "Only one nested condition is allowed.");
    }
    public void testNested6() {
        expectSpecificBuildException("testNested6",
            "it is required to fail :-)",
            "testNested6\ntestNested6\ntestNested6");
    }
    public void testNested7() {
        String specificMessage = "A single nested condition is required.";
        char[] c = {'a', 'b'};
        StringBuffer target = new StringBuffer("testNested7x");
        for (int i = 0; i < c.length; i++) {
            target.setCharAt(target.length() - 1, c[i]);
            expectSpecificBuildException(target.toString(),
                "it is required to fail :-)", specificMessage);
        }
    }
}
