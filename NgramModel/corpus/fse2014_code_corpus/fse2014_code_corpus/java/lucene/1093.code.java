package org.apache.lucene.search.similar;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestMoreLikeThis extends LuceneTestCase {
    private RAMDirectory directory;
    private IndexReader reader;
    private IndexSearcher searcher;
    @Override
    protected void setUp() throws Exception {
      super.setUp();
	directory = new RAMDirectory();
	IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new StandardAnalyzer(TEST_VERSION_CURRENT)));
	addDoc(writer, "lucene");
	addDoc(writer, "lucene release");
	writer.close();
	reader = IndexReader.open(directory, true);
	searcher = new IndexSearcher(reader);
    }
    @Override
    protected void tearDown() throws Exception {
	reader.close();
	searcher.close();
	directory.close();
  super.tearDown();
    }
    private void addDoc(IndexWriter writer, String text) throws IOException {
	Document doc = new Document();
	doc.add(new Field("text", text, Field.Store.YES, Field.Index.ANALYZED));
	writer.addDocument(doc);
    }
    public void testBoostFactor() throws Throwable {
	Map<String,Float> originalValues = getOriginalValues();
	MoreLikeThis mlt = new MoreLikeThis(
		reader);
	mlt.setMinDocFreq(1);
	mlt.setMinTermFreq(1);
	mlt.setMinWordLen(1);
	mlt.setFieldNames(new String[] { "text" });
	mlt.setBoost(true);
	float boostFactor = 5;
	mlt.setBoostFactor(boostFactor);
	BooleanQuery query = (BooleanQuery) mlt.like(new StringReader(
		"lucene release"));
	List<BooleanClause> clauses = query.clauses();
	assertEquals("Expected " + originalValues.size() + " clauses.",
		originalValues.size(), clauses.size());
	for (int i = 0; i < clauses.size(); i++) {
	    BooleanClause clause =  clauses.get(i);
	    TermQuery tq = (TermQuery) clause.getQuery();
	    Float termBoost = originalValues.get(tq.getTerm().text());
	    assertNotNull("Expected term " + tq.getTerm().text(), termBoost);
	    float totalBoost = termBoost.floatValue() * boostFactor;
	    assertEquals("Expected boost of " + totalBoost + " for term '"
                         + tq.getTerm().text() + "' got " + tq.getBoost(),
                         totalBoost, tq.getBoost(), 0.0001);
	}
    }
    private Map<String,Float> getOriginalValues() throws IOException {
	Map<String,Float> originalValues = new HashMap<String,Float>();
	MoreLikeThis mlt = new MoreLikeThis(reader);
	mlt.setMinDocFreq(1);
	mlt.setMinTermFreq(1);
	mlt.setMinWordLen(1);
	mlt.setFieldNames(new String[] { "text" });
	mlt.setBoost(true);
	BooleanQuery query = (BooleanQuery) mlt.like(new StringReader(
		"lucene release"));
	List<BooleanClause> clauses = query.clauses();
	for (int i = 0; i < clauses.size(); i++) {
	    BooleanClause clause = clauses.get(i);
	    TermQuery tq = (TermQuery) clause.getQuery();
	    originalValues.put(tq.getTerm().text(), Float.valueOf(tq.getBoost()));
	}
	return originalValues;
    }
}
