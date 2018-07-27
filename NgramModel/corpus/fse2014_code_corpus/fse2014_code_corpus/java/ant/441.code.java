package org.apache.tools.ant.taskdefs.optional.extension.resolvers;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Ant;
import org.apache.tools.ant.taskdefs.optional.extension.Extension;
import org.apache.tools.ant.taskdefs.optional.extension.ExtensionResolver;
public class AntResolver implements ExtensionResolver {
    private File antfile;
    private File destfile;
    private String target;
    public void setAntfile(final File antfile) {
        this.antfile = antfile;
    }
    public void setDestfile(final File destfile) {
        this.destfile = destfile;
    }
    public void setTarget(final String target) {
        this.target = target;
    }
    public File resolve(final Extension extension,
                         final Project project) throws BuildException {
        validate();
        final Ant ant = new Ant();
        ant.setProject(project);
        ant.setInheritAll(false);
        ant.setAntfile(antfile.getName());
        try {
            final File dir =
                antfile.getParentFile().getCanonicalFile();
            ant.setDir(dir);
        } catch (final IOException ioe) {
            throw new BuildException(ioe.getMessage(), ioe);
        }
        if (null != target) {
            ant.setTarget(target);
        }
        ant.execute();
        return destfile;
    }
    private void validate() {
        if (null == antfile) {
            final String message = "Must specify Buildfile";
            throw new BuildException(message);
        }
        if (null == destfile) {
            final String message = "Must specify destination file";
            throw new BuildException(message);
        }
    }
    public String toString() {
        return "Ant[" + antfile + "==>" + destfile + "]";
    }
}
