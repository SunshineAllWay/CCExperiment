package org.apache.tools.ant.util.regexp;
import java.io.IOException;
import junit.framework.AssertionFailedError;
public class Jdk14RegexpRegexpTest extends RegexpTest {
    public Regexp getRegexpImplementation() {
        return new Jdk14RegexpRegexp();
    }
    public Jdk14RegexpRegexpTest(String name) {
        super(name);
    }
    public void testParagraphCharacter() throws IOException {
        try {
            super.testParagraphCharacter();
            fail("Should trigger once fixed. {@since JDK 1.4RC1}");
        } catch (AssertionFailedError e){
        }
    }
    public void testLineSeparatorCharacter() throws IOException {
        try {
            super.testLineSeparatorCharacter();
            fail("Should trigger once fixed. {@since JDK 1.4RC1}");
        } catch (AssertionFailedError e){
        }
    }
    public void testStandaloneCR() throws IOException {
        try {
            super.testStandaloneCR();
            fail("Should trigger once fixed. {@since JDK 1.4RC1}");
        } catch (AssertionFailedError e){
        }
    }
    public void testWindowsLineSeparator() throws IOException {
        try {
            super.testWindowsLineSeparator();
            fail("Should trigger once fixed. {@since JDK 1.4RC1}");
        } catch (AssertionFailedError e){
        }
    }
}
