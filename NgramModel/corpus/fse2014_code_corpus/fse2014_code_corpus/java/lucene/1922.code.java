package org.apache.lucene.search;
import java.util.Random;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.RAMDirectory;
public class BaseTestRangeFilter extends LuceneTestCase {
    public static final boolean F = false;
    public static final boolean T = true;
    protected Random rand;
    class TestIndex { 
        int maxR;
        int minR;
        boolean allowNegativeRandomInts;
        RAMDirectory index = new RAMDirectory();
        TestIndex(int minR, int maxR, boolean allowNegativeRandomInts) {
            this.minR = minR;
            this.maxR = maxR;
            this.allowNegativeRandomInts = allowNegativeRandomInts;
        }
    }
    TestIndex signedIndex = new TestIndex(Integer.MAX_VALUE, Integer.MIN_VALUE, true);
    TestIndex unsignedIndex = new TestIndex(Integer.MAX_VALUE, 0, false);
    int minId = 0;
    int maxId = 10000;
    static final int intLength = Integer.toString(Integer.MAX_VALUE).length();
    public static String pad(int n) {
        StringBuilder b = new StringBuilder(40);
        String p = "0";
        if (n < 0) {
            p = "-";
            n = Integer.MAX_VALUE + n + 1;
        }
        b.append(p);
        String s = Integer.toString(n);
        for (int i = s.length(); i <= intLength; i++) {
            b.append("0");
        }
        b.append(s);
        return b.toString();
    }
    public BaseTestRangeFilter(String name) {
	super(name);
        rand = newRandom();
        build(signedIndex);
        build(unsignedIndex);
    }
    public BaseTestRangeFilter() {
        rand = newRandom();
        build(signedIndex);
        build(unsignedIndex);
    }
    private void build(TestIndex index) {
        try {
          IndexWriter writer = new IndexWriter(index.index, new IndexWriterConfig(
              TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT))
                  .setOpenMode(OpenMode.CREATE));
          for (int d = minId; d <= maxId; d++) {
                Document doc = new Document();
                doc.add(new Field("id",pad(d), Field.Store.YES, Field.Index.NOT_ANALYZED));
                int r= index.allowNegativeRandomInts 
                       ? rand.nextInt() : rand.nextInt(Integer.MAX_VALUE);
                if (index.maxR < r) {
                    index.maxR = r;
                }
                  if (r < index.minR) {
                    index.minR = r;
                }
                doc.add(new Field("rand",pad(r), Field.Store.YES, Field.Index.NOT_ANALYZED));
                doc.add(new Field("body","body", Field.Store.YES, Field.Index.NOT_ANALYZED));
                writer.addDocument(doc);
            }
            writer.optimize();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("can't build index", e);
        }
    }
    public void testPad() {
        int[] tests = new int[] {
            -9999999, -99560, -100, -3, -1, 0, 3, 9, 10, 1000, 999999999
        };
        for (int i = 0; i < tests.length - 1; i++) {
            int a = tests[i];
            int b = tests[i+1];
            String aa = pad(a);
            String bb = pad(b);
            String label = a + ":" + aa + " vs " + b + ":" + bb;
            assertEquals("length of " + label, aa.length(), bb.length());
            assertTrue("compare less than " + label, aa.compareTo(bb) < 0);
        }
    }
}
