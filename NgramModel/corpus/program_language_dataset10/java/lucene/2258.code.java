package org.apache.solr.analysis;
import java.io.IOException;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.commons.io.IOUtils;
public final class PatternTokenizer extends Tokenizer {
  private final TermAttribute termAtt = (TermAttribute) addAttribute(TermAttribute.class);
  private final OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  private String str;
  private int index;
  private final Pattern pattern;
  private final int group;
  private final Matcher matcher;
  public PatternTokenizer(Reader input, Pattern pattern, int group) throws IOException {
    super(input);
    this.pattern = pattern;
    this.group = group;
    str = IOUtils.toString(input);
    matcher = pattern.matcher(str);
    index = 0;
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (index >= str.length()) return false;
    clearAttributes();
    if (group >= 0) {
      while (matcher.find()) {
        final String match = matcher.group(group);
        if (match.length() == 0) continue;
        termAtt.setTermBuffer(match);
        index = matcher.start(group);
        offsetAtt.setOffset(correctOffset(index), correctOffset(matcher.end(group)));
        return true;
      }
      index = Integer.MAX_VALUE; 
      return false;
    } else {
      while (matcher.find()) {
        if (matcher.start() - index > 0) {
          termAtt.setTermBuffer(str, index, matcher.start() - index);
          offsetAtt.setOffset(correctOffset(index), correctOffset(matcher.start()));
          index = matcher.end();
          return true;
        }
        index = matcher.end();
      }
      if (str.length() - index == 0) {
        index = Integer.MAX_VALUE; 
        return false;
      }
      termAtt.setTermBuffer(str, index, str.length() - index);
      offsetAtt.setOffset(correctOffset(index), correctOffset(str.length()));
      index = Integer.MAX_VALUE; 
      return true;
    }
  }
  @Override
  public void end() throws IOException {
    final int ofs = correctOffset(str.length());
    offsetAtt.setOffset(ofs, ofs);
  }
  @Override
  public void reset(Reader input) throws IOException {
    super.reset(input);
    str = IOUtils.toString(input);
    matcher.reset(str);
    index = 0;
  }
}
