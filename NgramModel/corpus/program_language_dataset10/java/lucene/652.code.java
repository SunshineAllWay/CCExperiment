package org.apache.lucene.analysis.el;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;  
import org.apache.lucene.util.Version;
import java.io.Reader;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
public final class GreekAnalyzer extends StopwordAnalyzerBase
{
    private static final String[] GREEK_STOP_WORDS = {
      "ο", "η", "το", "οι", "τα", "του", "τησ", "των", "τον", "την", "και", 
      "κι", "κ", "ειμαι", "εισαι", "ειναι", "ειμαστε", "ειστε", "στο", "στον",
      "στη", "στην", "μα", "αλλα", "απο", "για", "προσ", "με", "σε", "ωσ",
      "παρα", "αντι", "κατα", "μετα", "θα", "να", "δε", "δεν", "μη", "μην",
      "επι", "ενω", "εαν", "αν", "τοτε", "που", "πωσ", "ποιοσ", "ποια", "ποιο",
      "ποιοι", "ποιεσ", "ποιων", "ποιουσ", "αυτοσ", "αυτη", "αυτο", "αυτοι",
      "αυτων", "αυτουσ", "αυτεσ", "αυτα", "εκεινοσ", "εκεινη", "εκεινο",
      "εκεινοι", "εκεινεσ", "εκεινα", "εκεινων", "εκεινουσ", "οπωσ", "ομωσ",
      "ισωσ", "οσο", "οτι"
    };
    public static final Set<?> getDefaultStopSet(){
      return DefaultSetHolder.DEFAULT_SET;
    }
    private static class DefaultSetHolder {
      private static final Set<?> DEFAULT_SET = CharArraySet.unmodifiableSet(new CharArraySet(
          Version.LUCENE_CURRENT, Arrays.asList(GREEK_STOP_WORDS), false));
    }
    public GreekAnalyzer(Version matchVersion) {
      this(matchVersion, DefaultSetHolder.DEFAULT_SET);
    }
    public GreekAnalyzer(Version matchVersion, Set<?> stopwords) {
      super(matchVersion, stopwords);
    }
    @Deprecated
    public GreekAnalyzer(Version matchVersion, String... stopwords)
    {
      this(matchVersion, StopFilter.makeStopSet(matchVersion, stopwords));
    }
    @Deprecated
    public GreekAnalyzer(Version matchVersion, Map<?,?> stopwords)
    {
      this(matchVersion, stopwords.keySet());
    }
    @Override
    protected TokenStreamComponents createComponents(String fieldName,
        Reader reader) {
      final Tokenizer source = new StandardTokenizer(matchVersion, reader);
      TokenStream result = new GreekLowerCaseFilter(source);
      if (matchVersion.onOrAfter(Version.LUCENE_31))
        result = new StandardFilter(result);
      return new TokenStreamComponents(source, new StopFilter(matchVersion, result, stopwords));
    }
}
