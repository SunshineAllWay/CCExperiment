package org.apache.lucene.index;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
public class TestIndexWriterLockRelease extends LuceneTestCase {
    private java.io.File __test_dir;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        if (this.__test_dir == null) {
            this.__test_dir = new File(TEMP_DIR, "testIndexWriter");
            if (this.__test_dir.exists()) {
                throw new IOException("test directory \"" + this.__test_dir.getPath() + "\" already exists (please remove by hand)");
            }
            if (!this.__test_dir.mkdirs()
                && !this.__test_dir.isDirectory()) {
                throw new IOException("unable to create test directory \"" + this.__test_dir.getPath() + "\"");
            }
        }
    }
    @Override
    protected void tearDown() throws Exception {
        if (this.__test_dir != null) {
            File[] files = this.__test_dir.listFiles();
            for (int i = 0;
                i < files.length;
                ++i) {
                if (!files[i].delete()) {
                    throw new IOException("unable to remove file in test directory \"" + this.__test_dir.getPath() + "\" (please remove by hand)");
                }
            }
            if (!this.__test_dir.delete()) {
                throw new IOException("unable to remove test directory \"" + this.__test_dir.getPath() + "\" (please remove by hand)");
            }
        }
        super.tearDown();
    }
    public void testIndexWriterLockRelease() throws IOException {
        FSDirectory dir = FSDirectory.open(this.__test_dir);
        try {
          new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
              new StandardAnalyzer(TEST_VERSION_CURRENT))
          .setOpenMode(OpenMode.APPEND));
        } catch (FileNotFoundException e) {
            try {
              new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT,
                  new StandardAnalyzer(TEST_VERSION_CURRENT))
              .setOpenMode(OpenMode.APPEND));
            } catch (FileNotFoundException e1) {
            }
        } finally {
          dir.close();
        }
    }
}
