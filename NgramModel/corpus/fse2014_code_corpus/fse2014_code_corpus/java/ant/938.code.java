package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
public class AbstractCvsTaskTest extends BuildFileTest {
    public AbstractCvsTaskTest() {
        this( "AbstractCvsTaskTest" );
    }
    public AbstractCvsTaskTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/abstractcvstask.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testAbstractCvsTask() {
        executeTarget( "all" );
    }
    public void testPackageAttribute() {
        File f = getProject().resolveFile("tmpdir/ant/build.xml");
        assertTrue("starting empty", !f.exists());
        expectLogContaining("package-attribute", "U ant/build.xml");
        assertTrue("now it is there", f.exists());
    }
    public void testTagAttribute() {
        File f = getProject().resolveFile("tmpdir/ant/build.xml");
        assertTrue("starting empty", !f.exists());
        expectLogContaining("tag-attribute", "ANT_141 (revision: 1.175.2.13)");
        assertTrue("now it is there", f.exists());
    }
}
