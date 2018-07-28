package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;
public final class PatternAnalyzer extends Analyzer {
  public static final Pattern NON_WORD_PATTERN = Pattern.compile("\\W+");
  public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
  private static final CharArraySet EXTENDED_ENGLISH_STOP_WORDS =
    CharArraySet.unmodifiableSet(new CharArraySet(Version.LUCENE_CURRENT, 
        Arrays.asList(
      "a", "about", "above", "across", "adj", "after", "afterwards",
      "again", "against", "albeit", "all", "almost", "alone", "along",
      "already", "also", "although", "always", "among", "amongst", "an",
      "and", "another", "any", "anyhow", "anyone", "anything",
      "anywhere", "are", "around", "as", "at", "be", "became", "because",
      "become", "becomes", "becoming", "been", "before", "beforehand",
      "behind", "being", "below", "beside", "besides", "between",
      "beyond", "both", "but", "by", "can", "cannot", "co", "could",
      "down", "during", "each", "eg", "either", "else", "elsewhere",
      "enough", "etc", "even", "ever", "every", "everyone", "everything",
      "everywhere", "except", "few", "first", "for", "former",
      "formerly", "from", "further", "had", "has", "have", "he", "hence",
      "her", "here", "hereafter", "hereby", "herein", "hereupon", "hers",
      "herself", "him", "himself", "his", "how", "however", "i", "ie", "if",
      "in", "inc", "indeed", "into", "is", "it", "its", "itself", "last",
      "latter", "latterly", "least", "less", "ltd", "many", "may", "me",
      "meanwhile", "might", "more", "moreover", "most", "mostly", "much",
      "must", "my", "myself", "namely", "neither", "never",
      "nevertheless", "next", "no", "nobody", "none", "noone", "nor",
      "not", "nothing", "now", "nowhere", "of", "off", "often", "on",
      "once one", "only", "onto", "or", "other", "others", "otherwise",
      "our", "ours", "ourselves", "out", "over", "own", "per", "perhaps",
      "rather", "s", "same", "seem", "seemed", "seeming", "seems",
      "several", "she", "should", "since", "so", "some", "somehow",
      "someone", "something", "sometime", "sometimes", "somewhere",
      "still", "such", "t", "than", "that", "the", "their", "them",
      "themselves", "then", "thence", "there", "thereafter", "thereby",
      "therefor", "therein", "thereupon", "these", "they", "this",
      "those", "though", "through", "throughout", "thru", "thus", "to",
      "together", "too", "toward", "towards", "under", "until", "up",
      "upon", "us", "very", "via", "was", "we", "well", "were", "what",
      "whatever", "whatsoever", "when", "whence", "whenever",
      "whensoever", "where", "whereafter", "whereas", "whereat",
      "whereby", "wherefrom", "wherein", "whereinto", "whereof",
      "whereon", "whereto", "whereunto", "whereupon", "wherever",
      "wherewith", "whether", "which", "whichever", "whichsoever",
      "while", "whilst", "whither", "who", "whoever", "whole", "whom",
      "whomever", "whomsoever", "whose", "whosoever", "why", "will",
      "with", "within", "without", "would", "xsubj", "xcal", "xauthor",
      "xother ", "xnote", "yet", "you", "your", "yours", "yourself",
      "yourselves"
    ), true));
  public static final PatternAnalyzer DEFAULT_ANALYZER = new PatternAnalyzer(
    Version.LUCENE_CURRENT, NON_WORD_PATTERN, true, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
  public static final PatternAnalyzer EXTENDED_ANALYZER = new PatternAnalyzer(
    Version.LUCENE_CURRENT, NON_WORD_PATTERN, true, EXTENDED_ENGLISH_STOP_WORDS);
  private final Pattern pattern;
  private final boolean toLowerCase;
  private final Set<?> stopWords;
  private final Version matchVersion;
  public PatternAnalyzer(Version matchVersion, Pattern pattern, boolean toLowerCase, Set<?> stopWords) {
    if (pattern == null) 
      throw new IllegalArgumentException("pattern must not be null");
    if (eqPattern(NON_WORD_PATTERN, pattern)) pattern = NON_WORD_PATTERN;
    else if (eqPattern(WHITESPACE_PATTERN, pattern)) pattern = WHITESPACE_PATTERN;
    if (stopWords != null && stopWords.size() == 0) stopWords = null;
    this.pattern = pattern;
    this.toLowerCase = toLowerCase;
    this.stopWords = stopWords;
    this.matchVersion = matchVersion;
  }
  public TokenStream tokenStream(String fieldName, String text) {
    if (text == null) 
      throw new IllegalArgumentException("text must not be null");
    TokenStream stream;
    if (pattern == NON_WORD_PATTERN) { 
      stream = new FastStringTokenizer(text, true, toLowerCase, stopWords);
    }
    else if (pattern == WHITESPACE_PATTERN) { 
      stream = new FastStringTokenizer(text, false, toLowerCase, stopWords);
    }
    else {
      stream = new PatternTokenizer(text, pattern, toLowerCase);
      if (stopWords != null) stream = new StopFilter(matchVersion, stream, stopWords);
    }
    return stream;
  }
  @Override
  public TokenStream tokenStream(String fieldName, Reader reader) {
    if (reader instanceof FastStringReader) { 
      return tokenStream(fieldName, ((FastStringReader)reader).getString());
    }
    try {
      String text = toString(reader);
      return tokenStream(fieldName, text);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  @Override
  public boolean equals(Object other) {
    if (this  == other) return true;
    if (this  == DEFAULT_ANALYZER && other == EXTENDED_ANALYZER) return false;
    if (other == DEFAULT_ANALYZER && this  == EXTENDED_ANALYZER) return false;
    if (other instanceof PatternAnalyzer) {
      PatternAnalyzer p2 = (PatternAnalyzer) other;
      return 
        toLowerCase == p2.toLowerCase &&
        eqPattern(pattern, p2.pattern) &&
        eq(stopWords, p2.stopWords);
    }
    return false;
  }
  @Override
  public int hashCode() {
    if (this == DEFAULT_ANALYZER) return -1218418418; 
    if (this == EXTENDED_ANALYZER) return 1303507063; 
    int h = 1;
    h = 31*h + pattern.pattern().hashCode();
    h = 31*h + pattern.flags();
    h = 31*h + (toLowerCase ? 1231 : 1237);
    h = 31*h + (stopWords != null ? stopWords.hashCode() : 0);
    return h;
  }
  private static boolean eq(Object o1, Object o2) {
    return (o1 == o2) || (o1 != null ? o1.equals(o2) : false);
  }
  private static boolean eqPattern(Pattern p1, Pattern p2) {
    return p1 == p2 || (p1.flags() == p2.flags() && p1.pattern().equals(p2.pattern()));
  }
  private static String toString(Reader input) throws IOException {
    try {
      int len = 256;
      char[] buffer = new char[len];
      char[] output = new char[len];
      len = 0;
      int n;
      while ((n = input.read(buffer)) >= 0) {
        if (len + n > output.length) { 
          char[] tmp = new char[Math.max(output.length << 1, len + n)];
          System.arraycopy(output, 0, tmp, 0, len);
          System.arraycopy(buffer, 0, tmp, len, n);
          buffer = output; 
          output = tmp;
        } else {
          System.arraycopy(buffer, 0, output, len, n);
        }
        len += n;
      }
      return new String(output, 0, len);
    } finally {
      input.close();
    }
  }
  private static final class PatternTokenizer extends TokenStream {
    private final String str;
    private final boolean toLowerCase;
    private Matcher matcher;
    private int pos = 0;
    private static final Locale locale = Locale.getDefault();
    private TermAttribute termAtt = addAttribute(TermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    public PatternTokenizer(String str, Pattern pattern, boolean toLowerCase) {
      this.str = str;
      this.matcher = pattern.matcher(str);
      this.toLowerCase = toLowerCase;
    }
    @Override
    public final boolean incrementToken() {
      if (matcher == null) return false;
      clearAttributes();
      while (true) { 
        int start = pos;
        int end;
        boolean isMatch = matcher.find();
        if (isMatch) {
          end = matcher.start();
          pos = matcher.end();
        } else { 
          end = str.length();
          matcher = null; 
        }
        if (start != end) { 
          String text = str.substring(start, end);
          if (toLowerCase) text = text.toLowerCase(locale);
          termAtt.setTermBuffer(text);
          offsetAtt.setOffset(start, end);
          return true;
        }
        if (!isMatch) return false;
      }
    }
    @Override
    public final void end() {
      final int finalOffset = str.length();
    	this.offsetAtt.setOffset(finalOffset, finalOffset);
    }    
  } 
  private static final class FastStringTokenizer extends TokenStream {
    private final String str;
    private int pos;
    private final boolean isLetter;
    private final boolean toLowerCase;
    private final Set<?> stopWords;
    private static final Locale locale = Locale.getDefault();
    private TermAttribute termAtt = addAttribute(TermAttribute.class);
    private OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    public FastStringTokenizer(String str, boolean isLetter, boolean toLowerCase, Set<?> stopWords) {
      this.str = str;
      this.isLetter = isLetter;
      this.toLowerCase = toLowerCase;
      this.stopWords = stopWords;
    }
    @Override
    public boolean incrementToken() {
      clearAttributes();
      String s = str;
      int len = s.length();
      int i = pos;
      boolean letter = isLetter;
      int start = 0;
      String text;
      do {
        text = null;
        while (i < len && !isTokenChar(s.charAt(i), letter)) {
          i++;
        }
        if (i < len) { 
          start = i;
          while (i < len && isTokenChar(s.charAt(i), letter)) {
            i++;
          }
          text = s.substring(start, i);
          if (toLowerCase) text = text.toLowerCase(locale);
        }
      } while (text != null && isStopWord(text));
      pos = i;
      if (text == null)
      {
        return false;
      }
      termAtt.setTermBuffer(text);
      offsetAtt.setOffset(start, i);
      return true;
    }
    @Override
    public final void end() {
      final int finalOffset = str.length();
      this.offsetAtt.setOffset(finalOffset, finalOffset);
    }    
    private boolean isTokenChar(char c, boolean isLetter) {
      return isLetter ? Character.isLetter(c) : !Character.isWhitespace(c);
    }
    private boolean isStopWord(String text) {
      return stopWords != null && stopWords.contains(text);
    }
  }
  static final class FastStringReader extends StringReader {
    private final String s;
    FastStringReader(String s) {
      super(s);
      this.s = s;
    }
    String getString() {
      return s;
    }
  }
}
