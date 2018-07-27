package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.TeeOutputStream;
public class JavaTest extends BuildFileTest {
    private static final int TIME_TO_WAIT = 1;
    private static final int SECURITY_MARGIN = 2000;
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean runFatalTests=false;
    public JavaTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/java.xml");
        String runFatal=System.getProperty("junit.run.fatal.tests");
        if(runFatal!=null)
            runFatalTests=true;
    }
    public void tearDown() {
        project.executeTarget("cleanup");
    }
    public void testNoJarNoClassname(){
        expectBuildExceptionContaining("testNoJarNoClassname",
            "parameter validation",
            "Classname must not be null.");
    }
    public void testJarNoFork() {
        expectBuildExceptionContaining("testJarNoFork",
            "parameter validation",
            "Cannot execute a jar in non-forked mode. "
                + "Please set fork='true'. ");
    }
    public void testJarAndClassName() {
        expectBuildException("testJarAndClassName",
            "Should not be able to set both classname AND jar");
    }
    public void testClassnameAndJar() {
        expectBuildException("testClassnameAndJar",
            "Should not be able to set both classname AND jar");
    }
    public void testRun() {
        executeTarget("testRun");
    }
    public void testRunFail() {
        if(runFatalTests) {
            executeTarget("testRunFail");
        }
    }
    public void testRunFailFoe() {
        if(runFatalTests) {
            expectBuildExceptionContaining("testRunFailFoe",
                "java failures being propagated",
                "Java returned:");
        }
}
    public void testRunFailFoeFork() {
        expectBuildExceptionContaining("testRunFailFoeFork",
            "java failures being propagated",
            "Java returned:");
    }
    public void testExcepting() {
        expectLogContaining("testExcepting",
                            "Exception raised inside called program");
    }
    public void testExceptingFork() {
        expectLogContaining("testExceptingFork",
                            "Java Result:");
    }
    public void testExceptingFoe() {
        expectBuildExceptionContaining("testExceptingFoe",
            "passes exception through",
            "Exception raised inside called program");
    }
    public void testExceptingFoeFork() {
        expectBuildExceptionContaining("testExceptingFoeFork",
            "exceptions turned into error codes",
            "Java returned:");
    }
    public void testResultPropertyZero() {
        executeTarget("testResultPropertyZero");
        assertEquals("0",project.getProperty("exitcode"));
    }
    public void testResultPropertyNonZero() {
        executeTarget("testResultPropertyNonZero");
        assertEquals("2",project.getProperty("exitcode"));
    }
    public void testResultPropertyZeroNoFork() {
        executeTarget("testResultPropertyZeroNoFork");
        assertEquals("0",project.getProperty("exitcode"));
    }
    public void testResultPropertyNonZeroNoFork() {
        executeTarget("testResultPropertyNonZeroNoFork");
         assertEquals("-1",project.getProperty("exitcode"));
     }
    public void testRunFailWithFailOnError() {
        expectBuildExceptionContaining("testRunFailWithFailOnError",
            "non zero return code",
            "Java returned:");
    }
    public void testRunSuccessWithFailOnError() {
        executeTarget("testRunSuccessWithFailOnError");
    }
    public void testSpawn() {
        File logFile = FILE_UTILS.createTempFile("spawn","log", project.getBaseDir(), false, false);
        assertTrue("log file not existing", !logFile.exists());
        project.setProperty("logFile", logFile.getAbsolutePath());
        project.setProperty("timeToWait", Long.toString(TIME_TO_WAIT));
        project.executeTarget("testSpawn");
        try {
            Thread.sleep(TIME_TO_WAIT * 1000 + SECURITY_MARGIN);
        } catch (Exception ex) {
            System.out.println("my sleep was interrupted");
        }
        if (!logFile.exists()) {
            System.out.println("suggestion: increase the constant"
            + " SECURITY_MARGIN to give more time for java to start.");
        }
        assertTrue("log file exists", logFile.exists());
    }
    public void testRedirect1() {
        executeTarget("redirect1");
    }
    public void testRedirect2() {
        executeTarget("redirect2");
    }
    public void testRedirect3() {
        executeTarget("redirect3");
    }
    public void testRedirector1() {
        executeTarget("redirector1");
    }
    public void testRedirector2() {
        executeTarget("redirector2");
    }
    public void testReleasedInput() throws Exception {
        PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream in = new PipedInputStream(out);
        project.setInputHandler(new DefaultInputHandler() {
            protected InputStream getInputStream() {
                return in;
            }
        });
        project.setDefaultInputStream(in);
        Java java = new Java();
        java.setProject(project);
        java.setClassname("org.apache.tools.ant.Main");
        java.setArgs("-version");
        java.setFork(true);
        java.execute();
        Thread inputThread = new Thread(new Runnable() {
            public void run() {
                Input input = new Input();
                input.setProject(project);
                input.setAddproperty("input.value");
                input.execute();
            }
        });
        inputThread.start();
        Thread.sleep(100);
        out.write("foo\n".getBytes());
        out.flush();
        out.write("bar\n".getBytes());
        out.flush();
        inputThread.join(2000);
        assertEquals("foo", project.getProperty("input.value"));
    }
    public static class EntryPoint {
        public static void main(String[] argv) {
            int exitCode=0;
            if(argv.length>0) {
                try {
                    exitCode=Integer.parseInt(argv[0]);
                } catch(NumberFormatException nfe) {
                    exitCode=-1;
                }
            }
            if(argv.length>1) {
                System.out.println(argv[1]);
            }
            if(argv.length>2) {
                System.err.println(argv[2]);
            }
            if(exitCode!=0) {
                System.exit(exitCode);
            }
        }
    }
    public static class ExceptingEntryPoint {
        public static void main(String[] argv) {
            throw new NullPointerException("Exception raised inside called program");
        }
    }
    public static class SpawnEntryPoint {
        public static void main(String [] argv) {
            int sleepTime = 10;
            String logFile = "spawn.log";
            if (argv.length >= 1) {
                sleepTime = Integer.parseInt(argv[0]);
            }
            if (argv.length >= 2)
            {
                logFile = argv[1];
            }
            OutputStreamWriter out = null;
            try {
                Thread.sleep(sleepTime * 1000);
            } catch (InterruptedException ex) {
                System.out.println("my sleep was interrupted");
            }
            try {
                File dest = new File(logFile);
                FileOutputStream fos = new FileOutputStream(dest);
                out = new OutputStreamWriter(fos);
                out.write("bye bye\n");
            } catch (Exception ex) {}
            finally {
                try {out.close();} catch (IOException ioe) {}}
        }
    }
    public static class PipeEntryPoint {
        public static void main(String[] args) {
            OutputStream os = null;
            if (args.length > 0) {
                if ("out".equalsIgnoreCase(args[0])) {
                    os = System.out;
                } else if ("err".equalsIgnoreCase(args[0])) {
                    os = System.err;
                } else if ("both".equalsIgnoreCase(args[0])) {
                    os = new TeeOutputStream(System.out, System.err);
                }
            }
            if (os != null) {
                Thread t = new Thread(new StreamPumper(System.in, os, true));
                t.setName("PipeEntryPoint " + args[0]);
                t.start();
                try {
                    t.join();
                } catch (InterruptedException eyeEx) {
                }
            }
        }
    }
}
