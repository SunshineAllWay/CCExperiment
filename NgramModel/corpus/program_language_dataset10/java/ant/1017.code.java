package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import java.io.IOException;
public class UnzipTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public UnzipTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/unzip.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
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
    public void testRealTest() throws java.io.IOException {
        executeTarget("realTest");
        assertLogoUncorrupted();
    }
    private void assertLogoUncorrupted() throws IOException {
        assertTrue(FILE_UTILS.contentEquals(project.resolveFile("../asf-logo.gif"),
                                           project.resolveFile("asf-logo.gif")));
    }
    public void testTestZipTask() throws java.io.IOException {
        executeTarget("testZipTask");
        assertLogoUncorrupted();
    }
    public void testTestUncompressedZipTask() throws java.io.IOException {
        executeTarget("testUncompressedZipTask");
        assertLogoUncorrupted();
    }
    public void testPatternSetExcludeOnly() {
        executeTarget("testPatternSetExcludeOnly");
        assertFileMissing("1/foo is excluded", "unziptestout/1/foo");
        assertFileExists("2/bar is not excluded", "unziptestout/2/bar");
    }
    public void testPatternSetIncludeOnly() {
        executeTarget("testPatternSetIncludeOnly");
        assertFileMissing("1/foo is not included", "unziptestout/1/foo");
        assertFileExists("2/bar is included", "unziptestout/2/bar");
    }
    public void testPatternSetIncludeAndExclude() {
        executeTarget("testPatternSetIncludeAndExclude");
        assertFileMissing("1/foo is not included", "unziptestout/1/foo");
        assertFileMissing("2/bar is excluded", "unziptestout/2/bar");
    }
    public void testTwoPatternSets() {
        executeTarget("testTwoPatternSets");
        assertFileMissing("1/foo is not included", "unziptestout/1/foo");
        assertFileExists("2/bar is included", "unziptestout/2/bar");
    }
    public void testTwoPatternSetsWithExcludes() {
        executeTarget("testTwoPatternSetsWithExcludes");
        assertFileMissing("1/foo is not included", "unziptestout/1/foo");
        assertFileMissing("2/bar is excluded", "unziptestout/2/bar");
    }
    public void XtestSelfExtractingArchive() {
        executeTarget("selfExtractingArchive");
    }
    public void testPatternSetSlashOnly() {
        executeTarget("testPatternSetSlashOnly");
        assertFileMissing("1/foo is not included", "unziptestout/1/foo");
        assertFileExists("\"2/bar is included", "unziptestout/2/bar");
    }
    public void testEncoding() {
        executeTarget("encodingTest");
        assertFileExists("foo has been properly named", "unziptestout/foo");
    }
    public void testFlattenMapper() {
        executeTarget("testFlattenMapper");
        assertFileMissing("1/foo is not flattened", "unziptestout/1/foo");
        assertFileExists("foo is flattened", "unziptestout/foo");
    }
    private void assertFileExists(String message, String filename) {
        assertTrue(message,
                   getProject().resolveFile(filename).exists());
    }
    private void assertFileMissing(String message, String filename) {
        assertTrue(message,
                !getProject().resolveFile(filename).exists());
    }
    public void testGlobMapper() {
        executeTarget("testGlobMapper");
        assertFileMissing("1/foo is not mapped", "unziptestout/1/foo");
        assertFileExists("1/foo is mapped", "unziptestout/1/foo.txt");
    }
    public void testTwoMappers() {
        expectBuildException("testTwoMappers",Expand.ERROR_MULTIPLE_MAPPERS);
    }
    public void testResourceCollections() {
        executeTarget("testResourceCollection");
        assertFileExists("junit.jar has been extracted",
                         "unziptestout/junit/framework/Assert.class");
    }
    public void testDocumentationClaimsOnCopy() {
        executeTarget("testDocumentationClaimsOnCopy");
        assertFileMissing("1/foo is excluded", "unziptestout/1/foo");
        assertFileExists("2/bar is not excluded", "unziptestout/2/bar");
    }
}
