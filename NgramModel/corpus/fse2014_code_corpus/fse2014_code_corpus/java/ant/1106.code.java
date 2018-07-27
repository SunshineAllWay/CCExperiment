package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class PolyTest extends BuildFileTest {
    public PolyTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/poly.xml");
    }
    public void testFileSet() {
        expectLogContaining("fileset", "types.FileSet");
    }
    public void testFileSetAntType() {
        expectLogContaining("fileset-ant-type", "types.PolyTest$MyFileSet");
    }
    public void testPath() {
        expectLogContaining("path", "types.Path");
    }
    public void testPathAntType() {
        expectLogContaining("path-ant-type", "types.PolyTest$MyPath");
    }
    public static class MyFileSet extends FileSet {}
    public static class MyPath extends Path {
        public MyPath(Project project) {
            super(project);
        }
    }
    public static class MyTask extends Task {
        public void addPath(Path path) {
            log("class of path is " + path.getClass());
        }
        public void addFileset(FileSet fileset) {
            log("class of fileset is " + fileset.getClass());
        }
    }
}
