package org.apache.tools.ant.taskdefs.condition;
import java.io.IOException;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
public class Socket extends ProjectComponent implements Condition {
    private String server = null;
    private int port = 0;
    public void setServer(String server) {
        this.server = server;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public boolean eval() throws BuildException {
        if (server == null) {
            throw new BuildException("No server specified in socket "
                                     + "condition");
        }
        if (port == 0) {
            throw new BuildException("No port specified in socket condition");
        }
        log("Checking for listener at " + server + ":" + port,
            Project.MSG_VERBOSE);
        java.net.Socket s = null;
        try {
            s = new java.net.Socket(server, port);
        } catch (IOException e) {
            return false;
        } finally {
          if (s != null) {
            try {
              s.close();
            } catch (IOException ioe) {
            }
          }
        }
        return true;
    }
}
