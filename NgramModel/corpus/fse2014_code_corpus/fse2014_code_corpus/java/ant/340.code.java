package org.apache.tools.ant.taskdefs.cvslib;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.AbstractCvsTask;
import java.io.ByteArrayOutputStream;
import java.util.StringTokenizer;
public class CvsVersion extends AbstractCvsTask {
    static final long VERSION_1_11_2 = 11102;
    static final long MULTIPLY = 100;
    private String clientVersion;
    private String serverVersion;
    private String clientVersionProperty;
    private String serverVersionProperty;
    public String getClientVersion() {
        return clientVersion;
    }
    public String getServerVersion() {
        return serverVersion;
    }
    public void setClientVersionProperty(String clientVersionProperty) {
        this.clientVersionProperty = clientVersionProperty;
    }
    public void setServerVersionProperty(String serverVersionProperty) {
        this.serverVersionProperty = serverVersionProperty;
    }
    public boolean supportsCvsLogWithSOption() {
        if (serverVersion == null) {
            return false;
        }
        StringTokenizer tokenizer = new StringTokenizer(serverVersion, ".");
        long counter = MULTIPLY * MULTIPLY;
        long version = 0;
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            int i = 0;
            for (i = 0; i < s.length(); i++) {
                if (!Character.isDigit(s.charAt(i))) {
                    break;
                }
            }
            String s2 = s.substring(0, i);
            version = version + counter * Long.parseLong(s2);
            if (counter == 1) {
                break;
            }
            counter = counter / MULTIPLY;
        }
        return (version >= VERSION_1_11_2);
    }
    public void execute() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        this.setOutputStream(bos);
        ByteArrayOutputStream berr = new ByteArrayOutputStream();
        this.setErrorStream(berr);
        setCommand("version");
        super.execute();
        String output = bos.toString();
        log("Received version response \"" + output + "\"",
            Project.MSG_DEBUG);
        StringTokenizer st = new StringTokenizer(output);
        boolean client = false;
        boolean server = false;
        String cvs = null;
        String cachedVersion = null;
        boolean haveReadAhead = false;
        while (haveReadAhead || st.hasMoreTokens()) {
            String currentToken = haveReadAhead ? cachedVersion : st.nextToken();
            haveReadAhead = false;
            if (currentToken.equals("Client:")) {
                client = true;
            } else if (currentToken.equals("Server:")) {
                server = true;
            } else if (currentToken.startsWith("(CVS")
                       && currentToken.endsWith(")")) {
                cvs = currentToken.length() == 5 ? "" : " " + currentToken;
            }
            if (!client && !server && cvs != null
                && cachedVersion == null && st.hasMoreTokens()) {
                cachedVersion = st.nextToken();
                haveReadAhead = true;
            } else if (client && cvs != null) {
                if (st.hasMoreTokens()) {
                    clientVersion = st.nextToken() + cvs;
                }
                client = false;
                cvs = null;
            } else if (server && cvs != null) {
                if (st.hasMoreTokens()) {
                    serverVersion = st.nextToken() + cvs;
                }
                server = false;
                cvs = null;
            } else if (currentToken.equals("(client/server)")
                       && cvs != null && cachedVersion != null
                       && !client && !server) {
                client = server = true;
                clientVersion = serverVersion = cachedVersion + cvs;
                cachedVersion = cvs = null;
            }
        }
        if (clientVersionProperty != null) {
            getProject().setNewProperty(clientVersionProperty, clientVersion);
        }
        if (serverVersionProperty != null) {
            getProject().setNewProperty(serverVersionProperty, serverVersion);
        }
    }
}
