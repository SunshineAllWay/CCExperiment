package org.apache.tools.ant.taskdefs.compilers;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Javac;
public interface CompilerAdapter {
    void setJavac(Javac attributes);
    boolean execute() throws BuildException;
}
