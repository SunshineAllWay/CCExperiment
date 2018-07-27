package org.apache.lucene.search;
import java.util.Random;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestMultiValuedNumericRangeQuery extends LuceneTestCase {
  public void testMultiValuedNRQ() throws Exception {
    final Random rnd = newRandom();
    RAMDirectory directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    DecimalFormat format = new DecimalFormat("00000000000", new DecimalFormatSymbols(Locale.US));
    for (int l=0; l<5000; l++) {
      Document doc = new Document();
      for (int m=0, c=rnd.nextInt(10); m<=c; m++) {
        int value = rnd.nextInt(Integer.MAX_VALUE);
        doc.add(new Field("asc", format.format(value), Field.Store.NO, Field.Index.NOT_ANALYZED));
        doc.add(new NumericField("trie", Field.Store.NO, true).setIntValue(value));
      }
      writer.addDocument(doc);
    }  
    writer.close();
    Searcher searcher=new IndexSearcher(directory, true);
    for (int i=0; i<50; i++) {
      int lower=rnd.nextInt(Integer.MAX_VALUE);
      int upper=rnd.nextInt(Integer.MAX_VALUE);
      if (lower>upper) {
        int a=lower; lower=upper; upper=a;
      }
      TermRangeQuery cq=new TermRangeQuery("asc", format.format(lower), format.format(upper), true, true);
      NumericRangeQuery<Integer> tq=NumericRangeQuery.newIntRange("trie", lower, upper, true, true);
      TopDocs trTopDocs = searcher.search(cq, 1);
      TopDocs nrTopDocs = searcher.search(tq, 1);
      assertEquals("Returned count for NumericRangeQuery and TermRangeQuery must be equal", trTopDocs.totalHits, nrTopDocs.totalHits );
    }
    searcher.close();
    directory.close();
  }
}
