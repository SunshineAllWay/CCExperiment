package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestFailure;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Permissions;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.TeeOutputStream;
public class JUnitTestRunner implements TestListener, JUnitTaskMirror.JUnitTestRunnerMirror {
    private Vector formatters = new Vector();
    private TestResult res;
    private static boolean filtertrace = true;
    private boolean showOutput = false;
    private boolean outputToFormatters = true;
    private Permissions perm = null;
    private static final String JUNIT_4_TEST_ADAPTER
        = "junit.framework.JUnit4TestAdapter";
    private static final String[] DEFAULT_TRACE_FILTERS = new String[] {
                "junit.framework.TestCase",
                "junit.framework.TestResult",
                "junit.framework.TestSuite",
                "junit.framework.Assert.", 
                "junit.swingui.TestRunner",
                "junit.awtui.TestRunner",
                "junit.textui.TestRunner",
                "java.lang.reflect.Method.invoke(",
                "sun.reflect.",
                "org.apache.tools.ant.",
                "org.junit.",
                "junit.framework.JUnit4TestAdapter",
                " more",
        };
    private boolean haltOnError = false;
    private boolean haltOnFailure = false;
    private int retCode = SUCCESS;
    private JUnitTest junitTest;
    private PrintStream systemError;
    private PrintStream systemOut;
    private boolean forked = false;
    private static boolean multipleTests = false;
    private ClassLoader loader;
    private boolean logTestListenerEvents = false;
    private boolean junit4;
    private static String crashFile = null;
    private String[] methods = null;
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure) {
        this(test, haltOnError, filtertrace, haltOnFailure, false);
    }
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput) {
        this(test, haltOnError, filtertrace, haltOnFailure, showOutput, false);
    }
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput, boolean logTestListenerEvents) {
        this(test, null, haltOnError, filtertrace, haltOnFailure, showOutput,
             logTestListenerEvents, null);
    }
    public JUnitTestRunner(JUnitTest test, String[] methods, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput, boolean logTestListenerEvents) {
        this(test, methods, haltOnError, filtertrace, haltOnFailure, showOutput,
             logTestListenerEvents, null);
    }
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           ClassLoader loader) {
        this(test, haltOnError, filtertrace, haltOnFailure, false, loader);
    }
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput, ClassLoader loader) {
        this(test, haltOnError, filtertrace, haltOnFailure, showOutput,
             false, loader);
    }
    public JUnitTestRunner(JUnitTest test, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput, boolean logTestListenerEvents,
                           ClassLoader loader) {
        this(test, null, haltOnError, filtertrace, haltOnFailure, showOutput, 
             logTestListenerEvents, loader);
    }
    public JUnitTestRunner(JUnitTest test, String[] methods, boolean haltOnError,
                           boolean filtertrace, boolean haltOnFailure,
                           boolean showOutput, boolean logTestListenerEvents,
                           ClassLoader loader) {
        JUnitTestRunner.filtertrace = filtertrace; 
        this.junitTest = test;
        this.haltOnError = haltOnError;
        this.haltOnFailure = haltOnFailure;
        this.showOutput = showOutput;
        this.logTestListenerEvents = logTestListenerEvents;
        this.methods = methods != null ? (String[]) methods.clone() : null;
        this.loader = loader;
    }
    private PrintStream savedOut = null;
    private PrintStream savedErr = null;
    private PrintStream createEmptyStream() {
        return new PrintStream(
            new OutputStream() {
                public void write(int b) {
                }
            });
    }
    private PrintStream createTeePrint(PrintStream ps1, PrintStream ps2) {
        return new PrintStream(new TeeOutputStream(ps1, ps2));
    }
    private void setupIOStreams(ByteArrayOutputStream o,
                                ByteArrayOutputStream e) {
        systemOut = new PrintStream(o);
        systemError = new PrintStream(e);
        if (forked) {
            if (!outputToFormatters) {
                if (!showOutput) {
                    savedOut = System.out;
                    savedErr = System.err;
                    System.setOut(createEmptyStream());
                    System.setErr(createEmptyStream());
                }
            } else {
                savedOut = System.out;
                savedErr = System.err;
                if (!showOutput) {
                    System.setOut(systemOut);
                    System.setErr(systemError);
                } else {
                    System.setOut(createTeePrint(savedOut, systemOut));
                    System.setErr(createTeePrint(savedErr, systemError));
                }
                perm = null;
            }
        } else {
            if (perm != null) {
                perm.setSecurityManager();
            }
        }
    }
    public void run() {
        res = new TestResult();
        res.addListener(wrapListener(this));
        for (int i = 0; i < formatters.size(); i++) {
            res.addListener(wrapListener((TestListener) formatters.elementAt(i)));
        }
        ByteArrayOutputStream errStrm = new ByteArrayOutputStream();
        ByteArrayOutputStream outStrm = new ByteArrayOutputStream();
        setupIOStreams(outStrm, errStrm);
        Test suite = null;
        Throwable exception = null;
        boolean startTestSuiteSuccess = false;
        try {
            try {
                Class testClass = null;
                if (loader == null) {
                    testClass = Class.forName(junitTest.getName());
                } else {
                    testClass = Class.forName(junitTest.getName(), true,
                                              loader);
                }
                final boolean testMethodsSpecified = (methods != null);
                Method suiteMethod = null;
                if (!testMethodsSpecified) {
                try {
                    suiteMethod = testClass.getMethod("suite", new Class[0]);
                } catch (NoSuchMethodException e) {
                }
                }
                if (suiteMethod != null) {
                    suite = (Test) suiteMethod.invoke(null, new Class[0]);
                } else {
                    Class junit4TestAdapterClass = null;
                    boolean useSingleMethodAdapter = false;
                    if (junit.framework.TestCase.class.isAssignableFrom(testClass)) {
                    } else {
                    try {
                        Class.forName("java.lang.annotation.Annotation");
                        if (loader == null) {
                            junit4TestAdapterClass =
                                Class.forName(JUNIT_4_TEST_ADAPTER);
                            if (testMethodsSpecified) {
                                junit4TestAdapterClass = Class.forName(
                                    "org.apache.tools.ant.taskdefs.optional.junit.JUnit4TestMethodAdapter");
                                useSingleMethodAdapter = true;
                            }
                        } else {
                            junit4TestAdapterClass =
                                Class.forName(JUNIT_4_TEST_ADAPTER,
                                              true, loader);
                            if (testMethodsSpecified) {
                                junit4TestAdapterClass =
                                    Class.forName(
                                        "org.apache.tools.ant.taskdefs.optional.junit.JUnit4TestMethodAdapter",
                                        true, loader);
                                useSingleMethodAdapter = true;
                            }
                        }
                    } catch (ClassNotFoundException e) {
                    }
                    }
                    junit4 = junit4TestAdapterClass != null;
                    if (junit4) {
                        Class[] formalParams;
                        Object[] actualParams;
                        if (useSingleMethodAdapter) {
                            formalParams = new Class[] {Class.class, String[].class};
                            actualParams = new Object[] {testClass, methods};
                        } else {
                            formalParams = new Class[] {Class.class};
                            actualParams = new Object[] {testClass};
                        }
                        suite =
                            (Test) junit4TestAdapterClass
                            .getConstructor(formalParams).
                            newInstance(actualParams);
                    } else {
                        if (!testMethodsSpecified) {
                            suite = new TestSuite(testClass);
                        } else if (methods.length == 1) {
                            suite = TestSuite.createTest(testClass, methods[0]);
                        } else {
                            TestSuite testSuite = new TestSuite(testClass.getName());
                            for (int i = 0; i < methods.length; i++) {
                                testSuite.addTest(
                                    TestSuite.createTest(testClass, methods[i]));
                            }
                            suite = testSuite;
                        }
                    }
                }
            } catch (Throwable e) {
                retCode = ERRORS;
                exception = e;
            }
            long start = System.currentTimeMillis();
            fireStartTestSuite();
            startTestSuiteSuccess = true;
            if (exception != null) { 
                for (int i = 0; i < formatters.size(); i++) {
                    ((TestListener) formatters.elementAt(i))
                        .addError(null, exception);
                }
                junitTest.setCounts(1, 0, 1);
                junitTest.setRunTime(0);
            } else {
                try {
                    logTestListenerEvent("tests to run: " + suite.countTestCases());
                    suite.run(res);
                } finally {
                    if (junit4 ||
                        suite.getClass().getName().equals(JUNIT_4_TEST_ADAPTER)) {
                        int[] cnts = findJUnit4FailureErrorCount(res);
                        junitTest.setCounts(res.runCount(), cnts[0], cnts[1]);
                    } else {
                        junitTest.setCounts(res.runCount(), res.failureCount(),
                                res.errorCount());
                    }
                    junitTest.setRunTime(System.currentTimeMillis() - start);
                }
            }
        } finally {
            if (perm != null) {
                perm.restoreSecurityManager();
            }
            if (savedOut != null) {
                System.setOut(savedOut);
            }
            if (savedErr != null) {
                System.setErr(savedErr);
            }
            systemError.close();
            systemError = null;
            systemOut.close();
            systemOut = null;
            if (startTestSuiteSuccess) {
                sendOutAndErr(new String(outStrm.toByteArray()),
                              new String(errStrm.toByteArray()));
            }
        }
        fireEndTestSuite();
        if (retCode != SUCCESS || junitTest.errorCount() != 0) {
            retCode = ERRORS;
        } else if (junitTest.failureCount() != 0) {
            retCode = FAILURES;
        }
    }
    public int getRetCode() {
        return retCode;
    }
    public void startTest(Test t) {
        String testName = JUnitVersionHelper.getTestCaseName(t);
        logTestListenerEvent("startTest(" + testName + ")");
    }
    public void endTest(Test test) {
        String testName = JUnitVersionHelper.getTestCaseName(test);
        logTestListenerEvent("endTest(" + testName + ")");
    }
    private void logTestListenerEvent(String msg) {
        if (logTestListenerEvents) {
            PrintStream out = savedOut != null ? savedOut : System.out;
            out.flush();
            if (msg == null) {
                msg = "null";
            }
            StringTokenizer msgLines = new StringTokenizer(msg, "\r\n", false);
            while (msgLines.hasMoreTokens()) {
                out.println(JUnitTask.TESTLISTENER_PREFIX
                            + msgLines.nextToken());
            }
            out.flush();
        }
    }
    public void addFailure(Test test, Throwable t) {
        String testName = JUnitVersionHelper.getTestCaseName(test);
        logTestListenerEvent("addFailure(" + testName + ", " + t.getMessage() + ")");
        if (haltOnFailure) {
            res.stop();
        }
    }
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    public void addError(Test test, Throwable t) {
        String testName = JUnitVersionHelper.getTestCaseName(test);
        logTestListenerEvent("addError(" + testName + ", " + t.getMessage() + ")");
        if (haltOnError) {
            res.stop();
        }
    }
    public void setPermissions(Permissions permissions) {
        perm = permissions;
    }
    public void handleOutput(String output) {
        if (!logTestListenerEvents && output.startsWith(JUnitTask.TESTLISTENER_PREFIX)) {
        } else if (systemOut != null) {
            systemOut.print(output);
        }
    }
    public int handleInput(byte[] buffer, int offset, int length)
        throws IOException {
        return -1;
    }
    public void handleErrorOutput(String output) {
        if (systemError != null) {
            systemError.print(output);
        }
    }
    public void handleFlush(String output) {
        if (systemOut != null) {
            systemOut.print(output);
        }
    }
    public void handleErrorFlush(String output) {
        if (systemError != null) {
            systemError.print(output);
        }
    }
    private void sendOutAndErr(String out, String err) {
        for (int i = 0; i < formatters.size(); i++) {
            JUnitResultFormatter formatter =
                ((JUnitResultFormatter) formatters.elementAt(i));
            formatter.setSystemOutput(out);
            formatter.setSystemError(err);
        }
    }
    private void fireStartTestSuite() {
        for (int i = 0; i < formatters.size(); i++) {
            ((JUnitResultFormatter) formatters.elementAt(i))
                .startTestSuite(junitTest);
        }
    }
    private void fireEndTestSuite() {
        for (int i = 0; i < formatters.size(); i++) {
            ((JUnitResultFormatter) formatters.elementAt(i))
                .endTestSuite(junitTest);
        }
    }
    public void addFormatter(JUnitResultFormatter f) {
        formatters.addElement(f);
    }
    public void addFormatter(JUnitTaskMirror.JUnitResultFormatterMirror f) {
        formatters.addElement(f);
    }
    public static void main(String[] args) throws IOException {
        String[] methods = null;
        boolean haltError = false;
        boolean haltFail = false;
        boolean stackfilter = true;
        Properties props = new Properties();
        boolean showOut = false;
        boolean outputToFormat = true;
        boolean logFailedTests = true;
        boolean logTestListenerEvents = false;
        if (args.length == 0) {
            System.err.println("required argument TestClassName missing");
            System.exit(ERRORS);
        }
        if (args[0].startsWith(Constants.TESTSFILE)) {
            multipleTests = true;
            args[0] = args[0].substring(Constants.TESTSFILE.length());
        }
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith(Constants.METHOD_NAMES)) {
                try {
                    String methodsList = args[i].substring(Constants.METHOD_NAMES.length());
                    methods = JUnitTest.parseTestMethodNamesList(methodsList);
                } catch (IllegalArgumentException ex) {
                    System.err.println("Invalid specification of test method names: " + args[i]);
                    System.exit(ERRORS);
                }
            } else if (args[i].startsWith(Constants.HALT_ON_ERROR)) {
                haltError = Project.toBoolean(args[i].substring(Constants.HALT_ON_ERROR.length()));
            } else if (args[i].startsWith(Constants.HALT_ON_FAILURE)) {
                haltFail = Project.toBoolean(args[i].substring(Constants.HALT_ON_FAILURE.length()));
            } else if (args[i].startsWith(Constants.FILTERTRACE)) {
                stackfilter = Project.toBoolean(args[i].substring(Constants.FILTERTRACE.length()));
            } else if (args[i].startsWith(Constants.CRASHFILE)) {
                crashFile = args[i].substring(Constants.CRASHFILE.length());
                registerTestCase(Constants.BEFORE_FIRST_TEST);
            } else if (args[i].startsWith(Constants.FORMATTER)) {
                try {
                    createAndStoreFormatter(args[i].substring(Constants.FORMATTER.length()));
                } catch (BuildException be) {
                    System.err.println(be.getMessage());
                    System.exit(ERRORS);
                }
            } else if (args[i].startsWith(Constants.PROPSFILE)) {
                FileInputStream in = new FileInputStream(args[i]
                                                         .substring(Constants.PROPSFILE.length()));
                props.load(in);
                in.close();
            } else if (args[i].startsWith(Constants.SHOWOUTPUT)) {
                showOut = Project.toBoolean(args[i].substring(Constants.SHOWOUTPUT.length()));
            } else if (args[i].startsWith(Constants.LOGTESTLISTENEREVENTS)) {
                logTestListenerEvents = Project.toBoolean(
                    args[i].substring(Constants.LOGTESTLISTENEREVENTS.length()));
            } else if (args[i].startsWith(Constants.OUTPUT_TO_FORMATTERS)) {
                outputToFormat = Project.toBoolean(
                    args[i].substring(Constants.OUTPUT_TO_FORMATTERS.length()));
            } else if (args[i].startsWith(Constants.LOG_FAILED_TESTS)) {
                logFailedTests = Project.toBoolean(
                    args[i].substring(Constants.LOG_FAILED_TESTS.length()));
            }
        }
        Hashtable p = System.getProperties();
        for (Enumeration e = p.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            props.put(key, p.get(key));
        }
        int returnCode = SUCCESS;
        if (multipleTests) {
            try {
                java.io.BufferedReader reader =
                    new java.io.BufferedReader(new java.io.FileReader(args[0]));
                String testCaseName;
                String[] testMethodNames;
                int code = 0;
                boolean errorOccurred = false;
                boolean failureOccurred = false;
                String line = null;
                while ((line = reader.readLine()) != null) {
                    StringTokenizer st = new StringTokenizer(line, ",");
                    String testListSpec = st.nextToken();
                    int colonIndex = testListSpec.indexOf(':');
                    if (colonIndex == -1) {
                        testCaseName = testListSpec;
                        testMethodNames = null;
                    } else {
                        testCaseName = testListSpec.substring(0, colonIndex);
                        testMethodNames = JUnitTest.parseTestMethodNamesList(
                                                    testListSpec
                                                    .substring(colonIndex + 1)
                                                    .replace('+', ','));
                    }
                    JUnitTest t = new JUnitTest(testCaseName);
                    t.setTodir(new File(st.nextToken()));
                    t.setOutfile(st.nextToken());
                    t.setProperties(props);
                    code = launch(t, testMethodNames, haltError, stackfilter, haltFail,
                                  showOut, outputToFormat,
                                  logTestListenerEvents);
                    errorOccurred = (code == ERRORS);
                    failureOccurred = (code != SUCCESS);
                    if (errorOccurred || failureOccurred) {
                        if ((errorOccurred && haltError)
                            || (failureOccurred && haltFail)) {
                            registerNonCrash();
                            System.exit(code);
                        } else {
                            if (code > returnCode) {
                                returnCode = code;
                            }
                            if (logFailedTests) {
                                System.out.println("TEST " + t.getName()
                                                   + " FAILED");
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            JUnitTest t = new JUnitTest(args[0]);
            t.setProperties(props);
            returnCode = launch(
                t, methods, haltError, stackfilter, haltFail,
                showOut, outputToFormat, logTestListenerEvents);
        }
        registerNonCrash();
        System.exit(returnCode);
    }
    private static Vector fromCmdLine = new Vector();
    private static void transferFormatters(JUnitTestRunner runner,
                                           JUnitTest test) {
        runner.addFormatter(new JUnitResultFormatter() {
            public void startTestSuite(JUnitTest suite) throws BuildException {
            }
            public void endTestSuite(JUnitTest suite) throws BuildException {
            }
            public void setOutput(OutputStream out) {
            }
            public void setSystemOutput(String out) {
            }
            public void setSystemError(String err) {
            }
            public void addError(Test arg0, Throwable arg1) {
            }
            public void addFailure(Test arg0, AssertionFailedError arg1) {
            }
            public void endTest(Test arg0) {
            }
            public void startTest(Test arg0) {
                registerTestCase(JUnitVersionHelper.getTestCaseName(arg0));
            }
        });
        for (int i = 0; i < fromCmdLine.size(); i++) {
            FormatterElement fe = (FormatterElement) fromCmdLine.elementAt(i);
            if (multipleTests && fe.getUseFile()) {
                File destFile =
                    new File(test.getTodir(),
                             test.getOutfile() + fe.getExtension());
                fe.setOutfile(destFile);
            }
            runner.addFormatter((JUnitResultFormatter) fe.createFormatter());
        }
    }
    private static void createAndStoreFormatter(String line)
        throws BuildException {
        FormatterElement fe = new FormatterElement();
        int pos = line.indexOf(',');
        if (pos == -1) {
            fe.setClassname(line);
            fe.setUseFile(false);
        } else {
            fe.setClassname(line.substring(0, pos));
            fe.setUseFile(true);
            if (!multipleTests) {
                fe.setOutfile(new File(line.substring(pos + 1)));
            } else {
                int fName = line.indexOf(IGNORED_FILE_NAME);
                if (fName > -1) {
                    fe.setExtension(line
                                    .substring(fName
                                               + IGNORED_FILE_NAME.length()));
                }
            }
        }
        fromCmdLine.addElement(fe);
    }
    public static String getFilteredTrace(Throwable t) {
        String trace = StringUtils.getStackTrace(t);
        return JUnitTestRunner.filterStack(trace);
    }
    public static String filterStack(String stack) {
        if (!filtertrace) {
            return stack;
        }
        StringWriter sw = new StringWriter();
        BufferedWriter pw = new BufferedWriter(sw);
        StringReader sr = new StringReader(stack);
        BufferedReader br = new BufferedReader(sr);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                if (!filterLine(line)) {
                    pw.write(line);
                    pw.newLine();
                }
            }
        } catch (Exception e) {
            return stack; 
        } finally {
            FileUtils.close(pw);
        }
        return sw.toString();
    }
    private static boolean filterLine(String line) {
        for (int i = 0; i < DEFAULT_TRACE_FILTERS.length; i++) {
            if (line.indexOf(DEFAULT_TRACE_FILTERS[i]) != -1) {
                return true;
            }
        }
        return false;
    }
    private static int launch(JUnitTest t, String[] methods, boolean haltError,
                              boolean stackfilter, boolean haltFail,
                              boolean showOut, boolean outputToFormat,
                              boolean logTestListenerEvents) {
        JUnitTestRunner runner =
            new JUnitTestRunner(t, methods, haltError, stackfilter, haltFail, showOut,
                                logTestListenerEvents, null);
        runner.forked = true;
        runner.outputToFormatters = outputToFormat;
        transferFormatters(runner, t);
        runner.run();
        return runner.getRetCode();
     }
    private static void registerNonCrash()
            throws IOException {
        if (crashFile != null) {
            FileWriter out = null;
            try {
                out = new FileWriter(crashFile);
                out.write(Constants.TERMINATED_SUCCESSFULLY + "\n");
                out.flush();
            } finally {
                FileUtils.close(out);
            }
        }
    }
    private static void registerTestCase(String testCase) {
        if (crashFile != null) {
            try {
                FileWriter out = null;
                try {
                    out = new FileWriter(crashFile);
                    out.write(testCase + "\n");
                    out.flush();
                } finally {
                    FileUtils.close(out);
                }
            } catch (IOException e) {
            }
        }
    }
    private TestListener wrapListener(final TestListener testListener) {
        return new TestListener() {
            public void addError(Test test, Throwable t) {
                if (junit4 && t instanceof AssertionFailedError) {
                    testListener.addFailure(test, (AssertionFailedError) t);
                } else if (junit4 && t instanceof  AssertionError) {
                    String msg = t.getMessage();
                    AssertionFailedError failure = msg != null
                        ? new AssertionFailedError(msg) : new AssertionFailedError();
                    failure.setStackTrace(t.getStackTrace());
                    testListener.addFailure(test, failure);
                } else {
                    testListener.addError(test, t);
                }
            }
            public void addFailure(Test test, AssertionFailedError t) {
                testListener.addFailure(test, t);
            }
            public void addFailure(Test test, Throwable t) { 
                if (t instanceof AssertionFailedError) {
                    testListener.addFailure(test, (AssertionFailedError) t);
                } else {
                    testListener.addError(test, t);
                }
            }
            public void endTest(Test test) {
                testListener.endTest(test);
            }
            public void startTest(Test test) {
                testListener.startTest(test);
            }
        };
    }
    private int[] findJUnit4FailureErrorCount(TestResult result) {
        int failures = 0;
        int errors = 0;
        Enumeration e = result.failures();
        while (e.hasMoreElements()) {
            e.nextElement();
            failures++;
        }
        e = result.errors();
        while (e.hasMoreElements()) {
            Throwable t = ((TestFailure) e.nextElement()).thrownException();
            if (t instanceof AssertionFailedError
                || t instanceof AssertionError) {
                failures++;
            } else {
                errors++;
            }
        }
        return new int[] {failures, errors};
    }
} 
