package org.apache.solr.analysis;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import java.io.IOException;
public final class TrimFilter extends TokenFilter {
  final boolean updateOffsets;
  private final TermAttribute termAtt;
  private final OffsetAttribute offsetAtt;
  public TrimFilter(TokenStream in, boolean updateOffsets) {
    super(in);
    this.updateOffsets = updateOffsets;
    this.termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    this.offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  }
  @Override
  public boolean incrementToken() throws IOException {
    if (!input.incrementToken()) return false;
    char[] termBuffer = termAtt.termBuffer();
    int len = termAtt.termLength();
    if (len == 0){
      return true;
    }
    int start = 0;
    int end = 0;
    int endOff = 0;
    for (start = 0; start < len && termBuffer[start] <= ' '; start++) {
    }
    for (end = len; end >= start && termBuffer[end - 1] <= ' '; end--) {
      endOff++;
    }
    if (start > 0 || end < len) {
      if (start < end) {
        termAtt.setTermBuffer(termBuffer, start, (end - start));
      } else {
        termAtt.setTermLength(0);
      }
      if (updateOffsets) {
        int newStart = offsetAtt.startOffset()+start;
        int newEnd = offsetAtt.endOffset() - (start<end ? endOff:0);
        offsetAtt.setOffset(newStart, newEnd);
      }
    }
    return true;
  }
}
