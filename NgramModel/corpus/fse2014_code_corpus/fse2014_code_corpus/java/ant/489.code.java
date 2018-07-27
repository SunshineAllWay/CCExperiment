package org.apache.tools.ant.taskdefs.optional.junit;
import java.lang.reflect.Method;
import junit.framework.Test;
import junit.framework.TestCase;
public class JUnitVersionHelper {
    private static Method testCaseName = null;
    public static final String JUNIT_FRAMEWORK_JUNIT4_TEST_CASE_FACADE
        = "junit.framework.JUnit4TestCaseFacade";
    private static final String UNKNOWN_TEST_CASE_NAME = "unknown";
    static {
        try {
            testCaseName = TestCase.class.getMethod("getName", new Class[0]);
        } catch (NoSuchMethodException e) {
            try {
                testCaseName = TestCase.class.getMethod("name", new Class[0]);
            } catch (NoSuchMethodException ignored) {
            }
        }
    }
    public static String getTestCaseName(Test t) {
        if (t == null) {
            return UNKNOWN_TEST_CASE_NAME;
        }
        if (t.getClass().getName().equals(JUNIT_FRAMEWORK_JUNIT4_TEST_CASE_FACADE)) {
            String name = t.toString();
            if (name.endsWith(")")) {
                int paren = name.lastIndexOf('(');
                return name.substring(0, paren);
            } else {
                return name;
            }
        }
        if (t instanceof TestCase && testCaseName != null) {
            try {
                return (String) testCaseName.invoke(t, new Object[0]);
            } catch (Throwable ignored) {
            }
        } else {
            try {
                Method getNameMethod = null;
                try {
                    getNameMethod =
                        t.getClass().getMethod("getName", new Class [0]);
                } catch (NoSuchMethodException e) {
                    getNameMethod = t.getClass().getMethod("name",
                                                           new Class [0]);
                }
                if (getNameMethod != null
                    && getNameMethod.getReturnType() == String.class) {
                    return (String) getNameMethod.invoke(t, new Object[0]);
                }
            } catch (Throwable ignored) {
            }
        }
        return UNKNOWN_TEST_CASE_NAME;
    }
    public static String getTestCaseClassName(Test test) {
        String className = test.getClass().getName();
        if (test instanceof JUnitTaskMirrorImpl.VmExitErrorTest) {
            className = ((JUnitTaskMirrorImpl.VmExitErrorTest) test).getClassName();
        } else
        if (className.equals(JUNIT_FRAMEWORK_JUNIT4_TEST_CASE_FACADE)) {
            String name = test.toString();
            int paren = name.lastIndexOf('(');
            if (paren != -1 && name.endsWith(")")) {
                className = name.substring(paren + 1, name.length() - 1);
            }
        }
        return className;
    }
}
