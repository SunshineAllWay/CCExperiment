package org.apache.tools.ant.helper;
import java.util.Hashtable;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Executor;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
public class IgnoreDependenciesExecutor implements Executor {
    private static final SingleCheckExecutor SUB_EXECUTOR = new SingleCheckExecutor();
    public void executeTargets(Project project, String[] targetNames)
        throws BuildException {
        Hashtable targets = project.getTargets();
        BuildException thrownException = null;
        for (int i = 0; i < targetNames.length; i++) {
            try {
                Target t = (Target) targets.get(targetNames[i]);
                if (t == null) {
                  throw new BuildException("Unknown target " + targetNames[i]);
                }
                t.performTasks();
            } catch (BuildException ex) {
                if (project.isKeepGoingMode()) {
                    thrownException = ex;
                } else {
                    throw ex;
                }
            }
        }
        if (thrownException != null) {
            throw thrownException;
        }
    }
    public Executor getSubProjectExecutor() {
        return SUB_EXECUTOR;
    }
}
