package org.apache.solr.analysis;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
public final class CommonGramsFilter extends TokenFilter {
  static final String GRAM_TYPE = "gram";
  private static final char SEPARATOR = '_';
  private final CharArraySet commonWords;
  private final StringBuilder buffer = new StringBuilder();
  private final TermAttribute termAttribute = (TermAttribute) addAttribute(TermAttribute.class);
  private final OffsetAttribute offsetAttribute = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  private final TypeAttribute typeAttribute = (TypeAttribute) addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute posIncAttribute = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  private int lastStartOffset;
  private boolean lastWasCommon;
  private State savedState;
  public CommonGramsFilter(TokenStream input, Set commonWords) {
    this(input, commonWords, false);
  }
  public CommonGramsFilter(TokenStream input, Set commonWords, boolean ignoreCase) {
    super(input);
    if (commonWords instanceof CharArraySet) {
      this.commonWords = (CharArraySet) commonWords;
    } else {
      this.commonWords = new CharArraySet(commonWords.size(), ignoreCase);
      this.commonWords.addAll(commonWords);
    }
  }
  public CommonGramsFilter(TokenStream input, String[] commonWords) {
    this(input, commonWords, false);
  }
  public CommonGramsFilter(TokenStream input, String[] commonWords, boolean ignoreCase) {
    super(input);
    this.commonWords = makeCommonSet(commonWords, ignoreCase);
  }
  public static CharArraySet makeCommonSet(String[] commonWords) {
    return makeCommonSet(commonWords, false);
  }
  public static CharArraySet makeCommonSet(String[] commonWords, boolean ignoreCase) {
    CharArraySet commonSet = new CharArraySet(commonWords.length, ignoreCase);
    commonSet.addAll(Arrays.asList(commonWords));
    return commonSet;
  }
  public boolean incrementToken() throws IOException {
    if (savedState != null) {
      restoreState(savedState);
      savedState = null;
      saveTermBuffer();
      return true;
    } else if (!input.incrementToken()) {
        return false;
    }
    if (lastWasCommon || (isCommon() && buffer.length() > 0)) {
      savedState = captureState();
      gramToken();
      return true;      
    }
    saveTermBuffer();
    return true;
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    lastWasCommon = false;
    savedState = null;
    buffer.setLength(0);
  }
  private boolean isCommon() {
    return commonWords != null && commonWords.contains(termAttribute.termBuffer(), 0, termAttribute.termLength());
  }
  private void saveTermBuffer() {
    buffer.setLength(0);
    buffer.append(termAttribute.termBuffer(), 0, termAttribute.termLength());
    buffer.append(SEPARATOR);
    lastStartOffset = offsetAttribute.startOffset();
    lastWasCommon = isCommon();
  }
  private void gramToken() {
    buffer.append(termAttribute.termBuffer(), 0, termAttribute.termLength());
    int endOffset = offsetAttribute.endOffset();
    clearAttributes();
    int length = buffer.length();
    char termText[] = termAttribute.termBuffer();
    if (length > termText.length) {
      termText = termAttribute.resizeTermBuffer(length);
    }
    buffer.getChars(0, length, termText, 0);
    termAttribute.setTermLength(length);
    posIncAttribute.setPositionIncrement(0);
    offsetAttribute.setOffset(lastStartOffset, endOffset);
    typeAttribute.setType(GRAM_TYPE);
    buffer.setLength(0);
  }
}
