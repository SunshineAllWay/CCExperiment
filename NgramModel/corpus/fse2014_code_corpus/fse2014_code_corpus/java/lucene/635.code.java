package org.apache.lucene.analysis.compound;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter; 
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.compound.hyphenation.Hyphenation;
import org.apache.lucene.analysis.compound.hyphenation.HyphenationTree;
import org.apache.lucene.util.Version;
import org.xml.sax.InputSource;
public class HyphenationCompoundWordTokenFilter extends
    CompoundWordTokenFilterBase {
  private HyphenationTree hyphenator;
  public HyphenationCompoundWordTokenFilter(Version matchVersion, TokenStream input,
      HyphenationTree hyphenator, String[] dictionary, int minWordSize,
      int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    this(input, hyphenator, makeDictionary(dictionary), minWordSize,
        minSubwordSize, maxSubwordSize, onlyLongestMatch);
  }
  public HyphenationCompoundWordTokenFilter(Version matchVersion, TokenStream input,
      HyphenationTree hyphenator, String[] dictionary) {
    this(input, hyphenator, makeDictionary(dictionary), DEFAULT_MIN_WORD_SIZE,
        DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  public HyphenationCompoundWordTokenFilter(Version matchVersion, TokenStream input,
      HyphenationTree hyphenator, Set<?> dictionary) {
    this(input, hyphenator, dictionary, DEFAULT_MIN_WORD_SIZE,
        DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  public HyphenationCompoundWordTokenFilter(Version matchVersion, TokenStream input,
      HyphenationTree hyphenator, Set<?> dictionary, int minWordSize,
      int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    super(matchVersion, input, dictionary, minWordSize, minSubwordSize, maxSubwordSize,
        onlyLongestMatch);
    this.hyphenator = hyphenator;
  }
  @Deprecated
  public HyphenationCompoundWordTokenFilter(TokenStream input,
      HyphenationTree hyphenator, String[] dictionary, int minWordSize,
      int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    this(Version.LUCENE_30, input, hyphenator, makeDictionary(dictionary), minWordSize,
        minSubwordSize, maxSubwordSize, onlyLongestMatch);
  }
  @Deprecated
  public HyphenationCompoundWordTokenFilter(TokenStream input,
      HyphenationTree hyphenator, String[] dictionary) {
    this(Version.LUCENE_30, input, hyphenator, makeDictionary(dictionary), DEFAULT_MIN_WORD_SIZE,
        DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  @Deprecated
  public HyphenationCompoundWordTokenFilter(TokenStream input,
      HyphenationTree hyphenator, Set<?> dictionary) {
    this(Version.LUCENE_30, input, hyphenator, dictionary, DEFAULT_MIN_WORD_SIZE,
        DEFAULT_MIN_SUBWORD_SIZE, DEFAULT_MAX_SUBWORD_SIZE, false);
  }
  @Deprecated
  public HyphenationCompoundWordTokenFilter(TokenStream input,
      HyphenationTree hyphenator, Set<?> dictionary, int minWordSize,
      int minSubwordSize, int maxSubwordSize, boolean onlyLongestMatch) {
    super(Version.LUCENE_30, input, dictionary, minWordSize, minSubwordSize, maxSubwordSize,
        onlyLongestMatch);
    this.hyphenator = hyphenator;
  }
  public static HyphenationTree getHyphenationTree(String hyphenationFilename)
      throws Exception {
    return getHyphenationTree(new File(hyphenationFilename));
  }
  public static HyphenationTree getHyphenationTree(File hyphenationFile)
      throws Exception {
    return getHyphenationTree(new InputStreamReader(new FileInputStream(
        hyphenationFile), "ISO-8859-1"));
  }
  public static HyphenationTree getHyphenationTree(Reader hyphenationReader)
      throws Exception {
    HyphenationTree tree = new HyphenationTree();
    tree.loadPatterns(new InputSource(hyphenationReader));
    return tree;
  }
  @Override
  protected void decomposeInternal(final Token token) {
    Hyphenation hyphens = hyphenator.hyphenate(token.termBuffer(), 0, token
        .termLength(), 1, 1);
    if (hyphens == null) {
      return;
    }
    final int[] hyp = hyphens.getHyphenationPoints();
    char[] lowerCaseTermBuffer=makeLowerCaseCopy(token.termBuffer());
    for (int i = 0; i < hyp.length; ++i) {
      int remaining = hyp.length - i;
      int start = hyp[i];
      Token longestMatchToken = null;
      for (int j = 1; j < remaining; j++) {
        int partLength = hyp[i + j] - start;
        if (partLength > this.maxSubwordSize) {
          break;
        }
        if (partLength < this.minSubwordSize) {
          continue;
        }
        if (dictionary.contains(lowerCaseTermBuffer, start, partLength)) {
          if (this.onlyLongestMatch) {
            if (longestMatchToken != null) {
              if (longestMatchToken.termLength() < partLength) {
                longestMatchToken = createToken(start, partLength, token);
              }
            } else {
              longestMatchToken = createToken(start, partLength, token);
            }
          } else {
            tokens.add(createToken(start, partLength, token));
          }
        } else if (dictionary.contains(lowerCaseTermBuffer, start,
            partLength - 1)) {
          if (this.onlyLongestMatch) {
            if (longestMatchToken != null) {
              if (longestMatchToken.termLength() < partLength - 1) {
                longestMatchToken = createToken(start, partLength - 1, token);
              }
            } else {
              longestMatchToken = createToken(start, partLength - 1, token);
            }
          } else {
            tokens.add(createToken(start, partLength - 1, token));
          }
        }
      }
      if (this.onlyLongestMatch && longestMatchToken!=null) {
        tokens.add(longestMatchToken);
      }
    }
  }
}
