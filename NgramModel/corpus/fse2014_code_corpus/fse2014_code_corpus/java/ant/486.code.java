package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.OutputStream;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import junit.framework.TestResult;
import org.apache.tools.ant.AntClassLoader;
public final class JUnitTaskMirrorImpl implements JUnitTaskMirror {
    private final JUnitTask task;
    public JUnitTaskMirrorImpl(JUnitTask task) {
        this.task = task;
    }
    public void addVmExit(JUnitTest test, JUnitTaskMirror.JUnitResultFormatterMirror aFormatter,
            OutputStream out, String message, String testCase) {
        JUnitResultFormatter formatter = (JUnitResultFormatter) aFormatter;
        formatter.setOutput(out);
        formatter.startTestSuite(test);
        TestCase t = new VmExitErrorTest(message, test, testCase);
        formatter.startTest(t);
        formatter.addError(t, new AssertionFailedError(message));
        formatter.endTestSuite(test);
    }
    public JUnitTaskMirror.JUnitTestRunnerMirror newJUnitTestRunner(JUnitTest test,
            String[] methods,
            boolean haltOnError, boolean filterTrace, boolean haltOnFailure,
            boolean showOutput, boolean logTestListenerEvents, AntClassLoader classLoader) {
        return new JUnitTestRunner(test, methods, haltOnError, filterTrace, haltOnFailure,
                showOutput, logTestListenerEvents, classLoader);
    }
    public JUnitTaskMirror.SummaryJUnitResultFormatterMirror newSummaryJUnitResultFormatter() {
        return new SummaryJUnitResultFormatter();
    }
    static class VmExitErrorTest extends TestCase {
        private String message;
        private JUnitTest test;
        private String testCase;
        VmExitErrorTest(String aMessage, JUnitTest anOriginalTest, String aTestCase) {
            message = aMessage;
            test = anOriginalTest;
            testCase = aTestCase;
        }
        public int countTestCases() {
            return 1;
        }
        public void run(TestResult r) {
            throw new AssertionFailedError(message);
        }
        public String getName() {
            return testCase;
        }
        String getClassName() {
            return test.getName();
        }
        public String toString() {
            return test.getName() + ":" + testCase;
        }
    }
}
