package org.apache.tools.ant.taskdefs;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import junit.framework.TestCase;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
public class EchoTest extends TestCase {
    public EchoTest(String name) {
        super(name);
    }
    public void testLogBlankEcho() {
        Project p = new Project();
        p.init();
        EchoTestLogger logger = new EchoTestLogger();
        p.addBuildListener(logger);
        Echo echo = new Echo();
        echo.setProject(p);
        echo.setTaskName("testLogBlankEcho");
        echo.execute();
        assertEquals("[testLogBlankEcho] ", logger.lastLoggedMessage );
    }
    private class EchoTestLogger extends DefaultLogger {
        String lastLoggedMessage;
        public EchoTestLogger() {
            super();
            this.setMessageOutputLevel(Project.MSG_DEBUG);
            this.setOutputPrintStream(new PrintStream(new ByteArrayOutputStream(256)));
            this.setErrorPrintStream(new PrintStream(new ByteArrayOutputStream(256)));
        }
        protected void log(String message) {
            this.lastLoggedMessage = message;
        }
    }
}
