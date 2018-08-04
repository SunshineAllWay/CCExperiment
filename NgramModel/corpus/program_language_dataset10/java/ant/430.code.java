package org.apache.tools.ant.taskdefs.optional.extension;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public interface ExtensionResolver {
    File resolve(Extension extension, Project project)
        throws BuildException;
}