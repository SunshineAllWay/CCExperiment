package org.apache.tools.ant.taskdefs;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
public class Jikes {
    private static final int MAX_FILES_ON_COMMAND_LINE = 250;
    protected JikesOutputParser jop;
    protected String command;
    protected Project project;
    protected Jikes(JikesOutputParser jop, String command, Project project) {
        super();
        System.err.println("As of Ant 1.2 released in October 2000, "
            + "the Jikes class");
        System.err.println("is considered to be dead code by the Ant "
            + "developers and is unmaintained.");
        System.err.println("Don\'t use it!");
        this.jop = jop;
        this.command = command;
        this.project = project;
    }
    protected void compile(String[] args) {
        String[] commandArray = null;
        File tmpFile = null;
        try {
            String myos = System.getProperty("os.name");
            if (myos.toLowerCase(Locale.ENGLISH).indexOf("windows") >= 0
                && args.length > MAX_FILES_ON_COMMAND_LINE) {
                BufferedWriter out = null;
                try {
                    tmpFile = FileUtils.getFileUtils().createTempFile("jikes",
                            "tmp", null, false, true);
                    out = new BufferedWriter(new FileWriter(tmpFile));
                    for (int i = 0; i < args.length; i++) {
                        out.write(args[i]);
                        out.newLine();
                    }
                    out.flush();
                    commandArray = new String[] {command,
                                               "@" + tmpFile.getAbsolutePath()};
                } catch (IOException e) {
                    throw new BuildException("Error creating temporary file",
                                             e);
                } finally {
                    FileUtils.close(out);
                }
            } else {
                commandArray = new String[args.length + 1];
                commandArray[0] = command;
                System.arraycopy(args, 0, commandArray, 1, args.length);
            }
            try {
                Execute exe = new Execute(jop);
                exe.setAntRun(project);
                exe.setWorkingDirectory(project.getBaseDir());
                exe.setCommandline(commandArray);
                exe.execute();
            } catch (IOException e) {
                throw new BuildException("Error running Jikes compiler", e);
            }
        } finally {
            if (tmpFile != null) {
                if (!tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
            }
        }
    }
}
