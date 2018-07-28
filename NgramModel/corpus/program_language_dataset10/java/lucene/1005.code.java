package org.apache.lucene.search.highlight;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.search.spans.Spans;
public class SimpleSpanFragmenter implements Fragmenter {
  private static final int DEFAULT_FRAGMENT_SIZE = 100;
  private int fragmentSize;
  private int currentNumFrags;
  private int position = -1;
  private QueryScorer queryScorer;
  private int waitForPos = -1;
  private int textSize;
  private TermAttribute termAtt;
  private PositionIncrementAttribute posIncAtt;
  private OffsetAttribute offsetAtt;
  public SimpleSpanFragmenter(QueryScorer queryScorer) {
    this(queryScorer, DEFAULT_FRAGMENT_SIZE);
  }
  public SimpleSpanFragmenter(QueryScorer queryScorer, int fragmentSize) {
    this.fragmentSize = fragmentSize;
    this.queryScorer = queryScorer;
  }
  public boolean isNewFragment() {
    position += posIncAtt.getPositionIncrement();
    if (waitForPos == position) {
      waitForPos = -1;
    } else if (waitForPos != -1) {
      return false;
    }
    WeightedSpanTerm wSpanTerm = queryScorer.getWeightedSpanTerm(termAtt.term());
    if (wSpanTerm != null) {
      List<PositionSpan> positionSpans = wSpanTerm.getPositionSpans();
      for (int i = 0; i < positionSpans.size(); i++) {
        if (positionSpans.get(i).start == position) {
          waitForPos = positionSpans.get(i).end + 1;
          break;
        }
      }
    }
    boolean isNewFrag = offsetAtt.endOffset() >= (fragmentSize * currentNumFrags)
        && (textSize - offsetAtt.endOffset()) >= (fragmentSize >>> 1);
    if (isNewFrag) {
      currentNumFrags++;
    }
    return isNewFrag;
  }
  public void start(String originalText, TokenStream tokenStream) {
    position = -1;
    currentNumFrags = 1;
    textSize = originalText.length();
    termAtt = tokenStream.addAttribute(TermAttribute.class);
    posIncAtt = tokenStream.addAttribute(PositionIncrementAttribute.class);
    offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
  }
}
