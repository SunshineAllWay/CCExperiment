package org.apache.tools.ant.taskdefs.optional.image;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
public class ImageTest extends BuildFileTest {
    private final static String TASKDEFS_DIR = 
        "src/etc/testcases/taskdefs/optional/image/";
    private final static String LARGEIMAGE = "largeimage.jpg";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public ImageTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "image.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testEchoToLog() {
        expectLogContaining("testEchoToLog", "Processing File");
    }
    public void testSimpleScale(){
        expectLogContaining("testSimpleScale", "Processing File");
        File f = createRelativeFile("/dest/" + LARGEIMAGE);
        assertTrue(
                   "Did not create "+f.getAbsolutePath(),
                   f.exists());
    }
    public void testOverwriteTrue() {
        expectLogContaining("testSimpleScale", "Processing File");
        File f = createRelativeFile("/dest/" + LARGEIMAGE);
        long lastModified = f.lastModified();
        try {
            Thread.sleep(FILE_UTILS
                         .getFileTimestampGranularity());
        }
        catch (InterruptedException e) {}
        expectLogContaining("testOverwriteTrue", "Processing File");
        f = createRelativeFile("/dest/" + LARGEIMAGE);
        long overwrittenLastModified = f.lastModified();
        assertTrue("File was not overwritten.",
                   lastModified < overwrittenLastModified);
    }
    public void testOverwriteFalse() {
        expectLogContaining("testSimpleScale", "Processing File");
        File f = createRelativeFile("/dest/" + LARGEIMAGE);
        long lastModified = f.lastModified();
        expectLogContaining("testOverwriteFalse", "Processing File");
        f = createRelativeFile("/dest/" + LARGEIMAGE);
        long overwrittenLastModified = f.lastModified();
        assertTrue("File was overwritten.",
                   lastModified == overwrittenLastModified);
    }
    public void testSimpleScaleWithMapper() {
        expectLogContaining("testSimpleScaleWithMapper", "Processing File");
        File f = createRelativeFile("/dest/scaled-" + LARGEIMAGE);
        assertTrue(
                   "Did not create "+f.getAbsolutePath(),
                   f.exists());
    }
    public void off_testFailOnError() {
        try {
            expectLogContaining("testFailOnError", 
                                "Unable to process image stream");
        }
        catch (RuntimeException re){
            assertTrue("Run time exception should say "
                       + "'Unable to process image stream'. :" 
                       + re.toString(),
                       re.toString()
                       .indexOf("Unable to process image stream") > -1);
        }
    }
    protected File createRelativeFile(String filename) {
        if (filename.equals(".")) {
            return getProjectDir();
        }
        return new File(getProjectDir(), filename);
    }
}
