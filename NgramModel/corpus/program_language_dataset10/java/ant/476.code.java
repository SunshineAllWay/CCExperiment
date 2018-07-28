package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.NumberFormat;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
public class BriefJUnitResultFormatter implements JUnitResultFormatter {
    private static final double ONE_SECOND = 1000.0;
    private OutputStream out;
    private BufferedWriter output;
    private StringWriter results;
    private BufferedWriter resultWriter;
    private NumberFormat numberFormat = NumberFormat.getInstance();
    private String systemOutput = null;
    private String systemError = null;
    public BriefJUnitResultFormatter() {
        results = new StringWriter();
        resultWriter = new BufferedWriter(results);
    }
    public void setOutput(OutputStream out) {
        this.out = out;
        output = new BufferedWriter(new java.io.OutputStreamWriter(out));
    }
    public void setSystemOutput(String out) {
        systemOutput = out;
    }
    public void setSystemError(String err) {
        systemError = err;
    }
    public void startTestSuite(JUnitTest suite) {
        if (output == null) {
            return; 
        }
        StringBuffer sb = new StringBuffer("Testsuite: ");
        sb.append(suite.getName());
        sb.append(StringUtils.LINE_SEP);
        try {
            output.write(sb.toString());
            output.flush();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
    public void endTestSuite(JUnitTest suite) {
        StringBuffer sb = new StringBuffer("Tests run: ");
        sb.append(suite.runCount());
        sb.append(", Failures: ");
        sb.append(suite.failureCount());
        sb.append(", Errors: ");
        sb.append(suite.errorCount());
        sb.append(", Time elapsed: ");
        sb.append(numberFormat.format(suite.getRunTime() / ONE_SECOND));
        sb.append(" sec");
        sb.append(StringUtils.LINE_SEP);
        sb.append(StringUtils.LINE_SEP);
        if (systemOutput != null && systemOutput.length() > 0) {
            sb.append("------------- Standard Output ---------------")
                    .append(StringUtils.LINE_SEP)
                    .append(systemOutput)
                    .append("------------- ---------------- ---------------")
                    .append(StringUtils.LINE_SEP);
        }
        if (systemError != null && systemError.length() > 0) {
            sb.append("------------- Standard Error -----------------")
                    .append(StringUtils.LINE_SEP)
                    .append(systemError)
                    .append("------------- ---------------- ---------------")
                    .append(StringUtils.LINE_SEP);
        }
        if (output != null) {
            try {
                output.write(sb.toString());
                resultWriter.close();
                output.write(results.toString());
            } catch (IOException ex) {
                throw new BuildException(ex);
            } finally {
                try {
                    output.flush();
                } catch (IOException ex) {
                }
                if (out != System.out && out != System.err) {
                    FileUtils.close(out);
                }
            }
        }
    }
    public void startTest(Test test) {
    }
    public void endTest(Test test) {
    }
    public void addFailure(Test test, Throwable t) {
        formatError("\tFAILED", test, t);
    }
    public void addFailure(Test test, AssertionFailedError t) {
        addFailure(test, (Throwable) t);
    }
    public void addError(Test test, Throwable error) {
        formatError("\tCaused an ERROR", test, error);
    }
    protected String formatTest(Test test) {
        if (test == null) {
            return "Null Test: ";
        } else {
            return "Testcase: " + test.toString() + ":";
        }
    }
    protected synchronized void formatError(String type, Test test,
                                            Throwable error) {
        if (test != null) {
            endTest(test);
        }
        try {
            resultWriter.write(formatTest(test) + type);
            resultWriter.newLine();
            resultWriter.write(String.valueOf(error.getMessage()));
            resultWriter.newLine();
            String strace = JUnitTestRunner.getFilteredTrace(error);
            resultWriter.write(strace);
            resultWriter.newLine();
            resultWriter.newLine();
        } catch (IOException ex) {
            throw new BuildException(ex);
        }
    }
}
