package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.types.Commandline;
import junit.framework.TestCase;
public class RpmTest extends TestCase {
    public void testShouldThrowExceptionWhenRpmFails() throws Exception {
        Rpm rpm = new MyRpm();
        rpm.setProject(new org.apache.tools.ant.Project());
        rpm.setFailOnError(true);
        try {
            rpm.execute();
            fail("should have thrown a build exception");
        } catch (BuildException ex) {
            assertTrue(ex.getMessage()
                       .indexOf("' failed with exit code 2") != -1);
        }
    }
    public void testShouldNotThrowExceptionWhenRpmFails() throws Exception {
        Rpm rpm = new MyRpm();
        rpm.execute();
    }
    public static class MyRpm extends Rpm {
        protected Execute getExecute(Commandline toExecute,
                                     ExecuteStreamHandler streamhandler) {
            return new Execute() {
                    public int execute() {
                        return 2;
                    }
                };
        }
        public void log(String msg, int msgLevel) {
        }
    }
}
