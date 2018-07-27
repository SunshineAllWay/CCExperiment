package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class PvcsTest extends BuildFileTest {
    public PvcsTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/pvcs.xml");
    }
    public void test1() {
        expectBuildException("test1", "Required argument repository not specified");
    }
    public void test2() {
        executeTarget("test2");
    }
    public void test3() {
        executeTarget("test3");
    }
    public void test4() {
        executeTarget("test4");
    }
    public void test5() {
        executeTarget("test5");
    }
    public void test6() {
        expectBuildException("test6", "Failed executing: /never/heard/of/a/directory/structure/like/this/pcli lvf -z -aw -pr//ct4serv2/pvcs/monitor /. Exception: /never/heard/of/a/directory/structure/like/this/pcli: not found");
    }
}
