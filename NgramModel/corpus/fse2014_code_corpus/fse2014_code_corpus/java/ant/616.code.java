package org.apache.tools.ant.types;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public class Reference {
    private String refid;
    private Project project;
    public Reference() {
    }
    public Reference(String id) {
        setRefId(id);
    }
    public Reference(Project p, String id) {
        setRefId(id);
        setProject(p);
    }
    public void setRefId(String id) {
        refid = id;
    }
    public String getRefId() {
        return refid;
    }
    public void setProject(Project p) {
        this.project = p;
    }
    public Project getProject() {
        return project;
    }
    public Object getReferencedObject(Project fallback) throws BuildException {
        if (refid == null) {
            throw new BuildException("No reference specified");
        }
        Object o = project == null ? fallback.getReference(refid) : project.getReference(refid);
        if (o == null) {
            throw new BuildException("Reference " + refid + " not found.");
        }
        return o;
    }
    public Object getReferencedObject() throws BuildException {
        if (project == null) {
            throw new BuildException("No project set on reference to " + refid);
        }
        return getReferencedObject(project);
    }
}
