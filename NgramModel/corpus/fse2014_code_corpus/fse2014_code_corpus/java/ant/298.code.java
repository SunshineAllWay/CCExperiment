package org.apache.tools.ant.taskdefs.compilers;
import java.io.IOException;
import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.util.FileUtils;
public class JavacExternal extends DefaultCompilerAdapter {
    public boolean execute() throws BuildException {
        attributes.log("Using external javac compiler", Project.MSG_VERBOSE);
        Commandline cmd = new Commandline();
        cmd.setExecutable(getJavac().getJavacExecutable());
        if (!assumeJava11() && !assumeJava12()) {
            setupModernJavacCommandlineSwitches(cmd);
        } else {
            setupJavacCommandlineSwitches(cmd, true);
        }
        int firstFileName = assumeJava11() ? -1 : cmd.size();
        logAndAddFilesToCompile(cmd);
        if (Os.isFamily("openvms")) {
            return execOnVMS(cmd, firstFileName);
        }
        return
                executeExternalCompile(cmd.getCommandline(), firstFileName,
                        true)
                == 0;
    }
    private boolean execOnVMS(Commandline cmd, int firstFileName) {
        File vmsFile = null;
        try {
            vmsFile = JavaEnvUtils.createVmsJavaOptionFile(cmd.getArguments());
            String[] commandLine = {cmd.getExecutable(),
                                    "-V",
                                    vmsFile.getPath()};
            return 0 == executeExternalCompile(commandLine,
                            firstFileName,
                            true);
        } catch (IOException e) {
            throw new BuildException("Failed to create a temporary file for \"-V\" switch");
        } finally {
            FileUtils.delete(vmsFile);
        }
    }
}
