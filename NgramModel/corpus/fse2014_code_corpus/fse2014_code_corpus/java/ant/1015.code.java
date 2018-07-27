package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class TypedefTest extends BuildFileTest {
    public TypedefTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/typedef.xml");
    }
    public void testEmpty() {
        expectBuildException("empty", "required argument not specified");
    }
    public void testNoName() {
        expectBuildException("noName", "required argument not specified");
    }
    public void testNoClassname() {
        expectBuildException("noClassname", "required argument not specified");
    }
    public void testClassNotFound() {
        expectBuildException("classNotFound",
                             "classname specified doesn't exist");
    }
    public void testGlobal() {
        expectLog("testGlobal", "");
        Object ref = project.getReferences().get("global");
        assertNotNull("ref is not null", ref);
        assertEquals("org.example.types.TypedefTestType",
                     ref.getClass().getName());
    }
    public void testLocal() {
        expectLog("testLocal", "");
        Object ref = project.getReferences().get("local");
        assertNotNull("ref is not null", ref);
        assertEquals("org.example.types.TypedefTestType",
                     ref.getClass().getName());
    }
    public void testDoubleNotPresent() {
        expectLogContaining("double-notpresent", "hi");
    }
    public void testNoResourceOnErrorFailAll(){
    		this.expectBuildExceptionContaining("noresourcefailall","the requested resource does not exist","Could not load definitions from resource ");
    }
    public void testNoResourceOnErrorFail(){
		expectLogContaining("noresourcefail","Could not load definitions from resource ");
    }
    public void testNoResourceOnErrorNotFail(){
    		expectLogContaining("noresourcenotfail","Could not load definitions from resource ");
    }
}
