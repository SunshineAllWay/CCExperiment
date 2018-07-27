package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
public class TestIndexWriterMerging extends LuceneTestCase
{
  public void testLucene() throws IOException
  {
    int num=100;
    Directory indexA = new MockRAMDirectory();
    Directory indexB = new MockRAMDirectory();
    fillIndex(indexA, 0, num);
    boolean fail = verifyIndex(indexA, 0);
    if (fail)
    {
      fail("Index a is invalid");
    }
    fillIndex(indexB, num, num);
    fail = verifyIndex(indexB, num);
    if (fail)
    {
      fail("Index b is invalid");
    }
    Directory merged = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(merged, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(2);
    writer.addIndexesNoOptimize(new Directory[]{indexA, indexB});
    writer.optimize();
    writer.close();
    fail = verifyIndex(merged, 0);
    merged.close();
    assertFalse("The merged index is invalid", fail);
  }
  private boolean verifyIndex(Directory directory, int startAt) throws IOException
  {
    boolean fail = false;
    IndexReader reader = IndexReader.open(directory, true);
    int max = reader.maxDoc();
    for (int i = 0; i < max; i++)
    {
      Document temp = reader.document(i);
      if (!temp.getField("count").stringValue().equals((i + startAt) + ""))
      {
        fail = true;
        System.out.println("Document " + (i + startAt) + " is returning document " + temp.getField("count").stringValue());
      }
    }
    reader.close();
    return fail;
  }
  private void fillIndex(Directory dir, int start, int numDocs) throws IOException {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, 
        new StandardAnalyzer(TEST_VERSION_CURRENT))
        .setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(2));
    ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(2);
    for (int i = start; i < (start + numDocs); i++)
    {
      Document temp = new Document();
      temp.add(new Field("count", (""+i), Field.Store.YES, Field.Index.NOT_ANALYZED));
      writer.addDocument(temp);
    }
    writer.close();
  }
}
