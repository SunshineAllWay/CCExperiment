package org.apache.lucene.search.highlight;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public class WeightedSpanTerm extends WeightedTerm{
  boolean positionSensitive;
  private List<PositionSpan> positionSpans = new ArrayList<PositionSpan>();
  public WeightedSpanTerm(float weight, String term) {
    super(weight, term);
    this.positionSpans = new ArrayList<PositionSpan>();
  }
  public WeightedSpanTerm(float weight, String term, boolean positionSensitive) {
    super(weight, term);
    this.positionSensitive = positionSensitive;
  }
  public boolean checkPosition(int position) {
    Iterator<PositionSpan> positionSpanIt = positionSpans.iterator();
    while (positionSpanIt.hasNext()) {
      PositionSpan posSpan = positionSpanIt.next();
      if (((position >= posSpan.start) && (position <= posSpan.end))) {
        return true;
      }
    }
    return false;
  }
  public void addPositionSpans(List<PositionSpan> positionSpans) {
    this.positionSpans.addAll(positionSpans);
  }
  public boolean isPositionSensitive() {
    return positionSensitive;
  }
  public void setPositionSensitive(boolean positionSensitive) {
    this.positionSensitive = positionSensitive;
  }
  public List<PositionSpan> getPositionSpans() {
    return positionSpans;
  }
}
class PositionSpan {
  int start;
  int end;
  public PositionSpan(int start, int end) {
    this.start = start;
    this.end = end;
  }
}
