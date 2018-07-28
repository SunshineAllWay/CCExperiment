package org.apache.tools.ant.taskdefs.optional.splash;
import org.apache.tools.ant.Project;
public class SplashScreenTest {
    public static void main(String[] args) {
        Project p = new Project();
        SplashTask t = new SplashTask();
        t.setProject(p);
        t.execute();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        } 
        p.fireBuildFinished(null);
        System.err.println("finished");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        } 
        System.err.println("exiting");
        System.exit(0);
    }
}
