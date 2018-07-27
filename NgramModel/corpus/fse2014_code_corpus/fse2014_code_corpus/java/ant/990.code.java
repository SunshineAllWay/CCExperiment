package org.apache.tools.ant.taskdefs;
import java.io.PrintStream;
import junit.framework.AssertionFailedError;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.DemuxOutputStream;
import org.apache.tools.ant.Project;
public class ParallelTest extends BuildFileTest {
    public final static String DIRECT_MESSAGE = "direct";
    public final static String DELAYED_MESSAGE = "delayed";
    public final static String FAILURE_MESSAGE = "failure";
    public final static String TEST_BUILD_FILE
         = "src/etc/testcases/taskdefs/parallel.xml";
    public ParallelTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TEST_BUILD_FILE);
    }
    public void testBasic() {
        Project p = getProject();
        p.setUserProperty("test.direct", DIRECT_MESSAGE);
        p.setUserProperty("test.delayed", DELAYED_MESSAGE);
        expectOutputAndError("testBasic", "", "");
        String log = getLog();
        assertEquals("parallel tasks didn't output correct data", log,
            DIRECT_MESSAGE + DELAYED_MESSAGE);
    }
    public void testThreadCount() {
        Project p = getProject();
        p.setUserProperty("test.direct", DIRECT_MESSAGE);
        p.setUserProperty("test.delayed", DELAYED_MESSAGE);
        expectOutputAndError("testThreadCount", "", "");
        String log = getLog();
        int pos = 0;
        while (pos > -1) {
            pos = countThreads(log, pos);
        }
    }
    static int countThreads(String s, int start) {
        int firstPipe = s.indexOf('|', start);
        int beginSlash = s.indexOf('/', firstPipe);
        int lastPipe = s.indexOf('|', beginSlash);
        if ((firstPipe == -1) || (beginSlash == -1) || (lastPipe == -1)) {
            return -1;
        }
        int max = Integer.parseInt(s.substring(firstPipe + 1, beginSlash));
        int current = 0;
        int pos = beginSlash + 1;
        while (pos < lastPipe) {
            switch (s.charAt(pos++)) {
                case '+':
                    current++;
                    break;
                case '-':
                    current--;
                    break;
                default:
                    throw new AssertionFailedError("Only expect '+-' in result count, found "
                        + s.charAt(--pos) + " at position " + pos);
            }
            if (current > max) {
                throw new AssertionFailedError("Number of executing threads exceeded number allowed: "
                    + current + " > " + max);
            }
        }
        return lastPipe;
    }
    public void testFail() {
        Project p = getProject();
        p.setUserProperty("test.failure", FAILURE_MESSAGE);
        p.setUserProperty("test.delayed", DELAYED_MESSAGE);
        expectBuildExceptionContaining("testFail",
            "fail task in one parallel branch", FAILURE_MESSAGE);
    }
    public void testDemux() {
        Project p = getProject();
        p.addTaskDefinition("demuxtest", DemuxOutputTask.class);
        PrintStream out = System.out;
        PrintStream err = System.err;
        System.setOut(new PrintStream(new DemuxOutputStream(p, false)));
        System.setErr(new PrintStream(new DemuxOutputStream(p, true)));
        try {
            p.executeTarget("testDemux");
        } finally {
            System.setOut(out);
            System.setErr(err);
        }
    }
}
