package org.apache.tools.ant.taskdefs.optional.perforce;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
public interface P4Handler extends ExecuteStreamHandler {
    void process(String line) throws BuildException;
    void setOutput(String line) throws BuildException;
}
