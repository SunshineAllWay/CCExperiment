package org.apache.lucene.search.regex;
import java.io.IOException;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiSearcher;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestSpanRegexQuery extends LuceneTestCase {
  Directory indexStoreA = new RAMDirectory();
  Directory indexStoreB = new RAMDirectory();
  public void testSpanRegex() throws Exception {
    RAMDirectory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    Document doc = new Document();
    doc.add(new Field("field", "auto update", Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
    doc = new Document();
    doc.add(new Field("field", "first auto update", Field.Store.NO,
        Field.Index.ANALYZED));
    writer.addDocument(doc);
    writer.optimize();
    writer.close();
    IndexSearcher searcher = new IndexSearcher(directory, true);
    SpanRegexQuery srq = new SpanRegexQuery(new Term("field", "aut.*"));
    SpanFirstQuery sfq = new SpanFirstQuery(srq, 1);
    int numHits = searcher.search(sfq, null, 1000).totalHits;
    assertEquals(1, numHits);
  }
  public void testSpanRegexBug() throws CorruptIndexException, IOException {
    createRAMDirectories();
    SpanRegexQuery srq = new SpanRegexQuery(new Term("field", "a.*"));
    SpanRegexQuery stq = new SpanRegexQuery(new Term("field", "b.*"));
    SpanNearQuery query = new SpanNearQuery(new SpanQuery[] { srq, stq }, 6,
        true);
    IndexSearcher[] arrSearcher = new IndexSearcher[2];
    arrSearcher[0] = new IndexSearcher(indexStoreA, true);
    arrSearcher[1] = new IndexSearcher(indexStoreB, true);
    MultiSearcher searcher = new MultiSearcher(arrSearcher);
    int numHits = searcher.search(query, null, 1000).totalHits;
    arrSearcher[0].close();
    arrSearcher[1].close();
    assertEquals(2, numHits);
    indexStoreA.close();
    indexStoreB.close();
  }
  private void createRAMDirectories() throws CorruptIndexException,
      LockObtainFailedException, IOException {
    Document lDoc = new Document();
    lDoc.add(new Field("field", "a1 b1", Field.Store.NO,
        Field.Index.ANALYZED_NO_NORMS));
    Document lDoc2 = new Document();
    lDoc2.add(new Field("field", "a2 b2", Field.Store.NO,
        Field.Index.ANALYZED_NO_NORMS));
    IndexWriter writerA = new IndexWriter(indexStoreA, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    writerA.addDocument(lDoc);
    writerA.optimize();
    writerA.close();
    IndexWriter writerB = new IndexWriter(indexStoreB, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(OpenMode.CREATE));
    writerB.addDocument(lDoc2);
    writerB.optimize();
    writerB.close();
  }
}
