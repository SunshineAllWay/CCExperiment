package org.apache.tools.ant;
public class TopLevelTaskTest extends BuildFileTest {
    public TopLevelTaskTest(String name) {
        super(name);
    }
    public void testNoTarget() {
        configureProject("src/etc/testcases/core/topleveltasks/notarget.xml");
        expectLog("", "Called");
    }
    public void testCalledFromTopLevelAnt() {
        configureProject("src/etc/testcases/core/topleveltasks/toplevelant.xml");
        expectLog("", "Called");
    }
    public void testCalledFromTargetLevelAnt() {
        configureProject("src/etc/testcases/core/topleveltasks/targetlevelant.xml");
        expectLog("foo", "Called");
    }
}
