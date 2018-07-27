package org.apache.tools.ant.taskdefs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelperRepository;
import org.apache.tools.ant.Task;
public class ProjectHelperTask extends Task {
    private List projectHelpers = new ArrayList();
    public synchronized void addConfigured(ProjectHelper projectHelper) {
        this.projectHelpers.add(projectHelper);
    }
    public void execute() throws BuildException {
        ProjectHelperRepository repo = ProjectHelperRepository.getInstance();
        for (Iterator it = projectHelpers.iterator(); it.hasNext();) {
            ProjectHelper helper = (ProjectHelper) it.next();
            repo.registerProjectHelper(helper.getClass());
        }
    }
}
