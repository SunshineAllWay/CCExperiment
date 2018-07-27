package org.apache.tools.ant.taskdefs.email;
import org.apache.tools.ant.BuildFileTest;
public class EmailTaskTest extends BuildFileTest {
    public EmailTaskTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/email/mail.xml");
    }
    public void test1() {
        expectBuildException("test1", "SMTP auth only possible with MIME mail");
    }
    public void test2() {
        expectBuildException("test2", "SSL only possible with MIME mail");
    }
}
