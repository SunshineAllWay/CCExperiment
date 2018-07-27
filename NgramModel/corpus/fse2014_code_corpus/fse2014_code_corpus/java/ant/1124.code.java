package org.apache.tools.ant.types.selectors;
import java.io.File;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
public abstract class BaseSelectorTest extends TestCase {
    private Project project;
    private TaskdefForMakingBed tbed = null;
    protected String basedirname = "src/etc/testcases/types";
    protected String beddirname = basedirname + "/selectortest";
    protected String mirrordirname = basedirname + "/selectortest2";
    protected File basedir = new File(System.getProperty("root"), basedirname);
    protected File beddir = new File(System.getProperty("root"), beddirname);
    protected File mirrordir = new File(System.getProperty("root"), mirrordirname);
    protected String[] filenames = {".","asf-logo.gif.md5","asf-logo.gif.bz2",
            "asf-logo.gif.gz","copy.filterset.filtered","zip/asf-logo.gif.zip",
            "tar/asf-logo.gif.tar","tar/asf-logo-huge.tar.gz",
            "tar/gz/asf-logo.gif.tar.gz","tar/bz2/asf-logo.gif.tar.bz2",
            "tar/bz2/asf-logo-huge.tar.bz2","tar/bz2"};
    protected File[] files = new File[filenames.length];
    protected File[] mirrorfiles = new File[filenames.length];
    public BaseSelectorTest(String name) {
        super(name);
    }
    public void setUp() {
        project = new Project();
        project.init();
        project.setBaseDir(basedir);
        for (int x = 0; x < files.length; x++) {
            files[x] = new File(beddir,filenames[x]);
            mirrorfiles[x] = new File(mirrordir,filenames[x]);
        }
    }
    public abstract BaseSelector getInstance();
    public BaseSelector getSelector() {
        BaseSelector selector = getInstance();
        selector.setProject( getProject() );
        return selector;
    }
    public Project getProject() {
        return project;
    }
    public void testRespondsToError() {
        BaseSelector s = getInstance();
        if (s == null) {
            return;
        }
        s.setError("test error");
        try {
            s.isSelected(beddir,filenames[0],files[0]);
            fail("Cannot cause BuildException when setError() is called");
        } catch (BuildException be) {
            assertEquals("test error",
                         be.getMessage());
        }
    }
    public String selectionString(FileSelector selector) {
        return selectionString(beddir,files,selector);
    }
    public String mirrorSelectionString(FileSelector selector) {
        return selectionString(mirrordir,mirrorfiles,selector);
    }
    public String selectionString(File basedir, File[] files, FileSelector selector) {
        StringBuffer buf = new StringBuffer();
        for (int x = 0; x < files.length; x++) {
            if (selector.isSelected(basedir,filenames[x],files[x])) {
                buf.append('T');
            }
            else {
                buf.append('F');
            }
        }
        return buf.toString();
    }
    public void performTests(FileSelector selector, String expected) {
        String result = selectionString(selector);
        String diff = diff(expected, result);
        String resolved = resolve(diff);
        assertEquals("Differing files: " + resolved, result, expected);
    }
    public String diff(String expected, String result) {
        int length1 = expected.length();
        int length2 = result.length();
        int min = (length1 > length2) ? length2 : length1;
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<min; i++) {
            sb.append(
                  (expected.charAt(i) == result.charAt(i))
                ? "-"
                : "X"
            );
        }
        return sb.toString();
    }
    public String resolve(String filelist) {
        StringBuffer sb = new StringBuffer();
        int min = (filenames.length > filelist.length())
                ? filelist.length()
                : filenames.length;
        for (int i=0; i<min; i++) {
            if ('X'==filelist.charAt(i)) {
                sb.append(filenames[i]);
                sb.append(";");
            }
        }
        return sb.toString();
    }
    protected void makeBed() {
        tbed = new TaskdefForMakingBed("setupfiles");
        tbed.setUp();
        tbed.makeTestbed();
    }
    protected void cleanupBed() {
        if (tbed != null) {
            tbed.tearDown();
            tbed = null;
        }
    }
    protected void makeMirror() {
        tbed = new TaskdefForMakingBed("mirrorfiles");
        tbed.setUp();
        tbed.makeMirror();
    }
    protected void cleanupMirror() {
        if (tbed != null) {
            tbed.deleteMirror();
            tbed = null;
        }
    }
    private class TaskdefForMakingBed extends BuildFileTest {
        TaskdefForMakingBed(String name) {
            super(name);
        }
        public void setUp() {
            configureProject("src/etc/testcases/types/selectors.xml");
        }
        public void tearDown() {
            executeTarget("cleanup");
        }
        public void makeTestbed() {
            executeTarget("setupfiles");
        }
        public void makeMirror() {
            executeTarget("mirrorfiles");
        }
        public void deleteMirror() {
            executeTarget("cleanup.mirrorfiles");
        }
    }
}
