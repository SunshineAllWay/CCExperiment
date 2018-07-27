package org.apache.tools.ant.taskdefs.optional.net;
import org.apache.commons.net.telnet.TelnetClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
public class TelnetTask extends Task {
    private static final int WAIT_INTERVAL = 250;
    private static final int TELNET_PORT = 23;
    private String userid  = null;
    private String password = null;
    private String server  = null;
    private int port = TELNET_PORT;
    private Vector telnetTasks = new Vector();
    private boolean addCarriageReturn = false;
    private Integer defaultTimeout = null;
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
       AntTelnetClient telnet = null;
       try {
           telnet = new AntTelnetClient();
           try {
               telnet.connect(server, port);
           } catch (IOException e) {
               throw new BuildException("Can't connect to " + server);
           }
           if (userid != null && password != null) {
               login(telnet);
           }
           Enumeration tasksToRun = telnetTasks.elements();
           while (tasksToRun != null && tasksToRun.hasMoreElements()) {
               TelnetSubTask task = (TelnetSubTask) tasksToRun.nextElement();
               if (task instanceof TelnetRead && defaultTimeout != null) {
                   ((TelnetRead) task).setDefaultTimeout(defaultTimeout);
               }
               task.execute(telnet);
           }
       } finally {
           if (telnet != null && telnet.isConnected()) {
               try {
                   telnet.disconnect();
               } catch (IOException e) {
                   throw new BuildException("Error disconnecting from "
                                            + server);
               }
           }
       }
    }
    private void login(AntTelnetClient telnet) {
       if (addCarriageReturn) {
          telnet.sendString("\n", true);
       }
       telnet.waitForString("ogin:");
       telnet.sendString(userid, true);
       telnet.waitForString("assword:");
       telnet.sendString(password, false);
    }
    public void setUserid(String u) {
        this.userid = u;
    }
    public void setPassword(String p) {
        this.password = p;
    }
    public void setServer(String m) {
        this.server = m;
    }
    public void setPort(int p) {
        this.port = p;
    }
    public void setInitialCR(boolean b) {
       this.addCarriageReturn = b;
    }
    public void setTimeout(Integer i) {
       this.defaultTimeout = i;
    }
    public TelnetSubTask createRead() {
        TelnetSubTask task = (TelnetSubTask) new TelnetRead();
        telnetTasks.addElement(task);
        return task;
    }
    public TelnetSubTask createWrite() {
        TelnetSubTask task = (TelnetSubTask) new TelnetWrite();
        telnetTasks.addElement(task);
        return task;
    }
    public class TelnetSubTask {
        protected String taskString = "";
        public void execute(AntTelnetClient telnet)
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
    public class TelnetWrite extends TelnetSubTask {
        private boolean echoString = true;
        public void execute(AntTelnetClient telnet)
               throws BuildException {
           telnet.sendString(taskString, echoString);
        }
        public void setEcho(boolean b) {
           echoString = b;
        }
    }
    public class TelnetRead extends TelnetSubTask {
        private Integer timeout = null;
        public void execute(AntTelnetClient telnet)
               throws BuildException {
            telnet.waitForString(taskString, timeout);
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
    public class AntTelnetClient extends TelnetClient {
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
                            Thread.sleep(WAIT_INTERVAL);
                        }
                        if (is.available() == 0) {
                            log("Read before running into timeout: "
                                + sb.toString(), Project.MSG_DEBUG);
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
    }
}
