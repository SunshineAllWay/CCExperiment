package org.apache.tools.ant.taskdefs;
import java.io.PrintWriter;
import java.util.Hashtable;
import junit.framework.Assert;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
public class AntStructureTest extends BuildFileTest {
    public AntStructureTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/antstructure.xml");
    }
    public void tearDown() {
        executeTarget("tearDown");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void testCustomPrinter() {
        executeTarget("testCustomPrinter");
        assertLogContaining(MyPrinter.TAIL_CALLED);
    }
    public static class MyPrinter implements AntStructure.StructurePrinter {
        private static final String TAIL_CALLED = "tail has been called";
        private boolean headCalled = false;
        private boolean targetCalled = false;
        private boolean tailCalled = false;
        private int elementCalled = 0;
        private Project p;
        public void printHead(PrintWriter out, Project p, Hashtable tasks,
                              Hashtable types) {
            Assert.assertTrue(!headCalled);
            Assert.assertTrue(!targetCalled);
            Assert.assertTrue(!tailCalled);
            Assert.assertEquals(0, elementCalled);
            headCalled = true;
        }
        public void printTargetDecl(PrintWriter out) {
            Assert.assertTrue(headCalled);
            Assert.assertTrue(!targetCalled);
            Assert.assertTrue(!tailCalled);
            Assert.assertEquals(0, elementCalled);
            targetCalled = true;
        }
        public void printElementDecl(PrintWriter out, Project p, String name,
                                     Class element) {
            Assert.assertTrue(headCalled);
            Assert.assertTrue(targetCalled);
            Assert.assertTrue(!tailCalled);
            elementCalled++;
            this.p = p;
        }
        public void printTail(PrintWriter out) {
            Assert.assertTrue(headCalled);
            Assert.assertTrue(targetCalled);
            Assert.assertTrue(!tailCalled);
            Assert.assertTrue(elementCalled > 0);
            tailCalled = true;
            p.log(TAIL_CALLED);
        }
    }
}
