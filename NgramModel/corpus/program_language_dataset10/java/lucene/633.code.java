package org.apache.lucene.analysis.compound;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.FlagsAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.Version;
public abstract class CompoundWordTokenFilterBase extends TokenFilter {
  public static final int DEFAULT_MIN_WORD_SIZE = 5;
  public static final int DEFAULT_MIN_SUBWORD_SIZE = 2;
  public static final int DEFAULT_MAX_SUBWORD_SIZE = 15;
  protected final CharArraySet dictionary;
  protected final LinkedList<Token> tokens;
  protected final int minWordSize;
  protected final int minSubwordSize;
  protected final int maxSubwordSize;
  protected final boolean onlyLongestMatch;
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private FlagsAttribute flagsAtt;
  private PositionIncrementAttribute posIncAtt;
  private TypeAttribute typeAtt;
  private PayloadAttribute payloadAtt;
  private final Token wrapper = new Token();
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, String[] dictionary, int minWordSize, int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    this(Version.LUCENE_30, input, makeDictionary(dictionary),minWordSize,minSubwordSize,maxSubwordSize, onlyLongestMatch);
  }
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, String[] dictionary, boolean onlyLongestMatch) {
    this(Version.LUCENE_30, input, makeDictionary(dictionary),DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, onlyLongestMatch);
  }
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, Set<?> dictionary, boolean onlyLongestMatch) {
    this(Version.LUCENE_30, input, dictionary,DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, onlyLongestMatch);
  }
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, String[] dictionary) {
    this(Version.LUCENE_30, input, makeDictionary(dictionary),DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, Set<?> dictionary) {
    this(Version.LUCENE_30, input, dictionary,DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  @Deprecated
  protected CompoundWordTokenFilterBase(TokenStream input, Set<?> dictionary, int minWordSize, int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    this(Version.LUCENE_30, input, dictionary, minWordSize, minSubwordSize, maxSubwordSize, onlyLongestMatch);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, String[] dictionary, int minWordSize, int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    this(matchVersion, input,makeDictionary(dictionary),minWordSize,minSubwordSize,maxSubwordSize, onlyLongestMatch);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, String[] dictionary, boolean onlyLongestMatch) {
    this(matchVersion, input,makeDictionary(dictionary),DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, onlyLongestMatch);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, Set dictionary, boolean onlyLongestMatch) {
    this(matchVersion, input,dictionary,DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, onlyLongestMatch);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, String[] dictionary) {
    this(matchVersion, input,makeDictionary(dictionary),DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, Set dictionary) {
    this(matchVersion, input,dictionary,DEFAULT_MIN_WORD_SIZE,DEFAULT_MIN_SUBWORD_SIZE,DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  protected CompoundWordTokenFilterBase(Version matchVersion, TokenStream input, Set dictionary, int minWordSize, int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    super(input);
    this.tokens=new LinkedList<Token>();
    this.minWordSize=minWordSize;
    this.minSubwordSize=minSubwordSize;
    this.maxSubwordSize=maxSubwordSize;
    this.onlyLongestMatch=onlyLongestMatch;
    if (dictionary instanceof CharArraySet) {
      this.dictionary = (CharArraySet) dictionary;
    } else {
      this.dictionary = new CharArraySet(matchVersion, dictionary.size(), false);
      addAllLowerCase(this.dictionary, dictionary);
    }
    termAtt = addAttribute(TermAttribute.class);
    offsetAtt = addAttribute(OffsetAttribute.class);
    flagsAtt = addAttribute(FlagsAttribute.class);
    posIncAtt = addAttribute(PositionIncrementAttribute.class);
    typeAtt = addAttribute(TypeAttribute.class);
    payloadAtt = addAttribute(PayloadAttribute.class);
  }
  public static final Set<?> makeDictionary(final String[] dictionary) {
    return makeDictionary(Version.LUCENE_30, dictionary);
  }
  public static final Set<?> makeDictionary(final Version matchVersion, final String[] dictionary) {
    CharArraySet dict = new CharArraySet(matchVersion, dictionary.length, false);
    addAllLowerCase(dict, Arrays.asList(dictionary));
    return dict;
  }
  private final void setToken(final Token token) throws IOException {
    clearAttributes();
    termAtt.setTermBuffer(token.termBuffer(), 0, token.termLength());
    flagsAtt.setFlags(token.getFlags());
    typeAtt.setType(token.type());
    offsetAtt.setOffset(token.startOffset(), token.endOffset());
    posIncAtt.setPositionIncrement(token.getPositionIncrement());
    payloadAtt.setPayload(token.getPayload());
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (tokens.size() > 0) {
      setToken(tokens.removeFirst());
      return true;
    }
    if (!input.incrementToken())
      return false;
    wrapper.setTermBuffer(termAtt.termBuffer(), 0, termAtt.termLength());
    wrapper.setStartOffset(offsetAtt.startOffset());
    wrapper.setEndOffset(offsetAtt.endOffset());
    wrapper.setFlags(flagsAtt.getFlags());
    wrapper.setType(typeAtt.type());
    wrapper.setPositionIncrement(posIncAtt.getPositionIncrement());
    wrapper.setPayload(payloadAtt.getPayload());
    decompose(wrapper);
    if (tokens.size() > 0) {
      setToken(tokens.removeFirst());
      return true;
    } else {
      return false;
    }
  }
  protected static final void addAllLowerCase(Set<Object> target, Collection<String> col) {
    for (String string : col) {
      target.add(string.toLowerCase());
    }
  }
  protected static char[] makeLowerCaseCopy(final char[] buffer) {
    char[] result=new char[buffer.length];
    System.arraycopy(buffer, 0, result, 0, buffer.length);
    for (int i=0;i<buffer.length;++i) {
       result[i]=Character.toLowerCase(buffer[i]);
    }
    return result;
  }
  protected final Token createToken(final int offset, final int length,
      final Token prototype) {
    int newStart = prototype.startOffset() + offset;
    Token t = prototype.clone(prototype.termBuffer(), offset, length, newStart, newStart+length);
    t.setPositionIncrement(0);
    return t;
  }
  protected void decompose(final Token token) {
    tokens.add((Token) token.clone());
    if (token.termLength() < this.minWordSize) {
      return;
    }
    decomposeInternal(token);
  }
  protected abstract void decomposeInternal(final Token token);
  @Override
  public void reset() throws IOException {
    super.reset();
    tokens.clear();
  }
}
