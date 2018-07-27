package org.apache.tools.ant.util;
import java.io.IOException;
import junit.framework.TestCase;
public class LineOrientedOutputStreamTest extends TestCase {
    private static String LINE = "This is a line";
    private DummyStream stream;
    public LineOrientedOutputStreamTest(String name) {
        super(name);
    }
    public void setUp() {
        stream = new DummyStream();
    }
    public void tearDown() throws IOException {
        if (stream != null) {
            stream.close();
        }
    }
    public void testLineWithLinefeedArray() throws IOException {
        writeByteArray();
        writeAsArray('\n');
        stream.assertInvoked();
    }
    public void testLineWithLinefeedSingleBytes() throws IOException {
        writeSingleBytes();
        stream.write('\n');
        stream.assertInvoked();
    }
    public void testLineWithCariagereturnArray() throws IOException {
        writeByteArray();
        writeAsArray('\r');
        stream.assertInvoked();
    }
    public void testLineWithCariagereturnSingleBytes() throws IOException {
        writeSingleBytes();
        stream.write('\r');
        stream.assertInvoked();
    }
    public void testLineWithCariagereturnLinefeedArray() throws IOException {
        writeByteArray();
        writeAsArray('\r');
        writeAsArray('\n');
        stream.assertInvoked();
    }
    public void testLineWithCariagereturnLinefeedSingleBytes() throws IOException {
        writeSingleBytes();
        stream.write('\r');
        stream.write('\n');
        stream.assertInvoked();
    }
    public void testFlushArray() throws IOException {
        writeByteArray();
        stream.flush();
        stream.assertInvoked();
    }
    public void testFlushSingleBytes() throws IOException {
        writeSingleBytes();
        stream.flush();
        stream.assertInvoked();
    }
    public void testCloseArray() throws IOException {
        writeByteArray();
        stream.close();
        stream.assertInvoked();
        stream = null;
    }
    public void testCloseSingleBytes() throws IOException {
        writeSingleBytes();
        stream.close();
        stream.assertInvoked();
        stream = null;
    }
    private void writeByteArray() throws IOException {
        stream.write(LINE.getBytes(), 0, LINE.length());
    }
    private void writeSingleBytes() throws IOException {
        byte[] b = LINE.getBytes();
        for (int i = 0; i < b.length; i++) {
            stream.write(b[i]);
        }
    }
    private void writeAsArray(char c) throws IOException {
        stream.write(new byte[] {(byte) c}, 0, 1);
    }
    private class DummyStream extends LineOrientedOutputStream {
        private boolean invoked;
        protected void processLine(String line) {
            assertFalse("Only one line", invoked);
            assertEquals(LINE, line);
            invoked = true;
        }
        private void assertInvoked() {
            assertTrue("At least one line", invoked);
        }
    }
}
