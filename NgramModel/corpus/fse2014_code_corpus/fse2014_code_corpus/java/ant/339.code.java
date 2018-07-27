package org.apache.tools.ant.taskdefs.cvslib;
import org.apache.tools.ant.BuildException;
public class CvsUser {
    private String userID;
    private String displayName;
    public void setDisplayname(final String displayName) {
        this.displayName = displayName;
    }
    public void setUserid(final String userID) {
        this.userID = userID;
    }
    public String getUserID() {
        return userID;
    }
    public String getDisplayname() {
        return displayName;
    }
    public void validate() throws BuildException {
        if (null == userID) {
            final String message = "Username attribute must be set.";
            throw new BuildException(message);
        }
        if (null == displayName) {
            final String message =
                "Displayname attribute must be set for userID " + userID;
            throw new BuildException(message);
        }
    }
}
