package org.apache.tools.ant.taskdefs.optional.ssh;
import com.jcraft.jsch.UserInfo;
import com.jcraft.jsch.UIKeyboardInteractive;
public class SSHUserInfo implements UserInfo, UIKeyboardInteractive {
    private String name;
    private String password = null;
    private String keyfile;
    private String passphrase = null;
    private boolean trustAllCertificates;
    public SSHUserInfo() {
        super();
        this.trustAllCertificates = false;
    }
    public SSHUserInfo(String password, boolean trustAllCertificates) {
        super();
        this.password = password;
        this.trustAllCertificates = trustAllCertificates;
    }
    public String getName() {
        return name;
    }
    public String getPassphrase(String message) {
        return passphrase;
    }
    public String getPassword() {
        return password;
    }
    public boolean prompt(String str) {
        return false;
    }
    public boolean retry() {
        return false;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setTrust(boolean trust) {
        this.trustAllCertificates = trust;
    }
    public boolean getTrust() {
        return this.trustAllCertificates;
    }
    public String getPassphrase() {
        return passphrase;
    }
    public String getKeyfile() {
        return keyfile;
    }
    public void setKeyfile(String keyfile) {
        this.keyfile = keyfile;
    }
    public boolean promptPassphrase(String message) {
        return true;
    }
    public boolean promptPassword(String passwordPrompt) {
        return true;
    }
    public boolean promptYesNo(String message) {
        return trustAllCertificates;
    }
    public void showMessage(String message) {
    }
    public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo) {
        if (prompt.length != 1 || echo[0] || this.password == null) {
            return null;
        }
        String[] response = new String[1];
        response[0] = this.password;
        return response;
    }
}
