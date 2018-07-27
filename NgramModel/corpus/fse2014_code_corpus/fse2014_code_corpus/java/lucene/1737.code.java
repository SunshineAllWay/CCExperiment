package org.apache.lucene.search.payloads;
import java.io.Serializable;
public abstract class PayloadFunction implements Serializable {
  public abstract float currentScore(int docId, String field, int start, int end, int numPayloadsSeen, float currentScore, float currentPayloadScore);
  public abstract float docScore(int docId, String field, int numPayloadsSeen, float payloadScore);
  @Override
  public abstract int hashCode();
  @Override
  public abstract boolean equals(Object o);
}
