package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class TarTest extends BuildFileTest {
    public TarTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/tar.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        expectBuildException("test2", "required argument not specified");
    }
    public void test3() {
        expectBuildException("test3", "required argument not specified");
    }
    public void test4() {
        expectBuildException("test4", "tar cannot include itself");
    }
    public void test5() {
        executeTarget("test5");
        File f
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test5.tar");
        if (!f.exists()) {
            fail("Tarring a directory failed");
        }
    }
    public void test6() {
        expectBuildException("test6", "Invalid value specified for longfile attribute.");
    }
    public void test7() {
        test7("test7");
    }
    public void test7UsingPlainFileSet() {
        test7("test7UsingPlainFileSet");
    }
    public void test7UsingFileList() {
        test7("test7UsingFileList");
    }
    private void test7(String target) {
        executeTarget(target);
        File f1
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test7-prefix");
        if (!(f1.exists() && f1.isDirectory())) {
            fail("The prefix attribute is not working properly.");
        }
        File f2
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test7dir");
        if (!(f2.exists() && f2.isDirectory())) {
            fail("The prefix attribute is not working properly.");
        }
    }
    public void test8() {
        test8("test8");
    }
    public void test8UsingZipFileset() {
        test8("test8UsingZipFileset");
    }
    public void test8UsingZipFilesetSrc() {
        test8("test8UsingZipFilesetSrc");
    }
    public void test8UsingTarFilesetSrc() {
        test8("test8UsingTarFilesetSrc");
    }
    public void test8UsingZipEntry() {
        test8("test8UsingZipEntry");
    }
    private void test8(String target) {
        executeTarget(target);
        File f1
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test8.xml");
        if (! f1.exists()) {
            fail("The fullpath attribute or the preserveLeadingSlashes attribute does not work propertly");
        }
    }
    public void test9() {
        expectBuildException("test9", "Invalid value specified for compression attribute.");
    }
    public void test10() {
        executeTarget("test10");
        File f1
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test10.xml");
        if (! f1.exists()) {
            fail("The fullpath attribute or the preserveLeadingSlashes attribute does not work propertly");
        }
    }
    public void test11() {
        executeTarget("test11");
        File f1
            = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/test11.xml");
        if (! f1.exists()) {
            fail("The fullpath attribute or the preserveLeadingSlashes attribute does not work propertly");
        }
    }
    public void testGZipResource() throws IOException {
        executeTarget("testGZipResource");
        assertTrue(FileUtils.getFileUtils()
                   .contentEquals(getProject().resolveFile("../asf-logo.gif"),
                                  getProject().resolveFile("testout/asf-logo.gif.gz")));
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
}
