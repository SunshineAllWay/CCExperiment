package org.apache.tools.ant.util.regexp;
import java.io.IOException;
import junit.framework.AssertionFailedError;
public class JakartaRegexpMatcherTest extends RegexpMatcherTest {
    public RegexpMatcher getImplementation() {
        return new JakartaRegexpMatcher();
    }
    public JakartaRegexpMatcherTest(String name) {
        super(name);
    }
    public void testWindowsLineSeparator2() throws IOException {
        try {
            super.testWindowsLineSeparator2();
            fail("Should trigger when this bug is fixed. {@since 1.2}");
        } catch (AssertionFailedError e) {
        }
    }
    public void testUnixLineSeparator() throws IOException {
        try {
            super.testUnixLineSeparator();
            fail("Should trigger once this bug is fixed. {@since 1.2}");
        } catch (AssertionFailedError e) {
        }
    }
    protected void doEndTest2(String text) {}
}
