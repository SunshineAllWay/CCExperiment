package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
public abstract class BaseSelector extends DataType implements FileSelector {
    private String errmsg = null;
    public BaseSelector() {
    }
    public void setError(String msg) {
        if (errmsg == null) {
            errmsg = msg;
        }
    }
    public String getError() {
        return errmsg;
    }
    public void verifySettings() {
        if (isReference()) {
            ((BaseSelector) getCheckedRef()).verifySettings();
        }
    }
    public void validate() {
        if (getError() == null) {
            verifySettings();
        }
        if (getError() != null) {
            throw new BuildException(errmsg);
        }
        if (!isReference()) {
            dieOnCircularReference();
        }
    }
    public abstract boolean isSelected(File basedir, String filename,
                                       File file);
}
