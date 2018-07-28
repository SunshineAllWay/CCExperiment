package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
public class PreSetDefTest extends BuildFileTest {
    public PreSetDefTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/presetdef.xml");
    }
    public void testSimple() {
        expectLog("simple", "Hello world");
    }
    public void testText() {
        expectLog("text", "Inner Text");
    }
    public void testUri() {
        expectLog("uri", "Hello world");
    }
    public void testDefaultTest() {
        expectLog("defaulttest", "attribute is false");
    }
    public void testDoubleDefault() {
        expectLog("doubledefault", "attribute is falseattribute is true");
    }
    public void testTextOptional() {
        expectLog("text.optional", "MyTextoverride text");
    }
    public void testElementOrder() {
        expectLog("element.order", "Line 1Line 2");
    }
    public void testElementOrder2() {
        expectLog("element.order2", "Line 1Line 2Line 3");
    }
    public void testAntTypeTest() {
        expectLog("antTypeTest", "");
    }
    public void testCorrectTaskNameBadAttr() {
        expectBuildExceptionContaining(
            "correct_taskname_badattr", "attribute message", "javac doesn't support the");
    }
    public void testCorrectTaskNameBadEl() {
        expectBuildExceptionContaining(
            "correct_taskname_badel", "element message", "javac doesn't support the");
    }
    public void testPresetdefWithNestedElementTwice() { 
        executeTarget("presetdef-with-nested-element-twice");
        executeTarget("presetdef-with-nested-element-twice");
    }
    public static class DefaultTest extends Task {
        boolean isSet = false;
        boolean attribute = false;
        public void setAttribute(boolean b) {
            if (isSet) {
                throw new BuildException("Attribute Already set");
            }
            attribute = b;
            isSet = true;
        }
        public void execute() {
            getProject().log("attribute is " + attribute);
        }
    }
    public static class AntTypeTest extends Task {
        public void addFileSet(FileSet fileset) {
        }
        public void addConfiguredConfigured(FileSet fileset) {
        }
    }
}
