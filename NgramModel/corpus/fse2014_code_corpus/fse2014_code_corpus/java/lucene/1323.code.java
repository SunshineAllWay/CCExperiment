package org.apache.lucene.queryParser.surround.query;
import java.io.IOException;
public interface DistanceSubQuery {
  String distanceSubQueryNotAllowed();
  void addSpanQueries(SpanNearClauseFactory sncf) throws IOException;
}
