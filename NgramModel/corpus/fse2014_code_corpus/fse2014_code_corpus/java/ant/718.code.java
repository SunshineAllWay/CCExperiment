package org.apache.tools.ant.types.resources.selectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.EnumeratedAttribute;
public class Type implements ResourceSelector {
    private static final String FILE_ATTR = "file";
    private static final String DIR_ATTR = "dir";
    private static final String ANY_ATTR = "any";
    public static final Type FILE = new Type(new FileDir(FILE_ATTR));
    public static final Type DIR = new Type(new FileDir(DIR_ATTR));
    public static final Type ANY = new Type(new FileDir(ANY_ATTR));
    public static class FileDir extends EnumeratedAttribute {
        private static final String[] VALUES = new String[] { FILE_ATTR, DIR_ATTR, ANY_ATTR };
        public FileDir() {
        }
        public FileDir(String value) {
            setValue(value);
        }
        public String[] getValues() {
            return VALUES;
        }
    }
    private FileDir type = null;
    public Type() {
    }
    public Type(FileDir fd) {
        setType(fd);
    }
    public void setType(FileDir fd) {
        type = fd;
    }
    public boolean isSelected(Resource r) {
        if (type == null) {
            throw new BuildException("The type attribute is required.");
        }
        int i = type.getIndex();
        return i == 2 || (r.isDirectory() ? i == 1 : i == 0);
    }
}
