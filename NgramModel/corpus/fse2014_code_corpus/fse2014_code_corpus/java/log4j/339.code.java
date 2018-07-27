package org.apache.log4j.util;
import junit.framework.TestCase;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
public class SerializationTestHelper {
  private SerializationTestHelper() {
  }
  public static Object serializeClone(final Object obj)
    throws IOException, ClassNotFoundException {
    ByteArrayOutputStream memOut = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(memOut);
    objOut.writeObject(obj);
    objOut.close();
    ByteArrayInputStream src = new ByteArrayInputStream(memOut.toByteArray());
    ObjectInputStream objIs = new ObjectInputStream(src);
    return objIs.readObject();
  }
  public static Object deserializeStream(final String witness)
    throws Exception {
    FileInputStream fileIs = new FileInputStream(witness);
    ObjectInputStream objIs = new ObjectInputStream(fileIs);
    return objIs.readObject();
  }
  public static void assertSerializationEquals(
    final String witness, final Object obj, final int[] skip,
    final int endCompare) throws Exception {
    ByteArrayOutputStream memOut = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(memOut);
    objOut.writeObject(obj);
    objOut.close();
    assertStreamEquals(witness, memOut.toByteArray(), skip, endCompare);
  }
  public static void assertStreamEquals(
    final String witness, final byte[] actual, final int[] skip,
    final int endCompare) throws IOException {
    File witnessFile = new File(witness);
    if (witnessFile.exists()) {
      int skipIndex = 0;
      byte[] expected = new byte[actual.length];
      FileInputStream is = new FileInputStream(witnessFile);
      int bytesRead = is.read(expected);
      is.close();
      if(bytesRead < endCompare) {
          TestCase.assertEquals(bytesRead, actual.length);
      }
      int endScan = actual.length;
      if (endScan > endCompare) {
        endScan = endCompare;
      }
      for (int i = 0; i < endScan; i++) {
        if ((skipIndex < skip.length) && (skip[skipIndex] == i)) {
          skipIndex++;
        } else {
          if (expected[i] != actual[i]) {
            TestCase.assertEquals(
              "Difference at offset " + i, expected[i], actual[i]);
          }
        }
      }
    } else {
      FileOutputStream os = new FileOutputStream(witnessFile);
      os.write(actual);
      os.close();
      TestCase.fail("Writing witness file " + witness);
    }
  }
}
