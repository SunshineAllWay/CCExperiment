package org.apache.lucene.search.spans;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.CheckHits;
public class TestSpanExplanationsOfNonMatches
  extends TestSpanExplanations {
  @Override
  public void qtest(Query q, int[] expDocNrs) throws Exception {
    CheckHits.checkNoMatchExplanations(q, FIELD, searcher, expDocNrs);
  }
}
