package org.apache.lucene.search.vectorhighlight;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.Field.TermVector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public abstract class AbstractTestCase extends LuceneTestCase {
  protected final String F = "f";
  protected final String F1 = "f1";
  protected final String F2 = "f2";
  protected Directory dir;
  protected Analyzer analyzerW;
  protected Analyzer analyzerB;
  protected Analyzer analyzerK;
  protected IndexReader reader;  
  protected QueryParser paW;
  protected QueryParser paB;
  protected static final String[] shortMVValues = {
    "a b c",
    "",   
    "d e"
  };
  protected static final String[] longMVValues = {
    "Followings are the examples of customizable parameters and actual examples of customization:",
    "The most search engines use only one of these methods. Even the search engines that says they can use the both methods basically"
  };
  protected static final String[] biMVValues = {
    "\nLucene/Solr does not require such additional hardware.",
    "\nWhen you talk about processing speed, the"
  };
  protected static final String[] strMVValues = {
    "abc",
    "defg",
    "hijkl"
  };
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    analyzerW = new WhitespaceAnalyzer(TEST_VERSION_CURRENT);
    analyzerB = new BigramAnalyzer();
    analyzerK = new KeywordAnalyzer();
    paW = new QueryParser(TEST_VERSION_CURRENT,  F, analyzerW );
    paB = new QueryParser(TEST_VERSION_CURRENT,  F, analyzerB );
    dir = new RAMDirectory();
  }
  @Override
  protected void tearDown() throws Exception {
    if( reader != null ){
      reader.close();
      reader = null;
    }
    super.tearDown();
  }
  protected Query tq( String text ){
    return tq( 1F, text );
  }
  protected Query tq( float boost, String text ){
    return tq( boost, F, text );
  }
  protected Query tq( String field, String text ){
    return tq( 1F, field, text );
  }
  protected Query tq( float boost, String field, String text ){
    Query query = new TermQuery( new Term( field, text ) );
    query.setBoost( boost );
    return query;
  }
  protected Query pqF( String... texts ){
    return pqF( 1F, texts );
  }
  protected Query pqF( float boost, String... texts ){
    return pqF( boost, 0, texts );
  }
  protected Query pqF( float boost, int slop, String... texts ){
    return pq( boost, slop, F, texts );
  }
  protected Query pq( String field, String... texts ){
    return pq( 1F, 0, field, texts );
  }
  protected Query pq( float boost, String field, String... texts ){
    return pq( boost, 0, field, texts );
  }
  protected Query pq( float boost, int slop, String field, String... texts ){
    PhraseQuery query = new PhraseQuery();
    for( String text : texts ){
      query.add( new Term( field, text ) );
    }
    query.setBoost( boost );
    query.setSlop( slop );
    return query;
  }
  protected Query dmq( Query... queries ){
    return dmq( 0.0F, queries );
  }
  protected Query dmq( float tieBreakerMultiplier, Query... queries ){
    DisjunctionMaxQuery query = new DisjunctionMaxQuery( tieBreakerMultiplier );
    for( Query q : queries ){
      query.add( q );
    }
    return query;
  }
  protected void assertCollectionQueries( Collection<Query> actual, Query... expected ){
    assertEquals( expected.length, actual.size() );
    for( Query query : expected ){
      assertTrue( actual.contains( query ) );
    }
  }
  static class BigramAnalyzer extends Analyzer {
    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
      return new BasicNGramTokenizer( reader );
    }
  }
  static class BasicNGramTokenizer extends Tokenizer {
    public static final int DEFAULT_N_SIZE = 2;
    public static final String DEFAULT_DELIMITERS = " \t\n.,";
    private final int n;
    private final String delimiters;
    private int startTerm;
    private int lenTerm;
    private int startOffset;
    private int nextStartOffset;
    private int ch;
    private String snippet;
    private StringBuilder snippetBuffer;
    private static final int BUFFER_SIZE = 4096;
    private char[] charBuffer;
    private int charBufferIndex;
    private int charBufferLen;
    public BasicNGramTokenizer( Reader in ){
      this( in, DEFAULT_N_SIZE );
    }
    public BasicNGramTokenizer( Reader in, int n ){
      this( in, n, DEFAULT_DELIMITERS );
    }
    public BasicNGramTokenizer( Reader in, String delimiters ){
      this( in, DEFAULT_N_SIZE, delimiters );
    }
    public BasicNGramTokenizer( Reader in, int n, String delimiters ){
      super(in);
      this.n = n;
      this.delimiters = delimiters;
      startTerm = 0;
      nextStartOffset = 0;
      snippet = null;
      snippetBuffer = new StringBuilder();
      charBuffer = new char[BUFFER_SIZE];
      charBufferIndex = BUFFER_SIZE;
      charBufferLen = 0;
      ch = 0;
    }
    TermAttribute termAtt = addAttribute(TermAttribute.class);
    OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    @Override
    public boolean incrementToken() throws IOException {
      if( !getNextPartialSnippet() )
        return false;
      clearAttributes();
      termAtt.setTermBuffer(snippet, startTerm, lenTerm);
      offsetAtt.setOffset(correctOffset(startOffset), correctOffset(startOffset + lenTerm));
      return true;
    }
    private int getFinalOffset() {
      return nextStartOffset;
    }
    @Override
    public final void end(){
      offsetAtt.setOffset(getFinalOffset(),getFinalOffset());
    }
    protected boolean getNextPartialSnippet() throws IOException {
      if( snippet != null && snippet.length() >= startTerm + 1 + n ){
        startTerm++;
        startOffset++;
        lenTerm = n;
        return true;
      }
      return getNextSnippet();
    }
    protected boolean getNextSnippet() throws IOException {
      startTerm = 0;
      startOffset = nextStartOffset;
      snippetBuffer.delete( 0, snippetBuffer.length() );
      while( true ){
        if( ch != -1 )
          ch = readCharFromBuffer();
        if( ch == -1 ) break;
        else if( !isDelimiter( ch ) )
          snippetBuffer.append( (char)ch );
        else if( snippetBuffer.length() > 0 )
          break;
        else
          startOffset++;
      }
      if( snippetBuffer.length() == 0 )
        return false;
      snippet = snippetBuffer.toString();
      lenTerm = snippet.length() >= n ? n : snippet.length();
      return true;
    }
    protected int readCharFromBuffer() throws IOException {
      if( charBufferIndex >= charBufferLen ){
        charBufferLen = input.read( charBuffer );
        if( charBufferLen == -1 ){
          return -1;
        }
        charBufferIndex = 0;
      }
      int c = charBuffer[charBufferIndex++];
      nextStartOffset++;
      return c;
    }
    protected boolean isDelimiter( int c ){
      return delimiters.indexOf( c ) >= 0;
    }
    @Override
    public void reset( Reader input ) throws IOException {
      super.reset( input );
      reset();
    }
    @Override
    public void reset() throws IOException {
      startTerm = 0;
      nextStartOffset = 0;
      snippet = null;
      snippetBuffer.setLength( 0 );
      charBufferIndex = BUFFER_SIZE;
      charBufferLen = 0;
      ch = 0;
    }
  }
  protected void make1d1fIndex( String value ) throws Exception {
    make1dmfIndex( value );
  }
  protected void make1d1fIndexB( String value ) throws Exception {
    make1dmfIndexB( value );
  }
  protected void make1dmfIndex( String... values ) throws Exception {
    make1dmfIndex( analyzerW, values );
  }
  protected void make1dmfIndexB( String... values ) throws Exception {
    make1dmfIndex( analyzerB, values );
  }
  protected void make1dmfIndex( Analyzer analyzer, String... values ) throws Exception {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzer).setOpenMode(OpenMode.CREATE));
    Document doc = new Document();
    for( String value: values )
      doc.add( new Field( F, value, Store.YES, Index.ANALYZED, TermVector.WITH_POSITIONS_OFFSETS ) );
    writer.addDocument( doc );
    writer.close();
    reader = IndexReader.open( dir, true );
  }
  protected void make1dmfIndexNA( String... values ) throws Exception {
    IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
        TEST_VERSION_CURRENT, analyzerK).setOpenMode(OpenMode.CREATE));
    Document doc = new Document();
    for( String value: values )
      doc.add( new Field( F, value, Store.YES, Index.NOT_ANALYZED, TermVector.WITH_POSITIONS_OFFSETS ) );
    writer.addDocument( doc );
    writer.close();
    reader = IndexReader.open( dir, true );
  }
  protected void makeIndexShortMV() throws Exception {
    make1dmfIndex( shortMVValues );
  }
  protected void makeIndexLongMV() throws Exception {
    make1dmfIndex( longMVValues );
  }
  protected void makeIndexLongMVB() throws Exception {
    make1dmfIndexB( biMVValues );
  }
  protected void makeIndexStrMV() throws Exception {
    make1dmfIndexNA( strMVValues );
  }
}
