package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import java.io.IOException;
public class ChecksumTest extends BuildFileTest {
    public ChecksumTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/checksum.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testCreateMd5() throws IOException {
        executeTarget("createMd5");
    }
    public void testCreateMD5SUMformat() throws IOException {
        executeTarget("createMD5SUMformat");
    }
    public void testCreateSVFformat() throws IOException {
        executeTarget("createSVFformat");
    }
    public void testCreatePattern() throws IOException {
        executeTarget("createPattern");
    }
    public void testSetProperty() {
        executeTarget("setProperty");
    }
    public void testVerifyTotal() {
        executeTarget("verifyTotal");
    }
    public void testVerifyTotalRC() {
        executeTarget("verifyTotalRC");
    }
    public void testVerifyChecksumdir() {
        executeTarget("verifyChecksumdir");
    }
    public void testVerifyAsTask() {
        executeTarget("verifyAsTask");
    }
    public void testVerifyMD5SUMAsTask() {
        executeTarget("verifyMD5SUMAsTask");
    }
    public void testVerifyAsCondition() {
        executeTarget("verifyAsCondition");
    }
    public void testVerifyFromProperty() {
        executeTarget("verifyFromProperty");
    }
    public void testVerifyChecksumdirNoTotal() {
        executeTarget("verifyChecksumdirNoTotal");
    }
}
