package org.apache.tools.ant.taskdefs.optional.unix;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.SymbolicLinkUtils;
public class SymlinkTest extends BuildFileTest {
    private Project p;
    private boolean supportsSymlinks = Os.isFamily("unix");
    public SymlinkTest(String name) {
        super(name);
    }
    public void setUp() {
        if (supportsSymlinks) {
            configureProject("src/etc/testcases/taskdefs/optional/unix/symlink.xml");
            executeTarget("setup");
        }
    }
    public void testSingle() {
        if (supportsSymlinks) {
            executeTarget("test-single");
            p = getProject();
            assertNotNull("Failed to create file",
                          p.getProperty("test.single.file.created"));
            assertNotNull("Failed to create link",
                          p.getProperty("test.single.link.created"));
        }
    }
    public void testDelete() {
        if (supportsSymlinks) {
            executeTarget("test-delete");
            p = getProject();
            String linkDeleted = p.getProperty("test.delete.link.still.there");
            assertNotNull("Actual file deleted by symlink",
                          p.getProperty("test.delete.file.still.there"));
            if (linkDeleted != null) {
                fail(linkDeleted);
            }
        }
    }
    public void testRecord() {
        if (supportsSymlinks) {
            executeTarget("test-record");
            p = getProject();
            assertNotNull("Failed to create dir1",
                          p.getProperty("test.record.dir1.created"));
            assertNotNull("Failed to create dir2",
                          p.getProperty("test.record.dir2.created"));
            assertNotNull("Failed to create file1",
                          p.getProperty("test.record.file1.created"));
            assertNotNull("Failed to create file2",
                          p.getProperty("test.record.file2.created"));
            assertNotNull("Failed to create fileA",
                          p.getProperty("test.record.fileA.created"));
            assertNotNull("Failed to create fileB",
                          p.getProperty("test.record.fileB.created"));
            assertNotNull("Failed to create fileC",
                          p.getProperty("test.record.fileC.created"));
            assertNotNull("Failed to create link1",
                          p.getProperty("test.record.link1.created"));
            assertNotNull("Failed to create link2",
                          p.getProperty("test.record.link2.created"));
            assertNotNull("Failed to create link3",
                          p.getProperty("test.record.link3.created"));
            assertNotNull("Failed to create dirlink",
                          p.getProperty("test.record.dirlink.created"));
            assertNotNull("Failed to create dirlink2",
                          p.getProperty("test.record.dirlink2.created"));
            assertNotNull("Couldn't record links in dir1",
                          p.getProperty("test.record.dir1.recorded"));
            assertNotNull("Couldn't record links in dir2",
                          p.getProperty("test.record.dir2.recorded"));
            String dir3rec = p.getProperty("test.record.dir3.recorded");
            if (dir3rec != null) {
                fail(dir3rec);
            }
        }
    }
    public void testRecreate() {
        if (supportsSymlinks) {
            executeTarget("test-recreate");
            p = getProject();
            String link1Rem = p.getProperty("test.recreate.link1.not.removed");
            String link2Rem = p.getProperty("test.recreate.link2.not.removed");
            String link3Rem = p.getProperty("test.recreate.link3.not.removed");
            String dirlinkRem = p.getProperty("test.recreate.dirlink.not.removed");
            if (link1Rem != null) {
                fail(link1Rem);
            }
            if (link2Rem != null) {
                fail(link2Rem);
            }
            if (link3Rem != null) {
                fail(link3Rem);
            }
            if (dirlinkRem != null) {
                fail(dirlinkRem);
            }
            assertNotNull("Failed to recreate link1",
                          p.getProperty("test.recreate.link1.recreated"));
            assertNotNull("Failed to recreate link2",
                          p.getProperty("test.recreate.link2.recreated"));
            assertNotNull("Failed to recreate link3",
                          p.getProperty("test.recreate.link3.recreated"));
            assertNotNull("Failed to recreate dirlink",
                          p.getProperty("test.recreate.dirlink.recreated"));
            String doubleRecreate = p.getProperty("test.recreate.dirlink2.recreated.twice");
            if (doubleRecreate != null) {
                fail(doubleRecreate);
            }
            assertNotNull("Failed to alter dirlink3",
                          p.getProperty("test.recreate.dirlink3.was.altered"));
        }
    }
    public void testSymbolicLinkUtilsMethods() throws Exception {
        if (supportsSymlinks) {
            executeTarget("test-fileutils");
            SymbolicLinkUtils su = SymbolicLinkUtils.getSymbolicLinkUtils();
            java.io.File f = getProject().resolveFile("test-working/file1");
            assertTrue(f.exists());
            assertFalse(f.isDirectory());
            assertTrue(f.isFile());
            assertFalse(su.isSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isSymbolicLink(f.getParentFile(),
                                          f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/dir1");
            assertTrue(f.exists());
            assertTrue(f.isDirectory());
            assertFalse(f.isFile());
            assertFalse(su.isSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isSymbolicLink(f.getParentFile(),
                                          f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/file2");
            assertFalse(f.exists());
            assertFalse(f.isDirectory());
            assertFalse(f.isFile());
            assertFalse(su.isSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isSymbolicLink(f.getParentFile(),
                                          f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/dir2");
            assertFalse(f.exists());
            assertFalse(f.isDirectory());
            assertFalse(f.isFile());
            assertFalse(su.isSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isSymbolicLink(f.getParentFile(),
                                          f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/file.there");
            assertTrue(f.exists());
            assertFalse(f.isDirectory());
            assertTrue(f.isFile());
            assertTrue(su.isSymbolicLink(f.getAbsolutePath()));
            assertTrue(su.isSymbolicLink(f.getParentFile(),
                                         f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/dir.there");
            assertTrue(f.exists());
            assertTrue(f.isDirectory());
            assertFalse(f.isFile());
            assertTrue(su.isSymbolicLink(f.getAbsolutePath()));
            assertTrue(su.isSymbolicLink(f.getParentFile(),
                                         f.getName()));
            assertFalse(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertFalse(su.isDanglingSymbolicLink(f.getParentFile(),
                                                  f.getName()));
            f = getProject().resolveFile("test-working/file.notthere");
            assertFalse(f.exists());
            assertFalse(f.isDirectory());
            assertFalse(f.isFile());
            assertTrue(su.isSymbolicLink(f.getAbsolutePath()) == false);
            assertTrue(su.isSymbolicLink(f.getParentFile(), f.getName()) == false);
            assertTrue(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertTrue(su.isDanglingSymbolicLink(f.getParentFile(),
                                                 f.getName()));
            f = getProject().resolveFile("test-working/dir.notthere");
            assertFalse(f.exists());
            assertFalse(f.isDirectory());
            assertFalse(f.isFile());
            assertTrue(su.isSymbolicLink(f.getAbsolutePath()) == false);
            assertTrue(su.isSymbolicLink(f.getParentFile(), f.getName()) == false);
            assertTrue(su.isDanglingSymbolicLink(f.getAbsolutePath()));
            assertTrue(su.isDanglingSymbolicLink(f.getParentFile(),
                                                 f.getName()));
        }
    }
    public void tearDown() {
        if (supportsSymlinks) {
            executeTarget("teardown");
        }
    }
}
