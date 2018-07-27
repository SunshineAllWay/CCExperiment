package org.apache.lucene.spatial.tier;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import junit.framework.TestCase;
public class TestCartesianShapeFilter extends TestCase {
  public void testSerializable() throws IOException {
    CartesianShapeFilter filter = new CartesianShapeFilter(new Shape("1"),
        "test");
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(filter);
    } catch (NotSerializableException e) {
      fail("Filter should be serializable but raised a NotSerializableException ["+e.getMessage()+"]");
    }
  }
}
