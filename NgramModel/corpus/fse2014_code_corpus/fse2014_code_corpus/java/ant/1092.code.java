package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
public class AssertionsTest extends BuildFileTest {
    public AssertionsTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        configureProject("src/etc/testcases/types/assertions.xml");
    }
    protected void tearDown() throws Exception {
        executeTarget("teardown");
    }
    protected void expectAssertion(String target) {
        expectBuildExceptionContaining(target,
                "assertion not thrown in "+target,
                "Java returned: 1");
    }
    public void testClassname() {
        expectAssertion("test-classname");
    }
    public void testPackage() {
        expectAssertion("test-package");
    }
    public void testEmptyAssertions() {
        executeTarget("test-empty-assertions");
    }
    public void testDisable() {
        executeTarget("test-disable");
    }
    public void testOverride() {
        expectAssertion("test-override");
    }
    public void testOverride2() {
        executeTarget("test-override2");
    }
    public void testReferences() {
        expectAssertion("test-references");
    }
    public void testMultipleAssertions() {
        expectBuildExceptionContaining("test-multiple-assertions",
                "multiple assertions rejected",
                "Only one assertion declaration is allowed");
    }
    public void testReferenceAbuse() {
        expectBuildExceptionContaining("test-reference-abuse",
                "reference abuse rejected",
                "You must not specify");
    }
    public void testNofork() {
        if (AssertionsTest.class.desiredAssertionStatus()) {
            return; 
        }
        expectLogContaining("test-nofork",
                "Assertion statements are currently ignored in non-forked mode");
    }
    public void testJunit() {
        executeTarget("test-junit");
    }
}
