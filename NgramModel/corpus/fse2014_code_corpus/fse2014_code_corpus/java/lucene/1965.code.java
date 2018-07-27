package org.apache.lucene.search;
import java.io.Reader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.analysis.LowerCaseTokenizer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.index.Payload;
import org.apache.lucene.search.payloads.PayloadSpanUtil;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.apache.lucene.util.Version;
import org.apache.lucene.util.LuceneTestCase;
public class TestPositionIncrement extends LuceneTestCase {
  public void testSetPosition() throws Exception {
    Analyzer analyzer = new Analyzer() {
      @Override
      public TokenStream tokenStream(String fieldName, Reader reader) {
        return new TokenStream() {
          private final String[] TOKENS = {"1", "2", "3", "4", "5"};
          private final int[] INCREMENTS = {0, 2, 1, 0, 1};
          private int i = 0;
          PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
          TermAttribute termAtt = addAttribute(TermAttribute.class);
          OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
          @Override
          public boolean incrementToken() {
            if (i == TOKENS.length)
              return false;
            clearAttributes();
            termAtt.setTermBuffer(TOKENS[i]);
            offsetAtt.setOffset(i,i);
            posIncrAtt.setPositionIncrement(INCREMENTS[i]);
            i++;
            return true;
          }
        };
      }
    };
    Directory store = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(store, new IndexWriterConfig(TEST_VERSION_CURRENT, analyzer));
    Document d = new Document();
    d.add(new Field("field", "bogus", Field.Store.YES, Field.Index.ANALYZED));
    writer.addDocument(d);
    writer.optimize();
    writer.close();
    IndexSearcher searcher = new IndexSearcher(store, true);
    TermPositions pos = searcher.getIndexReader().termPositions(new Term("field", "1"));
    pos.next();
    assertEquals(0, pos.nextPosition());
    pos = searcher.getIndexReader().termPositions(new Term("field", "2"));
    pos.next();
    assertEquals(2, pos.nextPosition());
    PhraseQuery q;
    ScoreDoc[] hits;
    q = new PhraseQuery();
    q.add(new Term("field", "1"));
    q.add(new Term("field", "2"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    q = new PhraseQuery(); 
    q.add(new Term("field", "1"),0);
    q.add(new Term("field", "2"),1);
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "1"),0);
    q.add(new Term("field", "2"),2);
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "2"));
    q.add(new Term("field", "3"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "3"));
    q.add(new Term("field", "4"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "3"),0);
    q.add(new Term("field", "4"),0);
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "3"),0);
    q.add(new Term("field", "9"),0);
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    MultiPhraseQuery mq = new MultiPhraseQuery();
    mq.add(new Term[]{new Term("field", "3"),new Term("field", "9")},0);
    hits = searcher.search(mq, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "2"));
    q.add(new Term("field", "4"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "3"));
    q.add(new Term("field", "5"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "4"));
    q.add(new Term("field", "5"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
    q = new PhraseQuery();
    q.add(new Term("field", "2"));
    q.add(new Term("field", "5"));
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    QueryParser qp = new QueryParser(TEST_VERSION_CURRENT, "field",
                                     new StopWhitespaceAnalyzer(false));
    q = (PhraseQuery) qp.parse("\"1 2\"");
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    q = (PhraseQuery) qp.parse("\"1 stop 2\"");
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    qp.setEnablePositionIncrements(true);
    q = (PhraseQuery) qp.parse("\"1 stop 2\"");
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    qp.setEnablePositionIncrements(false);
    q = (PhraseQuery) qp.parse("\"1 stop 2\"");
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(0, hits.length);
    qp = new QueryParser(TEST_VERSION_CURRENT, "field",
                         new StopWhitespaceAnalyzer(true));
    qp.setEnablePositionIncrements(true);
    q = (PhraseQuery) qp.parse("\"1 stop 2\"");
    hits = searcher.search(q, null, 1000).scoreDocs;
    assertEquals(1, hits.length);
  }
  private static class StopWhitespaceAnalyzer extends Analyzer {
    boolean enablePositionIncrements;
    final WhitespaceAnalyzer a = new WhitespaceAnalyzer(TEST_VERSION_CURRENT);
    public StopWhitespaceAnalyzer(boolean enablePositionIncrements) {
      this.enablePositionIncrements = enablePositionIncrements;
    }
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      TokenStream ts = a.tokenStream(fieldName,reader);
      return new StopFilter(enablePositionIncrements?TEST_VERSION_CURRENT:Version.LUCENE_24, ts,
          new CharArraySet(TEST_VERSION_CURRENT, Collections.singleton("stop"), true));
    }
  }
  public void testPayloadsPos0() throws Exception {
    Directory dir = new MockRAMDirectory();
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new TestPayloadAnalyzer()));
    Document doc = new Document();
    doc.add(new Field("content",
                      new StringReader("a a b c d e a f g h i j a b k k")));
    writer.addDocument(doc);
    IndexReader r = writer.getReader();
    TermPositions tp = r.termPositions(new Term("content", "a"));
    int count = 0;
    assertTrue(tp.next());
    assertEquals(4, tp.freq());
    int expected = 0;
    assertEquals(expected, tp.nextPosition());
    assertEquals(1, tp.nextPosition());
    assertEquals(3, tp.nextPosition());
    assertEquals(6, tp.nextPosition());
    assertFalse(tp.next());
    IndexSearcher is = new IndexSearcher(r);
    SpanTermQuery stq1 = new SpanTermQuery(new Term("content", "a"));
    SpanTermQuery stq2 = new SpanTermQuery(new Term("content", "k"));
    SpanQuery[] sqs = { stq1, stq2 };
    SpanNearQuery snq = new SpanNearQuery(sqs, 30, false);
    count = 0;
    boolean sawZero = false;
    Spans pspans = snq.getSpans(is.getIndexReader());
    while (pspans.next()) {
      Collection<byte[]> payloads = pspans.getPayload();
      sawZero |= pspans.start() == 0;
      count += payloads.size();
    }
    assertEquals(5, count);
    assertTrue(sawZero);
    Spans spans = snq.getSpans(is.getIndexReader());
    count = 0;
    sawZero = false;
    while (spans.next()) {
      count++;
      sawZero |= spans.start() == 0;
    }
    assertEquals(4, count);
    assertTrue(sawZero);
    sawZero = false;
    PayloadSpanUtil psu = new PayloadSpanUtil(is.getIndexReader());
    Collection<byte[]> pls = psu.getPayloadsForQuery(snq);
    count = pls.size();
    for (byte[] bytes : pls) {
      String s = new String(bytes);
      sawZero |= s.equals("pos: 0");
    }
    assertEquals(5, count);
    assertTrue(sawZero);
    writer.close();
    is.getIndexReader().close();
    dir.close();
  }
}
class TestPayloadAnalyzer extends Analyzer {
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new LowerCaseTokenizer(LuceneTestCase.TEST_VERSION_CURRENT, reader);
    return new PayloadFilter(result, fieldName);
  }
}
class PayloadFilter extends TokenFilter {
  String fieldName;
  int pos;
  int i;
  final PositionIncrementAttribute posIncrAttr;
  final PayloadAttribute payloadAttr;
  final TermAttribute termAttr;
  public PayloadFilter(TokenStream input, String fieldName) {
    super(input);
    this.fieldName = fieldName;
    pos = 0;
    i = 0;
    posIncrAttr = input.addAttribute(PositionIncrementAttribute.class);
    payloadAttr = input.addAttribute(PayloadAttribute.class);
    termAttr = input.addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (input.incrementToken()) {
      payloadAttr.setPayload(new Payload(("pos: " + pos).getBytes()));
      int posIncr;
      if (i % 2 == 1) {
        posIncr = 1;
      } else {
        posIncr = 0;
      }
      posIncrAttr.setPositionIncrement(posIncr);
      pos += posIncr;
      i++;
      return true;
    } else {
      return false;
    }
  }
}
