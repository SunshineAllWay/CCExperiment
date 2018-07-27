package org.apache.lucene.analysis.query;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;
public class QueryAutoStopWordAnalyzerTest extends BaseTokenStreamTestCase {
  String variedFieldValues[] = {"the", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "boring", "dog"};
  String repetitiveFieldValues[] = {"boring", "boring", "vaguelyboring"};
  RAMDirectory dir;
  Analyzer appAnalyzer;
  IndexReader reader;
  QueryAutoStopWordAnalyzer protectedAnalyzer;
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    dir = new RAMDirectory();
    appAnalyzer = new WhitespaceAnalyzer(TEST_VERSION_CURRENT);
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(TEST_VERSION_CURRENT, appAnalyzer));
    int numDocs = 200;
    for (int i = 0; i < numDocs; i++) {
      Document doc = new Document();
      String variedFieldValue = variedFieldValues[i % variedFieldValues.length];
      String repetitiveFieldValue = repetitiveFieldValues[i % repetitiveFieldValues.length];
      doc.add(new Field("variedField", variedFieldValue, Field.Store.YES, Field.Index.ANALYZED));
      doc.add(new Field("repetitiveField", repetitiveFieldValue, Field.Store.YES, Field.Index.ANALYZED));
      writer.addDocument(doc);
    }
    writer.close();
    reader = IndexReader.open(dir, true);
    protectedAnalyzer = new QueryAutoStopWordAnalyzer(TEST_VERSION_CURRENT, appAnalyzer);
  }
  @Override
  protected void tearDown() throws Exception {
    reader.close();
    super.tearDown();
  }
  private int search(Analyzer a, String queryString) throws IOException, ParseException {
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, "repetitiveField", a);
    Query q = qp.parse(queryString);
    return new IndexSearcher(reader).search(q, null, 1000).totalHits;
  }
  public void testUninitializedAnalyzer() throws Exception {
    String query = "variedField:quick repetitiveField:boring";
    int numHits1 = search(protectedAnalyzer, query);
    int numHits2 = search(appAnalyzer, query);
    assertEquals("No filtering test", numHits1, numHits2);
  }
  public void testDefaultAddStopWordsIndexReader() throws Exception {
    protectedAnalyzer.addStopWords(reader);
    int numHits = search(protectedAnalyzer, "repetitiveField:boring");
    assertEquals("Default filter should remove all docs", 0, numHits);
  }
  public void testAddStopWordsIndexReaderInt() throws Exception {
    protectedAnalyzer.addStopWords(reader, 1f / 2f);
    int numHits = search(protectedAnalyzer, "repetitiveField:boring");
    assertEquals("A filter on terms in > one half of docs remove boring docs", 0, numHits);
    numHits = search(protectedAnalyzer, "repetitiveField:vaguelyboring");
    assertTrue("A filter on terms in > half of docs should not remove vaguelyBoring docs", numHits > 1);
    protectedAnalyzer.addStopWords(reader, 1f / 4f);
    numHits = search(protectedAnalyzer, "repetitiveField:vaguelyboring");
    assertEquals("A filter on terms in > quarter of docs should remove vaguelyBoring docs", 0, numHits);
  }
  public void testAddStopWordsIndexReaderStringFloat() throws Exception {
    protectedAnalyzer.addStopWords(reader, "variedField", 1f / 2f);
    int numHits = search(protectedAnalyzer, "repetitiveField:boring");
    assertTrue("A filter on one Field should not affect queris on another", numHits > 0);
    protectedAnalyzer.addStopWords(reader, "repetitiveField", 1f / 2f);
    numHits = search(protectedAnalyzer, "repetitiveField:boring");
    assertEquals("A filter on the right Field should affect queries on it", numHits, 0);
  }
  public void testAddStopWordsIndexReaderStringInt() throws Exception {
    int numStopWords = protectedAnalyzer.addStopWords(reader, "repetitiveField", 10);
    assertTrue("Should have identified stop words", numStopWords > 0);
    Term[] t = protectedAnalyzer.getStopWords();
    assertEquals("num terms should = num stopwords returned", t.length, numStopWords);
    int numNewStopWords = protectedAnalyzer.addStopWords(reader, "variedField", 10);
    assertTrue("Should have identified more stop words", numNewStopWords > 0);
    t = protectedAnalyzer.getStopWords();
    assertEquals("num terms should = num stopwords returned", t.length, numStopWords + numNewStopWords);
  }
  public void testNoFieldNamePollution() throws Exception {
    protectedAnalyzer.addStopWords(reader, "repetitiveField", 10);
    int numHits = search(protectedAnalyzer, "repetitiveField:boring");
    assertEquals("Check filter set up OK", 0, numHits);
    numHits = search(protectedAnalyzer, "variedField:boring");
    assertTrue("Filter should not prevent stopwords in one field being used in another ", numHits > 0);
  }
  private class NonreusableAnalyzer extends Analyzer {
    int invocationCount = 0;
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      if (++invocationCount % 2 == 0)
        return new WhitespaceTokenizer(TEST_VERSION_CURRENT, reader);
      else
        return new LetterTokenizer(TEST_VERSION_CURRENT, reader);
    }
  }
  public void testWrappingNonReusableAnalyzer() throws Exception {
    QueryAutoStopWordAnalyzer a = new QueryAutoStopWordAnalyzer(TEST_VERSION_CURRENT, new NonreusableAnalyzer());
    a.addStopWords(reader, 10);
    int numHits = search(a, "repetitiveField:boring");
    assertTrue(numHits == 0);
    numHits = search(a, "repetitiveField:vaguelyboring");
    assertTrue(numHits == 0);
  }
  public void testTokenStream() throws Exception {
    QueryAutoStopWordAnalyzer a = new QueryAutoStopWordAnalyzer(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    a.addStopWords(reader, 10);
    TokenStream ts = a.tokenStream("repetitiveField", new StringReader("this boring"));
    TermAttribute termAtt = ts.getAttribute(TermAttribute.class);
    assertTrue(ts.incrementToken());
    assertEquals("this", termAtt.term());
    assertFalse(ts.incrementToken());
  }
}
