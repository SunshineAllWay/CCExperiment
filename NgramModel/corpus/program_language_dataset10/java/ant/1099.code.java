package org.apache.tools.ant.types;
public class FileSetTest extends AbstractFileSetTest {
    public FileSetTest(String name) {
        super(name);
    }
    protected AbstractFileSet getInstance() {
        return new FileSet();
    }
}
