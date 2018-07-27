package org.apache.solr.common.util;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
public class TestFastInputStream {
  @Test
  public void testgzip() throws Exception {
    ByteArrayOutputStream b = new ByteArrayOutputStream();
    FastOutputStream fos = new FastOutputStream(b);
    GZIPOutputStream gzos = new GZIPOutputStream(fos);
    String ss = "Helloooooooooooooooooooo";
    writeChars(gzos, ss, 0, ss.length());
    gzos.close();
    NamedListCodec.writeVInt(10, fos);
    fos.flushBuffer();
    GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(b.toByteArray(), 0, b.size()));
    char[] cbuf = new char[ss.length()];
    readChars(gzis, cbuf, 0, ss.length());
    assertEquals(new String(cbuf), ss);
    ByteArrayInputStream bis = new ByteArrayInputStream(b.toByteArray(), 0, b.size());
    gzis = new GZIPInputStream(new FastInputStream(bis));
    cbuf = new char[ss.length()];
    readChars(gzis, cbuf, 0, ss.length());
    assertEquals(new String(cbuf), ss);
  }
  public static void readChars(InputStream in, char[] buffer, int start, int length)
          throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      int b = in.read();
      if ((b & 0x80) == 0)
        buffer[i] = (char) b;
      else if ((b & 0xE0) != 0xE0) {
        buffer[i] = (char) (((b & 0x1F) << 6)
                | (in.read() & 0x3F));
      } else
        buffer[i] = (char) (((b & 0x0F) << 12)
                | ((in.read() & 0x3F) << 6)
                | (in.read() & 0x3F));
    }
  }
  public static void writeChars(OutputStream os, String s, int start, int length) throws IOException {
    final int end = start + length;
    for (int i = start; i < end; i++) {
      final int code = (int) s.charAt(i);
      if (code >= 0x01 && code <= 0x7F)
        os.write(code);
      else if (((code >= 0x80) && (code <= 0x7FF)) || code == 0) {
        os.write(0xC0 | (code >> 6));
        os.write(0x80 | (code & 0x3F));
      } else {
        os.write(0xE0 | (code >>> 12));
        os.write(0x80 | ((code >> 6) & 0x3F));
        os.write(0x80 | (code & 0x3F));
      }
    }
  }
}
