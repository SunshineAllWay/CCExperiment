package org.apache.tools.ant.taskdefs.rmic;
import org.apache.tools.ant.types.Commandline;
public class XNewRmic extends ForkingSunRmic {
    public static final String COMPILER_NAME = "xnew";
    public XNewRmic() {
    }
    protected Commandline setupRmicCommand() {
        String[] options = new String[] {
                "-Xnew"
        };
        Commandline commandline = super.setupRmicCommand(options);
        return commandline;
    }
}
