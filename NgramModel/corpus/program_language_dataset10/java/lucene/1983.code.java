package org.apache.lucene.search;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
public class TestTermScorer extends LuceneTestCase
{
    protected RAMDirectory directory;
    private static final String FIELD = "field";
    protected String[] values = new String[]{"all", "dogs dogs", "like", "playing", "fetch", "all"};
    protected IndexSearcher indexSearcher;
    protected IndexReader indexReader;
    public TestTermScorer(String s)
    {
        super(s);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        directory = new RAMDirectory();
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
        for (int i = 0; i < values.length; i++) {
            Document doc = new Document();
            doc.add(new Field(FIELD, values[i], Field.Store.YES, Field.Index.ANALYZED));
            writer.addDocument(doc);
        }
        writer.close();
        indexSearcher = new IndexSearcher(directory, false);
        indexReader = indexSearcher.getIndexReader();
    }
    public void test() throws IOException {
        Term allTerm = new Term(FIELD, "all");
        TermQuery termQuery = new TermQuery(allTerm);
        Weight weight = termQuery.weight(indexSearcher);
        TermScorer ts = new TermScorer(weight,
                                       indexReader.termDocs(allTerm), indexSearcher.getSimilarity(),
                                       indexReader.norms(FIELD));
        final List<TestHit> docs = new ArrayList<TestHit>();
        ts.score(new Collector() {
            private int base = 0;
            private Scorer scorer;
            @Override
            public void setScorer(Scorer scorer) throws IOException {
              this.scorer = scorer; 
            }
            @Override
            public void collect(int doc) throws IOException {
              float score = scorer.score();
              doc = doc + base;
              docs.add(new TestHit(doc, score));
              assertTrue("score " + score + " is not greater than 0", score > 0);
              assertTrue("Doc: " + doc + " does not equal 0 or doc does not equal 5",
                            doc == 0 || doc == 5);
            }
            @Override
            public void setNextReader(IndexReader reader, int docBase) {
              base = docBase;
            }
            @Override
            public boolean acceptsDocsOutOfOrder() {
              return true;
            }
        });
        assertTrue("docs Size: " + docs.size() + " is not: " + 2, docs.size() == 2);
        TestHit doc0 =  docs.get(0);
        TestHit doc5 =  docs.get(1);
        assertTrue(doc0.score + " does not equal: " + doc5.score, doc0.score == doc5.score);
        assertTrue(doc0.score + " does not equal: " + 1.6931472f, doc0.score == 1.6931472f);
    }
    public void testNext() throws Exception {
        Term allTerm = new Term(FIELD, "all");
        TermQuery termQuery = new TermQuery(allTerm);
        Weight weight = termQuery.weight(indexSearcher);
        TermScorer ts = new TermScorer(weight,
                                       indexReader.termDocs(allTerm), indexSearcher.getSimilarity(),
                                       indexReader.norms(FIELD));
        assertTrue("next did not return a doc", ts.nextDoc() != DocIdSetIterator.NO_MORE_DOCS);
        assertTrue("score is not correct", ts.score() == 1.6931472f);
        assertTrue("next did not return a doc", ts.nextDoc() != DocIdSetIterator.NO_MORE_DOCS);
        assertTrue("score is not correct", ts.score() == 1.6931472f);
        assertTrue("next returned a doc and it should not have", ts.nextDoc() == DocIdSetIterator.NO_MORE_DOCS);
    }
    public void testSkipTo() throws Exception {
        Term allTerm = new Term(FIELD, "all");
        TermQuery termQuery = new TermQuery(allTerm);
        Weight weight = termQuery.weight(indexSearcher);
        TermScorer ts = new TermScorer(weight,
                                       indexReader.termDocs(allTerm), indexSearcher.getSimilarity(),
                                       indexReader.norms(FIELD));
        assertTrue("Didn't skip", ts.advance(3) != DocIdSetIterator.NO_MORE_DOCS);
        assertTrue("doc should be number 5", ts.docID() == 5);
    }
    private class TestHit {
        public int doc;
        public float score;
        public TestHit(int doc, float score) {
            this.doc = doc;
            this.score = score;
        }
        @Override
        public String toString() {
            return "TestHit{" + "doc=" + doc + ", score=" + score + "}";
        }
    }
}
