package org.apache.lucene.store;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
public class TestDirectory extends LuceneTestCase {
  public void testDetectClose() throws Throwable {
    Directory dir = new RAMDirectory();
    dir.close();
    try {
      dir.createOutput("test");
      fail("did not hit expected exception");
    } catch (AlreadyClosedException ace) {
    }
    dir = FSDirectory.open(TEMP_DIR);
    dir.close();
    try {
      dir.createOutput("test");
      fail("did not hit expected exception");
    } catch (AlreadyClosedException ace) {
    }
  }
  public void testDirectInstantiation() throws Exception {
    File path = new File(TEMP_DIR, "testDirectInstantiation");
    int sz = 3;
    Directory[] dirs = new Directory[sz];
    dirs[0] = new SimpleFSDirectory(path, null);
    dirs[1] = new NIOFSDirectory(path, null);
    dirs[2] = new MMapDirectory(path, null);
    for (int i=0; i<sz; i++) {
      Directory dir = dirs[i];
      dir.ensureOpen();
      String fname = "foo." + i;
      String lockname = "foo" + i + ".lck";
      IndexOutput out = dir.createOutput(fname);
      out.writeByte((byte)i);
      out.close();
      for (int j=0; j<sz; j++) {
        Directory d2 = dirs[j];
        d2.ensureOpen();
        assertTrue(d2.fileExists(fname));
        assertEquals(1, d2.fileLength(fname));
        if (d2 instanceof MMapDirectory) continue;
        IndexInput input = d2.openInput(fname);
        assertEquals((byte)i, input.readByte());
        input.close();
      }
      dirs[(i+1)%sz].deleteFile(fname);
      for (int j=0; j<sz; j++) {
        Directory d2 = dirs[j];
        assertFalse(d2.fileExists(fname));
      }
      Lock lock = dir.makeLock(lockname);
      assertTrue(lock.obtain());
      for (int j=0; j<sz; j++) {
        Directory d2 = dirs[j];
        Lock lock2 = d2.makeLock(lockname);
        try {
          assertFalse(lock2.obtain(1));
        } catch (LockObtainFailedException e) {
        }
      }
      lock.release();
      lock = dirs[(i+1)%sz].makeLock(lockname);
      assertTrue(lock.obtain());
      lock.release();
    }
    for (int i=0; i<sz; i++) {
      Directory dir = dirs[i];
      dir.ensureOpen();
      dir.close();
      assertFalse(dir.isOpen);
    }
    _TestUtil.rmDir(path);
  }
  public void testDontCreate() throws Throwable {
    File path = new File(TEMP_DIR, "doesnotexist");
    try {
      assertTrue(!path.exists());
      Directory dir = new SimpleFSDirectory(path, null);
      assertTrue(!path.exists());
      dir.close();
    } finally {
      _TestUtil.rmDir(path);
    }
  }
  public void testRAMDirectoryFilter() throws IOException {
    checkDirectoryFilter(new RAMDirectory());
  }
  public void testFSDirectoryFilter() throws IOException {
    checkDirectoryFilter(FSDirectory.open(new File(TEMP_DIR,"test")));
  }
  private void checkDirectoryFilter(Directory dir) throws IOException {
    String name = "file";
    try {
      dir.createOutput(name).close();
      assertTrue(dir.fileExists(name));
      assertTrue(Arrays.asList(dir.listAll()).contains(name));
    } finally {
      dir.close();
    }
  }
  public void testCopySubdir() throws Throwable {
    File path = new File(TEMP_DIR, "testsubdir");
    try {
      path.mkdirs();
      new File(path, "subdir").mkdirs();
      Directory fsDir = new SimpleFSDirectory(path, null);
      assertEquals(0, new RAMDirectory(fsDir).listAll().length);
    } finally {
      _TestUtil.rmDir(path);
    }
  }
  public void testNotDirectory() throws Throwable {
    File path = new File(TEMP_DIR, "testnotdir");
    Directory fsDir = new SimpleFSDirectory(path, null);
    try {
      IndexOutput out = fsDir.createOutput("afile");
      out.close();
      assertTrue(fsDir.fileExists("afile"));
      try {
        new SimpleFSDirectory(new File(path, "afile"), null);
        fail("did not hit expected exception");
      } catch (NoSuchDirectoryException nsde) {
      }
    } finally {
      fsDir.close();
      _TestUtil.rmDir(path);
    }
  }
}
