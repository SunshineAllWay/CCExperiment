package org.apache.tools.ant.taskdefs.optional.ssh;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.JSch;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
public abstract class SSHBase extends Task implements LogListener {
    private static final int SSH_PORT = 22;
    private String host;
    private String knownHosts;
    private int port = SSH_PORT;
    private boolean failOnError = true;
    private boolean verbose;
    private SSHUserInfo userInfo;
    public SSHBase() {
        super();
        userInfo = new SSHUserInfo();
    }
    public void setHost(String host) {
        this.host = host;
    }
    public String getHost() {
        return host;
    }
    public void setFailonerror(boolean failure) {
        failOnError = failure;
    }
    public boolean getFailonerror() {
        return failOnError;
    }
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
    public boolean getVerbose() {
        return verbose;
    }
    public void setUsername(String username) {
        userInfo.setName(username);
    }
    public void setPassword(String password) {
        userInfo.setPassword(password);
    }
    public void setKeyfile(String keyfile) {
        userInfo.setKeyfile(keyfile);
    }
    public void setPassphrase(String passphrase) {
        userInfo.setPassphrase(passphrase);
    }
    public void setKnownhosts(String knownHosts) {
        this.knownHosts = knownHosts;
    }
    public void setTrust(boolean yesOrNo) {
        userInfo.setTrust(yesOrNo);
    }
    public void setPort(int port) {
        this.port = port;
    }
    public int getPort() {
        return port;
    }
    public void init() throws BuildException {
        super.init();
        this.knownHosts = System.getProperty("user.home") + "/.ssh/known_hosts";
        this.port = SSH_PORT;
    }
    protected Session openSession() throws JSchException {
        JSch jsch = new JSch();
        final SSHBase base = this;
        if(verbose) {
        	JSch.setLogger(new com.jcraft.jsch.Logger(){
        		public boolean isEnabled(int level){
        			return true;
        		}
        		public void log(int level, String message){
        			base.log(message, Project.MSG_INFO);
        		}
        	});
        }
        if (null != userInfo.getKeyfile()) {
            jsch.addIdentity(userInfo.getKeyfile());
        }
        if (!userInfo.getTrust() && knownHosts != null) {
            log("Using known hosts: " + knownHosts, Project.MSG_DEBUG);
            jsch.setKnownHosts(knownHosts);
        }
        Session session = jsch.getSession(userInfo.getName(), host, port);
        session.setUserInfo(userInfo);
        log("Connecting to " + host + ":" + port);
        session.connect();
        return session;
    }
    protected SSHUserInfo getUserInfo() {
        return userInfo;
    }
}
