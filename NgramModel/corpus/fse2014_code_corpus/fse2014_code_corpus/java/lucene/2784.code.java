package org.apache.solr.core;
import junit.framework.TestCase;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.easymock.EasyMock;
import java.io.IOException;
import java.io.File;
public class RAMDirectoryFactoryTest extends TestCase {
  public void testOpenReturnsTheSameForSamePath() throws IOException {
    final Directory directory = new RAMDirectory();
    RAMDirectoryFactory factory = new RAMDirectoryFactory() {
      @Override
      Directory openNew(String path) throws IOException {
        return directory;
      }
    };
    String path = "/fake/path";
    Directory dir1 = factory.open(path);
    Directory dir2 = factory.open(path);
    assertEquals("RAMDirectoryFactory should not create new instance of RAMDirectory " +
        "every time open() is called for the same path", directory, dir1);
    assertEquals("RAMDirectoryFactory should not create new instance of RAMDirectory " +
        "every time open() is called for the same path", directory, dir2);
  }
  public void testOpenSucceedForEmptyDir() throws IOException {
    RAMDirectoryFactory factory = new RAMDirectoryFactory();
    Directory dir = factory.open("/fake/path");
    assertNotNull("RAMDirectoryFactory should create RAMDirectory even if the path doen't lead " +
        "to index directory on the file system", dir);
  }
}
