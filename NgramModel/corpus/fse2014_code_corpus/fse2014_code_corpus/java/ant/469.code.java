package org.apache.tools.ant.taskdefs.optional.jsp.compilers;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.optional.jsp.JspC;
import org.apache.tools.ant.types.CommandlineJava;
public abstract class DefaultJspCompilerAdapter
    implements JspCompilerAdapter {
    private static String lSep = System.getProperty("line.separator");
    protected void logAndAddFilesToCompile(JspC jspc,
                                           Vector compileList,
                                           CommandlineJava cmd) {
        jspc.log("Compilation " + cmd.describeJavaCommand(),
                 Project.MSG_VERBOSE);
        StringBuffer niceSourceList = new StringBuffer("File");
        if (compileList.size() != 1) {
            niceSourceList.append("s");
        }
        niceSourceList.append(" to be compiled:");
        niceSourceList.append(lSep);
        Enumeration e = compileList.elements();
        while (e.hasMoreElements()) {
            String arg = (String) e.nextElement();
            cmd.createArgument().setValue(arg);
            niceSourceList.append("    ");
            niceSourceList.append(arg);
            niceSourceList.append(lSep);
        }
        jspc.log(niceSourceList.toString(), Project.MSG_VERBOSE);
    }
    protected JspC owner;
    public void setJspc(JspC owner) {
        this.owner = owner;
    }
    public JspC getJspc() {
        return owner;
    }
    protected void addArg(CommandlineJava cmd, String argument) {
        if (argument != null && argument.length() != 0) {
           cmd.createArgument().setValue(argument);
        }
    }
    protected void addArg(CommandlineJava cmd, String argument, String value) {
        if (value != null) {
            cmd.createArgument().setValue(argument);
            cmd.createArgument().setValue(value);
        }
    }
    protected void addArg(CommandlineJava cmd, String argument, File file) {
        if (file != null) {
            cmd.createArgument().setValue(argument);
            cmd.createArgument().setFile(file);
        }
    }
    public boolean implementsOwnDependencyChecking() {
        return false;
    }
    public Project getProject() {
        return getJspc().getProject();
    }
}
