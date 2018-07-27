package org.apache.tools.ant.taskdefs.optional.ssh;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.KeepAliveOutputStream;
import org.apache.tools.ant.util.TeeOutputStream;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
public class SSHExec extends SSHBase {
    private static final int BUFFER_SIZE = 8192;
    private static final int RETRY_INTERVAL = 500;
    private String command = null;
    private long maxwait = 0;
    private Thread thread = null;
    private String outputProperty = null;   
    private File outputFile = null;   
    private String inputProperty = null;   
    private File inputFile = null;   
    private boolean append = false;   
    private Resource commandResource = null;
    private static final String TIMEOUT_MESSAGE =
        "Timeout period exceeded, connection dropped.";
    public SSHExec() {
        super();
    }
    public void setCommand(String command) {
        this.command = command;
    }
    public void setCommandResource(String f) {
        this.commandResource = new FileResource(new File(f));
    }
    public void setTimeout(long timeout) {
        maxwait = timeout;
    }
    public void setOutput(File output) {
        outputFile = output;
    }
    public void setInput(File input) {
        inputFile = input;
    }
    public void setInputProperty(String inputProperty) {
    	this.inputProperty = inputProperty;
    }
    public void setAppend(boolean append) {
        this.append = append;
    }
    public void setOutputproperty(String property) {
        outputProperty = property;
    }
    public void execute() throws BuildException {
        if (getHost() == null) {
            throw new BuildException("Host is required.");
        }
        if (getUserInfo().getName() == null) {
            throw new BuildException("Username is required.");
        }
        if (getUserInfo().getKeyfile() == null
            && getUserInfo().getPassword() == null) {
            throw new BuildException("Password or Keyfile is required.");
        }
        if (command == null && commandResource == null) {
            throw new BuildException("Command or commandResource is required.");
        }
        if (inputFile != null && inputProperty != null) {
            throw new BuildException("You can't specify both inputFile and"
                                     + " inputProperty.");
        }
        if (inputFile != null && !inputFile.exists()) {
            throw new BuildException("The input file "
                                     + inputFile.getAbsolutePath()
                                     + " does not exist.");
        }
        Session session = null;
        StringBuffer output = new StringBuffer();
        try {
            session = openSession();
            if (command != null) {
                log("cmd : " + command, Project.MSG_INFO);
                executeCommand(session, command, output);
            } else { 
                try {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(commandResource.getInputStream()));
                    String cmd;
                    while ((cmd = br.readLine()) != null) {
                        log("cmd : " + cmd, Project.MSG_INFO);
                        output.append(cmd).append(" : ");
                        executeCommand(session, cmd, output);
                        output.append("\n");
                    }
                    FileUtils.close(br);
                } catch (IOException e) {
                    if (getFailonerror()) {
                        throw new BuildException(e);
                    } else {
                        log("Caught exception: " + e.getMessage(),
                            Project.MSG_ERR);
                    }
                }
            }
        } catch (JSchException e) {
            if (getFailonerror()) {
                throw new BuildException(e);
            } else {
                log("Caught exception: " + e.getMessage(), Project.MSG_ERR);
            }
        } finally {
            if (outputProperty != null) {
                getProject().setNewProperty(outputProperty, output.toString());
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }
    private void executeCommand(Session session, String cmd, StringBuffer sb)
        throws BuildException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        TeeOutputStream tee =
            new TeeOutputStream(out,
                                KeepAliveOutputStream.wrapSystemOut());
        InputStream istream = null ;
        if (inputFile != null) {
            try {
                istream = new FileInputStream(inputFile) ;
            } catch (IOException e) {
                log("Failed to read " + inputFile + " because of: "
                    + e.getMessage(), Project.MSG_WARN);
            }
        }
        if (inputProperty != null) {
            String inputData = getProject().getProperty(inputProperty) ;
            if (inputData != null) {
                istream = new ByteArrayInputStream(inputData.getBytes()) ;
            }        	
        }
        try {
            final ChannelExec channel;
            session.setTimeout((int) maxwait);
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(cmd);
            channel.setOutputStream(tee);
            channel.setExtOutputStream(tee);
            if (istream != null) {
                channel.setInputStream(istream);
            }
            channel.connect();
            thread =
                new Thread() {
                    public void run() {
                        while (!channel.isClosed()) {
                            if (thread == null) {
                                return;
                            }
                            try {
                                sleep(RETRY_INTERVAL);
                            } catch (Exception e) {
                            }
                        }
                    }
                };
            thread.start();
            thread.join(maxwait);
            if (thread.isAlive()) {
                thread = null;
                if (getFailonerror()) {
                    throw new BuildException(TIMEOUT_MESSAGE);
                } else {
                    log(TIMEOUT_MESSAGE, Project.MSG_ERR);
                }
            } else {
                if (outputFile != null) {
                    writeToFile(out.toString(), append, outputFile);
                }
                int ec = channel.getExitStatus();
                if (ec != 0) {
                    String msg = "Remote command failed with exit status " + ec;
                    if (getFailonerror()) {
                        throw new BuildException(msg);
                    } else {
                        log(msg, Project.MSG_ERR);
                    }
                }
            }
        } catch (BuildException e) {
            throw e;
        } catch (JSchException e) {
            if (e.getMessage().indexOf("session is down") >= 0) {
                if (getFailonerror()) {
                    throw new BuildException(TIMEOUT_MESSAGE, e);
                } else {
                    log(TIMEOUT_MESSAGE, Project.MSG_ERR);
                }
            } else {
                if (getFailonerror()) {
                    throw new BuildException(e);
                } else {
                    log("Caught exception: " + e.getMessage(),
                        Project.MSG_ERR);
                }
            }
        } catch (Exception e) {
            if (getFailonerror()) {
                throw new BuildException(e);
            } else {
                log("Caught exception: " + e.getMessage(), Project.MSG_ERR);
            }
        } finally {
            sb.append(out.toString());
            FileUtils.close(istream);
        }
    }
    private void writeToFile(String from, boolean append, File to)
        throws IOException {
        FileWriter out = null;
        try {
            out = new FileWriter(to.getAbsolutePath(), append);
            StringReader in = new StringReader(from);
            char[] buffer = new char[BUFFER_SIZE];
            int bytesRead;
            while (true) {
                bytesRead = in.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                out.write(buffer, 0, bytesRead);
            }
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
}