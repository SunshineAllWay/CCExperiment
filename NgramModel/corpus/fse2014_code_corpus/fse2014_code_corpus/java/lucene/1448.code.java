package org.apache.lucene.analysis;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.queryParser.QueryParser; 
import org.apache.lucene.util.Version;
public final class StopFilter extends TokenFilter {
  private final CharArraySet stopWords;
  private boolean enablePositionIncrements = false;
  private TermAttribute termAtt;
  private PositionIncrementAttribute posIncrAtt;
  @Deprecated
  public StopFilter(boolean enablePositionIncrements, TokenStream input, Set<?> stopWords, boolean ignoreCase)
  {
    this(Version.LUCENE_30, enablePositionIncrements, input, stopWords, ignoreCase);
  }
  public StopFilter(Version matchVersion, TokenStream input, Set<?> stopWords, boolean ignoreCase)
  {
   this(matchVersion, matchVersion.onOrAfter(Version.LUCENE_29), input, stopWords, ignoreCase);
  }
  private StopFilter(Version matchVersion, boolean enablePositionIncrements, TokenStream input, Set<?> stopWords, boolean ignoreCase){
    super(input);
    this.stopWords = stopWords instanceof CharArraySet ? (CharArraySet)stopWords : new CharArraySet(matchVersion, stopWords, ignoreCase);  
    this.enablePositionIncrements = enablePositionIncrements;
    termAtt = addAttribute(TermAttribute.class);
    posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  }
  @Deprecated
  public StopFilter(boolean enablePositionIncrements, TokenStream in, Set<?> stopWords) {
    this(Version.LUCENE_CURRENT, enablePositionIncrements, in, stopWords, false);
  }
  public StopFilter(Version matchVersion, TokenStream in, Set<?> stopWords) {
    this(matchVersion, in, stopWords, false);
  }
  @Deprecated
  public static final Set<Object> makeStopSet(String... stopWords) {
    return makeStopSet(Version.LUCENE_30, stopWords, false);
  }
  public static final Set<Object> makeStopSet(Version matchVersion, String... stopWords) {
    return makeStopSet(matchVersion, stopWords, false);
  }
  @Deprecated
  public static final Set<Object> makeStopSet(List<?> stopWords) {
    return makeStopSet(Version.LUCENE_30, stopWords, false);
  }
  public static final Set<Object> makeStopSet(Version matchVersion, List<?> stopWords) {
    return makeStopSet(matchVersion, stopWords, false);
  }
  @Deprecated
  public static final Set<Object> makeStopSet(String[] stopWords, boolean ignoreCase) {
    return makeStopSet(Version.LUCENE_30, stopWords, ignoreCase);
  }
  public static final Set<Object> makeStopSet(Version matchVersion, String[] stopWords, boolean ignoreCase) {
    CharArraySet stopSet = new CharArraySet(matchVersion, stopWords.length, ignoreCase);
    stopSet.addAll(Arrays.asList(stopWords));
    return stopSet;
  }
  @Deprecated
  public static final Set<Object> makeStopSet(List<?> stopWords, boolean ignoreCase){
    return makeStopSet(Version.LUCENE_30, stopWords, ignoreCase);
  }
  public static final Set<Object> makeStopSet(Version matchVersion, List<?> stopWords, boolean ignoreCase){
    CharArraySet stopSet = new CharArraySet(matchVersion, stopWords.size(), ignoreCase);
    stopSet.addAll(stopWords);
    return stopSet;
  }
  @Override
  public final boolean incrementToken() throws IOException {
    int skippedPositions = 0;
    while (input.incrementToken()) {
      if (!stopWords.contains(termAtt.termBuffer(), 0, termAtt.termLength())) {
        if (enablePositionIncrements) {
          posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
        }
        return true;
      }
      skippedPositions += posIncrAtt.getPositionIncrement();
    }
    return false;
  }
  @Deprecated
  public static boolean getEnablePositionIncrementsVersionDefault(Version matchVersion) {
    return matchVersion.onOrAfter(Version.LUCENE_29);
  }
  public boolean getEnablePositionIncrements() {
    return enablePositionIncrements;
  }
  public void setEnablePositionIncrements(boolean enable) {
    this.enablePositionIncrements = enable;
  }
}
