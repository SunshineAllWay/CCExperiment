package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.Resource;
public class ResourceExists extends ProjectComponent implements Condition {
    private Resource resource;
    public void add(Resource r) {
        if (resource != null) {
            throw new BuildException("only one resource can be tested");
        }
        resource = r;
    }
    protected void validate() throws BuildException {
        if (resource == null) {
            throw new BuildException("resource is required");
        }
    }
    public boolean eval() throws BuildException {
        validate();
        return resource.isExists();
    }
}
