package org.apache.tools.ant.taskdefs.optional.junit;
import java.util.Iterator;
import java.util.List;
import junit.framework.JUnit4TestAdapterCache;
import junit.framework.Test;
import junit.framework.TestResult;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
public class JUnit4TestMethodAdapter implements Test {
    private final Class testClass;
    private final String[] methodNames;
    private final Runner runner;
    private final Cache cache;
    public JUnit4TestMethodAdapter(final Class testClass,
                                   final String[] methodNames) {
        if (testClass == null) {
            throw new IllegalArgumentException("testClass is <null>");
        }
        if (methodNames == null) {
            throw new IllegalArgumentException("methodNames is <null>");
        }
        for (int i = 0; i < methodNames.length; i++) {
            if (methodNames[i] == null) {
                throw new IllegalArgumentException("method name #" + i + " is <null>");
            }
            if (methodNames[i].length() == 0) {
                throw new IllegalArgumentException("method name #" + i + " is empty");
            }
        }
        this.testClass = testClass;
        this.methodNames = (String[]) methodNames.clone();
        this.cache = Cache.instance;
        Request request;
        if (methodNames.length == 1) {
            request = Request.method(testClass, methodNames[0]);
        } else {
            request = Request.aClass(testClass).filterWith(
                            new MultipleMethodsFilter(testClass, methodNames));
        }
        runner = request.getRunner();
    }
    public int countTestCases() {
        return runner.testCount();
    }
    public Description getDescription() {
        return runner.getDescription();
    }
    public List getTests() {
        return cache.asTestList(getDescription());
    }
    public Class getTestClass() {
        return testClass;
    }
    public void run(final TestResult result) {
        runner.run(cache.getNotifier(result));
    }
    public String toString() {
        String testClassName = testClass.getName();
        StringBuilder buf = new StringBuilder(testClassName.length()
                                              + 12 * methodNames.length)
                            .append(':');
        if (methodNames.length != 0) {
            buf.append(methodNames[0]);
            for (int i = 1; i < methodNames.length; i++) {
                buf.append(',')
                   .append(methodNames[i]);
            }
        }
        return buf.toString();
    }
    private static final class MultipleMethodsFilter extends Filter {
        private final Description methodsListDescription;
        private final Class testClass;
        private final String[] methodNames;
        private MultipleMethodsFilter(Class testClass, String[] methodNames) {
            if (testClass == null) {
                throw new IllegalArgumentException("testClass is <null>");
            }
            if (methodNames == null) {
                throw new IllegalArgumentException("methodNames is <null>");
            }
            methodsListDescription = Description.createSuiteDescription(testClass);
            for (int i = 0; i < methodNames.length; i++) {
                methodsListDescription.addChild(
                        Description.createTestDescription(testClass, methodNames[i]));
            }
            this.testClass = testClass;
            this.methodNames = methodNames;
        }
        public boolean shouldRun(Description description) {
            if (methodNames.length == 0) {
                return false;
            }
            if (description.isTest()) {
                Iterator it = methodsListDescription.getChildren().iterator();
                while (it.hasNext()) {
                    Description methodDescription = (Description) it.next();
                    if (methodDescription.equals(description)) {
                        return true;
                    }
                }
            } else {
                Iterator it = description.getChildren().iterator();
                while (it.hasNext()) {
                    Description each = (Description) it.next();
                    if (shouldRun(each)) {
                        return true;
                    }
                }
            }
            return false;					
        }
        public String describe() {
            StringBuilder buf = new StringBuilder(40);
            if (methodNames.length == 0) {
                buf.append("No methods");
            } else {
                buf.append(methodNames.length == 1 ? "Method" : "Methods");
                buf.append(' ');
                buf.append(methodNames[0]);
                for (int i = 1; i < methodNames.length; i++) {
                    buf.append(',').append(methodNames[i]);
                }
            }
            buf.append('(').append(testClass.getName()).append(')');
            return buf.toString();
        }
    }
    private static final class Cache extends JUnit4TestAdapterCache {
        private static final long serialVersionUID = 8454901854293461610L;
	private static final Cache instance = new Cache();
	public static JUnit4TestAdapterCache getDefault() {
            return instance;
	}
	public RunNotifier getNotifier(final TestResult result) {
            RunNotifier notifier = new RunNotifier();
            notifier.addListener(new RunListener() {
                    public void testFailure(Failure failure) throws Exception {
                        result.addError(asTest(failure.getDescription()),
                                        failure.getException());
                    }
                    public void testFinished(Description description)
                                    throws Exception {
                        result.endTest(asTest(description));
                    }
                    public void testStarted(Description description)
                                    throws Exception {
                        result.startTest(asTest(description));
                    }
            });
            return notifier;
	}
    }
}
