package org.apache.tools.tar;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import junit.framework.TestCase;
public class TarOutputStreamTest extends TestCase {
    public void testClose() throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        TarOutputStream stream = new TarOutputStream(byteStream);
        stream.close();
        stream.close();
    }
}
