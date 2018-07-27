package org.apache.lucene;
import java.util.GregorianCalendar;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.lucene.util.LuceneTestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import org.apache.lucene.store.*;
import org.apache.lucene.document.*;
import org.apache.lucene.analysis.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.queryParser.*;
public class TestSearch extends LuceneTestCase {
    public static void main(String args[]) {
        TestRunner.run (new TestSuite(TestSearch.class));
    }
    public void testSearch() throws Exception {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw, true);
      doTestSearch(pw, false);
      pw.close();
      sw.close();
      String multiFileOutput = sw.getBuffer().toString();
      sw = new StringWriter();
      pw = new PrintWriter(sw, true);
      doTestSearch(pw, true);
      pw.close();
      sw.close();
      String singleFileOutput = sw.getBuffer().toString();
      assertEquals(multiFileOutput, singleFileOutput);
    }
    private void doTestSearch(PrintWriter out, boolean useCompoundFile)
    throws Exception {
      Directory directory = new RAMDirectory();
      Analyzer analyzer = new SimpleAnalyzer(TEST_VERSION_CURRENT);
      IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer));
      LogMergePolicy lmp = (LogMergePolicy) writer.getMergePolicy();
      lmp.setUseCompoundFile(useCompoundFile);
      lmp.setUseCompoundDocStore(useCompoundFile);
      String[] docs = {
        "a b c d e",
        "a b c d e a b c d e",
        "a b c d e f g h i j",
        "a c e",
        "e c a",
        "a c e a c e",
        "a c e a b c"
      };
      for (int j = 0; j < docs.length; j++) {
        Document d = new Document();
        d.add(new Field("contents", docs[j], Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(d);
      }
      writer.close();
      Searcher searcher = new IndexSearcher(directory, true);
      String[] queries = {
        "a b",
        "\"a b\"",
        "\"a b c\"",
        "a c",
        "\"a c\"",
        "\"a c e\"",
      };
      ScoreDoc[] hits = null;
      QueryParser parser = new QueryParser(TEST_VERSION_CURRENT, "contents", analyzer);
      parser.setPhraseSlop(4);
      for (int j = 0; j < queries.length; j++) {
        Query query = parser.parse(queries[j]);
        out.println("Query: " + query.toString("contents"));
        hits = searcher.search(query, null, 1000).scoreDocs;
        out.println(hits.length + " total results");
        for (int i = 0 ; i < hits.length && i < 10; i++) {
          Document d = searcher.doc(hits[i].doc);
          out.println(i + " " + hits[i].score
                             + " " + d.get("contents"));
        }
      }
      searcher.close();
  }
  static long Time(int year, int month, int day) {
    GregorianCalendar calendar = new GregorianCalendar();
    calendar.clear();
    calendar.set(year, month, day);
    return calendar.getTime().getTime();
  }
}
