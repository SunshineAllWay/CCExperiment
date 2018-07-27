package org.apache.solr.handler;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharReader;
import org.apache.lucene.analysis.CharStream;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.solr.analysis.CharFilterFactory;
import org.apache.solr.analysis.TokenFilterFactory;
import org.apache.solr.analysis.TokenizerChain;
import org.apache.solr.analysis.TokenizerFactory;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.SolrException;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.FieldType;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
public abstract class AnalysisRequestHandlerBase extends RequestHandlerBase {
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception {
    rsp.add("analysis", doAnalysis(req));
  }
  protected abstract NamedList doAnalysis(SolrQueryRequest req) throws Exception;
  protected NamedList<List<NamedList>> analyzeValue(String value, AnalysisContext context) {
    Analyzer analyzer = context.getAnalyzer();
    if (!TokenizerChain.class.isInstance(analyzer)) {
      TokenStream tokenStream = null;
      try {
        tokenStream = analyzer.reusableTokenStream(context.getFieldName(), new StringReader(value));
        tokenStream.reset();
      } catch (IOException e) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
      }
      NamedList<List<NamedList>> namedList = new NamedList<List<NamedList>>();
      namedList.add(tokenStream.getClass().getName(), convertTokensToNamedLists(analyzeTokenStream(tokenStream), context));
      return namedList;
    }
    TokenizerChain tokenizerChain = (TokenizerChain) analyzer;
    CharFilterFactory[] cfiltfacs = tokenizerChain.getCharFilterFactories();
    TokenizerFactory tfac = tokenizerChain.getTokenizerFactory();
    TokenFilterFactory[] filtfacs = tokenizerChain.getTokenFilterFactories();
    NamedList<List<NamedList>> namedList = new NamedList<List<NamedList>>();
    if( cfiltfacs != null ){
      String source = value;
      for(CharFilterFactory cfiltfac : cfiltfacs ){
        CharStream reader = CharReader.get(new StringReader(source));
        reader = cfiltfac.create(reader);
        source = writeCharStream(namedList, reader);
      }
    }
    TokenStream tokenStream = tfac.create(tokenizerChain.charStream(new StringReader(value)));
    List<Token> tokens = analyzeTokenStream(tokenStream);
    namedList.add(tokenStream.getClass().getName(), convertTokensToNamedLists(tokens, context));
    ListBasedTokenStream listBasedTokenStream = new ListBasedTokenStream(tokens);
    for (TokenFilterFactory tokenFilterFactory : filtfacs) {
      tokenStream = tokenFilterFactory.create(listBasedTokenStream);
      List<Token> tokenList = analyzeTokenStream(tokenStream);
      namedList.add(tokenStream.getClass().getName(), convertTokensToNamedLists(tokenList, context));
      listBasedTokenStream = new ListBasedTokenStream(tokenList);
    }
    return namedList;
  }
  protected List<Token> analyzeValue(String value, Analyzer analyzer) {
    TokenStream tokenStream = analyzer.tokenStream("", new StringReader(value));
    return analyzeTokenStream(tokenStream);
  }
  private List<Token> analyzeTokenStream(TokenStream tokenStream) {
    List<Token> tokens = new ArrayList<Token>();
    TermAttribute termAtt = (TermAttribute) 
      tokenStream.addAttribute(TermAttribute.class);
    OffsetAttribute offsetAtt = (OffsetAttribute) 
      tokenStream.addAttribute(OffsetAttribute.class);
    TypeAttribute typeAtt = (TypeAttribute) 
      tokenStream.addAttribute(TypeAttribute.class);
    FlagsAttribute flagsAtt = (FlagsAttribute) 
      tokenStream.addAttribute(FlagsAttribute.class);
    PayloadAttribute payloadAtt = (PayloadAttribute) 
      tokenStream.addAttribute(PayloadAttribute.class);
    PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) 
      tokenStream.addAttribute(PositionIncrementAttribute.class);
    try {
      while (tokenStream.incrementToken()) {
        Token token = new Token();
        token.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
        token.setOffset(offsetAtt.startOffset(), offsetAtt.endOffset());
        token.setType(typeAtt.type());
        token.setFlags(flagsAtt.getFlags());
        token.setPayload(payloadAtt.getPayload());
        token.setPositionIncrement(posIncAtt.getPositionIncrement());
        tokens.add((Token) token.clone());
      }
    } catch (IOException ioe) {
      throw new RuntimeException("Error occured while iterating over tokenstream", ioe);
    }
    return tokens;
  }
  private List<NamedList> convertTokensToNamedLists(List<Token> tokens, AnalysisContext context) {
    List<NamedList> tokensNamedLists = new ArrayList<NamedList>();
    Collections.sort(tokens, new Comparator<Token>() {
      public int compare(Token o1, Token o2) {
        return o1.endOffset() - o2.endOffset();
      }
    });
    int position = 0;
    FieldType fieldType = context.getFieldType();
    for (Token token : tokens) {
      NamedList<Object> tokenNamedList = new SimpleOrderedMap<Object>();
      String text = fieldType.indexedToReadable(token.term());
      tokenNamedList.add("text", text);
      if (!text.equals(token.term())) {
        tokenNamedList.add("raw_text", token.term());
      }
      tokenNamedList.add("type", token.type());
      tokenNamedList.add("start", token.startOffset());
      tokenNamedList.add("end", token.endOffset());
      position += token.getPositionIncrement();
      tokenNamedList.add("position", position);
      if (context.getTermsToMatch().contains(token.term())) {
        tokenNamedList.add("match", true);
      }
      if (token.getPayload() != null) {
        tokenNamedList.add("payload", token.getPayload());
      }
      tokensNamedLists.add(tokenNamedList);
    }
    return tokensNamedLists;
  }
  private String writeCharStream(NamedList out, CharStream input ){
    final int BUFFER_SIZE = 1024;
    char[] buf = new char[BUFFER_SIZE];
    int len = 0;
    StringBuilder sb = new StringBuilder();
    do {
      try {
        len = input.read( buf, 0, BUFFER_SIZE );
      } catch (IOException e) {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
      }
      if( len > 0 )
        sb.append(buf, 0, len);
    } while( len == BUFFER_SIZE );
    out.add( input.getClass().getName(), sb.toString());
    return sb.toString();
  }
  protected static class ListBasedTokenStream extends TokenStream {
    private final List<Token> tokens;
    private Iterator<Token> tokenIterator;
    private final TermAttribute termAtt = (TermAttribute) 
      addAttribute(TermAttribute.class);
    private final OffsetAttribute offsetAtt = (OffsetAttribute) 
      addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = (TypeAttribute) 
      addAttribute(TypeAttribute.class);
    private final FlagsAttribute flagsAtt = (FlagsAttribute) 
      addAttribute(FlagsAttribute.class);
    private final PayloadAttribute payloadAtt = (PayloadAttribute) 
      addAttribute(PayloadAttribute.class);
    private final PositionIncrementAttribute posIncAtt = (PositionIncrementAttribute) 
      addAttribute(PositionIncrementAttribute.class);
    ListBasedTokenStream(List<Token> tokens) {
      this.tokens = tokens;
      tokenIterator = tokens.iterator();
    }
    @Override
    public boolean incrementToken() throws IOException {
      if (tokenIterator.hasNext()) {
        Token next = tokenIterator.next();
        termAtt.setTermBuffer(next.termBuffer(), 0, next.termLength());
        typeAtt.setType(next.type());
        offsetAtt.setOffset(next.startOffset(), next.endOffset());
        flagsAtt.setFlags(next.getFlags());
        payloadAtt.setPayload(next.getPayload());
        posIncAtt.setPositionIncrement(next.getPositionIncrement());
        return true;
      } else {
        return false;
      }
    }
    @Override
    public void reset() throws IOException {
      super.reset();
      tokenIterator = tokens.iterator();
    }
  }
  protected static class AnalysisContext {
    private final String fieldName;
    private final FieldType fieldType;
    private final Analyzer analyzer;
    private final Set<String> termsToMatch;
    public AnalysisContext(FieldType fieldType, Analyzer analyzer, Set<String> termsToMatch) {
      this(null, fieldType, analyzer, termsToMatch);
    }
    public AnalysisContext(String fieldName, FieldType fieldType, Analyzer analyzer) {
      this(fieldName, fieldType, analyzer, Collections.EMPTY_SET);
    }
    public AnalysisContext(String fieldName, FieldType fieldType, Analyzer analyzer, Set<String> termsToMatch) {
      this.fieldName = fieldName;
      this.fieldType = fieldType;
      this.analyzer = analyzer;
      this.termsToMatch = termsToMatch;
    }
    public String getFieldName() {
      return fieldName;
    }
    public FieldType getFieldType() {
      return fieldType;
    }
    public Analyzer getAnalyzer() {
      return analyzer;
    }
    public Set<String> getTermsToMatch() {
      return termsToMatch;
    }
  }
}
