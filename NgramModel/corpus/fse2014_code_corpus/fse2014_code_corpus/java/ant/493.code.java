package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
public class TearDownOnVmCrash implements JUnitResultFormatter {
    private String suiteName;
    public void startTestSuite(final JUnitTest suite) {
        suiteName = suite.getName();
        if (suiteName != null &&
            suiteName.endsWith(JUnitTask.NAME_OF_DUMMY_TEST)) {
            suiteName = null;
        }
    }
    public void addError(final Test fakeTest, final Throwable t) {
        if (suiteName != null
            && fakeTest instanceof JUnitTaskMirrorImpl.VmExitErrorTest) {
            tearDown();
        }
    }
    public void addFailure(Test test, Throwable t) {}
    public void addFailure(Test test, AssertionFailedError t) {}
    public void startTest(Test test) {}
    public void endTest(Test test) {}
    public void endTestSuite(JUnitTest suite) {}
    public void setOutput(OutputStream out) {}
    public void setSystemOutput(String out) {}
    public void setSystemError(String err) {}
    private void tearDown() {
        try {
            Class testClass = null;
            if (Thread.currentThread().getContextClassLoader() != null) {
                try {
                    testClass = Thread.currentThread().getContextClassLoader()
                        .loadClass(suiteName);
                } catch (ClassNotFoundException cnfe) {
                }
            }
            if (testClass == null && getClass().getClassLoader() != null) {
                try {
                    testClass =
                        getClass().getClassLoader().loadClass(suiteName);
                } catch (ClassNotFoundException cnfe) {
                }
            }
            if (testClass == null) {
                testClass = Class.forName(suiteName);
            }
            try {
                testClass.getMethod("suite", new Class[0]);
                return;
            } catch (NoSuchMethodException e) {
            }
            try {
                Method td = testClass.getMethod("tearDown", new Class[0]);
                if (td.getReturnType() == Void.TYPE) {
                    td.invoke(testClass.newInstance(), new Object[0]);
                }
            } catch (NoSuchMethodException nsme) {
            }
        } catch (ClassNotFoundException cnfe) {
        } catch (InvocationTargetException ite) {
            System.err.println("Caught an exception while trying to invoke"
                               + " tearDown: " + ite.getMessage());
        } catch (Throwable t) {
            System.err.println("Caught an exception while trying to invoke"
                               + " tearDown: " + t.getMessage());
        }
    }
}