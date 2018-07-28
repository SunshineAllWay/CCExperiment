package org.apache.tools.ant.taskdefs;
import java.io.File;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.JavaEnvUtils;
public class SignJarTest extends BuildFileTest {
    public static final String EXPANDED_MANIFEST
        = "src/etc/testcases/taskdefs/manifests/META-INF/MANIFEST.MF";
    public SignJarTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/signjar.xml");
    }
    public void tearDown() {
        executeTarget("clean");
    }
    private boolean isOffline() {
        return Boolean.getBoolean("offline");
    }
    public void testSigFile() {
        executeTarget("sigfile");
        SignJarChild sj = new SignJarChild();
        sj.setAlias("testonly");
        sj.setKeystore("testkeystore");
        sj.setStorepass("apacheant");
        File jar = new File(getProject().getProperty("test.jar"));
        sj.setJar(jar);
        assertFalse("mustn't find signature without sigfile attribute",
                    sj.isSigned());
        sj.setSigfile("TEST");
        assertTrue("must find signature with sigfile attribute",
                   sj.isSigned());
    }
    public void testInvalidChars() {
        executeTarget("invalidchars");
        SignJarChild sj = new SignJarChild();
        sj.setAlias("test@nly");
        sj.setKeystore("testkeystore");
        sj.setStorepass("apacheant");
        File jar = new File(getProject().getProperty("test.jar"));
        sj.setJar(jar);
        assertTrue(sj.isSigned());
    }
    private static class SignJarChild extends SignJar {
        public boolean isSigned() {
            return isSigned(jar);
        }
    }
    public void testURLKeystoreFile() {
        executeTarget("urlKeystoreFile");
    }
    public void testURLKeystoreHTTP() {
        if(!isOffline()) {
            executeTarget("urlKeystoreHTTP");
        }
    }
    public void testTsaLocalhost() {
        if(JavaEnvUtils.getJavaVersionNumber()>=15) {
            expectBuildException("testTsaLocalhost",
                "no TSA at localhost:0");
            assertLogContaining("java.net.ConnectException");
        }
    }
    public void testSignUnnormalizedJar() throws Exception {
        executeTarget("jar");
        File testJar = new File(getProject().getProperty("test.jar"));
        File testJarParent = testJar.getParentFile();
        File f = new File(testJarParent,
                          "../" + testJarParent.getName() + "/"
                          + testJar.getName());
        assertFalse(testJar.equals(f));
        assertEquals(testJar.getCanonicalPath(), f.getCanonicalPath());
        SignJar s = new SignJar();
        s.setProject(getProject());
        s.setJar(f);
        s.setAlias("testonly");
        s.setStorepass("apacheant");
        s.setKeystore("testkeystore");
        s.execute();
    }
}
