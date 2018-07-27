package org.apache.tools.ant.taskdefs.compilers;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Apt;
import org.apache.tools.ant.types.Commandline;
public class AptExternalCompilerAdapter extends DefaultCompilerAdapter {
    protected Apt getApt() {
        return (Apt) getJavac();
    }
    public boolean execute() throws BuildException {
        attributes.log("Using external apt compiler", Project.MSG_VERBOSE);
        Apt apt = getApt();
        Commandline cmd = new Commandline();
        cmd.setExecutable(apt.getAptExecutable());
        setupModernJavacCommandlineSwitches(cmd);
        AptCompilerAdapter.setAptCommandlineSwitches(apt, cmd);
        int firstFileName = cmd.size();
        logAndAddFilesToCompile(cmd);
        return 0 == executeExternalCompile(cmd.getCommandline(),
                firstFileName,
                true);
    }
}
