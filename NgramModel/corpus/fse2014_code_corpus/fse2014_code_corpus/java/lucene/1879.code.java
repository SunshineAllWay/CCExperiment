package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.store.IndexInput;
import java.io.IOException;
public class TestIndexInput extends LuceneTestCase {
  public void testRead() throws IOException {
    IndexInput is = new MockIndexInput(new byte[] { 
      (byte) 0x80, 0x01,
      (byte) 0xFF, 0x7F,
      (byte) 0x80, (byte) 0x80, 0x01,
      (byte) 0x81, (byte) 0x80, 0x01,
      0x06, 'L', 'u', 'c', 'e', 'n', 'e',
      0x02, (byte) 0xC2, (byte) 0xBF,
      0x0A, 'L', 'u', (byte) 0xC2, (byte) 0xBF, 
            'c', 'e', (byte) 0xC2, (byte) 0xBF, 
            'n', 'e',
      0x03, (byte) 0xE2, (byte) 0x98, (byte) 0xA0,
      0x0C, 'L', 'u', (byte) 0xE2, (byte) 0x98, (byte) 0xA0,
            'c', 'e', (byte) 0xE2, (byte) 0x98, (byte) 0xA0,
            'n', 'e',
      0x04, (byte) 0xF0, (byte) 0x9D, (byte) 0x84, (byte) 0x9E,
      0x08, (byte) 0xF0, (byte) 0x9D, (byte) 0x84, (byte) 0x9E, 
            (byte) 0xF0, (byte) 0x9D, (byte) 0x85, (byte) 0xA0, 
      0x0E, 'L', 'u',
            (byte) 0xF0, (byte) 0x9D, (byte) 0x84, (byte) 0x9E,
            'c', 'e', 
            (byte) 0xF0, (byte) 0x9D, (byte) 0x85, (byte) 0xA0, 
            'n', 'e',  
      0x01, 0x00,
      0x08, 'L', 'u', 0x00, 'c', 'e', 0x00, 'n', 'e',
    });
    assertEquals(128,is.readVInt());
    assertEquals(16383,is.readVInt());
    assertEquals(16384,is.readVInt());
    assertEquals(16385,is.readVInt());
    assertEquals("Lucene",is.readString());
    assertEquals("\u00BF",is.readString());
    assertEquals("Lu\u00BFce\u00BFne",is.readString());
    assertEquals("\u2620",is.readString());
    assertEquals("Lu\u2620ce\u2620ne",is.readString());
    assertEquals("\uD834\uDD1E",is.readString());
    assertEquals("\uD834\uDD1E\uD834\uDD60",is.readString());
    assertEquals("Lu\uD834\uDD1Ece\uD834\uDD60ne",is.readString());
    assertEquals("\u0000",is.readString());
    assertEquals("Lu\u0000ce\u0000ne",is.readString());
  }
  public void testSkipChars() throws IOException {
    byte[] bytes = new byte[]{(byte) 0x80, 0x01,
            (byte) 0xFF, 0x7F,
            (byte) 0x80, (byte) 0x80, 0x01,
            (byte) 0x81, (byte) 0x80, 0x01,
            0x06, 'L', 'u', 'c', 'e', 'n', 'e',
    };
    String utf8Str = "\u0634\u1ea1";
    byte [] utf8Bytes = utf8Str.getBytes("UTF-8");
    byte [] theBytes = new byte[bytes.length + 1 + utf8Bytes.length];
    System.arraycopy(bytes, 0, theBytes, 0, bytes.length);
    theBytes[bytes.length] = (byte)utf8Str.length();
    System.arraycopy(utf8Bytes, 0, theBytes, bytes.length + 1, utf8Bytes.length);
    IndexInput is = new MockIndexInput(theBytes);
    assertEquals(128, is.readVInt());
    assertEquals(16383, is.readVInt());
    assertEquals(16384, is.readVInt());
    assertEquals(16385, is.readVInt());
    int charsToRead = is.readVInt();
    assertTrue(0x06 + " does not equal: " + charsToRead, 0x06 == charsToRead);
    is.skipChars(3);
    char [] chars = new char[3];
    is.readChars(chars, 0, 3);
    String tmpStr = new String(chars);
    assertTrue(tmpStr + " is not equal to " + "ene", tmpStr.equals("ene" ) == true);
    charsToRead = is.readVInt() - 1;
    is.skipChars(1);
    assertTrue(utf8Str.length() - 1 + " does not equal: " + charsToRead, utf8Str.length() - 1 == charsToRead);
    chars = new char[charsToRead];
    is.readChars(chars, 0, charsToRead);
    tmpStr = new String(chars);
    assertTrue(tmpStr + " is not equal to " + utf8Str.substring(1), tmpStr.equals(utf8Str.substring(1)) == true);
  }
}
