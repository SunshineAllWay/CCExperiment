package org.apache.tools.ant.taskdefs;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.util.StringUtils;
public class Execute {
    private static final int ONE_SECOND = 1000;
    public static final int INVALID = Integer.MAX_VALUE;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String[] cmdl = null;
    private String[] env = null;
    private int exitValue = INVALID;
    private ExecuteStreamHandler streamHandler;
    private ExecuteWatchdog watchdog;
    private File workingDirectory = null;
    private Project project = null;
    private boolean newEnvironment = false;
    private boolean useVMLauncher = true;
    private static String antWorkingDirectory = System.getProperty("user.dir");
    private static CommandLauncher vmLauncher = null;
    private static CommandLauncher shellLauncher = null;
    private static Map procEnvironment = null;
    private static ProcessDestroyer processDestroyer = new ProcessDestroyer();
    private static boolean environmentCaseInSensitive = false;
    static {
        try {
            if (!Os.isFamily("os/2")) {
                vmLauncher = new Java13CommandLauncher();
            }
        } catch (NoSuchMethodException exc) {
        }
        if (Os.isFamily("mac") && !Os.isFamily("unix")) {
            shellLauncher = new MacCommandLauncher(new CommandLauncher());
        } else if (Os.isFamily("os/2")) {
            shellLauncher = new OS2CommandLauncher(new CommandLauncher());
        } else if (Os.isFamily("windows")) {
            environmentCaseInSensitive = true;
            CommandLauncher baseLauncher = new CommandLauncher();
            if (!Os.isFamily("win9x")) {
                shellLauncher = new WinNTCommandLauncher(baseLauncher);
            } else {
                shellLauncher
                    = new ScriptCommandLauncher("bin/antRun.bat", baseLauncher);
            }
        } else if (Os.isFamily("netware")) {
            CommandLauncher baseLauncher = new CommandLauncher();
            shellLauncher
                = new PerlScriptCommandLauncher("bin/antRun.pl", baseLauncher);
        } else if (Os.isFamily("openvms")) {
            try {
                shellLauncher = new VmsCommandLauncher();
            } catch (NoSuchMethodException exc) {
            }
        } else {
            shellLauncher = new ScriptCommandLauncher("bin/antRun",
                new CommandLauncher());
        }
    }
    public void setSpawn(boolean spawn) {
    }
    public static synchronized Map getEnvironmentVariables() {
        if (procEnvironment != null) {
            return procEnvironment;
        }
        if (JavaEnvUtils.isAtLeastJavaVersion(JavaEnvUtils.JAVA_1_5)
            && !Os.isFamily("openvms")) {
            try {
                procEnvironment = (Map) System.class
                    .getMethod("getenv", new Class[0])
                    .invoke(null, new Object[0]);
                return procEnvironment;
            } catch (Exception x) {
                x.printStackTrace();
            }
        }
        procEnvironment = new LinkedHashMap();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Execute exe = new Execute(new PumpStreamHandler(out));
            exe.setCommandline(getProcEnvCommand());
            exe.setNewenvironment(true);
            int retval = exe.execute();
            if (retval != 0) {
            }
            BufferedReader in =
                new BufferedReader(new StringReader(toString(out)));
            if (Os.isFamily("openvms")) {
                procEnvironment = getVMSLogicals(in);
                return procEnvironment;
            }
            String var = null;
            String line, lineSep = StringUtils.LINE_SEP;
            while ((line = in.readLine()) != null) {
                if (line.indexOf('=') == -1) {
                    if (var == null) {
                        var = lineSep + line;
                    } else {
                        var += lineSep + line;
                    }
                } else {
                    if (var != null) {
                        int eq = var.indexOf("=");
                        procEnvironment.put(var.substring(0, eq),
                                            var.substring(eq + 1));
                    }
                    var = line;
                }
            }
            if (var != null) {
                int eq = var.indexOf("=");
                procEnvironment.put(var.substring(0, eq), var.substring(eq + 1));
            }
        } catch (java.io.IOException exc) {
            exc.printStackTrace();
        }
        return procEnvironment;
    }
    public static synchronized Vector getProcEnvironment() {
        Vector v = new Vector();
        Iterator it = getEnvironmentVariables().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            v.add(entry.getKey() + "=" + entry.getValue());
        }
        return v;
    }
    private static String[] getProcEnvCommand() {
        if (Os.isFamily("os/2")) {
            return new String[] {"cmd", "/c", "set" };
        } else if (Os.isFamily("windows")) {
            if (Os.isFamily("win9x")) {
                return new String[] {"command.com", "/c", "set" };
            } else {
                return new String[] {"cmd", "/c", "set" };
            }
        } else if (Os.isFamily("z/os") || Os.isFamily("unix")) {
            String[] cmd = new String[1];
            if (new File("/bin/env").canRead()) {
                cmd[0] = "/bin/env";
            } else if (new File("/usr/bin/env").canRead()) {
                cmd[0] = "/usr/bin/env";
            } else {
                cmd[0] = "env";
            }
            return cmd;
        } else if (Os.isFamily("netware") || Os.isFamily("os/400")) {
            return new String[] {"env"};
        } else if (Os.isFamily("openvms")) {
            return new String[] {"show", "logical"};
        } else {
            return null;
        }
    }
    public static String toString(ByteArrayOutputStream bos) {
        if (Os.isFamily("z/os")) {
            try {
                return bos.toString("Cp1047");
            } catch (java.io.UnsupportedEncodingException e) {
            }
        } else if (Os.isFamily("os/400")) {
            try {
                return bos.toString("Cp500");
            } catch (java.io.UnsupportedEncodingException e) {
            }
        }
        return bos.toString();
    }
    public Execute() {
        this(new PumpStreamHandler(), null);
    }
    public Execute(ExecuteStreamHandler streamHandler) {
        this(streamHandler, null);
    }
    public Execute(ExecuteStreamHandler streamHandler,
                   ExecuteWatchdog watchdog) {
        setStreamHandler(streamHandler);
        this.watchdog = watchdog;
        if (Os.isFamily("openvms")) {
            useVMLauncher = false;
        }
    }
    public void setStreamHandler(ExecuteStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
    }
    public String[] getCommandline() {
        return cmdl;
    }
    public void setCommandline(String[] commandline) {
        cmdl = commandline;
    }
    public void setNewenvironment(boolean newenv) {
        newEnvironment = newenv;
    }
    public String[] getEnvironment() {
        return (env == null || newEnvironment)
            ? env : patchEnvironment();
    }
    public void setEnvironment(String[] env) {
        this.env = env;
    }
    public void setWorkingDirectory(File wd) {
        workingDirectory =
            (wd == null || wd.getAbsolutePath().equals(antWorkingDirectory))
            ? null : wd;
    }
    public File getWorkingDirectory() {
        return workingDirectory == null ? new File(antWorkingDirectory)
                                        : workingDirectory;
    }
    public void setAntRun(Project project) throws BuildException {
        this.project = project;
    }
    public void setVMLauncher(boolean useVMLauncher) {
        this.useVMLauncher = useVMLauncher;
    }
    public static Process launch(Project project, String[] command,
                                 String[] env, File dir, boolean useVM)
        throws IOException {
        if (dir != null && !dir.exists()) {
            throw new BuildException(dir + " doesn't exist.");
        }
        CommandLauncher launcher
            = ((useVM && vmLauncher != null) ? vmLauncher : shellLauncher);
        return launcher.exec(project, command, env, dir);
    }
    public int execute() throws IOException {
        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new BuildException(workingDirectory + " doesn't exist.");
        }
        final Process process = launch(project, getCommandline(),
                                       getEnvironment(), workingDirectory,
                                       useVMLauncher);
        try {
            streamHandler.setProcessInputStream(process.getOutputStream());
            streamHandler.setProcessOutputStream(process.getInputStream());
            streamHandler.setProcessErrorStream(process.getErrorStream());
        } catch (IOException e) {
            process.destroy();
            throw e;
        }
        streamHandler.start();
        try {
            processDestroyer.add(process);
            if (watchdog != null) {
                watchdog.start(process);
            }
            waitFor(process);
            if (watchdog != null) {
                watchdog.stop();
            }
            streamHandler.stop();
            closeStreams(process);
            if (watchdog != null) {
                watchdog.checkException();
            }
            return getExitValue();
        } catch (ThreadDeath t) {
            process.destroy();
            throw t;
        } finally {
            processDestroyer.remove(process);
        }
    }
    public void spawn() throws IOException {
        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new BuildException(workingDirectory + " doesn't exist.");
        }
        final Process process = launch(project, getCommandline(),
                                       getEnvironment(), workingDirectory,
                                       useVMLauncher);
        if (Os.isFamily("windows")) {
            try {
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) {
                project.log("interruption in the sleep after having spawned a"
                            + " process", Project.MSG_VERBOSE);
            }
        }
        OutputStream dummyOut = new OutputStream() {
            public void write(int b) throws IOException {
            }
        };
        ExecuteStreamHandler handler = new PumpStreamHandler(dummyOut);
        handler.setProcessErrorStream(process.getErrorStream());
        handler.setProcessOutputStream(process.getInputStream());
        handler.start();
        process.getOutputStream().close();
        project.log("spawned process " + process.toString(),
                    Project.MSG_VERBOSE);
    }
    protected void waitFor(Process process) {
        try {
            process.waitFor();
            setExitValue(process.exitValue());
        } catch (InterruptedException e) {
            process.destroy();
        }
    }
    protected void setExitValue(int value) {
        exitValue = value;
    }
    public int getExitValue() {
        return exitValue;
    }
    public static boolean isFailure(int exitValue) {
        return Os.isFamily("openvms")
            ? (exitValue % 2 == 0) : (exitValue != 0);
    }
    public boolean isFailure() {
        return isFailure(getExitValue());
    }
    public boolean killedProcess() {
        return watchdog != null && watchdog.killedProcess();
    }
    private String[] patchEnvironment() {
        if (Os.isFamily("openvms")) {
            return env;
        }
        Map osEnv =
            new LinkedHashMap(getEnvironmentVariables());
        for (int i = 0; i < env.length; i++) {
            String keyValue = env[i];
            String key = keyValue.substring(0, keyValue.indexOf('='));
            if (osEnv.remove(key) == null && environmentCaseInSensitive) {
                for (Iterator it = osEnv.keySet().iterator(); it.hasNext(); ) {
                    String osEnvItem = (String) it.next();
                    if (osEnvItem.toLowerCase().equals(key.toLowerCase())) {
                        key = osEnvItem;
                        break;
                    }
                }
            }
            osEnv.put(key, keyValue.substring(key.length() + 1));
        }
        ArrayList l = new ArrayList();
        for (Iterator it = osEnv.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            l.add(entry.getKey() + "=" + entry.getValue());
        }
        return (String[]) (l.toArray(new String[osEnv.size()]));
    }
    public static void runCommand(Task task, String[] cmdline)
        throws BuildException {
        try {
            task.log(Commandline.describeCommand(cmdline),
                     Project.MSG_VERBOSE);
            Execute exe = new Execute(
                new LogStreamHandler(task, Project.MSG_INFO, Project.MSG_ERR));
            exe.setAntRun(task.getProject());
            exe.setCommandline(cmdline);
            int retval = exe.execute();
            if (isFailure(retval)) {
                throw new BuildException(cmdline[0]
                    + " failed with return code " + retval, task.getLocation());
            }
        } catch (java.io.IOException exc) {
            throw new BuildException("Could not launch " + cmdline[0] + ": "
                + exc, task.getLocation());
        }
    }
    public static void closeStreams(Process process) {
        FileUtils.close(process.getInputStream());
        FileUtils.close(process.getOutputStream());
        FileUtils.close(process.getErrorStream());
    }
    private static Map getVMSLogicals(BufferedReader in)
        throws IOException {
        HashMap logicals = new HashMap();
        String logName = null, logValue = null, newLogName;
        String line = null;
        while ((line = in.readLine()) != null) {
            if (line.startsWith("\t=")) {
                if (logName != null) {
                    logValue += "," + line.substring(4, line.length() - 1);
                }
            } else if (line.startsWith("  \"")) {
                if (logName != null) {
                    logicals.put(logName, logValue);
                }
                int eqIndex = line.indexOf('=');
                newLogName = line.substring(3, eqIndex - 2);
                if (logicals.containsKey(newLogName)) {
                    logName = null;
                } else {
                    logName = newLogName;
                    logValue = line.substring(eqIndex + 3, line.length() - 1);
                }
            }
        }
        if (logName != null) {
            logicals.put(logName, logValue);
        }
        return logicals;
    }
    private static class CommandLauncher {
        public Process exec(Project project, String[] cmd, String[] env)
             throws IOException {
            if (project != null) {
                project.log("Execute:CommandLauncher: "
                    + Commandline.describeCommand(cmd), Project.MSG_DEBUG);
            }
            return Runtime.getRuntime().exec(cmd, env);
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            if (workingDir == null) {
                return exec(project, cmd, env);
            }
            throw new IOException("Cannot execute a process in different "
                + "directory under this JVM");
        }
    }
    private static class Java13CommandLauncher extends CommandLauncher {
        public Java13CommandLauncher() throws NoSuchMethodException {
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            try {
                if (project != null) {
                    project.log("Execute:Java13CommandLauncher: "
                        + Commandline.describeCommand(cmd), Project.MSG_DEBUG);
                }
                return Runtime.getRuntime().exec(cmd, env, workingDir);
            } catch (IOException ioex) {
                throw ioex;
            } catch (Exception exc) {
                throw new BuildException("Unable to execute command", exc);
            }
        }
    }
    private static class CommandLauncherProxy extends CommandLauncher {
        private CommandLauncher myLauncher;
        CommandLauncherProxy(CommandLauncher launcher) {
            myLauncher = launcher;
        }
        public Process exec(Project project, String[] cmd, String[] env)
            throws IOException {
            return myLauncher.exec(project, cmd, env);
        }
    }
    private static class OS2CommandLauncher extends CommandLauncherProxy {
        OS2CommandLauncher(CommandLauncher launcher) {
            super(launcher);
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            File commandDir = workingDir;
            if (workingDir == null) {
                if (project != null) {
                    commandDir = project.getBaseDir();
                } else {
                    return exec(project, cmd, env);
                }
            }
            final int preCmdLength = 7;
            final String cmdDir = commandDir.getAbsolutePath();
            String[] newcmd = new String[cmd.length + preCmdLength];
            newcmd[0] = "cmd";
            newcmd[1] = "/c";
            newcmd[2] = cmdDir.substring(0, 2);
            newcmd[3] = "&&";
            newcmd[4] = "cd";
            newcmd[5] = cmdDir.substring(2);
            newcmd[6] = "&&";
            System.arraycopy(cmd, 0, newcmd, preCmdLength, cmd.length);
            return exec(project, newcmd, env);
        }
    }
    private static class WinNTCommandLauncher extends CommandLauncherProxy {
        WinNTCommandLauncher(CommandLauncher launcher) {
            super(launcher);
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            File commandDir = workingDir;
            if (workingDir == null) {
                if (project != null) {
                    commandDir = project.getBaseDir();
                } else {
                    return exec(project, cmd, env);
                }
            }
            final int preCmdLength = 6;
            String[] newcmd = new String[cmd.length + preCmdLength];
            newcmd[0] = "cmd";
            newcmd[1] = "/c";
            newcmd[2] = "cd";
            newcmd[3] = "/d";
            newcmd[4] = commandDir.getAbsolutePath();
            newcmd[5] = "&&";
            System.arraycopy(cmd, 0, newcmd, preCmdLength, cmd.length);
            return exec(project, newcmd, env);
        }
    }
    private static class MacCommandLauncher extends CommandLauncherProxy {
        MacCommandLauncher(CommandLauncher launcher) {
            super(launcher);
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            if (workingDir == null) {
                return exec(project, cmd, env);
            }
            System.getProperties().put("user.dir", workingDir.getAbsolutePath());
            try {
                return exec(project, cmd, env);
            } finally {
                System.getProperties().put("user.dir", antWorkingDirectory);
            }
        }
    }
    private static class ScriptCommandLauncher extends CommandLauncherProxy {
        ScriptCommandLauncher(String script, CommandLauncher launcher) {
            super(launcher);
            myScript = script;
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            if (project == null) {
                if (workingDir == null) {
                    return exec(project, cmd, env);
                }
                throw new IOException("Cannot locate antRun script: "
                    + "No project provided");
            }
            String antHome = project.getProperty(MagicNames.ANT_HOME);
            if (antHome == null) {
                throw new IOException("Cannot locate antRun script: "
                    + "Property '" + MagicNames.ANT_HOME + "' not found");
            }
            String antRun =
                FILE_UTILS.resolveFile(project.getBaseDir(),
                        antHome + File.separator + myScript).toString();
            File commandDir = workingDir;
            if (workingDir == null) {
                commandDir = project.getBaseDir();
            }
            String[] newcmd = new String[cmd.length + 2];
            newcmd[0] = antRun;
            newcmd[1] = commandDir.getAbsolutePath();
            System.arraycopy(cmd, 0, newcmd, 2, cmd.length);
            return exec(project, newcmd, env);
        }
        private String myScript;
    }
    private static class PerlScriptCommandLauncher
        extends CommandLauncherProxy {
        private String myScript;
        PerlScriptCommandLauncher(String script, CommandLauncher launcher) {
            super(launcher);
            myScript = script;
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            if (project == null) {
                if (workingDir == null) {
                    return exec(project, cmd, env);
                }
                throw new IOException("Cannot locate antRun script: "
                    + "No project provided");
            }
            String antHome = project.getProperty(MagicNames.ANT_HOME);
            if (antHome == null) {
                throw new IOException("Cannot locate antRun script: "
                    + "Property '" + MagicNames.ANT_HOME + "' not found");
            }
            String antRun =
                FILE_UTILS.resolveFile(project.getBaseDir(),
                        antHome + File.separator + myScript).toString();
            File commandDir = workingDir;
            if (workingDir == null) {
                commandDir = project.getBaseDir();
            }
            String[] newcmd = new String[cmd.length + 3];
            newcmd[0] = "perl";
            newcmd[1] = antRun;
            newcmd[2] = commandDir.getAbsolutePath();
            System.arraycopy(cmd, 0, newcmd, 3, cmd.length);
            return exec(project, newcmd, env);
        }
    }
    private static class VmsCommandLauncher extends Java13CommandLauncher {
        public VmsCommandLauncher() throws NoSuchMethodException {
            super();
        }
        public Process exec(Project project, String[] cmd, String[] env)
            throws IOException {
            File cmdFile = createCommandFile(cmd, env);
            Process p
                = super.exec(project, new String[] {cmdFile.getPath()}, env);
            deleteAfter(cmdFile, p);
            return p;
        }
        public Process exec(Project project, String[] cmd, String[] env,
                            File workingDir) throws IOException {
            File cmdFile = createCommandFile(cmd, env);
            Process p = super.exec(project, new String[] {cmdFile.getPath()},
                                   env, workingDir);
            deleteAfter(cmdFile, p);
            return p;
        }
        private File createCommandFile(String[] cmd, String[] env)
            throws IOException {
            File script = FILE_UTILS.createTempFile("ANT", ".COM", null, true, true);
            BufferedWriter out = null;
            try {
                out = new BufferedWriter(new FileWriter(script));
                if (env != null) {
                    int eqIndex;
                    for (int i = 0; i < env.length; i++) {
                        eqIndex = env[i].indexOf('=');
                        if (eqIndex != -1) {
                            out.write("$ DEFINE/NOLOG ");
                            out.write(env[i].substring(0, eqIndex));
                            out.write(" \"");
                            out.write(env[i].substring(eqIndex + 1));
                            out.write('\"');
                            out.newLine();
                        }
                    }
                }
                out.write("$ " + cmd[0]);
                for (int i = 1; i < cmd.length; i++) {
                    out.write(" -");
                    out.newLine();
                    out.write(cmd[i]);
                }
            } finally {
                FileUtils.close(out);
            }
            return script;
        }
        private void deleteAfter(final File f, final Process p) {
            new Thread() {
                public void run() {
                    try {
                        p.waitFor();
                    } catch (InterruptedException e) {
                    }
                    FileUtils.delete(f);
                }
            }
            .start();
        }
    }
}
