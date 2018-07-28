package org.apache.tools.ant.taskdefs.optional.net;
import org.apache.commons.net.bsd.RExecClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class RExecTask extends Task {
    private static final int PAUSE_TIME = 250;
    private String userid  = null;
    private String password = null;
    private String command = null;
    private String server  = null;
    private int port = RExecClient.DEFAULT_PORT;
    private Vector rexecTasks = new Vector();
    private boolean addCarriageReturn = false;
    private Integer defaultTimeout = null;
    public class RExecSubTask {
        protected String taskString = "";
        public void execute(AntRExecClient rexec)
                throws BuildException {
            throw new BuildException("Shouldn't be able instantiate a SubTask directly");
        }
        public void addText(String s) {
            setString(getProject().replaceProperties(s));
        }
        public void setString(String s) {
           taskString += s;
        }
    }
    public class RExecWrite extends RExecSubTask {
        private boolean echoString = true;
        public void execute(AntRExecClient rexec)
               throws BuildException {
           rexec.sendString(taskString, echoString);
        }
        public void setEcho(boolean b) {
           echoString = b;
        }
    }
    public class RExecRead extends RExecSubTask {
        private Integer timeout = null;
        public void execute(AntRExecClient rexec)
               throws BuildException {
            rexec.waitForString(taskString, timeout);
        }
        public void setTimeout(Integer i) {
           this.timeout = i;
        }
        public void setDefaultTimeout(Integer defaultTimeout) {
           if (timeout == null) {
              timeout = defaultTimeout;
           }
        }
    }
    public class AntRExecClient extends RExecClient {
        public void waitForString(String s) {
            waitForString(s, null);
        }
        public void waitForString(String s, Integer timeout) {
            InputStream is = this.getInputStream();
            try {
                StringBuffer sb = new StringBuffer();
                int windowStart = -s.length();
                if (timeout == null || timeout.intValue() == 0) {
                    while (windowStart < 0
                           || !sb.substring(windowStart).equals(s)) {
                        sb.append((char) is.read());
                        windowStart++;
                    }
                } else {
                    Calendar endTime = Calendar.getInstance();
                    endTime.add(Calendar.SECOND, timeout.intValue());
                    while (windowStart < 0
                           || !sb.substring(windowStart).equals(s)) {
                        while (Calendar.getInstance().before(endTime)
                            && is.available() == 0) {
                            Thread.sleep(PAUSE_TIME);
                        }
                        if (is.available() == 0) {
                            throw new BuildException(
                                "Response timed-out waiting for \"" + s + '\"',
                                getLocation());
                        }
                        sb.append((char) is.read());
                        windowStart++;
                    }
                }
                log(sb.toString(), Project.MSG_INFO);
            } catch (BuildException be) {
                throw be;
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }
        public void sendString(String s, boolean echoString) {
            OutputStream os = this.getOutputStream();
            try {
                os.write((s + "\n").getBytes());
                if (echoString) {
                    log(s, Project.MSG_INFO);
                }
                os.flush();
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }
        public void waitForEOF(Integer timeout) {
            InputStream is = this.getInputStream();
            try {
                StringBuffer sb = new StringBuffer();
                if (timeout == null || timeout.intValue() == 0) {
                int read;
                    while ((read = is.read()) != -1) {
                        char c = (char) read;
                        sb.append(c);
                        if (c == '\n') {
                        log(sb.toString(), Project.MSG_INFO);
                        sb.delete(0, sb.length());
                        }
                    }
                } else {
                    Calendar endTime = Calendar.getInstance();
                    endTime.add(Calendar.SECOND, timeout.intValue());
                int read = 0;
                    while (read != -1) {
                        while (Calendar.getInstance().before(endTime) && is.available() == 0) {
                            Thread.sleep(PAUSE_TIME);
                        }
                        if (is.available() == 0) {
                        log(sb.toString(), Project.MSG_INFO);
                            throw new BuildException(
                                                     "Response timed-out waiting for EOF",
                                                     getLocation());
                        }
                        read =  is.read();
                        if (read != -1) {
                        char c = (char) read;
                        sb.append(c);
                        if (c == '\n') {
                                log(sb.toString(), Project.MSG_INFO);
                                sb.delete(0, sb.length());
                        }
                        }
                    }
                }
                if (sb.length() > 0) {
                log(sb.toString(), Project.MSG_INFO);
                }
            } catch (BuildException be) {
                throw be;
            } catch (Exception e) {
                throw new BuildException(e, getLocation());
            }
        }
    }
    public RExecSubTask createRead() {
        RExecSubTask task = (RExecSubTask) new RExecRead();
        rexecTasks.addElement(task);
        return task;
    }
    public RExecSubTask createWrite() {
        RExecSubTask task = (RExecSubTask) new RExecWrite();
        rexecTasks.addElement(task);
        return task;
    }
    public void execute() throws BuildException {
        if (server == null) {
            throw new BuildException("No Server Specified");
        }
        if (userid == null && password != null) {
            throw new BuildException("No Userid Specified");
        }
        if (password == null && userid != null) {
            throw new BuildException("No Password Specified");
        }
        AntRExecClient rexec = null;
        try {
            rexec = new AntRExecClient();
            try {
                rexec.connect(server, port);
            } catch (IOException e) {
                throw new BuildException("Can't connect to " + server);
            }
            if (userid != null && password != null && command != null
                && rexecTasks.size() == 0) {
                rexec.rexec(userid, password, command);
            } else {
                handleMultipleTasks(rexec);
            }
            rexec.waitForEOF(defaultTimeout);
        } catch (IOException e) {
            throw new BuildException("Error r-executing command", e);
        } finally {
            if (rexec != null && rexec.isConnected()) {
                try {
                    rexec.disconnect();
                } catch (IOException e) {
                    throw new BuildException("Error disconnecting from "
                                             + server);
                }
            }
        }
    }
    private void login(AntRExecClient rexec) {
        if (addCarriageReturn) {
            rexec.sendString("\n", true);
        }
        rexec.waitForString("ogin:");
        rexec.sendString(userid, true);
        rexec.waitForString("assword:");
        rexec.sendString(password, false);
    }
    public void setCommand(String c) {
        this.command = c;
    }
    public void setInitialCR(boolean b) {
        this.addCarriageReturn = b;
    }
    public void setPassword(String p) {
        this.password = p;
    }
    public void setPort(int p) {
        this.port = p;
    }
    public void setServer(String m) {
        this.server = m;
    }
    public void setTimeout(Integer i) {
        this.defaultTimeout = i;
    }
    public void setUserid(String u) {
        this.userid = u;
    }
    private void handleMultipleTasks(AntRExecClient rexec) {
        if (userid != null && password != null) {
            login(rexec);
        }
        Enumeration tasksToRun = rexecTasks.elements();
        while (tasksToRun != null && tasksToRun.hasMoreElements()) {
            RExecSubTask task = (RExecSubTask) tasksToRun.nextElement();
            if (task instanceof RExecRead && defaultTimeout != null) {
                ((RExecRead) task).setDefaultTimeout(defaultTimeout);
            }
            task.execute(rexec);
        }
    }
}
