package org.apache.lucene.index;
import org.apache.lucene.store.IndexOutput;
import java.io.IOException;
final class FormatPostingsPositionsWriter extends FormatPostingsPositionsConsumer {
  final FormatPostingsDocsWriter parent;
  final IndexOutput out;
  boolean omitTermFreqAndPositions;
  boolean storePayloads;
  int lastPayloadLength = -1;
  FormatPostingsPositionsWriter(SegmentWriteState state, FormatPostingsDocsWriter parent) throws IOException {
    this.parent = parent;
    omitTermFreqAndPositions = parent.omitTermFreqAndPositions;
    if (parent.parent.parent.fieldInfos.hasProx()) {
      final String fileName = IndexFileNames.segmentFileName(parent.parent.parent.segment, IndexFileNames.PROX_EXTENSION);
      state.flushedFiles.add(fileName);
      out = parent.parent.parent.dir.createOutput(fileName);
      parent.skipListWriter.setProxOutput(out);
    } else
      out = null;
  }
  int lastPosition;
  @Override
  void addPosition(int position, byte[] payload, int payloadOffset, int payloadLength) throws IOException {
    assert !omitTermFreqAndPositions: "omitTermFreqAndPositions is true";
    assert out != null;
    final int delta = position - lastPosition;
    lastPosition = position;
    if (storePayloads) {
      if (payloadLength != lastPayloadLength) {
        lastPayloadLength = payloadLength;
        out.writeVInt((delta<<1)|1);
        out.writeVInt(payloadLength);
      } else
        out.writeVInt(delta << 1);
      if (payloadLength > 0)
        out.writeBytes(payload, payloadLength);
    } else
      out.writeVInt(delta);
  }
  void setField(FieldInfo fieldInfo) {
    omitTermFreqAndPositions = fieldInfo.omitTermFreqAndPositions;
    storePayloads = omitTermFreqAndPositions ? false : fieldInfo.storePayloads;
  }
  @Override
  void finish() {       
    lastPosition = 0;
    lastPayloadLength = -1;
  }
  void close() throws IOException {
    if (out != null)
      out.close();
  }
}
