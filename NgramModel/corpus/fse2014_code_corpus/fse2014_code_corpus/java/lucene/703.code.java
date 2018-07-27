package org.apache.lucene.analysis.ru;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.KeywordMarkerTokenFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WordlistLoader;
import org.apache.lucene.util.Version;
public final class RussianAnalyzer extends StopwordAnalyzerBase
{
    @Deprecated
    private static final String[] RUSSIAN_STOP_WORDS_30 = {
      "а", "без", "более", "бы", "был", "была", "были", "было", "быть", "в",
      "вам", "вас", "весь", "во", "вот", "все", "всего", "всех", "вы", "где", 
      "да", "даже", "для", "до", "его", "ее", "ей", "ею", "если", "есть", 
      "еще", "же", "за", "здесь", "и", "из", "или", "им", "их", "к", "как",
      "ко", "когда", "кто", "ли", "либо", "мне", "может", "мы", "на", "надо", 
      "наш", "не", "него", "нее", "нет", "ни", "них", "но", "ну", "о", "об", 
      "однако", "он", "она", "они", "оно", "от", "очень", "по", "под", "при", 
      "с", "со", "так", "также", "такой", "там", "те", "тем", "то", "того", 
      "тоже", "той", "только", "том", "ты", "у", "уже", "хотя", "чего", "чей", 
      "чем", "что", "чтобы", "чье", "чья", "эта", "эти", "это", "я"
    };
    public final static String DEFAULT_STOPWORD_FILE = "russian_stop.txt";
    private static class DefaultSetHolder {
      @Deprecated
      static final Set<?> DEFAULT_STOP_SET_30 = CharArraySet
          .unmodifiableSet(new CharArraySet(Version.LUCENE_CURRENT, 
              Arrays.asList(RUSSIAN_STOP_WORDS_30), false));
      static final Set<?> DEFAULT_STOP_SET;
      static {
        try {
          DEFAULT_STOP_SET = 
            WordlistLoader.getSnowballWordSet(SnowballFilter.class, DEFAULT_STOPWORD_FILE);
        } catch (IOException ex) {
          throw new RuntimeException("Unable to load default stopword set");
        }
      }
    }
    private final Set<?> stemExclusionSet;
    public static Set<?> getDefaultStopSet() {
      return DefaultSetHolder.DEFAULT_STOP_SET;
    }
    public RussianAnalyzer(Version matchVersion) {
      this(matchVersion,
        matchVersion.onOrAfter(Version.LUCENE_31) ? DefaultSetHolder.DEFAULT_STOP_SET
            : DefaultSetHolder.DEFAULT_STOP_SET_30);
    }
    @Deprecated
    public RussianAnalyzer(Version matchVersion, String... stopwords) {
      this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
    }
    public RussianAnalyzer(Version matchVersion, Set<?> stopwords){
      this(matchVersion, stopwords, CharArraySet.EMPTY_SET);
    }
    public RussianAnalyzer(Version matchVersion, Set<?> stopwords, Set<?> stemExclusionSet){
      super(matchVersion, stopwords);
      this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(matchVersion, stemExclusionSet));
    }
    @Deprecated
    public RussianAnalyzer(Version matchVersion, Map<?,?> stopwords)
    {
      this(matchVersion, stopwords.keySet());
    }
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
        Reader reader) {
      if (matchVersion.onOrAfter(Version.LUCENE_31)) {
        final Tokenizer source = new StandardTokenizer(matchVersion, reader);
        TokenStream result = new StandardFilter(source);
        result = new LowerCaseFilter(matchVersion, result);
        result = new StopFilter(matchVersion, result, stopwords);
        if (!stemExclusionSet.isEmpty()) result = new KeywordMarkerTokenFilter(
            result, stemExclusionSet);
        result = new SnowballFilter(result, new org.tartarus.snowball.ext.RussianStemmer());
        return new TokenStreamComponents(source, result);
      } else {
        final Tokenizer source = new RussianLetterTokenizer(matchVersion, reader);
        TokenStream result = new LowerCaseFilter(matchVersion, source);
        result = new StopFilter(matchVersion, result, stopwords);
        if (!stemExclusionSet.isEmpty()) result = new KeywordMarkerTokenFilter(
          result, stemExclusionSet);
        return new TokenStreamComponents(source, new RussianStemFilter(result));
      }
    }
}
