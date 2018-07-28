package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.store.Directory;
final class FormatPostingsFieldsWriter extends FormatPostingsFieldsConsumer {
  final Directory dir;
  final String segment;
  final TermInfosWriter termsOut;
  final FieldInfos fieldInfos;
  final FormatPostingsTermsWriter termsWriter;
  final DefaultSkipListWriter skipListWriter;
  final int totalNumDocs;
  public FormatPostingsFieldsWriter(SegmentWriteState state, FieldInfos fieldInfos) throws IOException {
    super();
    dir = state.directory;
    segment = state.segmentName;
    totalNumDocs = state.numDocs;
    this.fieldInfos = fieldInfos;
    termsOut = new TermInfosWriter(dir,
                                   segment,
                                   fieldInfos,
                                   state.termIndexInterval);
    skipListWriter = new DefaultSkipListWriter(termsOut.skipInterval,
                                               termsOut.maxSkipLevels,
                                               totalNumDocs,
                                               null,
                                               null);
    state.flushedFiles.add(state.segmentFileName(IndexFileNames.TERMS_EXTENSION));
    state.flushedFiles.add(state.segmentFileName(IndexFileNames.TERMS_INDEX_EXTENSION));
    termsWriter = new FormatPostingsTermsWriter(state, this);
  }
  @Override
  FormatPostingsTermsConsumer addField(FieldInfo field) {
    termsWriter.setField(field);
    return termsWriter;
  }
  @Override
  void finish() throws IOException {
    termsOut.close();
    termsWriter.close();
  }
}
