package org.apache.tools.ant.taskdefs.optional.extension.resolvers;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.extension.Extension;
import org.apache.tools.ant.taskdefs.optional.extension.ExtensionResolver;
public class LocationResolver implements ExtensionResolver {
    private String location;
    public void setLocation(final String location) {
        this.location = location;
    }
    public File resolve(final Extension extension,
                        final Project project) throws BuildException {
        if (null == location) {
            final String message = "No location specified for resolver";
            throw new BuildException(message);
        }
        return project.resolveFile(location);
    }
    public String toString() {
        return "Location[" + location + "]";
    }
}