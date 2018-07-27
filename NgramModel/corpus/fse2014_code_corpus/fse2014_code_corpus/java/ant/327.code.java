package org.apache.tools.ant.taskdefs.condition;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
public class ResourceContains implements Condition {
    private Project project;
    private String substring;
    private Resource resource;
    private String refid;
    private boolean casesensitive = true;
    public void setProject(Project project) {
        this.project = project;
    }
    public Project getProject() {
        return project;
    }
    public void setResource(String r) {
        this.resource = new FileResource(new File(r));
    }
    public void setRefid(String refid) {
        this.refid = refid;
    }
    private void resolveRefid() {
        try {
            if (getProject() == null) {
                throw new BuildException("Cannot retrieve refid; project unset");
            }
            Object o = getProject().getReference(refid);
            if (!(o instanceof Resource)) {
                if (o instanceof ResourceCollection) {
                    ResourceCollection rc = (ResourceCollection) o;
                    if (rc.size() == 1) {
                        o = rc.iterator().next();
                    }
                } else {
                    throw new BuildException(
                        "Illegal value at '" + refid + "': " + String.valueOf(o));
                }
            }
            this.resource = (Resource) o;
        } finally {
            refid = null;
        }
    }
    public void setSubstring(String substring) {
        this.substring = substring;
    }
    public void setCasesensitive(boolean casesensitive) {
        this.casesensitive = casesensitive;
    }
    private void validate() {
        if (resource != null && refid != null) {
            throw new BuildException("Cannot set both resource and refid");
        }
        if (resource == null && refid != null) {
            resolveRefid();
        }
        if (resource == null || substring == null) {
            throw new BuildException("both resource and substring are required "
                                     + "in <resourcecontains>");
        }
    }
    public synchronized boolean eval() throws BuildException {
        validate();
        if (substring.length() == 0) {
            if (getProject() != null) {
                getProject().log("Substring is empty; returning true",
                                 Project.MSG_VERBOSE);
            }
            return true;
        }
        if (resource.getSize() == 0) {
            return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String contents = FileUtils.safeReadFully(reader);
            String sub = substring;
            if (!casesensitive) {
                contents = contents.toLowerCase();
                sub = sub.toLowerCase();
            }
            return contents.indexOf(sub) >= 0;
        } catch (IOException e) {
            throw new BuildException("There was a problem accessing resource : " + resource);
        } finally {
            FileUtils.close(reader);
        }
    }
}
