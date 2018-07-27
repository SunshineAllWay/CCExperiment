package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class IsReachableTest extends BuildFileTest {
    public IsReachableTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(
                "src/etc/testcases/taskdefs/conditions/isreachable.xml");
    }
    public void testLocalhost() throws Exception {
        executeTarget("testLocalhost");
    }
    public void testLocalhostURL() throws Exception {
        executeTarget("testLocalhostURL");
    }
    public void testIpv4localhost() throws Exception {
        executeTarget("testIpv4localhost");
    }
    public void testFTPURL() throws Exception {
        executeTarget("testFTPURL");
    }
    public void testBoth() throws Exception {
        expectBuildExceptionContaining("testBoth",
                "error on two targets",
                IsReachable.ERROR_BOTH_TARGETS);
    }
    public void testNoTargets() throws Exception {
        expectBuildExceptionContaining("testNoTargets",
                "no params",
                IsReachable.ERROR_NO_HOSTNAME);
    }
    public void testBadTimeout() throws Exception {
        expectBuildExceptionContaining("testBadTimeout",
                "error on -ve timeout",
                IsReachable.ERROR_BAD_TIMEOUT);
    }
    public void NotestFile() throws Exception {
        expectBuildExceptionContaining("testFile",
                "error on file URL",
                IsReachable.ERROR_NO_HOST_IN_URL);
    }
    public void testBadURL() throws Exception {
        expectBuildExceptionContaining("testBadURL",
                "error in URL",
                IsReachable.ERROR_BAD_URL);
    }
}
