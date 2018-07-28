package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.UnicodeUtil;
import org.apache.lucene.store.IndexOutput;
final class FormatPostingsDocsWriter extends FormatPostingsDocsConsumer {
  final IndexOutput out;
  final FormatPostingsTermsWriter parent;
  final FormatPostingsPositionsWriter posWriter;
  final DefaultSkipListWriter skipListWriter;
  final int skipInterval;
  final int totalNumDocs;
  boolean omitTermFreqAndPositions;
  boolean storePayloads;
  long freqStart;
  FieldInfo fieldInfo;
  FormatPostingsDocsWriter(SegmentWriteState state, FormatPostingsTermsWriter parent) throws IOException {
    super();
    this.parent = parent;
    final String fileName = IndexFileNames.segmentFileName(parent.parent.segment, IndexFileNames.FREQ_EXTENSION);
    state.flushedFiles.add(fileName);
    out = parent.parent.dir.createOutput(fileName);
    totalNumDocs = parent.parent.totalNumDocs;
    skipInterval = parent.parent.termsOut.skipInterval;
    skipListWriter = parent.parent.skipListWriter;
    skipListWriter.setFreqOutput(out);
    posWriter = new FormatPostingsPositionsWriter(state, this);
  }
  void setField(FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
    omitTermFreqAndPositions = fieldInfo.omitTermFreqAndPositions;
    storePayloads = fieldInfo.storePayloads;
    posWriter.setField(fieldInfo);
  }
  int lastDocID;
  int df;
  @Override
  FormatPostingsPositionsConsumer addDoc(int docID, int termDocFreq) throws IOException {
    final int delta = docID - lastDocID;
    if (docID < 0 || (df > 0 && delta <= 0))
      throw new CorruptIndexException("docs out of order (" + docID + " <= " + lastDocID + " )");
    if ((++df % skipInterval) == 0) {
      skipListWriter.setSkipData(lastDocID, storePayloads, posWriter.lastPayloadLength);
      skipListWriter.bufferSkip(df);
    }
    assert docID < totalNumDocs: "docID=" + docID + " totalNumDocs=" + totalNumDocs;
    lastDocID = docID;
    if (omitTermFreqAndPositions)
      out.writeVInt(delta);
    else if (1 == termDocFreq)
      out.writeVInt((delta<<1) | 1);
    else {
      out.writeVInt(delta<<1);
      out.writeVInt(termDocFreq);
    }
    return posWriter;
  }
  private final TermInfo termInfo = new TermInfo();  
  final UnicodeUtil.UTF8Result utf8 = new UnicodeUtil.UTF8Result();
  @Override
  void finish() throws IOException {
    long skipPointer = skipListWriter.writeSkip(out);
    termInfo.set(df, parent.freqStart, parent.proxStart, (int) (skipPointer - parent.freqStart));
    UnicodeUtil.UTF16toUTF8(parent.currentTerm, parent.currentTermStart, utf8);
    if (df > 0) {
      parent.termsOut.add(fieldInfo.number,
                          utf8.result,
                          utf8.length,
                          termInfo);
    }
    lastDocID = 0;
    df = 0;
  }
  void close() throws IOException {
    out.close();
    posWriter.close();
  }
}
