package org.apache.lucene.search.spans;
import java.io.IOException;
import java.util.Collection;
public abstract class Spans {
  public abstract boolean next() throws IOException;
  public abstract boolean skipTo(int target) throws IOException;
  public abstract int doc();
  public abstract int start();
  public abstract int end();
  public abstract Collection<byte[]> getPayload() throws IOException;
  public abstract boolean isPayloadAvailable();
}
