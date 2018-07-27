package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.RAMDirectory;
public class TestParallelReaderEmptyIndex extends LuceneTestCase {
  public void testEmptyIndex() throws IOException {
    RAMDirectory rd1 = new MockRAMDirectory();
    IndexWriter iw = new IndexWriter(rd1, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    iw.close();
    RAMDirectory rd2 = new MockRAMDirectory(rd1);
    RAMDirectory rdOut = new MockRAMDirectory();
    IndexWriter iwOut = new IndexWriter(rdOut, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    ParallelReader pr = new ParallelReader();
    pr.add(IndexReader.open(rd1,true));
    pr.add(IndexReader.open(rd2,true));
    iwOut.addIndexes(new IndexReader[] { pr });
    iwOut.optimize();
    iwOut.close();
    _TestUtil.checkIndex(rdOut);
    rdOut.close();
    rd1.close();
    rd2.close();
  }
  public void testEmptyIndexWithVectors() throws IOException {
    RAMDirectory rd1 = new MockRAMDirectory();
    {
      IndexWriter iw = new IndexWriter(rd1, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
      Document doc = new Document();
      doc.add(new Field("test", "", Store.NO, Index.ANALYZED,
                        TermVector.YES));
      iw.addDocument(doc);
      doc.add(new Field("test", "", Store.NO, Index.ANALYZED,
                        TermVector.NO));
      iw.addDocument(doc);
      iw.close();
      IndexReader ir = IndexReader.open(rd1,false);
      ir.deleteDocument(0);
      ir.close();
      iw = new IndexWriter(rd1, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.APPEND));
      iw.optimize();
      iw.close();
    }
    RAMDirectory rd2 = new MockRAMDirectory();
    {
      IndexWriter iw = new IndexWriter(rd2, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
      Document doc = new Document();
      iw.addDocument(doc);
      iw.close();
    }
    RAMDirectory rdOut = new MockRAMDirectory();
    IndexWriter iwOut = new IndexWriter(rdOut, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    ParallelReader pr = new ParallelReader();
    pr.add(IndexReader.open(rd1,true));
    pr.add(IndexReader.open(rd2,true));
    iwOut.addIndexes(new IndexReader[] { pr });
    pr.close();
    rd1.close();
    rd2.close();
    iwOut.optimize();
    iwOut.close();
    _TestUtil.checkIndex(rdOut);
    rdOut.close();
  }
}
