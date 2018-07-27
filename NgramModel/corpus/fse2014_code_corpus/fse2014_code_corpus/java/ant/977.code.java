package org.apache.tools.ant.taskdefs;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class JarTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static String tempJar = "tmp.jar";
    private static String tempDir = "jartmp/";
    private Reader r1, r2;
    public JarTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/jar.xml");
    }
    public void tearDown() {
        if (r1 != null) {
            try {
                r1.close();
            } catch (IOException e) {
            }
        }
        if (r2 != null) {
            try {
                r2.close();
            } catch (IOException e) {
            }
        }
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        expectBuildException("test2", "manifest file does not exist");
    }
    public void test3() {
        expectBuildException("test3", "Unrecognized whenempty attribute: format C: /y");
    }
    public void test4() {
        executeTarget("test4");
        File jarFile = new File(getProjectDir(), tempJar);
        assertTrue(jarFile.exists());
    }
    public void testNoRecreateWithoutUpdate() {
        testNoRecreate("test4");
    }
    public void testNoRecreateWithUpdate() {
        testNoRecreate("testNoRecreateWithUpdate");
    }
    private void testNoRecreate(String secondTarget) {
        executeTarget("test4");
        File jarFile = new File(getProjectDir(), tempJar);
        long jarModifiedDate = jarFile.lastModified();
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
        } 
        executeTarget(secondTarget);
        assertEquals("jar has not been recreated in " + secondTarget,
                     jarModifiedDate, jarFile.lastModified());
    }
    public void testRecreateWithoutUpdateAdditionalFiles() {
        testRecreate("test4", "testRecreateWithoutUpdateAdditionalFiles");
    }
    public void testRecreateWithUpdateAdditionalFiles() {
        testRecreate("test4", "testRecreateWithUpdateAdditionalFiles");
    }
    public void testRecreateWithoutUpdateNewerFile() {
        testRecreate("testRecreateNewerFileSetup",
                     "testRecreateWithoutUpdateNewerFile");
    }
    public void testRecreateWithUpdateNewerFile() {
        testRecreate("testRecreateNewerFileSetup",
                     "testRecreateWithUpdateNewerFile");
    }
    private void testRecreate(String firstTarget, String secondTarget) {
        executeTarget(firstTarget);
        long sleeptime = 3000
            + FILE_UTILS.getFileTimestampGranularity();
        try {
            Thread.sleep(sleeptime);
        } catch (InterruptedException e) {
        } 
        File jarFile = new File(getProjectDir(), tempJar);
        long jarModifiedDate = jarFile.lastModified();
        executeTarget(secondTarget);
        jarFile = new File(getProjectDir(), tempJar);
        assertTrue("jar has been recreated in " + secondTarget,
                   jarModifiedDate < jarFile.lastModified());
    }
    public void testManifestStaysIntact()
        throws IOException, ManifestException {
        executeTarget("testManifestStaysIntact");
        r1 = new FileReader(getProject()
                            .resolveFile(tempDir + "manifest"));
        r2 = new FileReader(getProject()
                            .resolveFile(tempDir + "META-INF/MANIFEST.MF"));
        Manifest mf1 = new Manifest(r1);
        Manifest mf2 = new Manifest(r2);
        assertEquals(mf1, mf2);
    }
    public void testNoRecreateBasedirExcludesWithUpdate() {
        testNoRecreate("testNoRecreateBasedirExcludesWithUpdate");
    }
    public void testNoRecreateBasedirExcludesWithoutUpdate() {
        testNoRecreate("testNoRecreateBasedirExcludesWithoutUpdate");
    }
    public void testNoRecreateZipfilesetExcludesWithUpdate() {
        testNoRecreate("testNoRecreateZipfilesetExcludesWithUpdate");
    }
    public void testNoRecreateZipfilesetExcludesWithoutUpdate() {
        testNoRecreate("testNoRecreateZipfilesetExcludesWithoutUpdate");
    }
    public void testRecreateZipfilesetWithoutUpdateAdditionalFiles() {
        testRecreate("test4",
                     "testRecreateZipfilesetWithoutUpdateAdditionalFiles");
    }
    public void testRecreateZipfilesetWithUpdateAdditionalFiles() {
        testRecreate("test4",
                     "testRecreateZipfilesetWithUpdateAdditionalFiles");
    }
    public void testRecreateZipfilesetWithoutUpdateNewerFile() {
        testRecreate("testRecreateNewerFileSetup",
                     "testRecreateZipfilesetWithoutUpdateNewerFile");
    }
    public void testRecreateZipfilesetWithUpdateNewerFile() {
        testRecreate("testRecreateNewerFileSetup",
                     "testRecreateZipfilesetWithUpdateNewerFile");
    }
    public void testCreateWithEmptyFileset() {
        executeTarget("testCreateWithEmptyFilesetSetUp");
        executeTarget("testCreateWithEmptyFileset");
        executeTarget("testCreateWithEmptyFileset");
    }
    public void testUpdateIfOnlyManifestHasChanged() {
        executeTarget("testUpdateIfOnlyManifestHasChanged");
        File jarXml = getProject().resolveFile(tempDir + "jar.xml");
        assertTrue(jarXml.exists());
    }
    public void testNoDuplicateIndex() throws IOException {
        ZipFile archive = null;
        try {
            executeTarget("testIndexTests");
            archive = new ZipFile(getProject().resolveFile(tempJar));
            Enumeration e = archive.entries();
            int numberOfIndexLists = 0;
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();
                if (ze.getName().equals("META-INF/INDEX.LIST")) {
                    numberOfIndexLists++;
                }
            }
            assertEquals(1, numberOfIndexLists);
        } finally {
            if (archive != null) {
                archive.close();
            }
        }
    }
    public void testRootFilesInIndex() throws IOException {
        ZipFile archive = null;
        try {
            executeTarget("testIndexTests");
            archive = new ZipFile(getProject().resolveFile(tempJar));
            ZipEntry ze = archive.getEntry("META-INF/INDEX.LIST");
            InputStream is = archive.getInputStream(ze);
            BufferedReader r = new BufferedReader(new InputStreamReader(is,
                                                                        "UTF8"));
            boolean foundSub = false;
            boolean foundSubFoo = false;
            boolean foundFoo = false;
            String line = r.readLine();
            while (line != null) {
                if (line.equals("foo")) {
                    foundFoo = true;
                } else if (line.equals("sub")) {
                    foundSub = true;
                } else if (line.equals("sub/foo")) {
                    foundSubFoo = true;
                }
                line = r.readLine();
            }
            assertTrue(foundSub);
            assertTrue(!foundSubFoo);
            assertTrue(foundFoo);
        } finally {
            if (archive != null) {
                archive.close();
            }
        }
    }
    public void testManifestOnlyJar() {
        expectLogContaining("testManifestOnlyJar", "Building MANIFEST-only jar: ");
        File manifestFile = getProject().resolveFile(tempDir + "META-INF" + File.separator + "MANIFEST.MF");
        assertTrue(manifestFile.exists());
    }
    public void testIndexJarsPlusJarMarker() {
        executeTarget("testIndexJarsPlusJarMarker");
    }
    public void testNoVersionInfoFail() {
        expectBuildExceptionContaining("testNoVersionInfoFail", "Manifest Implemention information missing.", "No Implementation-Title set.");
    }
    public void testNoVersionInfoIgnore() {
        executeTarget("testNoVersionInfoIgnore");
        assertTrue( getFullLog().indexOf("No Implementation-Title set.") > -1 );
        assertTrue( getFullLog().indexOf("No Implementation-Version set.") > -1 );
        assertTrue( getFullLog().indexOf("No Implementation-Vendor set.") > -1 );
    }
    public void testNoVersionInfoWarn() {
        executeTarget("testNoVersionInfoWarn");
        assertTrue( getLog().indexOf("No Implementation-Title set.") > -1 );
        assertTrue( getLog().indexOf("No Implementation-Version set.") > -1 );
        assertTrue( getLog().indexOf("No Implementation-Vendor set.") > -1 );
    }
    public void testNoVersionInfoNoStrict() {
        executeTarget("testNoVersionInfoNoStrict");
        assertFalse( getLog().indexOf("No Implementation-Title set.") > -1 );
        assertFalse( getLog().indexOf("No Implementation-Version set.") > -1 );
        assertFalse( getLog().indexOf("No Implementation-Vendor set.") > -1 );
    }
    public void testHasVersionInfo() {
        executeTarget("testHasVersionInfo");
        assertFalse( getLog().indexOf("No Implementation-Title set.") > -1 );
        assertFalse( getLog().indexOf("No Implementation-Version set.") > -1 );
        assertFalse( getLog().indexOf("No Implementation-Vendor set.") > -1 );
    }
}
