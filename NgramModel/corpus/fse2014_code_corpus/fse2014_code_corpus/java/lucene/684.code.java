package org.apache.lucene.analysis.nl;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.ReusableAnalyzerBase;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.analysis.miscellaneous.StemmerOverrideFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;  
import org.apache.lucene.util.Version;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
public final class DutchAnalyzer extends ReusableAnalyzerBase {
  @Deprecated
  public final static String[] DUTCH_STOP_WORDS = getDefaultStopSet().toArray(new String[0]);
  public final static String DEFAULT_STOPWORD_FILE = "dutch_stop.txt";
  public static Set<?> getDefaultStopSet(){
    return DefaultSetHolder.DEFAULT_STOP_SET;
  }
  private static class DefaultSetHolder {
    static final Set<?> DEFAULT_STOP_SET;
    static {
      try {
        DEFAULT_STOP_SET = WordlistLoader.getSnowballWordSet(SnowballFilter.class, 
            DEFAULT_STOPWORD_FILE);
      } catch (IOException ex) {
        throw new RuntimeException("Unable to load default stopword set");
      }
    }
  }
  private final Set<?> stoptable;
  private Set<?> excltable = Collections.emptySet();
  private Map<String, String> stemdict = new HashMap<String, String>();
  private final Version matchVersion;
  public DutchAnalyzer(Version matchVersion) {
    this(matchVersion, DefaultSetHolder.DEFAULT_STOP_SET);
    stemdict.put("fiets", "fiets"); 
    stemdict.put("bromfiets", "bromfiets"); 
    stemdict.put("ei", "eier");
    stemdict.put("kind", "kinder");
  }
  public DutchAnalyzer(Version matchVersion, Set<?> stopwords){
    this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
  }
  public DutchAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionTable){
    stoptable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stopwords));
    excltable = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionTable));
    this.matchVersion = matchVersion;
  }
  @Deprecated
  public DutchAnalyzer(Version matchVersion, String... stopwords) {
    this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
  }
  @Deprecated
  public DutchAnalyzer(Version matchVersion, HashSet<?> stopwords) {
    this(matchVersion, (Set<?>)stopwords);
  }
  @Deprecated
  public DutchAnalyzer(Version matchVersion, File stopwords) {
    try {
      stoptable = org.apache.lucene.analysis.WordlistLoader.getWordSet(stopwords);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.matchVersion = matchVersion;
  }
  @Deprecated
  public void setStemExclusionTable(String... exclusionlist) {
    excltable = StopFilter.makeStopSet(matchVersion, exclusionlist);
    setPreviousTokenStream(null); 
  }
  @Deprecated
  public void setStemExclusionTable(HashSet<?> exclusionlist) {
    excltable = exclusionlist;
    setPreviousTokenStream(null); 
  }
  @Deprecated
  public void setStemExclusionTable(File exclusionlist) {
    try {
      excltable = org.apache.lucene.analysis.WordlistLoader.getWordSet(exclusionlist);
      setPreviousTokenStream(null); 
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  public void setStemDictionary(File stemdictFile) {
    try {
      stemdict = WordlistLoader.getStemDict(stemdictFile);
      setPreviousTokenStream(null); 
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader aReader) {
    if (matchVersion.onOrAfter(Version.LUCENE_31)) {
      final Tokenizer source = new StandardTokenizer(matchVersion, aReader);
      TokenStream result = new StandardFilter(source);
      result = new LowerCaseFilter(matchVersion, result);
      result = new StopFilter(matchVersion, result, stoptable);
      if (!excltable.isEmpty())
        result = new KeywordMarkerTokenFilter(result, excltable);
      if (!stemdict.isEmpty())
        result = new StemmerOverrideFilter(matchVersion, result, stemdict);
      result = new SnowballFilter(result, new org.tartarus.snowball.ext.DutchStemmer());
      return new TokenStreamComponents(source, result);
    } else {
      final Tokenizer source = new StandardTokenizer(matchVersion, aReader);
      TokenStream result = new StandardFilter(source);
      result = new StopFilter(matchVersion, result, stoptable);
      if (!excltable.isEmpty())
        result = new KeywordMarkerTokenFilter(result, excltable);
      result = new DutchStemFilter(result, stemdict);
      return new TokenStreamComponents(source, result);
    }
  }
}
