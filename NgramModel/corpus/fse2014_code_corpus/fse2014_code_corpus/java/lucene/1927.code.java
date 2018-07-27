package org.apache.lucene.search;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import junit.framework.Assert;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.store.RAMDirectory;
import static org.apache.lucene.util.LuceneTestCaseJ4.TEST_VERSION_CURRENT;
public class QueryUtils {
  public static void check(Query q) {
    checkHashEquals(q);
  }
  public static void checkHashEquals(Query q) {
    Query q2 = (Query)q.clone();
    checkEqual(q,q2);
    Query q3 = (Query)q.clone();
    q3.setBoost(7.21792348f);
    checkUnequal(q,q3);
    Query whacky = new Query() {
      @Override
      public String toString(String field) {
        return "My Whacky Query";
      }
    };
    whacky.setBoost(q.getBoost());
    checkUnequal(q, whacky);
  }
  public static void checkEqual(Query q1, Query q2) {
    Assert.assertEquals(q1, q2);
    Assert.assertEquals(q1.hashCode(), q2.hashCode());
  }
  public static void checkUnequal(Query q1, Query q2) {
    Assert.assertTrue(!q1.equals(q2));
    Assert.assertTrue(!q2.equals(q1));
    Assert.assertTrue(q1.hashCode() != q2.hashCode());
  }
  public static void checkExplanations (final Query q, final Searcher s) throws IOException {
    CheckHits.checkExplanations(q, null, s, true);
  }
  public static void check(Query q1, Searcher s) {
    check(q1, s, true);
  }
  private static void check(Query q1, Searcher s, boolean wrap) {
    try {
      check(q1);
      if (s!=null) {
        if (s instanceof IndexSearcher) {
          IndexSearcher is = (IndexSearcher)s;
          checkFirstSkipTo(q1,is);
          checkSkipTo(q1,is);
          if (wrap) {
            check(q1, wrapUnderlyingReader(is, -1), false);
            check(q1, wrapUnderlyingReader(is,  0), false);
            check(q1, wrapUnderlyingReader(is, +1), false);
          }
        }
        if (wrap) {
          check(q1, wrapSearcher(s, -1), false);
          check(q1, wrapSearcher(s,  0), false);
          check(q1, wrapSearcher(s, +1), false);
        }
        checkExplanations(q1,s);
        checkSerialization(q1,s);
        Query q2 = (Query)q1.clone();
        checkEqual(s.rewrite(q1),
                   s.rewrite(q2));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public static IndexSearcher wrapUnderlyingReader(final IndexSearcher s, final int edge) 
    throws IOException {
    IndexReader r = s.getIndexReader();
    IndexReader[] readers = new IndexReader[] {
      edge < 0 ? r : IndexReader.open(makeEmptyIndex(0), true),
      IndexReader.open(makeEmptyIndex(0), true),
      new MultiReader(new IndexReader[] {
        IndexReader.open(makeEmptyIndex(edge < 0 ? 4 : 0), true),
        IndexReader.open(makeEmptyIndex(0), true),
        0 == edge ? r : IndexReader.open(makeEmptyIndex(0), true)
      }),
      IndexReader.open(makeEmptyIndex(0 < edge ? 0 : 7), true),
      IndexReader.open(makeEmptyIndex(0), true),
      new MultiReader(new IndexReader[] {
        IndexReader.open(makeEmptyIndex(0 < edge ? 0 : 5), true),
        IndexReader.open(makeEmptyIndex(0), true),
        0 < edge ? r : IndexReader.open(makeEmptyIndex(0), true)
      })
    };
    IndexSearcher out = new IndexSearcher(new MultiReader(readers));
    out.setSimilarity(s.getSimilarity());
    return out;
  }
  public static MultiSearcher wrapSearcher(final Searcher s, final int edge) 
    throws IOException {
    Searcher[] searchers = new Searcher[] {
      edge < 0 ? s : new IndexSearcher(makeEmptyIndex(0), true),
      new MultiSearcher(new Searcher[] {
        new IndexSearcher(makeEmptyIndex(edge < 0 ? 65 : 0), true),
        new IndexSearcher(makeEmptyIndex(0), true),
        0 == edge ? s : new IndexSearcher(makeEmptyIndex(0), true)
      }),
      new IndexSearcher(makeEmptyIndex(0 < edge ? 0 : 3), true),
      new IndexSearcher(makeEmptyIndex(0), true),
      new MultiSearcher(new Searcher[] {
        new IndexSearcher(makeEmptyIndex(0 < edge ? 0 : 5), true),
        new IndexSearcher(makeEmptyIndex(0), true),
        0 < edge ? s : new IndexSearcher(makeEmptyIndex(0), true)
      })
    };
    MultiSearcher out = new MultiSearcher(searchers);
    out.setSimilarity(s.getSimilarity());
    return out;
  }
  private static RAMDirectory makeEmptyIndex(final int numDeletedDocs) 
    throws IOException {
      RAMDirectory d = new RAMDirectory();
      IndexWriter w = new IndexWriter(d, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
      for (int i = 0; i < numDeletedDocs; i++) {
        w.addDocument(new Document());
      }
      w.commit();
      w.deleteDocuments( new MatchAllDocsQuery() );
      w.commit();
      if (0 < numDeletedDocs)
        Assert.assertTrue("writer has no deletions", w.hasDeletions());
      Assert.assertEquals("writer is missing some deleted docs", 
                          numDeletedDocs, w.maxDoc());
      Assert.assertEquals("writer has non-deleted docs", 
                          0, w.numDocs());
      w.close();
      IndexReader r = IndexReader.open(d, true);
      Assert.assertEquals("reader has wrong number of deleted docs", 
                          numDeletedDocs, r.numDeletedDocs());
      r.close();
      return d;
  }
  private static void checkSerialization(Query q, Searcher s) throws IOException {
    Weight w = q.weight(s);
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(bos);
      oos.writeObject(w);
      oos.close();
      ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
      ois.readObject();
      ois.close();
    } catch (Exception e) {
      IOException e2 = new IOException("Serialization failed for "+w);
      e2.initCause(e);
      throw e2;
    }
  }
  public static void checkSkipTo(final Query q, final IndexSearcher s) throws IOException {
    if (q.weight(s).scoresDocsOutOfOrder()) return;  
    final int skip_op = 0;
    final int next_op = 1;
    final int orders [][] = {
        {next_op},
        {skip_op},
        {skip_op, next_op},
        {next_op, skip_op},
        {skip_op, skip_op, next_op, next_op},
        {next_op, next_op, skip_op, skip_op},
        {skip_op, skip_op, skip_op, next_op, next_op},
    };
    for (int k = 0; k < orders.length; k++) {
        final int order[] = orders[k];
        final int opidx[] = { 0 };
        final int lastDoc[] = {-1};
        final float maxDiff = 1e-5f;
        final IndexReader lastReader[] = {null};
        s.search(q, new Collector() {
          private Scorer sc;
          private IndexReader reader;
          private Scorer scorer;
          @Override
          public void setScorer(Scorer scorer) throws IOException {
            this.sc = scorer;
          }
          @Override
          public void collect(int doc) throws IOException {
            float score = sc.score();
            lastDoc[0] = doc;
            try {
              if (scorer == null) {
                Weight w = q.weight(s);
                scorer = w.scorer(reader, true, false);
              }
              int op = order[(opidx[0]++) % order.length];
              boolean more = op == skip_op ? scorer.advance(scorer.docID() + 1) != DocIdSetIterator.NO_MORE_DOCS
                  : scorer.nextDoc() != DocIdSetIterator.NO_MORE_DOCS;
              int scorerDoc = scorer.docID();
              float scorerScore = scorer.score();
              float scorerScore2 = scorer.score();
              float scoreDiff = Math.abs(score - scorerScore);
              float scorerDiff = Math.abs(scorerScore2 - scorerScore);
              if (!more || doc != scorerDoc || scoreDiff > maxDiff
                  || scorerDiff > maxDiff) {
                StringBuilder sbord = new StringBuilder();
                for (int i = 0; i < order.length; i++)
                  sbord.append(order[i] == skip_op ? " skip()" : " next()");
                throw new RuntimeException("ERROR matching docs:" + "\n\t"
                    + (doc != scorerDoc ? "--> " : "") + "doc=" + doc + ", scorerDoc=" + scorerDoc
                    + "\n\t" + (!more ? "--> " : "") + "tscorer.more=" + more
                    + "\n\t" + (scoreDiff > maxDiff ? "--> " : "")
                    + "scorerScore=" + scorerScore + " scoreDiff=" + scoreDiff
                    + " maxDiff=" + maxDiff + "\n\t"
                    + (scorerDiff > maxDiff ? "--> " : "") + "scorerScore2="
                    + scorerScore2 + " scorerDiff=" + scorerDiff
                    + "\n\thitCollector.doc=" + doc + " score=" + score
                    + "\n\t Scorer=" + scorer + "\n\t Query=" + q + "  "
                    + q.getClass().getName() + "\n\t Searcher=" + s
                    + "\n\t Order=" + sbord + "\n\t Op="
                    + (op == skip_op ? " skip()" : " next()"));
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
          @Override
          public void setNextReader(IndexReader reader, int docBase) throws IOException {
            if (lastReader[0] != null) {
              final IndexReader previousReader = lastReader[0];
              Weight w = q.weight(new IndexSearcher(previousReader));
              Scorer scorer = w.scorer(previousReader, true, false);
              if (scorer != null) {
                boolean more = scorer.advance(lastDoc[0] + 1) != DocIdSetIterator.NO_MORE_DOCS;
                Assert.assertFalse("query's last doc was "+ lastDoc[0] +" but skipTo("+(lastDoc[0]+1)+") got to "+scorer.docID(),more);
              }
            }
            this.reader = reader;
            this.scorer = null;
            lastDoc[0] = -1;
          }
          @Override
          public boolean acceptsDocsOutOfOrder() {
            return true;
          }
        });
        if (lastReader[0] != null) {
          final IndexReader previousReader = lastReader[0];
          Weight w = q.weight(new IndexSearcher(previousReader));
          Scorer scorer = w.scorer(previousReader, true, false);
          if (scorer != null) {
            boolean more = scorer.advance(lastDoc[0] + 1) != DocIdSetIterator.NO_MORE_DOCS;
            Assert.assertFalse("query's last doc was "+ lastDoc[0] +" but skipTo("+(lastDoc[0]+1)+") got to "+scorer.docID(),more);
          }
        }
      }
  }
  private static void checkFirstSkipTo(final Query q, final IndexSearcher s) throws IOException {
    final float maxDiff = 1e-5f;
    final int lastDoc[] = {-1};
    final IndexReader lastReader[] = {null};
    s.search(q,new Collector() {
      private Scorer scorer;
      private IndexReader reader;
      @Override
      public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
      }
      @Override
      public void collect(int doc) throws IOException {
        float score = scorer.score();
        try {
          for (int i=lastDoc[0]+1; i<=doc; i++) {
            Weight w = q.weight(s);
            Scorer scorer = w.scorer(reader, true, false);
            Assert.assertTrue("query collected "+doc+" but skipTo("+i+") says no more docs!",scorer.advance(i) != DocIdSetIterator.NO_MORE_DOCS);
            Assert.assertEquals("query collected "+doc+" but skipTo("+i+") got to "+scorer.docID(),doc,scorer.docID());
            float skipToScore = scorer.score();
            Assert.assertEquals("unstable skipTo("+i+") score!",skipToScore,scorer.score(),maxDiff); 
            Assert.assertEquals("query assigned doc "+doc+" a score of <"+score+"> but skipTo("+i+") has <"+skipToScore+">!",score,skipToScore,maxDiff);
          }
          lastDoc[0] = doc;
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      @Override
      public void setNextReader(IndexReader reader, int docBase) throws IOException {
        if (lastReader[0] != null) {
          final IndexReader previousReader = lastReader[0];
          Weight w = q.weight(new IndexSearcher(previousReader));
          Scorer scorer = w.scorer(previousReader, true, false);
          if (scorer != null) {
            boolean more = scorer.advance(lastDoc[0] + 1) != DocIdSetIterator.NO_MORE_DOCS;
            Assert.assertFalse("query's last doc was "+ lastDoc[0] +" but skipTo("+(lastDoc[0]+1)+") got to "+scorer.docID(),more);
          }
        }
        this.reader = lastReader[0] = reader;
        lastDoc[0] = -1;
      }
      @Override
      public boolean acceptsDocsOutOfOrder() {
        return false;
      }
    });
    if (lastReader[0] != null) {
      final IndexReader previousReader = lastReader[0];
      Weight w = q.weight(new IndexSearcher(previousReader));
      Scorer scorer = w.scorer(previousReader, true, false);
      if (scorer != null) {
        boolean more = scorer.advance(lastDoc[0] + 1) != DocIdSetIterator.NO_MORE_DOCS;
        Assert.assertFalse("query's last doc was "+ lastDoc[0] +" but skipTo("+(lastDoc[0]+1)+") got to "+scorer.docID(),more);
      }
    }
  }
}
