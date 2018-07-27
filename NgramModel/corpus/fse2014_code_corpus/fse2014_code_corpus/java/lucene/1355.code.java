package org.apache.lucene.wikipedia.analysis;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
public final class WikipediaTokenizer extends Tokenizer {
  public static final String INTERNAL_LINK = "il";
  public static final String EXTERNAL_LINK = "el";
  public static final String EXTERNAL_LINK_URL = "elu";
  public static final String CITATION = "ci";
  public static final String CATEGORY = "c";
  public static final String BOLD = "b";
  public static final String ITALICS = "i";
  public static final String BOLD_ITALICS = "bi";
  public static final String HEADING = "h";
  public static final String SUB_HEADING = "sh";
  public static final int ALPHANUM_ID          = 0;
  public static final int APOSTROPHE_ID        = 1;
  public static final int ACRONYM_ID           = 2;
  public static final int COMPANY_ID           = 3;
  public static final int EMAIL_ID             = 4;
  public static final int HOST_ID              = 5;
  public static final int NUM_ID               = 6;
  public static final int CJ_ID                = 7;
  public static final int INTERNAL_LINK_ID     = 8;
  public static final int EXTERNAL_LINK_ID     = 9;
  public static final int CITATION_ID          = 10;
  public static final int CATEGORY_ID          = 11;
  public static final int BOLD_ID              = 12;
  public static final int ITALICS_ID           = 13;
  public static final int BOLD_ITALICS_ID      = 14;
  public static final int HEADING_ID           = 15;
  public static final int SUB_HEADING_ID       = 16;
  public static final int EXTERNAL_LINK_URL_ID = 17;
  public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<HOST>",
    "<NUM>",
    "<CJ>",
    INTERNAL_LINK,
    EXTERNAL_LINK,
    CITATION,
    CATEGORY,
    BOLD,
    ITALICS,
    BOLD_ITALICS,
    HEADING,
    SUB_HEADING,
    EXTERNAL_LINK_URL
  };
  public static final int TOKENS_ONLY = 0;
  public static final int UNTOKENIZED_ONLY = 1;
  public static final int BOTH = 2;
  public static final int UNTOKENIZED_TOKEN_FLAG = 1;
  private final WikipediaTokenizerImpl scanner;
  private int tokenOutput = TOKENS_ONLY;
  private Set<String> untokenizedTypes = Collections.emptySet();
  private Iterator<AttributeSource.State> tokens = null;
  private OffsetAttribute offsetAtt;
  private TypeAttribute typeAtt;
  private PositionIncrementAttribute posIncrAtt;
  private TermAttribute termAtt;
  private FlagsAttribute flagsAtt;
  public WikipediaTokenizer(Reader input) {
    this(input, TOKENS_ONLY, Collections.<String>emptySet());
  }
  public WikipediaTokenizer(Reader input, int tokenOutput, Set<String> untokenizedTypes) {
    super(input);
    this.scanner = new WikipediaTokenizerImpl(input);
    init(tokenOutput, untokenizedTypes);
  }
  public WikipediaTokenizer(AttributeFactory factory, Reader input, int tokenOutput, Set<String> untokenizedTypes) {
    super(factory, input);
    this.scanner = new WikipediaTokenizerImpl(input);
    init(tokenOutput, untokenizedTypes);
  }
  public WikipediaTokenizer(AttributeSource source, Reader input, int tokenOutput, Set<String> untokenizedTypes) {
    super(source, input);
    this.scanner = new WikipediaTokenizerImpl(input);
    init(tokenOutput, untokenizedTypes);
  }
  private void init(int tokenOutput, Set<String> untokenizedTypes) {
    this.tokenOutput = tokenOutput;
    this.untokenizedTypes = untokenizedTypes;
    this.offsetAtt = addAttribute(OffsetAttribute.class);
    this.typeAtt = addAttribute(TypeAttribute.class);
    this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    this.termAtt = addAttribute(TermAttribute.class);
    this.flagsAtt = addAttribute(FlagsAttribute.class);    
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (tokens != null && tokens.hasNext()){
      AttributeSource.State state = tokens.next();
      restoreState(state);
      return true;
    }
    clearAttributes();
    int tokenType = scanner.getNextToken();
    if (tokenType == WikipediaTokenizerImpl.YYEOF) {
      return false;
    }
    String type = WikipediaTokenizerImpl.TOKEN_TYPES[tokenType];
    if (tokenOutput == TOKENS_ONLY || untokenizedTypes.contains(type) == false){
      setupToken();
    } else if (tokenOutput == UNTOKENIZED_ONLY && untokenizedTypes.contains(type) == true){
      collapseTokens(tokenType);
    }
    else if (tokenOutput == BOTH){
      collapseAndSaveTokens(tokenType, type);
    }
    posIncrAtt.setPositionIncrement(scanner.getPositionIncrement());
    typeAtt.setType(type);
    return true;
  }
  private void collapseAndSaveTokens(int tokenType, String type) throws IOException {
    StringBuilder buffer = new StringBuilder(32);
    int numAdded = scanner.setText(buffer);
    int theStart = scanner.yychar();
    int lastPos = theStart + numAdded;
    int tmpTokType;
    int numSeen = 0;
    List<AttributeSource.State> tmp = new ArrayList<AttributeSource.State>();
    setupSavedToken(0, type);
    tmp.add(captureState());
    while ((tmpTokType = scanner.getNextToken()) != WikipediaTokenizerImpl.YYEOF && tmpTokType == tokenType && scanner.getNumWikiTokensSeen() > numSeen){
      int currPos = scanner.yychar();
      for (int i = 0; i < (currPos - lastPos); i++){
        buffer.append(' ');
      }
      numAdded = scanner.setText(buffer);
      setupSavedToken(scanner.getPositionIncrement(), type);
      tmp.add(captureState());
      numSeen++;
      lastPos = currPos + numAdded;
    }
    String s = buffer.toString().trim();
    termAtt.setTermBuffer(s.toCharArray(), 0, s.length());
    offsetAtt.setOffset(correctOffset(theStart), correctOffset(theStart + s.length()));
    flagsAtt.setFlags(UNTOKENIZED_TOKEN_FLAG);
    if (tmpTokType != WikipediaTokenizerImpl.YYEOF){
      scanner.yypushback(scanner.yylength());
    }
    tokens = tmp.iterator();
  }
  private void setupSavedToken(int positionInc, String type){
    setupToken();
    posIncrAtt.setPositionIncrement(positionInc);
    typeAtt.setType(type);
  }
  private void collapseTokens(int tokenType) throws IOException {
    StringBuilder buffer = new StringBuilder(32);
    int numAdded = scanner.setText(buffer);
    int theStart = scanner.yychar();
    int lastPos = theStart + numAdded;
    int tmpTokType;
    int numSeen = 0;
    while ((tmpTokType = scanner.getNextToken()) != WikipediaTokenizerImpl.YYEOF && tmpTokType == tokenType && scanner.getNumWikiTokensSeen() > numSeen){
      int currPos = scanner.yychar();
      for (int i = 0; i < (currPos - lastPos); i++){
        buffer.append(' ');
      }
      numAdded = scanner.setText(buffer);
      numSeen++;
      lastPos = currPos + numAdded;
    }
    String s = buffer.toString().trim();
    termAtt.setTermBuffer(s.toCharArray(), 0, s.length());
    offsetAtt.setOffset(correctOffset(theStart), correctOffset(theStart + s.length()));
    flagsAtt.setFlags(UNTOKENIZED_TOKEN_FLAG);
    if (tmpTokType != WikipediaTokenizerImpl.YYEOF){
      scanner.yypushback(scanner.yylength());
    } else {
      tokens = null;
    }
  }
  private void setupToken() {
    scanner.getText(termAtt);
    final int start = scanner.yychar();
    offsetAtt.setOffset(correctOffset(start), correctOffset(start + termAtt.termLength()));
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(input);
  }
  @Override
  public void reset(Reader reader) throws IOException {
    super.reset(reader);
    reset();
  }
  @Override
  public void end() throws IOException {
    final int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
    this.offsetAtt.setOffset(finalOffset, finalOffset);
  }
}