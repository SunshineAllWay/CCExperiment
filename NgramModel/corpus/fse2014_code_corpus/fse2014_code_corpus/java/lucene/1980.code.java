package org.apache.lucene.search;
import java.util.List;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.English;
import org.apache.lucene.util.LuceneTestCase;
public class TestSpanQueryFilter extends LuceneTestCase {
  public TestSpanQueryFilter(String s) {
    super(s);
  }
  public void testFilterWorks() throws Exception {
    Directory dir = new RAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < 500; i++) {
      Document document = new Document();
      document.add(new Field("field", English.intToEnglish(i) + " equals " + English.intToEnglish(i),
              Field.Store.NO, Field.Index.ANALYZED));
      writer.addDocument(document);
    }
    writer.close();
    IndexReader reader = IndexReader.open(dir, true);
    SpanTermQuery query = new SpanTermQuery(new Term("field", English.intToEnglish(10).trim()));
    SpanQueryFilter filter = new SpanQueryFilter(query);
    SpanFilterResult result = filter.bitSpans(reader);
    DocIdSet docIdSet = result.getDocIdSet();
    assertTrue("docIdSet is null and it shouldn't be", docIdSet != null);
    assertContainsDocId("docIdSet doesn't contain docId 10", docIdSet, 10);
    List<SpanFilterResult.PositionInfo> spans = result.getPositions();
    assertTrue("spans is null and it shouldn't be", spans != null);
    int size = getDocIdSetSize(docIdSet);
    assertTrue("spans Size: " + spans.size() + " is not: " + size, spans.size() == size);
    for (final SpanFilterResult.PositionInfo info: spans) {
      assertTrue("info is null and it shouldn't be", info != null);
      assertContainsDocId("docIdSet doesn't contain docId " + info.getDoc(), docIdSet, info.getDoc());
      assertTrue("info.getPositions() Size: " + info.getPositions().size() + " is not: " + 2, info.getPositions().size() == 2);
    }
    reader.close();
  }
  int getDocIdSetSize(DocIdSet docIdSet) throws Exception {
    int size = 0;
    DocIdSetIterator it = docIdSet.iterator();
    while (it.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
      size++;
    }
    return size;
  }
  public void assertContainsDocId(String msg, DocIdSet docIdSet, int docId) throws Exception {
    DocIdSetIterator it = docIdSet.iterator();
    assertTrue(msg, it.advance(docId) != DocIdSetIterator.NO_MORE_DOCS);
    assertTrue(msg, it.docID() == docId);
  }
}
