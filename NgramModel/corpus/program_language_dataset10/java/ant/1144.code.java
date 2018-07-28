package org.apache.tools.ant.util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Os;
public class FileUtilsTest extends TestCase {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File removeThis;
    private String root;
    public FileUtilsTest(String name) {
        super(name);
    }
    public void setUp() {
        root = new File(File.separator).getAbsolutePath().toUpperCase();
    }
    public void tearDown() {
        if (removeThis != null && removeThis.exists()) {
            if (!removeThis.delete())
            {
                removeThis.deleteOnExit();
            }
        }
    }
    public void testSetLastModified() throws IOException {
        removeThis = new File("dummy");
        FileOutputStream fos = new FileOutputStream(removeThis);
        fos.write(new byte[0]);
        fos.close();
        long modTime = removeThis.lastModified();
        assertTrue(modTime != 0);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ie) {
            fail(ie.getMessage());
        }
        FILE_UTILS.setFileLastModified(removeThis, -1);
        long secondModTime = removeThis.lastModified();
        assertTrue(secondModTime > modTime);
        final int millisperday=24 * 3600 * 1000;
        FILE_UTILS.setFileLastModified(removeThis, secondModTime + millisperday);
        long thirdModTime = removeThis.lastModified();
        assertTrue(thirdModTime != secondModTime);
    }
    public void testResolveFile() {
        if (!(Os.isFamily("dos") || Os.isFamily("netware"))) {
            assertEquals(File.separator,
                         FILE_UTILS.resolveFile(null, "/").getPath());
            assertEquals(File.separator,
                         FILE_UTILS.resolveFile(null, "\\").getPath());
        } else {
            assertEqualsIgnoreDriveCase(localize(File.separator),
                FILE_UTILS.resolveFile(null, "/").getPath());
            assertEqualsIgnoreDriveCase(localize(File.separator),
                FILE_UTILS.resolveFile(null, "\\").getPath());
            String driveSpec = "C:";
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.resolveFile(null, driveSpec + "/").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.resolveFile(null, driveSpec + "\\").getPath());
            String driveSpecLower = "c:";
            assertEquals(driveSpecLower + "\\",
                         FILE_UTILS.resolveFile(null, driveSpecLower + "/").getPath());
            assertEquals(driveSpecLower + "\\",
                         FILE_UTILS.resolveFile(null, driveSpecLower + "\\").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.resolveFile(null, driveSpec + "/////").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.resolveFile(null, driveSpec + "\\\\\\\\\\\\").getPath());
        }
        if (Os.isFamily("netware")) {
            String driveSpec = "SYS:";
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "/").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "\\").getPath());
            String driveSpecLower = "sys:";
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpecLower + "/").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpecLower + "\\").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "/////").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "\\\\\\\\\\\\").getPath());
        } else if (!(Os.isFamily("dos"))) {
            String driveSpec = "C:";
            String udir = System.getProperty("user.dir");
            assertEquals(udir + File.separator + driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "/").getPath());
            assertEquals(udir + File.separator + driveSpec,
                         FILE_UTILS.resolveFile(null, driveSpec + "\\").getPath());
            String driveSpecLower = "c:";
            assertEquals(udir + File.separator + driveSpecLower,
                         FILE_UTILS.resolveFile(null, driveSpecLower + "/").getPath());
            assertEquals(udir + File.separator + driveSpecLower,
                         FILE_UTILS.resolveFile(null, driveSpecLower + "\\").getPath());
        }
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "./4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), ".\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "./.\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "../3/4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "..\\3\\4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "../../5/.././2/./3/6/../4").getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.resolveFile(new File(localize("/1/2/3")), "..\\../5/..\\./2/./3/6\\../4").getPath());
        assertEquals("meaningless result but no exception",
                new File(localize("/1/../../b")),
                FILE_UTILS.resolveFile(new File(localize("/1")), "../../b"));
    }
    public void testNormalize() {
        if (!(Os.isFamily("dos") || Os.isFamily("netware"))) {
            assertEquals(File.separator,
                         FILE_UTILS.normalize("/").getPath());
            assertEquals(File.separator,
                         FILE_UTILS.normalize("\\").getPath());
        } else {
            try {
                 FILE_UTILS.normalize("/").getPath();
                 fail("normalized \"/\" on dos or netware");
            } catch (Exception e) {
            }
            try {
                 FILE_UTILS.normalize("\\").getPath();
                 fail("normalized \"\\\" on dos or netware");
            } catch (Exception e) {
            }
        }
        if (Os.isFamily("dos")) {
            String driveSpec = "C:";
            try {
                 FILE_UTILS.normalize(driveSpec).getPath();
                 fail(driveSpec + " is not an absolute path");
            } catch (Exception e) {
            }
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.normalize(driveSpec + "/").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.normalize(driveSpec + "\\").getPath());
            String driveSpecLower = "c:";
            assertEquals(driveSpecLower + "\\",
                         FILE_UTILS.normalize(driveSpecLower + "/").getPath());
            assertEquals(driveSpecLower + "\\",
                         FILE_UTILS.normalize(driveSpecLower + "\\").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.normalize(driveSpec + "/////").getPath());
            assertEquals(driveSpec + "\\",
                         FILE_UTILS.normalize(driveSpec + "\\\\\\\\\\\\").getPath());
        } else if (Os.isFamily("netware")) {
            String driveSpec = "SYS:";
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpec).getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpec + "/").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpec + "\\").getPath());
            String driveSpecLower = "sys:";
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpecLower).getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpecLower + "/").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpecLower + "\\").getPath());
            assertEquals(driveSpec + "\\junk",
                         FILE_UTILS.normalize(driveSpecLower + "\\junk").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpec + "/////").getPath());
            assertEquals(driveSpec,
                         FILE_UTILS.normalize(driveSpec + "\\\\\\\\\\\\").getPath());
        } else {
            try {
                String driveSpec = "C:";
                assertEquals(driveSpec,
                             FILE_UTILS.normalize(driveSpec).getPath());
                fail("Expected failure, C: isn't an absolute path on other os's");
            } catch (BuildException e) {
            }
        }
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/./4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/.\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/./.\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/../3/4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/..\\3\\4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/../../5/.././2/./3/6/../4")).getPath());
        assertEquals(localize("/1/2/3/4"),
                     FILE_UTILS.normalize(localize("/1/2/3/..\\../5/..\\./2/./3/6\\../4")).getPath());
        try {
            FILE_UTILS.normalize("foo");
            fail("foo is not an absolute path");
        } catch (BuildException e) {
        }
        assertEquals("will not go outside FS root (but will not throw an exception either)",
                new File(localize("/1/../../b")),
                FILE_UTILS.normalize(localize("/1/../../b")));
    }
    public void testNullArgs() {
        try {
            FILE_UTILS.normalize(null);
            fail("successfully normalized a null-file");
        } catch (NullPointerException npe) {
        }
        File f = FILE_UTILS.resolveFile(null, "a");
        assertEquals(f, new File("a").getAbsoluteFile());
    }
    public void testCreateTempFile()
    {
        File tmp1 = FILE_UTILS.createTempFile("pre", ".suf", null, false, true);
        String tmploc = System.getProperty("java.io.tmpdir");
        String name = tmp1.getName();
        assertTrue("starts with pre", name.startsWith("pre"));
        assertTrue("ends with .suf", name.endsWith(".suf"));
        assertTrue("File was created", tmp1.exists());
        assertEquals((new File(tmploc, tmp1.getName())).getAbsolutePath(), tmp1
                .getAbsolutePath());
        tmp1.delete();
        File dir2 = new File(tmploc + "/ant-test");
        dir2.mkdir();
        removeThis = dir2;
        File tmp2 = FILE_UTILS.createTempFile("pre", ".suf", dir2, true, true);
        String name2 = tmp2.getName();
        assertTrue("starts with pre", name2.startsWith("pre"));
        assertTrue("ends with .suf", name2.endsWith(".suf"));
        assertTrue("File was created", tmp2.exists());
        assertEquals((new File(dir2, tmp2.getName())).getAbsolutePath(), tmp2
                .getAbsolutePath());
        tmp2.delete();
        dir2.delete();
        File parent = new File((new File("/tmp")).getAbsolutePath());
        tmp1 = FILE_UTILS.createTempFile("pre", ".suf", parent, false);
        assertTrue("new file", !tmp1.exists());
        name = tmp1.getName();
        assertTrue("starts with pre", name.startsWith("pre"));
        assertTrue("ends with .suf", name.endsWith(".suf"));
        assertEquals("is inside parent dir", parent.getAbsolutePath(), tmp1
                .getParent());
        tmp2 = FILE_UTILS.createTempFile("pre", ".suf", parent, false);
        assertTrue("files are different", !tmp1.getAbsolutePath().equals(
                tmp2.getAbsolutePath()));
        File tmp3 = FILE_UTILS.createTempFile("pre", ".suf", null, false);
        tmploc = System.getProperty("java.io.tmpdir");
        assertEquals((new File(tmploc, tmp3.getName())).getAbsolutePath(), tmp3
                .getAbsolutePath());
    }
    public void testContentEquals() throws IOException {
        assertTrue("Non existing files", FILE_UTILS.contentEquals(new File(System.getProperty("root"), "foo"),
                                                          new File(System.getProperty("root"), "bar")));
        assertTrue("One exists, the other one doesn\'t",
                   !FILE_UTILS.contentEquals(new File(System.getProperty("root"), "foo"), new File(System.getProperty("root"), "build.xml")));
        assertTrue("Don\'t compare directories",
                   !FILE_UTILS.contentEquals(new File(System.getProperty("root"), "src"), new File(System.getProperty("root"), "src")));
        assertTrue("File equals itself",
                   FILE_UTILS.contentEquals(new File(System.getProperty("root"), "build.xml"),
                                    new File(System.getProperty("root"), "build.xml")));
        assertTrue("Files are different",
                   !FILE_UTILS.contentEquals(new File(System.getProperty("root"), "build.xml"),
                                     new File(System.getProperty("root"), "docs.xml")));
    }
    public void testCreateNewFile() throws IOException {
        removeThis = new File("dummy");
        assertTrue(!removeThis.exists());
        FILE_UTILS.createNewFile(removeThis);
        assertTrue(removeThis.exists());
    }
    public void testRemoveLeadingPath() {
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("/foo"),
                                                 new File("/foo/bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("/foo/"),
                                                 new File("/foo/bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("\\foo"),
                                                 new File("\\foo\\bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("\\foo\\"),
                                                 new File("\\foo\\bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("c:/foo"),
                                                 new File("c:/foo/bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("c:/foo/"),
                                                 new File("c:/foo/bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("c:\\foo"),
                                                 new File("c:\\foo\\bar")));
        assertEquals("bar", FILE_UTILS.removeLeadingPath(new File("c:\\foo\\"),
                                                 new File("c:\\foo\\bar")));
        if (!(Os.isFamily("dos") || Os.isFamily("netware"))) {
            assertEquals(FILE_UTILS.normalize("/bar").getAbsolutePath(),
                         FILE_UTILS.removeLeadingPath(new File("/foo"), new File("/bar")));
            assertEquals(FILE_UTILS.normalize("/foobar").getAbsolutePath(),
                         FILE_UTILS.removeLeadingPath(new File("/foo"), new File("/foobar")));
        }
        assertEquals("", FILE_UTILS.removeLeadingPath(new File("/foo/bar"),
                                              new File("/foo/bar")));
        assertEquals("", FILE_UTILS.removeLeadingPath(new File("/foo/bar"),
                                              new File("/foo/bar/")));
        assertEquals("", FILE_UTILS.removeLeadingPath(new File("/foo/bar/"),
                                              new File("/foo/bar/")));
        assertEquals("", FILE_UTILS.removeLeadingPath(new File("/foo/bar/"),
                                              new File("/foo/bar")));
        String expected = "foo/bar".replace('\\', File.separatorChar)
            .replace('/', File.separatorChar);
        assertEquals(expected, FILE_UTILS.removeLeadingPath(new File("/"),
                                                    new File("/foo/bar")));
        assertEquals(expected, FILE_UTILS.removeLeadingPath(new File("c:/"),
                                                    new File("c:/foo/bar")));
        assertEquals(expected, FILE_UTILS.removeLeadingPath(new File("c:\\"),
                                                    new File("c:\\foo\\bar")));
    }
    public void testToURI() {
        String dosRoot = null;
        if (Os.isFamily("dos") || Os.isFamily("netware")) {
            dosRoot = System.getProperty("user.dir")
                .substring(0, 3).replace(File.separatorChar, '/');
        }
        else
        {
            dosRoot = "";
        }
        if (Os.isFamily("dos")) {
            assertEquals("file:/c:/foo", removeExtraneousAuthority(FILE_UTILS.toURI("c:\\foo")));
        }
        if (Os.isFamily("netware")) {
            assertEquals("file:/SYS:/foo", removeExtraneousAuthority(FILE_UTILS.toURI("sys:\\foo")));
        }
        if (File.pathSeparatorChar == '/') {
            assertEquals("file:/foo", removeExtraneousAuthority(FILE_UTILS.toURI("/foo")));
            assertTrue("file: URIs must name absolute paths", FILE_UTILS.toURI("./foo").startsWith("file:/"));
            assertTrue(FILE_UTILS.toURI("./foo").endsWith("/foo"));
            assertEquals("file:/" + dosRoot + "foo%20bar", removeExtraneousAuthority(FILE_UTILS.toURI("/foo bar")));
            assertEquals("file:/" + dosRoot + "foo%23bar", removeExtraneousAuthority(FILE_UTILS.toURI("/foo#bar")));
        } else if (File.pathSeparatorChar == '\\') {
            assertEquals("file:/" + dosRoot + "foo", removeExtraneousAuthority(FILE_UTILS.toURI("\\foo")));
            assertTrue("file: URIs must name absolute paths", FILE_UTILS.toURI(".\\foo").startsWith("file:/"));
            assertTrue(FILE_UTILS.toURI(".\\foo").endsWith("/foo"));
            assertEquals("file:/" + dosRoot + "foo%20bar", removeExtraneousAuthority(FILE_UTILS.toURI("\\foo bar")));
            assertEquals("file:/" + dosRoot + "foo%23bar", removeExtraneousAuthority(FILE_UTILS.toURI("\\foo#bar")));
        }
        assertEquals("file:/" + dosRoot + "%C3%A4nt", removeExtraneousAuthority(FILE_UTILS.toURI("/\u00E4nt")));
    }
    private static String removeExtraneousAuthority(String uri) {
        String prefix = "file:///";
        if (uri.startsWith(prefix)) {
            return "file:/" + uri.substring(prefix.length());
        } else {
            return uri;
        }
    }
    public void testIsContextRelativePath() {
        if (Os.isFamily("dos")) {
            assertTrue(FileUtils.isContextRelativePath("/\u00E4nt"));
            assertTrue(FileUtils.isContextRelativePath("\\foo"));
        }
    }
    public void testFromURI() {
        String dosRoot = null;
        if (Os.isFamily("dos") || Os.isFamily("netware")) {
            dosRoot = System.getProperty("user.dir").substring(0, 2);
        } else {
            dosRoot = "";
        }
        if (Os.isFamily("netware")) {
            assertEqualsIgnoreDriveCase("SYS:\\foo", FILE_UTILS.fromURI("file:///sys:/foo"));
        }
        if (Os.isFamily("dos")) {
            assertEqualsIgnoreDriveCase("C:\\foo", FILE_UTILS.fromURI("file:///c:/foo"));
        }
        assertEqualsIgnoreDriveCase(dosRoot + File.separator + "foo", FILE_UTILS.fromURI("file:///foo"));
        assertEquals("." + File.separator + "foo",
                     FILE_UTILS.fromURI("file:./foo"));
        assertEquals(dosRoot + File.separator + "foo bar", FILE_UTILS.fromURI("file:///foo%20bar"));
        assertEquals(dosRoot + File.separator + "foo#bar", FILE_UTILS.fromURI("file:///foo%23bar"));
    }
    public void testModificationTests() {
        long firstTime=System.currentTimeMillis();
        long secondTime=firstTime+60000;
        assertTrue("older source files are up to date",
                FILE_UTILS.isUpToDate(firstTime,secondTime));
        assertFalse("newer source files are no up to date",
                FILE_UTILS.isUpToDate(secondTime, firstTime));
        assertTrue("-1 dest timestamp implies nonexistence",
                !FILE_UTILS.isUpToDate(firstTime,-1L));
    }
    public void testHasErrorInCase() {
        File tempFolder = new File(System.getProperty("java.io.tmpdir"));
        File wellcased = FILE_UTILS.createTempFile("alpha", "beta", tempFolder,
                                                   true, true);
        String s = wellcased.getName().toUpperCase();
        File wrongcased = new File(tempFolder, s);
        if (Os.isFamily("mac") && Os.isFamily("unix")) {
        } else if (Os.isFamily("dos")) {
            assertTrue(FILE_UTILS.hasErrorInCase(wrongcased));
            assertFalse(FILE_UTILS.hasErrorInCase(wellcased));
        } else {
            assertFalse(FILE_UTILS.hasErrorInCase(wrongcased));
            assertFalse(FILE_UTILS.hasErrorInCase(wellcased));
        }
    }
    public void testGetDefaultEncoding() {
        FILE_UTILS.getDefaultEncoding();
    }
    private String localize(String path) {
        path = root + path.substring(1);
        return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
    }
    private void assertEqualsIgnoreDriveCase(String s1, String s2) {
        if ((Os.isFamily("dos") || Os.isFamily("netware"))
            && s1.length() > 0 && s2.length() > 0) {
            StringBuffer sb1 = new StringBuffer(s1);
            StringBuffer sb2 = new StringBuffer(s2);
            sb1.setCharAt(0, Character.toUpperCase(s1.charAt(0)));
            sb2.setCharAt(0, Character.toUpperCase(s2.charAt(0)));
            assertEquals(sb1.toString(), sb2.toString());
        } else {
            assertEquals(s1, s2);
        }
    }
}
