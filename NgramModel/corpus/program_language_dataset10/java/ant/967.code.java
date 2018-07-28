package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.util.JavaEnvUtils;
import junit.framework.*;
import java.io.*;
public class ExecuteWatchdogTest extends TestCase {
    private final static long TIME_OUT = 5000;
    private final static String TEST_CLASSPATH = getTestClassPath();
    private final static int CLOCK_ERROR=200;
    private final static long TIME_OUT_TEST=TIME_OUT-CLOCK_ERROR;
    private ExecuteWatchdog watchdog;
    public ExecuteWatchdogTest(String name) {
        super(name);
    }
    protected void setUp(){
        watchdog = new ExecuteWatchdog(TIME_OUT);
    }
    private static String getTestClassPath(){
        String classpath = System.getProperty("build.tests");
        if (classpath == null) {
            System.err.println("WARNING: 'build.tests' property is not available !");
            classpath = System.getProperty("java.class.path");
        }
        return classpath;
    }
    private Process getProcess(long timetorun) throws Exception {
        String[] cmdArray = {
            JavaEnvUtils.getJreExecutable("java"), "-classpath", TEST_CLASSPATH,
            TimeProcess.class.getName(), String.valueOf(timetorun)
        };
        return Runtime.getRuntime().exec(cmdArray);
    }
    private String getErrorOutput(Process p) throws Exception {
        BufferedReader err = new BufferedReader( new InputStreamReader(p.getErrorStream()) );
        StringBuffer buf = new StringBuffer();
        String line;
        while ( (line = err.readLine()) != null){
            buf.append(line);
        }
        return buf.toString();
    }
    private int waitForEnd(Process p) throws Exception {
        int retcode = p.waitFor();
        if (retcode != 0){
            String err = getErrorOutput(p);
            if (err.length() > 0){
                System.err.println("ERROR:");
                System.err.println(err);
            }
        }
        return retcode;
    }
    public void testNoTimeOut() throws Exception {
        Process process = getProcess(TIME_OUT/2);
        watchdog.start(process);
        int retCode = waitForEnd(process);
        assertTrue("process should not have been killed", !watchdog.killedProcess());
        assertFalse(Execute.isFailure(retCode));
    }
    public void testTimeOut() throws Exception {
        Process process = getProcess(TIME_OUT*2);
        long now = System.currentTimeMillis();
        watchdog.start(process);
        int retCode = process.waitFor();
        long elapsed = System.currentTimeMillis() - now;
        assertTrue("process should have been killed", watchdog.killedProcess());
        assertTrue("elapse time of "+elapsed+" ms is less than timeout value of "+TIME_OUT_TEST+" ms", elapsed >= TIME_OUT_TEST);
        assertTrue("elapse time of "+elapsed+" ms is greater than run value of "+(TIME_OUT*2)+" ms", elapsed < TIME_OUT*2);
    }
    public void testFailed() throws Exception {
        Process process = getProcess(-1); 
        watchdog.start(process);
        int retCode = process.waitFor();
        assertTrue("process should not have been killed", !watchdog.killedProcess());
        assertTrue("return code is invalid: " + retCode, retCode!=0);
    }
    public void testManualStop() throws Exception {
        final Process process = getProcess(TIME_OUT*2);
        watchdog.start(process);
        Thread thread = new Thread(){
                public void run(){
                    try {
                        process.waitFor();
                    } catch(InterruptedException e){
                        fail("process interrupted in thread");
                    }
                }
        };
        thread.start();
        thread.join(TIME_OUT/2);
        watchdog.stop();
        thread.join();
        assertEquals(0, process.exitValue());
        assertTrue("process should not have been killed", !watchdog.killedProcess());
    }
}
