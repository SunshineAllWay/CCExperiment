package org.apache.tools.ant;
import java.io.File;
import junit.framework.TestCase;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
public class AntClassLoaderPerformance extends TestCase {
    public void testFindClass() throws Exception {
        String testCaseURL = getClass()
            .getClassLoader().getResource("junit/framework/TestCase.class")
            .toExternalForm();
        int pling = testCaseURL.indexOf('!');
        String jarName = testCaseURL.substring(4, pling);
        File f = new File(FileUtils.getFileUtils().fromURI(jarName));
        Path p = new Path(null);
        p.createPathElement().setLocation(f);
        AntClassLoader al = null;
        for (int i = 0; i < 1000; i++) {
            try {
                al = new AntClassLoader(null, null, p, false);
                al.findClass("junit.framework.TestCase");
            } finally {
                if (al != null) {
                    al.cleanup();
                }
            }
        }
    }
}
