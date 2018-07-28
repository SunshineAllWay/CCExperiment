package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class IsReferenceTest extends BuildFileTest {
    public IsReferenceTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/isreference.xml");
    }
    public void testBasic() {
       expectPropertySet("basic", "global-path");
       assertPropertySet("target-path");
       assertPropertyUnset("undefined");
    }
    public void testNotEnoughArgs() {
        expectSpecificBuildException("isreference-incomplete",
                                     "refid attribute has been omitted",
                                     "No reference specified for isreference "
                                     + "condition");
    }
    public void testType() {
       expectPropertySet("type", "global-path");
       assertPropertyUnset("global-path-as-fileset");
       assertPropertyUnset("global-path-as-foo");
       assertPropertySet("global-echo");
    }
}
