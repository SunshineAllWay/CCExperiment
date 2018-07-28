package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class JUnitReportTest extends BuildFileTest {
    public JUnitReportTest(String name){
        super(name);
    }
    protected void setUp() {
        configureProject("src/etc/testcases/taskdefs/optional/junitreport.xml");
    }
    protected void tearDown() {
        executeTarget("clean");
    }
    public void testNoFileJUnitNoFrames() {
        executeTarget("reports1");
        if (new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/junit-noframes.html").exists())
        {
            fail("No file junit-noframes.html expected");
        }
    }
    public void assertIndexCreated() {
        if (!new File(System.getProperty("root"),
                "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html").exists()) {
            fail("No file index file found");
        }
    }
    private void expectReportWithText(String targetName, String text) {
        executeTarget(targetName);
        assertIndexCreated();
        if(text!=null) {
            assertLogContaining(text);
        }
    }
    public void testEmptyFile() throws Exception {
        expectReportWithText("testEmptyFile",
                XMLResultAggregator.WARNING_EMPTY_FILE);
    }
    public void testIncompleteFile() throws Exception {
        expectReportWithText("testIncompleteFile",
                XMLResultAggregator.WARNING_IS_POSSIBLY_CORRUPTED);
    }
    public void testWrongElement() throws Exception {
        expectReportWithText("testWrongElement",
                XMLResultAggregator.WARNING_INVALID_ROOT_ELEMENT);
    }
    public void testStackTraceLineBreaks() throws Exception {
        expectReportWithText("testStackTraceLineBreaks", null);
        FileReader r = null;
        try {
            r = new FileReader(new File(System.getProperty("root"),
                                        "src/etc/testcases/taskdefs/optional/junitreport/test/html/sampleproject/coins/0_CoinTest.html"));
            String report = FileUtils.readFully(r);
            assertTrue("output must contain <br>",
                       report.indexOf("junit.framework.AssertionFailedError: DOEG<br/>")
                   > -1);
        } finally {
            FileUtils.close(r);
        }
    }
    public void testSpecialSignsInSrcPath() throws Exception {
        executeTarget("testSpecialSignsInSrcPath");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testSpecialSignsInHtmlPath() throws Exception {
        executeTarget("testSpecialSignsInHtmlPath");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html# $%\u00A7&-!report/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testWithStyleFromDir() throws Exception {
        executeTarget("testWithStyleFromDir");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testNoFrames() throws Exception {
        executeTarget("testNoFrames");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/junit-noframes.html");
        assertTrue("No junit-noframes.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testWithStyleFromDirAndXslImport() throws Exception {
        executeTarget("testWithStyleFromDirAndXslImport");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testWithStyleFromClasspath() throws Exception {
        executeTarget("testWithStyleFromClasspath");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
    public void testWithParams() throws Exception {
        expectLogContaining("testWithParams", "key1=value1,key2=value2");
        File reportFile = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/optional/junitreport/test/html/index.html");
        assertTrue("No index.html present. Not generated?", reportFile.exists() );
        assertTrue("Cant read the report file.", reportFile.canRead() );
        assertTrue("File shouldnt be empty.", reportFile.length() > 0 );
        URL reportUrl = new URL( FileUtils.getFileUtils().toURI(reportFile.getAbsolutePath()) );
        InputStream reportStream = reportUrl.openStream();
        assertTrue("This shouldnt be an empty stream.", reportStream.available() > 0);
    }
}
