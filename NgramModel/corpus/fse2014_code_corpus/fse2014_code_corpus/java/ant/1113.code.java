package org.apache.tools.ant.types;
import java.io.File;
import org.apache.tools.ant.BuildException;
public class ZipFileSetTest extends AbstractFileSetTest {
    public ZipFileSetTest(String name) {
        super(name);
    }
    protected AbstractFileSet getInstance() {
        return new ZipFileSet();
    }
    public final void testAttributes() {
        ZipFileSet f = (ZipFileSet)getInstance();
        f.setSrc(new File("example.zip"));
        try {
            f.setDir(new File("examples"));
            fail("can add dir to "
                    + f.getDataTypeName()
                    + " when a src is already present");
        } catch (BuildException be) {
            assertEquals("Cannot set both dir and src attributes",be.getMessage());
        }
        f = (ZipFileSet)getInstance();
        f.setDir(new File("examples"));
        try {
            f.setSrc(new File("example.zip"));
            fail("can add src to "
                    + f.getDataTypeName()
                    + " when a dir is already present");
        } catch (BuildException be) {
            assertEquals("Cannot set both dir and src attributes",be.getMessage());
        }
        f = (ZipFileSet)getInstance();
        f.setSrc(new File("example.zip"));
        f.setPrefix("/examples");
        try {
            f.setFullpath("/doc/manual/index.html");
            fail("Can add fullpath to "
                    + f.getDataTypeName()
                    + " when a prefix is already present");
        } catch (BuildException be) {
            assertEquals("Cannot set both fullpath and prefix attributes", be.getMessage());
        }
        f = (ZipFileSet)getInstance();
        f.setSrc(new File("example.zip"));
        f.setFullpath("/doc/manual/index.html");
        try {
            f.setPrefix("/examples");
            fail("Can add prefix to "
                    + f.getDataTypeName()
                    + " when a fullpath is already present");
        } catch (BuildException be) {
            assertEquals("Cannot set both fullpath and prefix attributes", be.getMessage());
        }
        f = (ZipFileSet)getInstance();
        f.setRefid(new Reference(getProject(), "test"));
        try {
            f.setSrc(new File("example.zip"));
            fail("Can add src to "
                    + f.getDataTypeName()
                    + " when a refid is already present");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one "
            + "attribute when using refid", be.getMessage());
        }
        f = (ZipFileSet)getInstance();
        f.setSrc(new File("example.zip"));
        f.setPrefix("/examples");
        f.setFileMode("600");
        f.setDirMode("530");
        getProject().addReference("test",f);
        ZipFileSet zid=(ZipFileSet)getInstance();
        zid.setRefid(new Reference(getProject(), "test"));
        assertTrue("src attribute copied by copy constructor",zid.getSrc(getProject()).equals(f.getSrc(getProject())));
        assertTrue("prefix attribute copied by copy constructor",f.getPrefix(getProject()).equals(zid.getPrefix(getProject())));
        assertTrue("file mode attribute copied by copy constructor",f.getFileMode(getProject())==zid.getFileMode(getProject()));
        assertTrue("dir mode attribute copied by copy constructor",f.getDirMode(getProject())==zid.getDirMode(getProject()));
      }
}
