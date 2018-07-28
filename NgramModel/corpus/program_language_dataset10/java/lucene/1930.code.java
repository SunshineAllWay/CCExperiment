package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import java.text.DecimalFormat;
import java.util.Random;
public class TestBooleanMinShouldMatch extends LuceneTestCase {
    public Directory index;
    public IndexReader r;
    public IndexSearcher s;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        String[] data = new String [] {
            "A 1 2 3 4 5 6",
            "Z       4 5 6",
            null,
            "B   2   4 5 6",
            "Y     3   5 6",
            null,
            "C     3     6",
            "X       4 5 6"
        };
        index = new RAMDirectory();
        IndexWriter writer = new IndexWriter(index, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < data.length; i++) {
            Document doc = new Document();
            doc.add(new Field("id", String.valueOf(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("all", "all", Field.Store.YES, Field.Index.NOT_ANALYZED));
            if (null != data[i]) {
                doc.add(new Field("data", data[i], Field.Store.YES, Field.Index.ANALYZED));
            }
            writer.addDocument(doc);
        }
        writer.optimize();
        writer.close();
        r = IndexReader.open(index, true);
        s = new IndexSearcher(r);
    }
    public void verifyNrHits(Query q, int expected) throws Exception {
        ScoreDoc[] h = s.search(q, null, 1000).scoreDocs;
        if (expected != h.length) {
            printHits(getName(), h, s);
        }
        assertEquals("result count", expected, h.length);
        QueryUtils.check(q,s);
    }
    public void testAllOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        for (int i = 1; i <=4; i++) {
            q.add(new TermQuery(new Term("data",""+i)), BooleanClause.Occur.SHOULD);
        }
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 2);
    }
    public void testOneReqAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 5);
    }
    public void testSomeReqAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 5);
    }
    public void testOneProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 1);
    }
    public void testSomeProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "C"  )), BooleanClause.Occur.MUST_NOT);
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 1);
    }
    public void testOneReqOneProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(3); 
        verifyNrHits(q, 1);
    }
    public void testSomeReqOneProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all",  "all")), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(3); 
        verifyNrHits(q, 1);
    }
    public void testOneReqSomeProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "C"  )), BooleanClause.Occur.MUST_NOT);
        q.setMinimumNumberShouldMatch(3); 
        verifyNrHits(q, 1);
    }
    public void testSomeReqSomeProhibAndSomeOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all",  "all")), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "C"  )), BooleanClause.Occur.MUST_NOT);
        q.setMinimumNumberShouldMatch(3); 
        verifyNrHits(q, 1);
    }
    public void testMinHigherThenNumOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all",  "all")), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "5"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "4"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST_NOT);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "1"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "C"  )), BooleanClause.Occur.MUST_NOT);
        q.setMinimumNumberShouldMatch(90); 
        verifyNrHits(q, 0);
    }
    public void testMinEqualToNumOptional() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "6"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.SHOULD);
        q.setMinimumNumberShouldMatch(2); 
        verifyNrHits(q, 1);
    }
    public void testOneOptionalEqualToMin() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "3"  )), BooleanClause.Occur.SHOULD);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.MUST);
        q.setMinimumNumberShouldMatch(1); 
        verifyNrHits(q, 1);
    }
    public void testNoOptionalButMin() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.MUST);
        q.add(new TermQuery(new Term("data", "2"  )), BooleanClause.Occur.MUST);
        q.setMinimumNumberShouldMatch(1); 
        verifyNrHits(q, 0);
    }
    public void testNoOptionalButMin2() throws Exception {
        BooleanQuery q = new BooleanQuery();
        q.add(new TermQuery(new Term("all", "all" )), BooleanClause.Occur.MUST);
        q.setMinimumNumberShouldMatch(1); 
        verifyNrHits(q, 0);
    }
    public void testRandomQueries() throws Exception {
      final Random rnd = newRandom();
      String field="data";
      String[] vals = {"1","2","3","4","5","6","A","Z","B","Y","Z","X","foo"};
      int maxLev=4;
      TestBoolean2.Callback minNrCB = new TestBoolean2.Callback() {
        public void postCreate(BooleanQuery q) {
          BooleanClause[] c =q.getClauses();
          int opt=0;
          for (int i=0; i<c.length;i++) {
            if (c[i].getOccur() == BooleanClause.Occur.SHOULD) opt++;
          }
          q.setMinimumNumberShouldMatch(rnd.nextInt(opt+2));
        }
      };
      for (int i=0; i<50; i++) {
        int lev = rnd.nextInt(maxLev);
        final long seed = rnd.nextLong();
        BooleanQuery q1 = TestBoolean2.randBoolQuery(new Random(seed), true, lev, field, vals, null);
        BooleanQuery q2 = TestBoolean2.randBoolQuery(new Random(seed), true, lev, field, vals, null);
        minNrCB.postCreate(q2);
        TopDocs top1 = s.search(q1,null,100);
        TopDocs top2 = s.search(q2,null,100);
        if (i < 100) {
          QueryUtils.check(q1,s);
          QueryUtils.check(q2,s);
        }
        if (top2.totalHits > top1.totalHits) {
          fail("Constrained results not a subset:\n"
                        + CheckHits.topdocsString(top1,0,0)
                        + CheckHits.topdocsString(top2,0,0)
                        + "for query:" + q2.toString());
        }
        for (int hit=0; hit<top2.totalHits; hit++) {
          int id = top2.scoreDocs[hit].doc;
          float score = top2.scoreDocs[hit].score;
          boolean found=false;
          for (int other=0; other<top1.totalHits; other++) {
            if (top1.scoreDocs[other].doc == id) {
              found=true;
              float otherScore = top1.scoreDocs[other].score;
              if (Math.abs(otherScore-score)>1.0e-6f) {
                        fail("Doc " + id + " scores don't match\n"
                + CheckHits.topdocsString(top1,0,0)
                + CheckHits.topdocsString(top2,0,0)
                + "for query:" + q2.toString());
              }
            }
          }
          if (!found) fail("Doc " + id + " not found\n"
                + CheckHits.topdocsString(top1,0,0)
                + CheckHits.topdocsString(top2,0,0)
                + "for query:" + q2.toString());
        }
      }
    }
    protected void printHits(String test, ScoreDoc[] h, Searcher searcher) throws Exception {
        System.err.println("------- " + test + " -------");
        DecimalFormat f = new DecimalFormat("0.000000");
        for (int i = 0; i < h.length; i++) {
            Document d = searcher.doc(h[i].doc);
            float score = h[i].score;
            System.err.println("#" + i + ": " + f.format(score) + " - " +
                               d.get("id") + " - " + d.get("data"));
        }
    }
}
