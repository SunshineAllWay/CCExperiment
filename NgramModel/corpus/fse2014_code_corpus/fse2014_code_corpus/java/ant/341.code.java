package org.apache.tools.ant.taskdefs.cvslib;
class RCSFile {
    private String name;
    private String revision;
    private String previousRevision;
    RCSFile(final String name, final String rev) {
        this(name, rev, null);
    }
    RCSFile(final String name,
                  final String revision,
                  final String previousRevision) {
        this.name = name;
        this.revision = revision;
        if (!revision.equals(previousRevision)) {
            this.previousRevision = previousRevision;
        }
    }
    String getName() {
        return name;
    }
    String getRevision() {
        return revision;
    }
    String getPreviousRevision() {
        return previousRevision;
    }
}
