package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.*;
import junit.framework.*;
import org.apache.tools.ant.BuildException;
public class JUnitTestRunnerTest extends TestCase {
    public JUnitTestRunnerTest(String name){
        super(name);
    }
    public void testValidMethod(){
        TestRunner runner = createRunnerForTestMethod(ValidMethodTestCase.class,"testA");
        runner.run();
        assertEquals(runner.getFormatter().getError(), JUnitTestRunner.SUCCESS, runner.getRetCode());
    }
    public void testInvalidMethod(){
        TestRunner runner = createRunnerForTestMethod(InvalidMethodTestCase.class,"testInvalid");
        runner.run();
        String error = runner.getFormatter().getError();
        assertTrue(error, runner.getRetCode() != JUnitTestRunner.SUCCESS);
    }    
    public void testNoSuite(){
        TestRunner runner = createRunner(NoSuiteTestCase.class);
        runner.run();
        assertEquals(runner.getFormatter().getError(), JUnitTestRunner.SUCCESS, runner.getRetCode());
    }
    public void testSuite(){
        TestRunner runner = createRunner(SuiteTestCase.class);
        runner.run();
        assertEquals(runner.getFormatter().getError(), JUnitTestRunner.SUCCESS, runner.getRetCode());
    }
    public void testInvalidSuite(){
        TestRunner runner = createRunner(InvalidSuiteTestCase.class);
        runner.run();
        String error = runner.getFormatter().getError();
        assertEquals(error, JUnitTestRunner.ERRORS, runner.getRetCode());
        assertTrue(error, error.indexOf("thrown on purpose") != -1);
    }
    public void testNoTestCase(){
        TestRunner runner = createRunner(NoTestCase.class);
        runner.run();
        int ret = runner.getRetCode();
        if (ret != JUnitTestRunner.FAILURES && ret != JUnitTestRunner.ERRORS) {
            fail("Unexpected result " + ret + " from junit runner");
        }
    }
    public void testInvalidTestCase(){
        TestRunner runner = createRunner(InvalidTestCase.class);
        runner.run();
        int ret = runner.getRetCode();
        if (ret != JUnitTestRunner.FAILURES && ret != JUnitTestRunner.ERRORS) {
            fail("Unexpected result " + ret + " from junit runner");
        }
    }
    protected TestRunner createRunner(Class clazz){
        return new TestRunner(new JUnitTest(clazz.getName()), null, 
                                            true, true, true);
    }
    protected TestRunner createRunnerForTestMethod(Class clazz, String method){
        return new TestRunner(new JUnitTest(clazz.getName()), new String[] {method},
                                            true, true, true);
    }    
    private final static class TestRunner extends JUnitTestRunner {
        private ResultFormatter formatter = new ResultFormatter();
        TestRunner(JUnitTest test, String[] methods, boolean haltonerror,
                   boolean filtertrace, boolean haltonfailure){
            super(test, methods, haltonerror, filtertrace,  haltonfailure, 
                  false, false, TestRunner.class.getClassLoader());
            addFormatter(formatter);
        }
        ResultFormatter getFormatter(){
            return formatter;
        }
    }
    private final static class ResultFormatter implements JUnitResultFormatter {
        private Throwable error;
        public void setSystemOutput(String output){}
        public void setSystemError(String output){}
        public void startTestSuite(JUnitTest suite) throws BuildException{}
        public void endTestSuite(JUnitTest suite) throws BuildException{}
        public void setOutput(java.io.OutputStream out){}
        public void startTest(Test t) {}
        public void endTest(Test test) {}
        public void addFailure(Test test, Throwable t) { }
        public void addFailure(Test test, AssertionFailedError t) { }
        public void addError(Test test, Throwable t) {
            error = t;
        }
        String getError(){
            if (error == null){
                return "";
            }
            StringWriter sw = new StringWriter();
            error.printStackTrace(new PrintWriter(sw));
            return sw.toString();
        }
    }
    public static class NoTestCase {
    }
    public static class InvalidMethodTestCase extends TestCase {
        public InvalidMethodTestCase(String name){ super(name); }
        public void testA(){
            throw new NullPointerException("thrown on purpose");
        }
    }
    public static class ValidMethodTestCase extends TestCase {
        public ValidMethodTestCase(String name){ super(name); }
        public void testA(){
        }
        public void testB(){
            throw new NullPointerException("thrown on purpose");
        }
    }    
    public static class InvalidTestCase extends TestCase {
        public InvalidTestCase(String name){
            super(name);
            throw new NullPointerException("thrown on purpose");
        }
    }
    public static class NoSuiteTestCase extends TestCase {
        public NoSuiteTestCase(String name){ super(name); }
        public void testA(){}
    }
    public static class SuiteTestCase extends NoSuiteTestCase {
        public SuiteTestCase(String name){ super(name); }
        public static Test suite(){
            return new TestSuite(SuiteTestCase.class);
        }
    }
    public static class InvalidSuiteTestCase extends NoSuiteTestCase {
        public InvalidSuiteTestCase(String name){ super(name); }
        public static Test suite(){
            throw new NullPointerException("thrown on purpose");
        }
    }
    public static void main(String[] args){
        junit.textui.TestRunner.run(JUnitTestRunnerTest.class);
    }
}
