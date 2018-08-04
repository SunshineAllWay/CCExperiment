package org.apache.tools.ant.taskdefs.optional.junit;
import java.io.IOException;
import java.io.OutputStream;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Permissions;
public interface JUnitTaskMirror {
    void addVmExit(JUnitTest test, JUnitResultFormatterMirror formatter,
            OutputStream out, String message, String testCase);
    JUnitTestRunnerMirror newJUnitTestRunner(JUnitTest test, String[] methods, boolean haltOnError,
            boolean filterTrace, boolean haltOnFailure, boolean showOutput,
            boolean logTestListenerEvents, AntClassLoader classLoader);
    SummaryJUnitResultFormatterMirror newSummaryJUnitResultFormatter();
    public interface JUnitResultFormatterMirror {
        void setOutput(OutputStream outputStream);
    }
    public interface SummaryJUnitResultFormatterMirror
        extends JUnitResultFormatterMirror {
        void setWithOutAndErr(boolean value);
    }
    public interface JUnitTestRunnerMirror {
        String IGNORED_FILE_NAME = "IGNORETHIS";
        int SUCCESS = 0;
        int FAILURES = 1;
        int ERRORS = 2;
        void setPermissions(Permissions perm);
        void run();
        void addFormatter(JUnitResultFormatterMirror formatter);
        int getRetCode();
        void handleErrorFlush(String output);
        void handleErrorOutput(String output);
        void handleOutput(String output);
        int handleInput(byte[] buffer, int offset, int length) throws IOException;
       void handleFlush(String output);
    }
}