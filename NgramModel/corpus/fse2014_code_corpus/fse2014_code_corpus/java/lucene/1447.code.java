package org.apache.lucene.analysis;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Set;
import java.util.List;
import org.apache.lucene.util.Version;
public final class StopAnalyzer extends StopwordAnalyzerBase {
  public static final Set<?> ENGLISH_STOP_WORDS_SET;
  static {
    final List<String> stopWords = Arrays.asList(
      "a", "an", "and", "are", "as", "at", "be", "but", "by",
      "for", "if", "in", "into", "is", "it",
      "no", "not", "of", "on", "or", "such",
      "that", "the", "their", "then", "there", "these",
      "they", "this", "to", "was", "will", "with"
    );
    final CharArraySet stopSet = new CharArraySet(Version.LUCENE_CURRENT, 
        stopWords.size(), false);
    stopSet.addAll(stopWords);  
    ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(stopSet); 
  }
  public StopAnalyzer(Version matchVersion) {
    this(matchVersion, ENGLISH_STOP_WORDS_SET);
  }
  public StopAnalyzer(Version matchVersion, Set<?> stopWords) {
    super(matchVersion, stopWords);
  }
  public StopAnalyzer(Version matchVersion, File stopwordsFile) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwordsFile));
  }
  public StopAnalyzer(Version matchVersion, Reader stopwords) throws IOException {
    this(matchVersion, WordlistLoader.getWordSet(stopwords));
  }
  @Override
  protected TokenStreamComponents createComponents(String fieldName,
      Reader reader) {
    final Tokenizer source = new LowerCaseTokenizer(matchVersion, reader);
    return new TokenStreamComponents(source, new StopFilter(matchVersion,
          source, stopwords));
  }
}
