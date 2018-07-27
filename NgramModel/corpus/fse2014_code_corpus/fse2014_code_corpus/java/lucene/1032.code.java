package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.TermPositions;
import java.io.IOException;
public class InstantiatedTermPositions
    extends InstantiatedTermDocs
    implements TermPositions {
  public int getPayloadLength() {
    return currentDocumentInformation.getPayloads()[currentTermPositionIndex].length;
  }
  public byte[] getPayload(byte[] data, int offset) throws IOException {
    byte[] payloads = currentDocumentInformation.getPayloads()[currentTermPositionIndex];
    if (data == null || data.length - offset < getPayloadLength()) {
      return payloads;
    } else {
      System.arraycopy(payloads, 0, data, offset, payloads.length);
      return data;
    }
  }
  public boolean isPayloadAvailable() {
    return currentDocumentInformation.getPayloads()[currentTermPositionIndex] != null;
  }
  public InstantiatedTermPositions(InstantiatedIndexReader reader) {
    super(reader);
  }
  public int nextPosition() {
    currentTermPositionIndex++;
    return currentDocumentInformation.getTermPositions()[currentTermPositionIndex];
  }
  private int currentTermPositionIndex;
  @Override
  public boolean next() {
    currentTermPositionIndex = -1;
    return super.next();
  }
  @Override
  public boolean skipTo(int target) {
    currentTermPositionIndex = -1;
    return super.skipTo(target);
  }
}
