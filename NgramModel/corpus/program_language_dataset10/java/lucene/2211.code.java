package org.apache.solr.analysis;
import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.StringTokenizer;
public class CapitalizationFilterFactory extends BaseTokenFilterFactory {
  public static final int DEFAULT_MAX_WORD_COUNT = Integer.MAX_VALUE;
  public static final String KEEP = "keep";
  public static final String KEEP_IGNORE_CASE = "keepIgnoreCase";
  public static final String OK_PREFIX = "okPrefix";
  public static final String MIN_WORD_LENGTH = "minWordLength";
  public static final String MAX_WORD_COUNT = "maxWordCount";
  public static final String MAX_TOKEN_LENGTH = "maxTokenLength";
  public static final String ONLY_FIRST_WORD = "onlyFirstWord";
  public static final String FORCE_FIRST_LETTER = "forceFirstLetter";
  CharArraySet keep;
  Collection<char[]> okPrefix = Collections.emptyList(); 
  int minWordLength = 0;  
  int maxWordCount = DEFAULT_MAX_WORD_COUNT;
  int maxTokenLength = DEFAULT_MAX_WORD_COUNT;
  boolean onlyFirstWord = true;
  boolean forceFirstLetter = true; 
  @Override
  public void init(Map<String, String> args) {
    super.init(args);
    String k = args.get(KEEP);
    if (k != null) {
      StringTokenizer st = new StringTokenizer(k);
      boolean ignoreCase = false;
      String ignoreStr = args.get(KEEP_IGNORE_CASE);
      if ("true".equalsIgnoreCase(ignoreStr)) {
        ignoreCase = true;
      }
      keep = new CharArraySet(10, ignoreCase);
      while (st.hasMoreTokens()) {
        k = st.nextToken().trim();
        keep.add(k.toCharArray());
      }
    }
    k = args.get(OK_PREFIX);
    if (k != null) {
      okPrefix = new ArrayList<char[]>();
      StringTokenizer st = new StringTokenizer(k);
      while (st.hasMoreTokens()) {
        okPrefix.add(st.nextToken().trim().toCharArray());
      }
    }
    k = args.get(MIN_WORD_LENGTH);
    if (k != null) {
      minWordLength = Integer.valueOf(k);
    }
    k = args.get(MAX_WORD_COUNT);
    if (k != null) {
      maxWordCount = Integer.valueOf(k);
    }
    k = args.get(MAX_TOKEN_LENGTH);
    if (k != null) {
      maxTokenLength = Integer.valueOf(k);
    }
    k = args.get(ONLY_FIRST_WORD);
    if (k != null) {
      onlyFirstWord = Boolean.valueOf(k);
    }
    k = args.get(FORCE_FIRST_LETTER);
    if (k != null) {
      forceFirstLetter = Boolean.valueOf(k);
    }
  }
  public void processWord(char[] buffer, int offset, int length, int wordCount) {
    if (length < 1) {
      return;
    }
    if (onlyFirstWord && wordCount > 0) {
      for (int i = 0; i < length; i++) {
        buffer[offset + i] = Character.toLowerCase(buffer[offset + i]);
      }
      return;
    }
    if (keep != null && keep.contains(buffer, offset, length)) {
      if (wordCount == 0 && forceFirstLetter) {
        buffer[offset] = Character.toUpperCase(buffer[offset]);
      }
      return;
    }
    if (length < minWordLength) {
      return;
    }
    for (char[] prefix : okPrefix) {
      if (length >= prefix.length) { 
        boolean match = true;
        for (int i = 0; i < prefix.length; i++) {
          if (prefix[i] != buffer[offset + i]) {
            match = false;
            break;
          }
        }
        if (match == true) {
          return;
        }
      }
    }
    buffer[offset] = Character.toUpperCase(buffer[offset]);
    for (int i = 1; i < length; i++) {
      buffer[offset + i] = Character.toLowerCase(buffer[offset + i]);
    }
  }
  public CapitalizationFilter create(TokenStream input) {
    return new CapitalizationFilter(input, this);
  }
}
class CapitalizationFilter extends TokenFilter {
  private final CapitalizationFilterFactory factory;
  private final TermAttribute termAtt;
  public CapitalizationFilter(TokenStream in, final CapitalizationFilterFactory factory) {
    super(in);
    this.factory = factory;
    this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;
    char[] termBuffer = termAtt.termBuffer();
    int termBufferLength = termAtt.termLength();
    char[] backup = null;
    if (factory.maxWordCount < CapitalizationFilterFactory.DEFAULT_MAX_WORD_COUNT) {
      backup = new char[termBufferLength];
      System.arraycopy(termBuffer, 0, backup, 0, termBufferLength);
    }
    if (termBufferLength < factory.maxTokenLength) {
      int wordCount = 0;
      int lastWordStart = 0;
      for (int i = 0; i < termBufferLength; i++) {
        char c = termBuffer[i];
        if (c <= ' ' || c == '.') {
          int len = i - lastWordStart;
          if (len > 0) {
            factory.processWord(termBuffer, lastWordStart, len, wordCount++);
            lastWordStart = i + 1;
            i++;
          }
        }
      }
      if (lastWordStart < termBufferLength) {
        factory.processWord(termBuffer, lastWordStart, termBufferLength - lastWordStart, wordCount++);
      }
      if (wordCount > factory.maxWordCount) {
        termAtt.setTermBuffer(backup, 0, termBufferLength);
      }
    }
    return true;
  }
}
