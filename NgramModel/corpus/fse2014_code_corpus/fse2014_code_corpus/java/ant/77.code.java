package org.apache.tools.ant;
public abstract class ProjectComponent implements Cloneable {
    protected Project project;
    protected Location location = Location.UNKNOWN_LOCATION;
    protected String description;
    public ProjectComponent() {
    }
    public void setProject(Project project) {
        this.project = project;
    }
    public Project getProject() {
        return project;
    }
    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }
    public void setDescription(String desc) {
        description = desc;
    }
    public String getDescription() {
        return description;
    }
    public void log(String msg) {
        log(msg, Project.MSG_INFO);
    }
    public void log(String msg, int msgLevel) {
        if (getProject() != null) {
            getProject().log(msg, msgLevel);
        } else {
            if (msgLevel <= Project.MSG_INFO) {
                System.err.println(msg);
            }
        }
    }
    public Object clone() throws CloneNotSupportedException {
        ProjectComponent pc = (ProjectComponent) super.clone();
        pc.setLocation(getLocation());
        pc.setProject(getProject());
        return pc;
    }
}
