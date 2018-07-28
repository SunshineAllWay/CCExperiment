package org.apache.lucene.analysis.tokenattributes;
import org.apache.lucene.index.Payload;
import org.apache.lucene.util.Attribute;
public interface PayloadAttribute extends Attribute {
  public Payload getPayload();
  public void setPayload(Payload payload);
}
