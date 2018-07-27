package org.apache.tools.ant;
public interface Executor {
    void executeTargets(Project project, String[] targetNames)
        throws BuildException;
    Executor getSubProjectExecutor();
}
