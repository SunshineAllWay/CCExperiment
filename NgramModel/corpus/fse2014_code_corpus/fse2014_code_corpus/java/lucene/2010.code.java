package org.apache.lucene.store;
import java.io.IOException;
public class MockRAMInputStream extends RAMInputStream {
  private MockRAMDirectory dir;
  private String name;
  private boolean isClone;
  public MockRAMInputStream(MockRAMDirectory dir, String name, RAMFile f) throws IOException {
    super(f);
    this.name = name;
    this.dir = dir;
  }
  @Override
  public void close() {
    super.close();
    if (!isClone) {
      synchronized(dir) {
        Integer v = dir.openFiles.get(name);
        if (v != null) {
          if (v.intValue() == 1) {
            dir.openFiles.remove(name);
          } else {
            v = Integer.valueOf(v.intValue()-1);
            dir.openFiles.put(name, v);
          }
        }
      }
    }
  }
  @Override
  public Object clone() {
    MockRAMInputStream clone = (MockRAMInputStream) super.clone();
    clone.isClone = true;
    return clone;
  }
}
