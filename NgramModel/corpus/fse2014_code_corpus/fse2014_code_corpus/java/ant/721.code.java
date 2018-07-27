package org.apache.tools.ant.types.selectors;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
public abstract class BaseExtendSelector
        extends BaseSelector
        implements ExtendFileSelector {
    protected Parameter[] parameters = null;
    public BaseExtendSelector() {
    }
    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }
    protected Parameter[] getParameters() {
        return parameters;
    }
    public abstract boolean isSelected(File basedir, String filename,
                                       File file)
            throws BuildException;
}
