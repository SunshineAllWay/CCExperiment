package org.apache.tools.ant.taskdefs;
import java.io.IOException;
import junit.framework.TestCase;
public class ProcessDestroyerTest extends TestCase {
    public ProcessDestroyerTest(String arg0) {
        super(arg0);
    }
    public void testProcessDestroyer(){
        try {
            ProcessDestroyer processDestroyer = new ProcessDestroyer();
            Process process =
                Runtime.getRuntime().exec(
                    "java -cp "
                        + System.getProperty("java.class.path")
                        + " "
                        + getClass().getName());
            assertFalse("Not registered as shutdown hook",
                        processDestroyer.isAddedAsShutdownHook());
            processDestroyer.add(process);
            assertTrue("Registered as shutdown hook",
                       processDestroyer.isAddedAsShutdownHook());
            try {
                process.destroy();
            } finally {
                processDestroyer.remove(process);
            }
            assertFalse("Not registered as shutdown hook",
                        processDestroyer.isAddedAsShutdownHook());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        new ProcessDestroyerTest("testProcessDestroyer").testProcessDestroyer();
        try{
            Thread.sleep(60000);
        }catch(InterruptedException ie){
            ie.printStackTrace();
        }
    }
}
