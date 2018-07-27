package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Parameter;
public class TypeSelector extends BaseExtendSelector {
    private String type = null;
    public static final String TYPE_KEY = "type";
    public TypeSelector() {
    }
    public String toString() {
        StringBuffer buf = new StringBuffer("{typeselector type: ");
        buf.append(type);
        buf.append("}");
        return buf.toString();
    }
    public void setType(FileType fileTypes) {
        this.type = fileTypes.getValue();
    }
    public void setParameters(Parameter[] parameters) {
        super.setParameters(parameters);
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                String paramname = parameters[i].getName();
                if (TYPE_KEY.equalsIgnoreCase(paramname)) {
                    FileType t = new FileType();
                    t.setValue(parameters[i].getValue());
                    setType(t);
                } else {
                    setError("Invalid parameter " + paramname);
                }
            }
        }
    }
    public void verifySettings() {
        if (type == null) {
            setError("The type attribute is required");
        }
    }
    public boolean isSelected(File basedir, String filename, File file) {
        validate();
        if (file.isDirectory()) {
            return type.equals(FileType.DIR);
        } else {
            return type.equals(FileType.FILE);
        }
    }
    public static class FileType extends EnumeratedAttribute {
        public static final String FILE = "file";
        public static final String DIR = "dir";
        public String[] getValues() {
            return new String[]{FILE, DIR};
        }
    }
}
