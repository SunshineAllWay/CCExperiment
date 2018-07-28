package org.apache.lucene.store;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.TestIndexWriterReader;
import org.apache.lucene.util.LuceneTestCase;
public class TestFileSwitchDirectory extends LuceneTestCase {
  public void testBasic() throws IOException {
    Set<String> fileExtensions = new HashSet<String>();
    fileExtensions.add(IndexFileNames.FIELDS_EXTENSION);
    fileExtensions.add(IndexFileNames.FIELDS_INDEX_EXTENSION);
    Directory primaryDir = new MockRAMDirectory();
    RAMDirectory secondaryDir = new MockRAMDirectory();
    FileSwitchDirectory fsd = new FileSwitchDirectory(fileExtensions, primaryDir, secondaryDir, true);
    IndexWriter writer = new IndexWriter(fsd, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundFile(false);
    ((LogMergePolicy) writer.getMergePolicy()).setUseCompoundDocStore(false);
    TestIndexWriterReader.createIndexNoClose(true, "ram", writer);
    IndexReader reader = writer.getReader();
    assertEquals(100, reader.maxDoc());
    writer.commit();
    String[] files = primaryDir.listAll();
    assertTrue(files.length > 0);
    for (int x=0; x < files.length; x++) {
      String ext = FileSwitchDirectory.getExtension(files[x]);
      assertTrue(fileExtensions.contains(ext));
    }
    files = secondaryDir.listAll();
    assertTrue(files.length > 0);
    for (int x=0; x < files.length; x++) {
      String ext = FileSwitchDirectory.getExtension(files[x]);
      assertFalse(fileExtensions.contains(ext));
    }
    reader.close();
    writer.close();
    files = fsd.listAll();
    for(int i=0;i<files.length;i++) {
      assertNotNull(files[i]);
    }
    fsd.close();
  }
}
