package org.apache.tools.ant.types;
import java.io.File;
import java.util.Locale;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.condition.Os;
public class PathTest extends TestCase {
    public static boolean isUnixStyle = File.pathSeparatorChar == ':';
    public static boolean isNetWare = Os.isFamily("netware");
    private Project project;
    public PathTest(String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.setBasedir(System.getProperty("root"));
    }
    public void testConstructorUnixStyle() {
        Path p = new Path(project, "/a:/b");
        String[] l = p.list();
        assertEquals("two items, Unix style", 2, l.length);
        if (isUnixStyle) {
            assertEquals("/a", l[0]);
            assertEquals("/b", l[1]);
        } else if (isNetWare) {
            assertEquals("\\a", l[0]);
            assertEquals("\\b", l[1]);
        } else {
            String base = new File(File.separator).getAbsolutePath();
            assertEquals(base + "a", l[0]);
            assertEquals(base + "b", l[1]);
        }
    }
    public void testRelativePathUnixStyle() {
        project.setBasedir(new File(System.getProperty("root"), "src/etc").getAbsolutePath());
        Path p = new Path(project, "..:testcases");
        String[] l = p.list();
        assertEquals("two items, Unix style", 2, l.length);
        if (isUnixStyle) {
           assertTrue("test resolved relative to src/etc",
                 l[0].endsWith("/src"));
           assertTrue("test resolved relative to src/etc",
                 l[1].endsWith("/src/etc/testcases"));
        } else if (isNetWare) {
           assertTrue("test resolved relative to src/etc",
                 l[0].endsWith("\\src"));
           assertTrue("test resolved relative to src/etc",
                 l[1].endsWith("\\src\\etc\\testcases"));
        } else {
           assertTrue("test resolved relative to src/etc",
                 l[0].endsWith("\\src"));
           assertTrue("test resolved relative to src/etc",
                 l[1].endsWith("\\src\\etc\\testcases"));
        }
    }
    public void testConstructorWindowsStyle() {
        Path p = new Path(project, "\\a;\\b");
        String[] l = p.list();
        assertEquals("two items, DOS style", 2, l.length);
        if (isUnixStyle) {
            assertEquals("/a", l[0]);
            assertEquals("/b", l[1]);
        } else if (isNetWare) {
            assertEquals("\\a", l[0]);
            assertEquals("\\b", l[1]);
        } else {
            String base = new File(File.separator).getAbsolutePath();
            assertEquals(base + "a", l[0]);
            assertEquals(base + "b", l[1]);
        }
        p = new Path(project, "c:\\test");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 2, l.length);
            assertTrue("c resolved relative to project\'s basedir",
                   l[0].endsWith("/c"));
            assertEquals("/test", l[1]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 1, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
        } else {
            assertEquals("drives on DOS", 1, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
        }
        p = new Path(project, "c:\\test;d:\\programs");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 4, l.length);
            assertTrue("c resolved relative to project\'s basedir",
                   l[0].endsWith("/c"));
            assertEquals("/test", l[1]);
            assertTrue("d resolved relative to project\'s basedir",
                   l[2].endsWith("/d"));
            assertEquals("/programs", l[3]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 2, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("d:\\programs", l[1].toLowerCase(Locale.US));
        } else {
            assertEquals("drives on DOS", 2, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("d:\\programs", l[1].toLowerCase(Locale.US));
        }
        p = new Path(project, "c:/test");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 2, l.length);
            assertTrue("c resolved relative to project\'s basedir",
                   l[0].endsWith("/c"));
            assertEquals("/test", l[1]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 1, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
        } else {
            assertEquals("drives on DOS", 1, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
        }
        p = new Path(project, "c:/test;d:/programs");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 4, l.length);
            assertTrue("c resolved relative to project\'s basedir",
                   l[0].endsWith("/c"));
            assertEquals("/test", l[1]);
            assertTrue("d resolved relative to project\'s basedir",
                   l[2].endsWith("/d"));
            assertEquals("/programs", l[3]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 2, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("d:\\programs", l[1].toLowerCase(Locale.US));
        } else {
            assertEquals("drives on DOS", 2, l.length);
            assertEquals("c:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("d:\\programs", l[1].toLowerCase(Locale.US));
        }
    }
    public void testConstructorNetWareStyle() {
        Path p = new Path(project, "sys:\\test");
        String[] l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 2, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("/sys"));
            assertEquals("/test", l[1]);
        } else if (isNetWare) {
            assertEquals("sys:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("volumes on NetWare", 1, l.length);
        } else {
            assertEquals("no multiple character-length volumes on Windows", 2, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("\\sys"));
            assertTrue("test resolved relative to project\'s basedir",
                   l[1].endsWith("\\test"));
        }
        p = new Path(project, "sys:\\test;dev:\\temp");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 4, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("/sys"));
            assertEquals("/test", l[1]);
            assertTrue("dev resolved relative to project\'s basedir",
                   l[2].endsWith("/dev"));
            assertEquals("/temp", l[3]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 2, l.length);
            assertEquals("sys:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("dev:\\temp", l[1].toLowerCase(Locale.US));
        } else {
            assertEquals("no multiple character-length volumes on Windows", 4, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("\\sys"));
            assertTrue("test resolved relative to project\'s basedir",
                   l[1].endsWith("\\test"));
            assertTrue("dev resolved relative to project\'s basedir",
                   l[2].endsWith("\\dev"));
            assertTrue("temp resolved relative to project\'s basedir",
                   l[3].endsWith("\\temp"));
        }
        p = new Path(project, "sys:/test");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 2, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("/sys"));
            assertEquals("/test", l[1]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 1, l.length);
            assertEquals("sys:\\test", l[0].toLowerCase(Locale.US));
        } else {
            assertEquals("no multiple character-length volumes on Windows", 2, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("\\sys"));
            assertTrue("test resolved relative to project\'s basedir",
                   l[1].endsWith("\\test"));
        }
        p = new Path(project, "sys:/test;dev:/temp");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 4, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("/sys"));
            assertEquals("/test", l[1]);
            assertTrue("dev resolved relative to project\'s basedir",
                   l[2].endsWith("/dev"));
            assertEquals("/temp", l[3]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 2, l.length);
            assertEquals("sys:\\test", l[0].toLowerCase(Locale.US));
            assertEquals("dev:\\temp", l[1].toLowerCase(Locale.US));
        } else {
            assertEquals("no multiple character-length volumes on Windows", 4, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("\\sys"));
            assertTrue("test resolved relative to project\'s basedir",
                   l[1].endsWith("\\test"));
            assertTrue("dev resolved relative to project\'s basedir",
                   l[2].endsWith("\\dev"));
            assertTrue("temp resolved relative to project\'s basedir",
                   l[3].endsWith("\\temp"));
         }
        p = new Path(project,
                     "SYS:\\JAVA/lib/rt.jar:SYS:\\JAVA/lib/classes.zip");
        l = p.list();
        if (isUnixStyle) {
            assertEquals("no drives on Unix", 3, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("/SYS"));
            assertEquals("/JAVA/lib/rt.jar", l[1]);
            assertEquals("/JAVA/lib/classes.zip", l[2]);
        } else if (isNetWare) {
            assertEquals("volumes on NetWare", 2, l.length);
            assertEquals("sys:\\java\\lib\\rt.jar", l[0].toLowerCase(Locale.US));
            assertEquals("sys:\\java\\lib\\classes.zip", l[1].toLowerCase(Locale.US));
        } else {
            assertEquals("no multiple character-length volumes on Windows", 3, l.length);
            assertTrue("sys resolved relative to project\'s basedir",
                   l[0].endsWith("\\SYS"));
            assertTrue("java/lib/rt.jar resolved relative to project\'s basedir",
                   l[1].endsWith("\\JAVA\\lib\\rt.jar"));
            assertTrue("java/lib/classes.zip resolved relative to project\'s basedir",
                   l[2].endsWith("\\JAVA\\lib\\classes.zip"));
        }
    }
    public void testConstructorMixedStyle() {
        Path p = new Path(project, "\\a;\\b:/c");
        String[] l = p.list();
        assertEquals("three items, mixed style", 3, l.length);
        if (isUnixStyle) {
            assertEquals("/a", l[0]);
            assertEquals("/b", l[1]);
            assertEquals("/c", l[2]);
        } else if (isNetWare) {
            assertEquals("\\a", l[0]);
            assertEquals("\\b", l[1]);
            assertEquals("\\c", l[2]);
        } else {
            String base = new File(File.separator).getAbsolutePath();
            assertEquals(base + "a", l[0]);
            assertEquals(base + "b", l[1]);
            assertEquals(base + "c", l[2]);
        }
    }
    public void testSetLocation() {
        Path p = new Path(project);
        p.setLocation(new File(File.separatorChar+"a"));
        String[] l = p.list();
        if (isUnixStyle) {
            assertEquals(1, l.length);
            assertEquals("/a", l[0]);
        } else if (isNetWare) {
            assertEquals(1, l.length);
            assertEquals("\\a", l[0]);
        } else {
            assertEquals(1, l.length);
            assertEquals(":\\a", l[0].substring(1));
        }
    }
    public void testAppending() {
        Path p = new Path(project, "/a:/b");
        String[] l = p.list();
        assertEquals("2 after construction", 2, l.length);
        p.setLocation(new File("/c"));
        l = p.list();
        assertEquals("3 after setLocation", 3, l.length);
        p.setPath("\\d;\\e");
        l = p.list();
        assertEquals("5 after setPath", 5, l.length);
        p.append(new Path(project, "\\f"));
        l = p.list();
        assertEquals("6 after append", 6, l.length);
        p.createPath().setLocation(new File("/g"));
        l = p.list();
        assertEquals("7 after append", 7, l.length);
    }
    public void testEmpyPath() {
        Path p = new Path(project, "");
        String[] l = p.list();
        assertEquals("0 after construction", 0, l.length);
        p.setPath("");
        l = p.list();
        assertEquals("0 after setPath", 0, l.length);
        p.append(new Path(project));
        l = p.list();
        assertEquals("0 after append", 0, l.length);
        p.createPath();
        l = p.list();
        assertEquals("0 after append", 0, l.length);
    }
    public void testUnique() {
        Path p = new Path(project, "/a:/a");
        String[] l = p.list();
        assertEquals("1 after construction", 1, l.length);
        String base = new File(File.separator).getAbsolutePath();
        p.setLocation(new File(base, "a"));
        l = p.list();
        assertEquals("1 after setLocation", 1, l.length);
        p.setPath("\\a;/a");
        l = p.list();
        assertEquals("1 after setPath", 1, l.length);
        p.append(new Path(project, "/a;\\a:\\a"));
        l = p.list();
        assertEquals("1 after append", 1, l.length);
        p.createPath().setPath("\\a:/a");
        l = p.list();
        assertEquals("1 after append", 1, l.length);
    }
    public void testEmptyElementIfIsReference() {
        Path p = new Path(project, "/a:/a");
        try {
            p.setRefid(new Reference(project, "dummyref"));
            fail("Can add reference to Path with elements from constructor");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one attribute when using refid",
                         be.getMessage());
        }
        p = new Path(project);
        p.setLocation(new File("/a"));
        try {
            p.setRefid(new Reference(project, "dummyref"));
            fail("Can add reference to Path with elements from setLocation");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one attribute when using refid",
                         be.getMessage());
        }
        Path another = new Path(project, "/a:/a");
        project.addReference("dummyref", another);
        p = new Path(project);
        p.setRefid(new Reference(project, "dummyref"));
        try {
            p.setLocation(new File("/a"));
            fail("Can set location in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one attribute when using refid",
                         be.getMessage());
        }
        try {
            p.setPath("/a;\\a");
            fail("Can set path in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify more than one attribute when using refid",
                         be.getMessage());
        }
        try {
            p.createPath();
            fail("Can create nested Path in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
        try {
            p.createPathElement();
            fail("Can create nested PathElement in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
        try {
            p.addFileset(new FileSet());
            fail("Can add nested FileSet in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
        try {
            p.addFilelist(new FileList());
            fail("Can add nested FileList in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
        try {
            p.addDirset(new DirSet());
            fail("Can add nested Dirset in Path that is a reference.");
        } catch (BuildException be) {
            assertEquals("You must not specify nested elements when using refid",
                         be.getMessage());
        }
    }
    public void testCircularReferenceCheck() {
        Path p = new Path(project);
        project.addReference("dummy", p);
        p.setRefid(new Reference(project, "dummy"));
        try {
            p.list();
            fail("Can make Path a Reference to itself.");
        } catch (BuildException be) {
            assertEquals("This data type contains a circular reference.",
                         be.getMessage());
        }
        Path p1 = new Path(project);
        project.addReference("dummy1", p1);
        Path p2 = p1.createPath();
        project.addReference("dummy2", p2);
        Path p3 = p2.createPath();
        project.addReference("dummy3", p3);
        p3.setRefid(new Reference(project, "dummy1"));
        try {
            p1.list();
            fail("Can make circular reference.");
        } catch (BuildException be) {
            assertEquals("This data type contains a circular reference.",
                         be.getMessage());
        }
        p1 = new Path(project);
        project.addReference("dummy1", p1);
        p2 = p1.createPath();
        project.addReference("dummy2", p2);
        p3 = p2.createPath();
        project.addReference("dummy3", p3);
        p3.setLocation(new File("/a"));
        String[] l = p1.list();
        assertEquals("One element buried deep inside a nested path structure",
                     1, l.length);
        if (isUnixStyle) {
            assertEquals("/a", l[0]);
        } else if (isNetWare) {
            assertEquals("\\a", l[0]);
        } else {
            assertEquals(":\\a", l[0].substring(1));
        }
    }
    public void testFileList() {
        Path p = new Path(project);
        FileList f = new FileList();
        f.setProject(project);
        f.setDir(project.resolveFile("."));
        f.setFiles("build.xml");
        p.addFilelist(f);
        String[] l = p.list();
        assertEquals(1, l.length);
        assertEquals(project.resolveFile("build.xml").getAbsolutePath(), l[0]);
    }
    public void testFileSet() {
        Path p = new Path(project);
        FileSet f = new FileSet();
        f.setProject(project);
        f.setDir(project.resolveFile("."));
        f.setIncludes("build.xml");
        p.addFileset(f);
        String[] l = p.list();
        assertEquals(1, l.length);
        assertEquals(project.resolveFile("build.xml").getAbsolutePath(), l[0]);
    }
    public void testDirSet() {
        Path p = new Path(project);
        DirSet d = new DirSet();
        d.setProject(project);
        d.setDir(project.resolveFile("."));
        d.setIncludes("build");
        p.addDirset(d);
        String[] l = p.list();
        assertEquals(1, l.length);
        assertEquals(project.resolveFile("build").getAbsolutePath(), l[0]);
    }
    public void testRecursion() {
        Path p = new Path(project);
        try {
            p.append(p);
            assertEquals(0, p.list().length);
        } catch (BuildException x) {
            String m = x.toString();
            assertTrue(m, m.indexOf("circular") != -1);
        }
    }
}
