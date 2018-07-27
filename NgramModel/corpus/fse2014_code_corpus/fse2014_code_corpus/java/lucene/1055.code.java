package org.apache.lucene.queryParser.ext;
import org.apache.lucene.queryParser.QueryParser;
public class ExtensionQuery {
  private final String field;
  private final String rawQueryString;
  private final QueryParser topLevelParser;
  public ExtensionQuery(QueryParser topLevelParser, String field, String rawQueryString) {
    this.field = field;
    this.rawQueryString = rawQueryString;
    this.topLevelParser = topLevelParser;
  }
  public String getField() {
    return field;
  }
  public String getRawQueryString() {
    return rawQueryString;
  }
  public QueryParser getTopLevelParser() {
    return topLevelParser;
  }
}
