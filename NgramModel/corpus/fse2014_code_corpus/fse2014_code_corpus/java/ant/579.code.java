package org.apache.tools.ant.taskdefs.rmic;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.taskdefs.Rmic;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.LogStreamHandler;
import org.apache.tools.ant.types.Commandline;
import java.io.IOException;
public class ForkingSunRmic extends DefaultRmicAdapter {
    public static final String COMPILER_NAME = "forking";
    public boolean execute() throws BuildException {
        Rmic owner = getRmic();
        Commandline cmd = setupRmicCommand();
        Project project = owner.getProject();
        String executable = owner.getExecutable();
        if (executable == null) {
            executable = JavaEnvUtils.getJdkExecutable(getExecutableName());
        }
        cmd.setExecutable(executable);
        String[] args = cmd.getCommandline();
        try {
            Execute exe = new Execute(new LogStreamHandler(owner,
                    Project.MSG_INFO,
                    Project.MSG_WARN));
            exe.setAntRun(project);
            exe.setWorkingDirectory(project.getBaseDir());
            exe.setCommandline(args);
            exe.execute();
            return !exe.isFailure();
        } catch (IOException exception) {
            throw new BuildException("Error running " + getExecutableName()
                    + " -maybe it is not on the path", exception);
        }
    }
    protected String getExecutableName() {
        return SunRmic.RMIC_EXECUTABLE;
    }
}
