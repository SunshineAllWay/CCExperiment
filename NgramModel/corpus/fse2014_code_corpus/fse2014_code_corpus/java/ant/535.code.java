package org.apache.tools.ant.taskdefs.optional.pvcs;
public class PvcsProject {
    private String name;
    public PvcsProject() {
        super();
    }
    public void setName(String name) {
        PvcsProject.this.name = name;
    }
    public String getName() {
        return name;
    }
}
