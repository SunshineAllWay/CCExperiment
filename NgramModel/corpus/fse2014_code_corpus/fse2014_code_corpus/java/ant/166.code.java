package org.apache.tools.ant.taskdefs;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.ProjectHelper.OnMissingExtensionPoint;
import org.apache.tools.ant.Task;
public class BindTargets extends Task {
    private String extensionPoint;
    private Listtargets = new ArrayList();
    private OnMissingExtensionPoint onMissingExtensionPoint;
    public void setExtensionPoint(String extensionPoint) {
        this.extensionPoint = extensionPoint;
    }
    public void setOnMissingExtensionPoint(String onMissingExtensionPoint) {
        try {
            this.onMissingExtensionPoint = OnMissingExtensionPoint.valueOf(onMissingExtensionPoint);
        } catch (IllegalArgumentException e) {
            throw new BuildException("Invalid onMissingExtensionPoint: " + onMissingExtensionPoint);
        }
    }
    public void setOnMissingExtensionPoint(OnMissingExtensionPoint onMissingExtensionPoint) {
        this.onMissingExtensionPoint = onMissingExtensionPoint;
    }
    public void setTargets(String target) {
        String[] inputs = target.split(",");
        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i].trim();
            if (input.length() > 0) {
                targets.add(input);
            }
        }
    }
    public void execute() throws BuildException {
        if (extensionPoint == null) {
            throw new BuildException("extensionPoint required", getLocation());
        }
        if (getOwningTarget() == null
                || !"".equals(getOwningTarget().getName())) {
            throw new BuildException(
                    "bindtargets only allowed as a top-level task");
        }
        if (onMissingExtensionPoint == null) {
            onMissingExtensionPoint = OnMissingExtensionPoint.FAIL;
        }
        ProjectHelper helper = (ProjectHelper) getProject().getReference(
                ProjectHelper.PROJECTHELPER_REFERENCE);
        Iterator itTarget = targets.iterator();
        while (itTarget.hasNext()) {
            helper.getExtensionStack().add(
                    new String[] { extensionPoint, (String) itTarget.next(),
                                            onMissingExtensionPoint.name() });
        }
    }
}
