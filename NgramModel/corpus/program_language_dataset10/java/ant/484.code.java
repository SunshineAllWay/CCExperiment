package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
import org.apache.tools.ant.taskdefs.LogOutputStream;
import org.apache.tools.ant.taskdefs.PumpStreamHandler;
import org.apache.tools.ant.types.Assertions;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.CommandlineJava;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Permissions;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.LoaderUtils;
import org.apache.tools.ant.util.SplitClassLoader;
public class JUnitTask extends Task {
    private static final String LINE_SEP
        = System.getProperty("line.separator");
    private static final String CLASSPATH = "CLASSPATH";
    private CommandlineJava commandline;
    private Vector tests = new Vector();
    private Vector batchTests = new Vector();
    private Vector formatters = new Vector();
    private File dir = null;
    private Integer timeout = null;
    private boolean summary = false;
    private boolean reloading = true;
    private String summaryValue = "";
    private JUnitTaskMirror.JUnitTestRunnerMirror runner = null;
    private boolean newEnvironment = false;
    private Environment env = new Environment();
    private boolean includeAntRuntime = true;
    private Path antRuntimeClasses = null;
    private boolean showOutput = false;
    private boolean outputToFormatters = true;
    private boolean logFailedTests = true;
    private File tmpDir;
    private AntClassLoader classLoader = null;
    private Permissions perm = null;
    private ForkMode forkMode = new ForkMode("perTest");
    private boolean splitJunit = false;
    private boolean enableTestListenerEvents = false;
    private JUnitTaskMirror delegate;
    private ClassLoader mirrorLoader;
    private boolean forkedPathChecked = false;
    private boolean haltOnError = false;
    private boolean haltOnFail  = false;
    private boolean filterTrace = true;
    private boolean fork        = false;
    private String  failureProperty;
    private String  errorProperty;
    private static final int STRING_BUFFER_SIZE = 128;
    public static final String TESTLISTENER_PREFIX =
        "junit.framework.TestListener: ";
    public static final String ENABLE_TESTLISTENER_EVENTS =
        "ant.junit.enabletestlistenerevents";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public void setReloading(boolean value) {
        reloading = value;
    }
    public void setFiltertrace(boolean value) {
        this.filterTrace = value;
    }
    public void setHaltonerror(boolean value) {
        this.haltOnError = value;
    }
    public void setErrorProperty(String propertyName) {
        this.errorProperty = propertyName;
    }
    public void setHaltonfailure(boolean value) {
        this.haltOnFail = value;
    }
    public void setFailureProperty(String propertyName) {
        this.failureProperty = propertyName;
    }
    public void setFork(boolean value) {
        this.fork = value;
    }
    public void setForkMode(ForkMode mode) {
        this.forkMode = mode;
    }
    public void setPrintsummary(SummaryAttribute value) {
        summaryValue = value.getValue();
        summary = value.asBoolean();
    }
    public static class SummaryAttribute extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"true", "yes", "false", "no",
                                 "on", "off", "withOutAndErr"};
        }
        public boolean asBoolean() {
            String v = getValue();
            return "true".equals(v)
                || "on".equals(v)
                || "yes".equals(v)
                || "withOutAndErr".equals(v);
        }
    }
    public void setTimeout(Integer value) {
        timeout = value;
    }
    public void setMaxmemory(String max) {
        getCommandline().setMaxmemory(max);
    }
    public void setJvm(String value) {
        getCommandline().setVm(value);
    }
    public Commandline.Argument createJvmarg() {
        return getCommandline().createVmArgument();
    }
    public void setDir(File dir) {
        this.dir = dir;
    }
    public void addSysproperty(Environment.Variable sysp) {
        getCommandline().addSysproperty(sysp);
    }
    public void addConfiguredSysproperty(Environment.Variable sysp) {
        String testString = sysp.getContent();
        getProject().log("sysproperty added : " + testString, Project.MSG_DEBUG);
        getCommandline().addSysproperty(sysp);
    }
    public void addSyspropertyset(PropertySet sysp) {
        getCommandline().addSyspropertyset(sysp);
    }
    public Path createClasspath() {
        return getCommandline().createClasspath(getProject()).createPath();
    }
    public Path createBootclasspath() {
        return getCommandline().createBootclasspath(getProject()).createPath();
    }
    public void addEnv(Environment.Variable var) {
        env.addVariable(var);
    }
    public void setNewenvironment(boolean newenv) {
        newEnvironment = newenv;
    }
    private void preConfigure(BaseTest test) {
        test.setFiltertrace(filterTrace);
        test.setHaltonerror(haltOnError);
        if (errorProperty != null) {
            test.setErrorProperty(errorProperty);
        }
        test.setHaltonfailure(haltOnFail);
        if (failureProperty != null) {
            test.setFailureProperty(failureProperty);
        }
        test.setFork(fork);
    }
    public void addTest(JUnitTest test) {
        tests.addElement(test);
        preConfigure(test);
    }
    public BatchTest createBatchTest() {
        BatchTest test = new BatchTest(getProject());
        batchTests.addElement(test);
        preConfigure(test);
        return test;
    }
    public void addFormatter(FormatterElement fe) {
        formatters.addElement(fe);
    }
    public void setIncludeantruntime(boolean b) {
        includeAntRuntime = b;
    }
    public void setShowOutput(boolean showOutput) {
        this.showOutput = showOutput;
    }
    public void setOutputToFormatters(boolean outputToFormatters) {
        this.outputToFormatters = outputToFormatters;
    }
    public void setLogFailedTests(boolean logFailedTests) {
        this.logFailedTests = logFailedTests;
    }
    public void addAssertions(Assertions asserts) {
        if (getCommandline().getAssertions() != null) {
            throw new BuildException("Only one assertion declaration is allowed");
        }
        getCommandline().setAssertions(asserts);
    }
    public Permissions createPermissions() {
        if (perm == null) {
            perm = new Permissions();
        }
        return perm;
    }
    public void setCloneVm(boolean cloneVm) {
        getCommandline().setCloneVm(cloneVm);
    }
    public JUnitTask() throws Exception {
    }
    public void setTempdir(File tmpDir) {
        if (tmpDir != null) {
            if (!tmpDir.exists() || !tmpDir.isDirectory()) {
                throw new BuildException(tmpDir.toString()
                                         + " is not a valid temp directory");
            }
        }
        this.tmpDir = tmpDir;
    }
    public void setEnableTestListenerEvents(boolean b) {
        enableTestListenerEvents = b;
    }
    public boolean getEnableTestListenerEvents() {
        String e = getProject().getProperty(ENABLE_TESTLISTENER_EVENTS);
        if (e != null) {
            return Project.toBoolean(e);
        }
        return enableTestListenerEvents;
    }
    public void init() {
        antRuntimeClasses = new Path(getProject());
        splitJunit = !addClasspathResource("/junit/framework/TestCase.class");
        addClasspathEntry("/org/apache/tools/ant/launch/AntMain.class");
        addClasspathEntry("/org/apache/tools/ant/Task.class");
        addClasspathEntry("/org/apache/tools/ant/taskdefs/optional/junit/JUnitTestRunner.class");
        addClasspathEntry("/org/apache/tools/ant/taskdefs/optional/junit/JUnit4TestMethodAdapter.class");
    }
    private static JUnitTaskMirror createMirror(JUnitTask task, ClassLoader loader) {
        try {
            loader.loadClass("junit.framework.Test"); 
        } catch (ClassNotFoundException e) {
            throw new BuildException(
                    "The <classpath> for <junit> must include junit.jar "
                    + "if not in Ant's own classpath",
                    e, task.getLocation());
        }
        try {
            Class c = loader.loadClass(JUnitTaskMirror.class.getName() + "Impl");
            if (c.getClassLoader() != loader) {
                throw new BuildException("Overdelegating loader", task.getLocation());
            }
            Constructor cons = c.getConstructor(new Class[] {JUnitTask.class});
            return (JUnitTaskMirror) cons.newInstance(new Object[] {task});
        } catch (Exception e) {
            throw new BuildException(e, task.getLocation());
        }
    }
    protected void setupJUnitDelegate() {
        final ClassLoader myLoader = JUnitTask.class.getClassLoader();
        if (splitJunit) {
            final Path path = new Path(getProject());
            path.add(antRuntimeClasses);
            Path extra = getCommandline().getClasspath();
            if (extra != null) {
                path.add(extra);
            }
            mirrorLoader = (ClassLoader) AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return new SplitClassLoader(myLoader, path, getProject(),
                                     new String[] {
                                         "BriefJUnitResultFormatter",
                                         "JUnit4TestMethodAdapter",
                                         "JUnitResultFormatter",
                                         "JUnitTaskMirrorImpl",
                                         "JUnitTestRunner",
                                         "JUnitVersionHelper",
                                         "OutErrSummaryJUnitResultFormatter",
                                         "PlainJUnitResultFormatter",
                                         "SummaryJUnitResultFormatter",
                                         "TearDownOnVmCrash",
                                         "XMLJUnitResultFormatter",
                                     });
                }
            });
        } else {
            mirrorLoader = myLoader;
        }
        delegate = createMirror(this, mirrorLoader);
    }
    public void execute() throws BuildException {
        checkMethodLists();
        setupJUnitDelegate();
        List testLists = new ArrayList();
        boolean forkPerTest = forkMode.getValue().equals(ForkMode.PER_TEST);
        if (forkPerTest || forkMode.getValue().equals(ForkMode.ONCE)) {
            testLists.addAll(executeOrQueue(getIndividualTests(),
                                            forkPerTest));
        } else { 
            final int count = batchTests.size();
            for (int i = 0; i < count; i++) {
                BatchTest batchtest = (BatchTest) batchTests.elementAt(i);
                testLists.addAll(executeOrQueue(batchtest.elements(), false));
            }
            testLists.addAll(executeOrQueue(tests.elements(), forkPerTest));
        }
        try {
            Iterator iter = testLists.iterator();
            while (iter.hasNext()) {
                List l = (List) iter.next();
                if (l.size() == 1) {
                    execute((JUnitTest) l.get(0));
                } else {
                    execute(l);
                }
            }
        } finally {
            cleanup();
        }
    }
    protected void execute(JUnitTest arg) throws BuildException {
        validateTestName(arg.getName());
        JUnitTest test = (JUnitTest) arg.clone();
        if (test.getTodir() == null) {
            test.setTodir(getProject().resolveFile("."));
        }
        if (test.getOutfile() == null) {
            test.setOutfile("TEST-" + test.getName());
        }
        TestResultHolder result = null;
        if (!test.getFork()) {
            result = executeInVM(test);
        } else {
            ExecuteWatchdog watchdog = createWatchdog();
            result = executeAsForked(test, watchdog, null);
        }
        actOnTestResult(result, test, "Test " + test.getName());
    }
    private void validateTestName(String testName) throws BuildException {
        if (testName == null || testName.length() == 0
            || testName.equals("null")) {
            throw new BuildException("test name must be specified");
        }
    }
    protected void execute(List testList) throws BuildException {
        JUnitTest test = null;
        File casesFile = createTempPropertiesFile("junittestcases");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(casesFile));
            log("Creating casesfile '" + casesFile.getAbsolutePath()
                + "' with content: ", Project.MSG_VERBOSE);
            PrintStream logWriter =
                new PrintStream(new LogOutputStream(this, Project.MSG_VERBOSE));
            Iterator iter = testList.iterator();
            while (iter.hasNext()) {
                test = (JUnitTest) iter.next();
                printDual(writer, logWriter, test.getName());
                if (test.getMethods() != null) {
                    printDual(writer, logWriter, ":" + test.getMethodsString().replace(',', '+'));
                }
                if (test.getTodir() == null) {
                    printDual(writer, logWriter,
                              "," + getProject().resolveFile("."));
                } else {
                    printDual(writer, logWriter, "," + test.getTodir());
                }
                if (test.getOutfile() == null) {
                    printlnDual(writer, logWriter,
                                "," + "TEST-" + test.getName());
                } else {
                    printlnDual(writer, logWriter, "," + test.getOutfile());
                }
            }
            writer.flush();
            writer.close();
            writer = null;
            ExecuteWatchdog watchdog = createWatchdog();
            TestResultHolder result =
                executeAsForked(test, watchdog, casesFile);
            actOnTestResult(result, test, "Tests");
        } catch (IOException e) {
            log(e.toString(), Project.MSG_ERR);
            throw new BuildException(e);
        } finally {
            FileUtils.close(writer);
            try {
                FILE_UTILS.tryHardToDelete(casesFile);
            } catch (Exception e) {
                log(e.toString(), Project.MSG_ERR);
            }
        }
    }
    private TestResultHolder executeAsForked(JUnitTest test,
                                             ExecuteWatchdog watchdog,
                                             File casesFile)
        throws BuildException {
        if (perm != null) {
            log("Permissions ignored when running in forked mode!",
                Project.MSG_WARN);
        }
        CommandlineJava cmd;
        try {
            cmd = (CommandlineJava) (getCommandline().clone());
        } catch (CloneNotSupportedException e) {
            throw new BuildException("This shouldn't happen", e, getLocation());
        }
        if (casesFile == null) {
            cmd.createArgument().setValue(test.getName());
            if (test.getMethods() != null) {
                cmd.createArgument().setValue(Constants.METHOD_NAMES + test.getMethodsString());
            }
        } else {
            log("Running multiple tests in the same VM", Project.MSG_VERBOSE);
            cmd.createArgument().setValue(Constants.TESTSFILE + casesFile);
        }
        cmd.createArgument().setValue(Constants.FILTERTRACE + test.getFiltertrace());
        cmd.createArgument().setValue(Constants.HALT_ON_ERROR + test.getHaltonerror());
        cmd.createArgument().setValue(Constants.HALT_ON_FAILURE
                                      + test.getHaltonfailure());
        checkIncludeAntRuntime(cmd);
        checkIncludeSummary(cmd);
        cmd.createArgument().setValue(Constants.SHOWOUTPUT
                                      + String.valueOf(showOutput));
        cmd.createArgument().setValue(Constants.OUTPUT_TO_FORMATTERS
                                      + String.valueOf(outputToFormatters));
        cmd.createArgument().setValue(Constants.LOG_FAILED_TESTS
                                      + String.valueOf(logFailedTests));
        cmd.createArgument().setValue(Constants.LOGTESTLISTENEREVENTS
                                      + String.valueOf(getEnableTestListenerEvents()));
        StringBuffer formatterArg = new StringBuffer(STRING_BUFFER_SIZE);
        final FormatterElement[] feArray = mergeFormatters(test);
        for (int i = 0; i < feArray.length; i++) {
            FormatterElement fe = feArray[i];
            if (fe.shouldUse(this)) {
                formatterArg.append(Constants.FORMATTER);
                formatterArg.append(fe.getClassname());
                File outFile = getOutput(fe, test);
                if (outFile != null) {
                    formatterArg.append(",");
                    formatterArg.append(outFile);
                }
                cmd.createArgument().setValue(formatterArg.toString());
                formatterArg = new StringBuffer();
            }
        }
        File vmWatcher = createTempPropertiesFile("junitvmwatcher");
        cmd.createArgument().setValue(Constants.CRASHFILE
                                      + vmWatcher.getAbsolutePath());
        File propsFile = createTempPropertiesFile("junit");
        cmd.createArgument().setValue(Constants.PROPSFILE
                                      + propsFile.getAbsolutePath());
        Hashtable p = getProject().getProperties();
        Properties props = new Properties();
        for (Enumeration e = p.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            props.put(key, p.get(key));
        }
        try {
            FileOutputStream outstream = new FileOutputStream(propsFile);
            props.store(outstream, "Ant JUnitTask generated properties file");
            outstream.close();
        } catch (java.io.IOException e) {
            FILE_UTILS.tryHardToDelete(propsFile);
            throw new BuildException("Error creating temporary properties "
                                     + "file.", e, getLocation());
        }
        Execute execute = new Execute(
            new JUnitLogStreamHandler(
                this,
                Project.MSG_INFO,
                Project.MSG_WARN),
            watchdog);
        execute.setCommandline(cmd.getCommandline());
        execute.setAntRun(getProject());
        if (dir != null) {
            execute.setWorkingDirectory(dir);
        }
        String[] environment = env.getVariables();
        if (environment != null) {
            for (int i = 0; i < environment.length; i++) {
                log("Setting environment variable: " + environment[i],
                    Project.MSG_VERBOSE);
            }
        }
        execute.setNewenvironment(newEnvironment);
        execute.setEnvironment(environment);
        log(cmd.describeCommand(), Project.MSG_VERBOSE);
        checkForkedPath(cmd);
        TestResultHolder result = new TestResultHolder();
        try {
            result.exitCode = execute.execute();
        } catch (IOException e) {
            throw new BuildException("Process fork failed.", e, getLocation());
        } finally {
            String vmCrashString = "unknown";
            BufferedReader br = null;
            try {
                if (vmWatcher.exists()) {
                    br = new BufferedReader(new FileReader(vmWatcher));
                    vmCrashString = br.readLine();
                } else {
                    vmCrashString = "Monitor file ("
                            + vmWatcher.getAbsolutePath()
                            + ") missing, location not writable,"
                            + " testcase not started or mixing ant versions?";
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                FileUtils.close(br);
                if (vmWatcher.exists()) {
                    FILE_UTILS.tryHardToDelete(vmWatcher);
                }
            }
            boolean crash = (watchdog != null && watchdog.killedProcess())
                || !Constants.TERMINATED_SUCCESSFULLY.equals(vmCrashString);
            if (casesFile != null && crash) {
                test = createDummyTestForBatchTest(test);
            }
            if (watchdog != null && watchdog.killedProcess()) {
                result.timedOut = true;
                logTimeout(feArray, test, vmCrashString);
            } else if (crash) {
                result.crashed = true;
                logVmCrash(feArray, test, vmCrashString);
            }
            if (!FILE_UTILS.tryHardToDelete(propsFile)) {
                throw new BuildException("Could not delete temporary "
                                         + "properties file '"
                                         + propsFile.getAbsolutePath() + "'.");
            }
        }
        return result;
    }
    private void checkIncludeAntRuntime(CommandlineJava cmd) {
        if (includeAntRuntime) {
            Map env = Execute.getEnvironmentVariables();
            String cp = (String) env.get(CLASSPATH);
            if (cp != null) {
                cmd.createClasspath(getProject()).createPath()
                    .append(new Path(getProject(), cp));
            }
            log("Implicitly adding " + antRuntimeClasses + " to CLASSPATH",
                Project.MSG_VERBOSE);
            cmd.createClasspath(getProject()).createPath()
                .append(antRuntimeClasses);
        }
    }
    private boolean equalsWithOutAndErr(String summaryOption) {
        return "withoutanderr".equalsIgnoreCase(summaryOption);
    }
    private void checkIncludeSummary(CommandlineJava cmd) {
        if (summary) {
            String prefix = "";
            if (equalsWithOutAndErr(summaryValue)) {
                prefix = "OutErr";
            }
            cmd.createArgument()
                .setValue(Constants.FORMATTER
                          + "org.apache.tools.ant.taskdefs.optional.junit."
                          + prefix + "SummaryJUnitResultFormatter");
        }
    }
    private void checkForkedPath(CommandlineJava cmd) {
        if (forkedPathChecked) {
            return;
        }
        forkedPathChecked = true;
        if (!cmd.haveClasspath()) {
            return;
        }
        AntClassLoader loader = null;
        try {
            loader =
                AntClassLoader.newAntClassLoader(null, getProject(),
                                                 cmd.createClasspath(getProject()),
                                                 true);
            String projectResourceName =
                LoaderUtils.classNameToResource(Project.class.getName());
            URL previous = null;
            try {
                for (Enumeration e = loader.getResources(projectResourceName);
                     e.hasMoreElements();) {
                    URL current = (URL) e.nextElement();
                    if (previous != null && !urlEquals(current, previous)) {
                        log("WARNING: multiple versions of ant detected "
                            + "in path for junit "
                            + LINE_SEP + "         " + previous
                            + LINE_SEP + "     and " + current,
                            Project.MSG_WARN);
                        return;
                    }
                    previous = current;
                }
            } catch (Exception ex) {
            }
        } finally {
            if (loader != null) {
                loader.cleanup();
            }
        }
    }
    private static boolean urlEquals(URL u1, URL u2) {
        String url1 = maybeStripJarAndClass(u1);
        String url2 = maybeStripJarAndClass(u2);
        if (url1.startsWith("file:") && url2.startsWith("file:")) {
            return new File(FILE_UTILS.fromURI(url1))
                .equals(new File(FILE_UTILS.fromURI(url2)));
        }
        return url1.equals(url2);
    }
    private static String maybeStripJarAndClass(URL u) {
        String s = u.toString();
        if (s.startsWith("jar:")) {
            int pling = s.indexOf('!');
            s = s.substring(4, pling == -1 ? s.length() : pling);
        }
        return s;
    }
    private File createTempPropertiesFile(String prefix) {
        File propsFile =
            FILE_UTILS.createTempFile(prefix, ".properties",
                tmpDir != null ? tmpDir : getProject().getBaseDir(), true, true);
        return propsFile;
    }
    protected void handleOutput(String output) {
        if (output.startsWith(TESTLISTENER_PREFIX)) {
            log(output, Project.MSG_VERBOSE);
        } else if (runner != null) {
            if (outputToFormatters) {
                runner.handleOutput(output);
            }
            if (showOutput) {
                super.handleOutput(output);
            }
        } else {
            super.handleOutput(output);
        }
    }
    protected int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        if (runner != null) {
            return runner.handleInput(buffer, offset, length);
        } else {
            return super.handleInput(buffer, offset, length);
        }
    }
    protected void handleFlush(String output) {
        if (runner != null) {
            runner.handleFlush(output);
            if (showOutput) {
                super.handleFlush(output);
            }
        } else {
            super.handleFlush(output);
        }
    }
    public void handleErrorOutput(String output) {
        if (runner != null) {
            runner.handleErrorOutput(output);
            if (showOutput) {
                super.handleErrorOutput(output);
            }
        } else {
            super.handleErrorOutput(output);
        }
    }
    public void handleErrorFlush(String output) {
        if (runner != null) {
            runner.handleErrorFlush(output);
            if (showOutput) {
                super.handleErrorFlush(output);
            }
        } else {
            super.handleErrorFlush(output);
        }
    }
    private TestResultHolder executeInVM(JUnitTest arg) throws BuildException {
        if (delegate == null) {
            setupJUnitDelegate();
        }
        JUnitTest test = (JUnitTest) arg.clone();
        test.setProperties(getProject().getProperties());
        if (dir != null) {
            log("dir attribute ignored if running in the same VM",
                Project.MSG_WARN);
        }
        if (newEnvironment || null != env.getVariables()) {
            log("Changes to environment variables are ignored if running in "
                + "the same VM.", Project.MSG_WARN);
        }
        if (getCommandline().getBootclasspath() != null) {
            log("bootclasspath is ignored if running in the same VM.",
                Project.MSG_WARN);
        }
        CommandlineJava.SysProperties sysProperties =
                getCommandline().getSystemProperties();
        if (sysProperties != null) {
            sysProperties.setSystem();
        }
        try {
            log("Using System properties " + System.getProperties(),
                Project.MSG_VERBOSE);
            if (splitJunit) {
                classLoader = (AntClassLoader) delegate.getClass().getClassLoader();
            } else {
                createClassLoader();
            }
            if (classLoader != null) {
                classLoader.setThreadContextLoader();
            }
            runner = delegate.newJUnitTestRunner(test, test.getMethods(), test.getHaltonerror(),
                                         test.getFiltertrace(),
                                         test.getHaltonfailure(), false,
                                         getEnableTestListenerEvents(),
                                         classLoader);
            if (summary) {
                JUnitTaskMirror.SummaryJUnitResultFormatterMirror f =
                    delegate.newSummaryJUnitResultFormatter();
                f.setWithOutAndErr(equalsWithOutAndErr(summaryValue));
                f.setOutput(getDefaultOutput());
                runner.addFormatter(f);
            }
            runner.setPermissions(perm);
            final FormatterElement[] feArray = mergeFormatters(test);
            for (int i = 0; i < feArray.length; i++) {
                FormatterElement fe = feArray[i];
                if (fe.shouldUse(this)) {
                    File outFile = getOutput(fe, test);
                    if (outFile != null) {
                        fe.setOutfile(outFile);
                    } else {
                        fe.setOutput(getDefaultOutput());
                    }
                    runner.addFormatter(fe.createFormatter(classLoader));
                }
            }
            runner.run();
            TestResultHolder result = new TestResultHolder();
            result.exitCode = runner.getRetCode();
            return result;
        } finally {
            if (sysProperties != null) {
                sysProperties.restoreSystem();
            }
            if (classLoader != null) {
                classLoader.resetThreadContextLoader();
            }
        }
    }
    protected ExecuteWatchdog createWatchdog() throws BuildException {
        if (timeout == null) {
            return null;
        }
        return new ExecuteWatchdog((long) timeout.intValue());
    }
    protected OutputStream getDefaultOutput() {
        return new LogOutputStream(this, Project.MSG_INFO);
    }
    protected Enumeration getIndividualTests() {
        final int count = batchTests.size();
        final Enumeration[] enums = new Enumeration[ count + 1];
        for (int i = 0; i < count; i++) {
            BatchTest batchtest = (BatchTest) batchTests.elementAt(i);
            enums[i] = batchtest.elements();
        }
        enums[enums.length - 1] = tests.elements();
        return Enumerations.fromCompound(enums);
    }
    private void checkMethodLists() throws BuildException {
        if (tests.isEmpty()) {
            return;
        }
        Enumeration testsEnum = tests.elements();
        while (testsEnum.hasMoreElements()) {
            JUnitTest test = (JUnitTest) testsEnum.nextElement();
            if (test.hasMethodsSpecified() && test.shouldRun(getProject())) {
                test.resolveMethods();
            }
        }
    }
    protected Enumeration allTests() {
        Enumeration[] enums = {tests.elements(), batchTests.elements()};
        return Enumerations.fromCompound(enums);
    }
    private FormatterElement[] mergeFormatters(JUnitTest test) {
        Vector feVector = (Vector) formatters.clone();
        test.addFormattersTo(feVector);
        FormatterElement[] feArray = new FormatterElement[feVector.size()];
        feVector.copyInto(feArray);
        return feArray;
    }
    protected File getOutput(FormatterElement fe, JUnitTest test) {
        if (fe.getUseFile()) {
            String base = test.getOutfile();
            if (base == null) {
                base = JUnitTaskMirror.JUnitTestRunnerMirror.IGNORED_FILE_NAME;
            }
            String filename = base + fe.getExtension();
            File destFile = new File(test.getTodir(), filename);
            String absFilename = destFile.getAbsolutePath();
            return getProject().resolveFile(absFilename);
        }
        return null;
    }
    protected void addClasspathEntry(String resource) {
        addClasspathResource(resource);
    }
    private boolean addClasspathResource(String resource) {
        if (resource.startsWith("/")) {
            resource = resource.substring(1);
        } else {
            resource = "org/apache/tools/ant/taskdefs/optional/junit/"
                + resource;
        }
        File f = LoaderUtils.getResourceSource(getClass().getClassLoader(),
                                               resource);
        if (f != null) {
            log("Found " + f.getAbsolutePath(), Project.MSG_DEBUG);
            antRuntimeClasses.createPath().setLocation(f);
            return true;
        } else {
            log("Couldn\'t find " + resource, Project.MSG_DEBUG);
            return false;
        }
    }
    static final String TIMEOUT_MESSAGE = 
        "Timeout occurred. Please note the time in the report does"
        + " not reflect the time until the timeout.";
    private void logTimeout(FormatterElement[] feArray, JUnitTest test,
                            String testCase) {
        logVmExit(feArray, test, TIMEOUT_MESSAGE, testCase);
    }
    private void logVmCrash(FormatterElement[] feArray, JUnitTest test, String testCase) {
        logVmExit(
            feArray, test,
            "Forked Java VM exited abnormally. Please note the time in the report"
            + " does not reflect the time until the VM exit.",
            testCase);
    }
    private void logVmExit(FormatterElement[] feArray, JUnitTest test,
                           String message, String testCase) {
        if (delegate == null) {
            setupJUnitDelegate();
        }
        try {
            log("Using System properties " + System.getProperties(),
                Project.MSG_VERBOSE);
            if (splitJunit) {
                classLoader = (AntClassLoader) delegate.getClass().getClassLoader();
            } else {
                createClassLoader();
            }
            if (classLoader != null) {
                classLoader.setThreadContextLoader();
            }
            test.setCounts(1, 0, 1);
            test.setProperties(getProject().getProperties());
            for (int i = 0; i < feArray.length; i++) {
                FormatterElement fe = feArray[i];
                if (fe.shouldUse(this)) {
                    JUnitTaskMirror.JUnitResultFormatterMirror formatter =
                        fe.createFormatter(classLoader);
                    if (formatter != null) {
                        OutputStream out = null;
                        File outFile = getOutput(fe, test);
                        if (outFile != null) {
                            try {
                                out = new FileOutputStream(outFile);
                            } catch (IOException e) {
                            }
                        }
                        if (out == null) {
                            out = getDefaultOutput();
                        }
                        delegate.addVmExit(test, formatter, out, message,
                                           testCase);
                    }
                }
            }
            if (summary) {
                JUnitTaskMirror.SummaryJUnitResultFormatterMirror f =
                    delegate.newSummaryJUnitResultFormatter();
                f.setWithOutAndErr(equalsWithOutAndErr(summaryValue));
                delegate.addVmExit(test, f, getDefaultOutput(), message, testCase);
            }
        } finally {
            if (classLoader != null) {
                classLoader.resetThreadContextLoader();
            }
        }
    }
    private void createClassLoader() {
        Path userClasspath = getCommandline().getClasspath();
        if (userClasspath != null) {
            if (reloading || classLoader == null) {
                deleteClassLoader();
                Path classpath = (Path) userClasspath.clone();
                if (includeAntRuntime) {
                    log("Implicitly adding " + antRuntimeClasses
                        + " to CLASSPATH", Project.MSG_VERBOSE);
                    classpath.append(antRuntimeClasses);
                }
                classLoader = getProject().createClassLoader(classpath);
                if (getClass().getClassLoader() != null
                    && getClass().getClassLoader() != Project.class.getClassLoader()) {
                    classLoader.setParent(getClass().getClassLoader());
                }
                classLoader.setParentFirst(false);
                classLoader.addJavaLibraries();
                log("Using CLASSPATH " + classLoader.getClasspath(),
                    Project.MSG_VERBOSE);
                classLoader.addSystemPackageRoot("junit");
                classLoader.addSystemPackageRoot("org.junit");
                classLoader.addSystemPackageRoot("org.apache.tools.ant");
            }
        }
    }
    protected void cleanup() {
        deleteClassLoader();
        delegate = null;
    }
    private void deleteClassLoader() {
        if (classLoader != null) {
            classLoader.cleanup();
            classLoader = null;
        }
        if (mirrorLoader instanceof SplitClassLoader) {
            ((SplitClassLoader) mirrorLoader).cleanup();
        }
        mirrorLoader = null;
    }
    protected CommandlineJava getCommandline() {
        if (commandline == null) {
            commandline = new CommandlineJava();
            commandline.setClassname("org.apache.tools.ant.taskdefs.optional.junit.JUnitTestRunner");
        }
        return commandline;
    }
    private static final class ForkedTestConfiguration {
        private boolean filterTrace;
        private boolean haltOnError;
        private boolean haltOnFailure;
        private String errorProperty;
        private String failureProperty;
        ForkedTestConfiguration(boolean filterTrace, boolean haltOnError,
                                boolean haltOnFailure, String errorProperty,
                                String failureProperty) {
            this.filterTrace = filterTrace;
            this.haltOnError = haltOnError;
            this.haltOnFailure = haltOnFailure;
            this.errorProperty = errorProperty;
            this.failureProperty = failureProperty;
        }
        ForkedTestConfiguration(JUnitTest test) {
            this(test.getFiltertrace(),
                    test.getHaltonerror(),
                    test.getHaltonfailure(),
                    test.getErrorProperty(),
                    test.getFailureProperty());
        }
        public boolean equals(Object other) {
            if (other == null
                || other.getClass() != ForkedTestConfiguration.class) {
                return false;
            }
            ForkedTestConfiguration o = (ForkedTestConfiguration) other;
            return filterTrace == o.filterTrace
                && haltOnError == o.haltOnError
                && haltOnFailure == o.haltOnFailure
                && ((errorProperty == null && o.errorProperty == null)
                    ||
                    (errorProperty != null
                     && errorProperty.equals(o.errorProperty)))
                && ((failureProperty == null && o.failureProperty == null)
                    ||
                    (failureProperty != null
                     && failureProperty.equals(o.failureProperty)));
        }
        public int hashCode() {
            return (filterTrace ? 1 : 0)
                + (haltOnError ? 2 : 0)
                + (haltOnFailure ? 4 : 0);
        }
    }
    public static final class ForkMode extends EnumeratedAttribute {
        public static final String ONCE = "once";
        public static final String PER_TEST = "perTest";
        public static final String PER_BATCH = "perBatch";
        public ForkMode() {
            super();
        }
        public ForkMode(String value) {
            super();
            setValue(value);
        }
        public String[] getValues() {
            return new String[] {ONCE, PER_TEST, PER_BATCH};
        }
    }
    protected Collection executeOrQueue(Enumeration testList,
                                        boolean runIndividual) {
        Map testConfigurations = new HashMap();
        while (testList.hasMoreElements()) {
            JUnitTest test = (JUnitTest) testList.nextElement();
            if (test.shouldRun(getProject())) {
                if (runIndividual || !test.getFork()) {
                    execute(test);
                } else {
                    ForkedTestConfiguration c =
                        new ForkedTestConfiguration(test);
                    List l = (List) testConfigurations.get(c);
                    if (l == null) {
                        l = new ArrayList();
                        testConfigurations.put(c, l);
                    }
                    l.add(test);
                }
            }
        }
        return testConfigurations.values();
    }
    protected void actOnTestResult(int exitValue, boolean wasKilled,
                                   JUnitTest test, String name) {
        TestResultHolder t = new TestResultHolder();
        t.exitCode = exitValue;
        t.timedOut = wasKilled;
        actOnTestResult(t, test, name);
    }
    protected void actOnTestResult(TestResultHolder result, JUnitTest test,
                                   String name) {
        boolean fatal = result.timedOut || result.crashed;
        boolean errorOccurredHere =
            result.exitCode == JUnitTaskMirror.JUnitTestRunnerMirror.ERRORS || fatal;
        boolean failureOccurredHere =
            result.exitCode != JUnitTaskMirror.JUnitTestRunnerMirror.SUCCESS || fatal;
        if (errorOccurredHere || failureOccurredHere) {
            if ((errorOccurredHere && test.getHaltonerror())
                || (failureOccurredHere && test.getHaltonfailure())) {
                throw new BuildException(name + " failed"
                    + (result.timedOut ? " (timeout)" : "")
                    + (result.crashed ? " (crashed)" : ""), getLocation());
            } else {
                if (logFailedTests) {
                    log(name + " FAILED"
                        + (result.timedOut ? " (timeout)" : "")
                        + (result.crashed ? " (crashed)" : ""),
                        Project.MSG_ERR);
                }
                if (errorOccurredHere && test.getErrorProperty() != null) {
                    getProject().setNewProperty(test.getErrorProperty(), "true");
                }
                if (failureOccurredHere && test.getFailureProperty() != null) {
                    getProject().setNewProperty(test.getFailureProperty(), "true");
                }
            }
        }
    }
    protected static class TestResultHolder {
        public int exitCode = JUnitTaskMirror.JUnitTestRunnerMirror.ERRORS;
        public boolean timedOut = false;
        public boolean crashed = false;
    }
    protected static class JUnitLogOutputStream extends LogOutputStream {
        private Task task; 
        public JUnitLogOutputStream(Task task, int level) {
            super(task, level);
            this.task = task;
        }
        protected void processLine(String line, int level) {
            if (line.startsWith(TESTLISTENER_PREFIX)) {
                task.log(line, Project.MSG_VERBOSE);
            } else {
                super.processLine(line, level);
            }
        }
    }
    protected static class JUnitLogStreamHandler extends PumpStreamHandler {
        public JUnitLogStreamHandler(Task task, int outlevel, int errlevel) {
            super(new JUnitLogOutputStream(task, outlevel),
                  new LogOutputStream(task, errlevel));
        }
    }
    static final String NAME_OF_DUMMY_TEST = "Batch-With-Multiple-Tests";
    private static JUnitTest createDummyTestForBatchTest(JUnitTest test) {
        JUnitTest t = (JUnitTest) test.clone();
        int index = test.getName().lastIndexOf('.');
        String pack = index > 0 ? test.getName().substring(0, index + 1) : "";
        t.setName(pack + NAME_OF_DUMMY_TEST);
        return t;
    }
    private static void printDual(BufferedWriter w, PrintStream s, String text)
        throws IOException {
        w.write(String.valueOf(text));
        s.print(text);
    }
    private static void printlnDual(BufferedWriter w, PrintStream s, String text)
        throws IOException {
        w.write(String.valueOf(text));
        w.newLine();
        s.println(text);
    }
}
