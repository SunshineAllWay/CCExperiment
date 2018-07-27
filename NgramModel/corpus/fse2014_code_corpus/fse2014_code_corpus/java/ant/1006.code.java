package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
public class SyncTest extends BuildFileTest {
    public SyncTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/sync.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testSimpleCopy() {
        executeTarget("simplecopy");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsPresent(d);
        assertTrue(getFullLog().indexOf("dangling") == -1);
    }
    public void testEmptyCopy() {
        executeTarget("emptycopy");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsNotPresent(d);
        String c = getProject().getProperty("dest") + "/a/b/c";
        assertFileIsNotPresent(c);
        assertTrue(getFullLog().indexOf("dangling") == -1);
    }
    public void testEmptyDirCopy() {
        executeTarget("emptydircopy");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsNotPresent(d);
        String c = getProject().getProperty("dest") + "/a/b/c";
        assertFileIsPresent(c);
        assertTrue(getFullLog().indexOf("dangling") == -1);
    }
    public void testCopyAndRemove() {
        testCopyAndRemove("copyandremove");
    }
    public void testCopyAndRemoveWithFileList() {
        testCopyAndRemove("copyandremove-with-filelist");
    }
    public void testCopyAndRemoveWithZipfileset() {
        testCopyAndRemove("copyandremove-with-zipfileset");
    }
    private void testCopyAndRemove(String target) {
        executeTarget(target);
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsPresent(d);
        String f = getProject().getProperty("dest") + "/e/f";
        assertFileIsNotPresent(f);
        assertTrue(getFullLog().indexOf("Removing orphan file:") > -1);
        assertDebuglogContaining("Removed 1 dangling file from");
        assertDebuglogContaining("Removed 1 dangling directory from");
    }
    public void testCopyAndRemoveEmptyPreserve() {
        executeTarget("copyandremove-emptypreserve");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsPresent(d);
        String f = getProject().getProperty("dest") + "/e/f";
        assertFileIsNotPresent(f);
        assertTrue(getFullLog().indexOf("Removing orphan file:") > -1);
        assertDebuglogContaining("Removed 1 dangling file from");
        assertDebuglogContaining("Removed 1 dangling directory from");
    }
    public void testEmptyDirCopyAndRemove() {
        executeTarget("emptydircopyandremove");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsNotPresent(d);
        String c = getProject().getProperty("dest") + "/a/b/c";
        assertFileIsPresent(c);
        String f = getProject().getProperty("dest") + "/e/f";
        assertFileIsNotPresent(f);
        assertTrue(getFullLog().indexOf("Removing orphan directory:") > -1);
        assertDebuglogContaining("NO dangling file to remove from");
        assertDebuglogContaining("Removed 2 dangling directories from");
    }
    public void testCopyNoRemove() {
        executeTarget("copynoremove");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsPresent(d);
        String f = getProject().getProperty("dest") + "/e/f";
        assertFileIsPresent(f);
        assertTrue(getFullLog().indexOf("Removing orphan file:") == -1);
    }
    public void testCopyNoRemoveSelectors() {
        executeTarget("copynoremove-selectors");
        String d = getProject().getProperty("dest") + "/a/b/c/d";
        assertFileIsPresent(d);
        String f = getProject().getProperty("dest") + "/e/f";
        assertFileIsPresent(f);
        assertTrue(getFullLog().indexOf("Removing orphan file:") == -1);
    }
    public void assertFileIsPresent(String f) {
        assertTrue("Expected file " + f,
                   getProject().resolveFile(f).exists());
    }
    public void assertFileIsNotPresent(String f) {
        assertTrue("Didn't expect file " + f,
                   !getProject().resolveFile(f).exists());
    }
}
