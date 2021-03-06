package org.apache.tools.bzip2;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.TestCase;
public class CBZip2StreamTest extends TestCase {
    public void testNullPointer() throws IOException {
        try {
            CBZip2InputStream cb = new CBZip2InputStream(new ByteArrayInputStream(new byte[0]));
            fail("expected an exception");
        } catch (IOException e) {
        }
    }
    public void testDivisionByZero() throws IOException {
        CBZip2OutputStream cb = new CBZip2OutputStream(new ByteArrayOutputStream());
        cb.close();
    }
}
