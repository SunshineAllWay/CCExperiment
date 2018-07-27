package org.apache.tools.ant.taskdefs.optional.jsp.compilers;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.jsp.JspC;
import org.apache.tools.ant.taskdefs.optional.jsp.JspMangler;
public interface JspCompilerAdapter {
    void setJspc(JspC attributes);
    boolean execute() throws BuildException;
    JspMangler createMangler();
    boolean implementsOwnDependencyChecking();
}
