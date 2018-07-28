package org.apache.tools.ant;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import junit.framework.TestCase;
import org.apache.tools.ant.util.FileUtils;
public class PropertyFileCLITest extends TestCase {
    public void testPropertyResolution() throws Exception {
        FileUtils fu = FileUtils.getFileUtils();
        File props = fu.createTempFile("propertyfilecli", ".properties",
                                       null, true, true);
        File build = fu.createTempFile("propertyfilecli", ".xml", null, true,
                                       true);
        File log = fu.createTempFile("propertyfilecli", ".log", null, true,
                                     true);
        FileWriter fw = null;
        FileReader fr = null;
        try {
            fw = new FileWriter(props);
            fw.write("w=world\nmessage=Hello, ${w}\n");
            fw.close();
            fw = new FileWriter(build);
            fw.write("<project><echo>${message}</echo></project>");
            fw.close();
            fw = null;
            Main m = new NoExitMain();
            m.startAnt(new String[] {
                    "-propertyfile", props.getAbsolutePath(),
                    "-f", build.getAbsolutePath(),
                    "-l", log.getAbsolutePath()
                }, null, null);
            String l = FileUtils.safeReadFully(fr = new FileReader(log));
            assertTrue("expected log to contain 'Hello, world' but was " + l,
                       l.indexOf("Hello, world") > -1);
        } finally {
            FileUtils.close(fw);
            FileUtils.close(fr);
        }
    }
    private static class NoExitMain extends Main {
        protected void exit(int exitCode) {
        }
    }
}
