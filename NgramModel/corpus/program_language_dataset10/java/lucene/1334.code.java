package org.apache.lucene.queryParser.surround.query;
import java.io.IOException; 
public class TooManyBasicQueries extends IOException {
  public TooManyBasicQueries(int maxBasicQueries) {
    super("Exceeded maximum of " + maxBasicQueries + " basic queries.");
  }
}
