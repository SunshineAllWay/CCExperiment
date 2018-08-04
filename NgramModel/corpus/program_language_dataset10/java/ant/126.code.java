package org.apache.tools.ant.helper;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Executor;
import org.apache.tools.ant.BuildException;
public class SingleCheckExecutor implements Executor {
    public void executeTargets(Project project, String[] targetNames)
        throws BuildException {
            project.executeSortedTargets(
                project.topoSort(targetNames, project.getTargets(), false));
    }
    public Executor getSubProjectExecutor() {
        return this;
    }
}