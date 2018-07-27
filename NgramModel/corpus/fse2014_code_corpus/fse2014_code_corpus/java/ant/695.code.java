package org.apache.tools.ant.types.resources.comparators;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.ResourceUtils;
public class Content extends ResourceComparator {
    private boolean binary = true;
    public void setBinary(boolean b) {
        binary = b;
    }
    public boolean isBinary() {
        return binary;
    }
    protected int resourceCompare(Resource foo, Resource bar) {
        try {
            return ResourceUtils.compareContent(foo, bar, !binary);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
