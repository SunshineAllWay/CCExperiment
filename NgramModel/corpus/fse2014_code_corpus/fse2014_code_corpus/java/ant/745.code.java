package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.taskdefs.condition.IsSigned;
public class SignedSelector extends DataType implements FileSelector {
    private IsSigned isSigned = new IsSigned();
    public void setName(String name) {
        isSigned.setName(name);
    }
    public boolean isSelected(File basedir, String filename, File file) {
        if (file.isDirectory()) {
            return false; 
        }
        isSigned.setProject(getProject());
        isSigned.setFile(file);
        return isSigned.eval();
    }
}
