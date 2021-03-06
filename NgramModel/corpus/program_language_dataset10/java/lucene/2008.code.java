package org.apache.lucene.search.spans;
import java.io.IOException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.*;
public class TestSpansAdvanced2 extends TestSpansAdvanced {
    IndexSearcher searcher2;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final IndexWriter writer = new IndexWriter(mDirectory,
            new IndexWriterConfig(TEST_VERSION_CURRENT, 
                new StandardAnalyzer(TEST_VERSION_CURRENT)).setOpenMode(
                    OpenMode.APPEND));
        addDocument(writer, "A", "Should we, could we, would we?");
        addDocument(writer, "B", "It should.  Should it?");
        addDocument(writer, "C", "It shouldn't.");
        addDocument(writer, "D", "Should we, should we, should we.");
        writer.close();
        searcher2 = new IndexSearcher(mDirectory, true);
    }
    public void testVerifyIndex() throws Exception {
        final IndexReader reader = IndexReader.open(mDirectory, true);
        assertEquals(8, reader.numDocs());
        reader.close();
    }
    public void testSingleSpanQuery() throws IOException {
        final Query spanQuery = new SpanTermQuery(new Term(FIELD_TEXT, "should"));
        final String[] expectedIds = new String[] { "B", "D", "1", "2", "3", "4", "A" };
        final float[] expectedScores = new float[] { 0.625f, 0.45927936f, 0.35355338f, 0.35355338f, 0.35355338f,
                0.35355338f, 0.26516503f, };
        assertHits(searcher2, spanQuery, "single span query", expectedIds, expectedScores);
    }
    public void testMultipleDifferentSpanQueries() throws IOException {
        final Query spanQuery1 = new SpanTermQuery(new Term(FIELD_TEXT, "should"));
        final Query spanQuery2 = new SpanTermQuery(new Term(FIELD_TEXT, "we"));
        final BooleanQuery query = new BooleanQuery();
        query.add(spanQuery1, BooleanClause.Occur.MUST);
        query.add(spanQuery2, BooleanClause.Occur.MUST);
        final String[] expectedIds = new String[] { "D", "A" };
        final float[] expectedScores = new float[] { 1.0191123f, 0.93163157f };
        assertHits(searcher2, query, "multiple different span queries", expectedIds, expectedScores);
    }
    @Override
    public void testBooleanQueryWithSpanQueries() throws IOException {
        doTestBooleanQueryWithSpanQueries(searcher2, 0.73500174f);
    }
}
