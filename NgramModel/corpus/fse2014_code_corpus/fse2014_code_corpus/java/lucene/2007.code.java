package org.apache.lucene.search.spans;
import java.io.IOException;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
public class TestSpansAdvanced extends LuceneTestCase {
    protected Directory mDirectory;
    protected IndexSearcher searcher;
    private final static String FIELD_ID = "ID";
    protected final static String FIELD_TEXT = "TEXT";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDirectory = new RAMDirectory();
        final IndexWriter writer = new IndexWriter(mDirectory,
        new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
        addDocument(writer, "1", "I think it should work.");
        addDocument(writer, "2", "I think it should work.");
        addDocument(writer, "3", "I think it should work.");
        addDocument(writer, "4", "I think it should work.");
        writer.close();
        searcher = new IndexSearcher(mDirectory, true);
    }
    @Override
    protected void tearDown() throws Exception {
        searcher.close();
        mDirectory.close();
        mDirectory = null;
        super.tearDown();
    }
    protected void addDocument(final IndexWriter writer, final String id, final String text) throws IOException {
        final Document document = new Document();
        document.add(new Field(FIELD_ID, id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        document.add(new Field(FIELD_TEXT, text, Field.Store.YES, Field.Index.ANALYZED));
        writer.addDocument(document);
    }
    public void testBooleanQueryWithSpanQueries() throws IOException {
        doTestBooleanQueryWithSpanQueries(searcher,0.3884282f);
    }
    protected void doTestBooleanQueryWithSpanQueries(IndexSearcher s, final float expectedScore) throws IOException {
        final Query spanQuery = new SpanTermQuery(new Term(FIELD_TEXT, "work"));
        final BooleanQuery query = new BooleanQuery();
        query.add(spanQuery, BooleanClause.Occur.MUST);
        query.add(spanQuery, BooleanClause.Occur.MUST);
        final String[] expectedIds = new String[] { "1", "2", "3", "4" };
        final float[] expectedScores = new float[] { expectedScore, expectedScore, expectedScore, expectedScore };
        assertHits(s, query, "two span queries", expectedIds, expectedScores);
    }
    protected static void assertHits(Searcher s, Query query, final String description, final String[] expectedIds,
            final float[] expectedScores) throws IOException {
        QueryUtils.check(query,s);
        final float tolerance = 1e-5f;
        TopDocs topdocs = s.search(query,null,10000);
        assertEquals(expectedIds.length, topdocs.totalHits);
        for (int i = 0; i < topdocs.totalHits; i++) {
            int id = topdocs.scoreDocs[i].doc;
            float score = topdocs.scoreDocs[i].score;
            Document doc = s.doc(id);
            assertEquals(expectedIds[i], doc.get(FIELD_ID));
            boolean scoreEq = Math.abs(expectedScores[i] - score) < tolerance;
            if (!scoreEq) {
              System.out.println(i + " warning, expected score: " + expectedScores[i] + ", actual " + score);
              System.out.println(s.explain(query,id));
            }
            assertEquals(expectedScores[i], score, tolerance);
            assertEquals(s.explain(query,id).getValue(), score, tolerance);
        }
    }
}