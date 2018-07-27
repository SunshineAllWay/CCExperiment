package org.apache.lucene.search.payloads;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.English;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.QueryUtils;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.CheckHits;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.search.spans.TermSpans;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Payload;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import java.io.Reader;
import java.io.IOException;
public class TestPayloadTermQuery extends LuceneTestCase {
  private IndexSearcher searcher;
  private BoostingSimilarity similarity = new BoostingSimilarity();
  private byte[] payloadField = new byte[]{1};
  private byte[] payloadMultiField1 = new byte[]{2};
  private byte[] payloadMultiField2 = new byte[]{4};
  protected RAMDirectory directory;
  public TestPayloadTermQuery(String s) {
    super(s);
  }
  private class PayloadAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream result = new LowerCaseTokenizer(TEST_VERSION_CURRENT, reader);
      result = new PayloadFilter(result, fieldName);
      return result;
    }
  }
  private class PayloadFilter extends TokenFilter {
    String fieldName;
    int numSeen = 0;
    PayloadAttribute payloadAtt;    
    public PayloadFilter(TokenStream input, String fieldName) {
      super(input);
      this.fieldName = fieldName;
      payloadAtt = addAttribute(PayloadAttribute.class);
    }
    @Override
    public boolean incrementToken() throws IOException {
      boolean hasNext = input.incrementToken();
      if (hasNext) {
        if (fieldName.equals("field")) {
          payloadAtt.setPayload(new Payload(payloadField));
        } else if (fieldName.equals("multiField")) {
          if (numSeen % 2 == 0) {
            payloadAtt.setPayload(new Payload(payloadMultiField1));
          } else {
            payloadAtt.setPayload(new Payload(payloadMultiField2));
          }
          numSeen++;
        }
        return true;
      } else {
        return false;
      }
    }
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    directory = new RAMDirectory();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new PayloadAnalyzer()).setSimilarity(
        similarity));
    for (int i = 0; i < 1000; i++) {
      Document doc = new Document();
      Field noPayloadField = new Field(PayloadHelper.NO_PAYLOAD_FIELD, English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED);
      doc.add(noPayloadField);
      doc.add(new Field("field", English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("multiField", English.intToEnglish(i) + "  " + English.intToEnglish(i), Field.Store.YES, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.optimize();
    writer.close();
    searcher = new IndexSearcher(directory, true);
    searcher.setSimilarity(similarity);
  }
  public void test() throws IOException {
    PayloadTermQuery query = new PayloadTermQuery(new Term("field", "seventy"),
            new MaxPayloadFunction());
    TopDocs hits = searcher.search(query, null, 100);
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue("hits Size: " + hits.totalHits + " is not: " + 100, hits.totalHits == 100);
    assertTrue(hits.getMaxScore() + " does not equal: " + 1, hits.getMaxScore() == 1);
    for (int i = 0; i < hits.scoreDocs.length; i++) {
      ScoreDoc doc = hits.scoreDocs[i];
      assertTrue(doc.score + " does not equal: " + 1, doc.score == 1);
    }
    CheckHits.checkExplanations(query, PayloadHelper.FIELD, searcher, true);
    Spans spans = query.getSpans(searcher.getIndexReader());
    assertTrue("spans is null and it shouldn't be", spans != null);
    assertTrue("spans is not an instanceof " + TermSpans.class, spans instanceof TermSpans);
  }
  public void testQuery() {
    PayloadTermQuery boostingFuncTermQuery = new PayloadTermQuery(new Term(PayloadHelper.MULTI_FIELD, "seventy"),
        new MaxPayloadFunction());
    QueryUtils.check(boostingFuncTermQuery);
    SpanTermQuery spanTermQuery = new SpanTermQuery(new Term(PayloadHelper.MULTI_FIELD, "seventy"));
    assertTrue(boostingFuncTermQuery.equals(spanTermQuery) == spanTermQuery.equals(boostingFuncTermQuery));
    PayloadTermQuery boostingFuncTermQuery2 = new PayloadTermQuery(new Term(PayloadHelper.MULTI_FIELD, "seventy"),
        new AveragePayloadFunction());
    QueryUtils.checkUnequal(boostingFuncTermQuery, boostingFuncTermQuery2);
  }
  public void testMultipleMatchesPerDoc() throws Exception {
    PayloadTermQuery query = new PayloadTermQuery(new Term(PayloadHelper.MULTI_FIELD, "seventy"),
            new MaxPayloadFunction());
    TopDocs hits = searcher.search(query, null, 100);
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue("hits Size: " + hits.totalHits + " is not: " + 100, hits.totalHits == 100);
    assertTrue(hits.getMaxScore() + " does not equal: " + 4.0, hits.getMaxScore() == 4.0);
    int numTens = 0;
    for (int i = 0; i < hits.scoreDocs.length; i++) {
      ScoreDoc doc = hits.scoreDocs[i];
      if (doc.doc % 10 == 0) {
        numTens++;
        assertTrue(doc.score + " does not equal: " + 4.0, doc.score == 4.0);
      } else {
        assertTrue(doc.score + " does not equal: " + 2, doc.score == 2);
      }
    }
    assertTrue(numTens + " does not equal: " + 10, numTens == 10);
    CheckHits.checkExplanations(query, "field", searcher, true);
    Spans spans = query.getSpans(searcher.getIndexReader());
    assertTrue("spans is null and it shouldn't be", spans != null);
    assertTrue("spans is not an instanceof " + TermSpans.class, spans instanceof TermSpans);
    int count = 0;
    while (spans.next()) {
      count++;
    }
    assertTrue(count + " does not equal: " + 200, count == 200);
  }
  public void testIgnoreSpanScorer() throws Exception {
    PayloadTermQuery query = new PayloadTermQuery(new Term(PayloadHelper.MULTI_FIELD, "seventy"),
            new MaxPayloadFunction(), false);
    IndexSearcher theSearcher = new IndexSearcher(directory, true);
    theSearcher.setSimilarity(new FullSimilarity());
    TopDocs hits = searcher.search(query, null, 100);
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue("hits Size: " + hits.totalHits + " is not: " + 100, hits.totalHits == 100);
    assertTrue(hits.getMaxScore() + " does not equal: " + 4.0, hits.getMaxScore() == 4.0);
    int numTens = 0;
    for (int i = 0; i < hits.scoreDocs.length; i++) {
      ScoreDoc doc = hits.scoreDocs[i];
      if (doc.doc % 10 == 0) {
        numTens++;
        assertTrue(doc.score + " does not equal: " + 4.0, doc.score == 4.0);
      } else {
        assertTrue(doc.score + " does not equal: " + 2, doc.score == 2);
      }
    }
    assertTrue(numTens + " does not equal: " + 10, numTens == 10);
    CheckHits.checkExplanations(query, "field", searcher, true);
    Spans spans = query.getSpans(searcher.getIndexReader());
    assertTrue("spans is null and it shouldn't be", spans != null);
    assertTrue("spans is not an instanceof " + TermSpans.class, spans instanceof TermSpans);
    int count = 0;
    while (spans.next()) {
      count++;
    }
  }
  public void testNoMatch() throws Exception {
    PayloadTermQuery query = new PayloadTermQuery(new Term(PayloadHelper.FIELD, "junk"),
            new MaxPayloadFunction());
    TopDocs hits = searcher.search(query, null, 100);
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue("hits Size: " + hits.totalHits + " is not: " + 0, hits.totalHits == 0);
  }
  public void testNoPayload() throws Exception {
    PayloadTermQuery q1 = new PayloadTermQuery(new Term(PayloadHelper.NO_PAYLOAD_FIELD, "zero"),
            new MaxPayloadFunction());
    PayloadTermQuery q2 = new PayloadTermQuery(new Term(PayloadHelper.NO_PAYLOAD_FIELD, "foo"),
            new MaxPayloadFunction());
    BooleanClause c1 = new BooleanClause(q1, BooleanClause.Occur.MUST);
    BooleanClause c2 = new BooleanClause(q2, BooleanClause.Occur.MUST_NOT);
    BooleanQuery query = new BooleanQuery();
    query.add(c1);
    query.add(c2);
    TopDocs hits = searcher.search(query, null, 100);
    assertTrue("hits is null and it shouldn't be", hits != null);
    assertTrue("hits Size: " + hits.totalHits + " is not: " + 1, hits.totalHits == 1);
    int[] results = new int[1];
    results[0] = 0;
    CheckHits.checkHitCollector(query, PayloadHelper.NO_PAYLOAD_FIELD, searcher, results);
  }
  static class BoostingSimilarity extends DefaultSimilarity {
    @Override
    public float scorePayload(int docId, String fieldName, int start, int end, byte[] payload, int offset, int length) {
      return payload[0];
    }
    @Override
    public float lengthNorm(String fieldName, int numTerms) {
      return 1;
    }
    @Override
    public float queryNorm(float sumOfSquaredWeights) {
      return 1;
    }
    @Override
    public float sloppyFreq(int distance) {
      return 1;
    }
    @Override
    public float coord(int overlap, int maxOverlap) {
      return 1;
    }
    @Override
    public float idf(int docFreq, int numDocs) {
      return 1;
    }
    @Override
    public float tf(float freq) {
      return freq == 0 ? 0 : 1;
    }
  }
  static class FullSimilarity extends DefaultSimilarity{
    public float scorePayload(int docId, String fieldName, byte[] payload, int offset, int length) {
      return payload[0];
    }
  }
}
