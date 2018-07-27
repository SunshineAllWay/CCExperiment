package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
final class DocFieldProcessor extends DocConsumer {
  final DocumentsWriter docWriter;
  final FieldInfos fieldInfos = new FieldInfos();
  final DocFieldConsumer consumer;
  final StoredFieldsWriter fieldsWriter;
  public DocFieldProcessor(DocumentsWriter docWriter, DocFieldConsumer consumer) {
    this.docWriter = docWriter;
    this.consumer = consumer;
    consumer.setFieldInfos(fieldInfos);
    fieldsWriter = new StoredFieldsWriter(docWriter, fieldInfos);
  }
  @Override
  public void closeDocStore(SegmentWriteState state) throws IOException {
    consumer.closeDocStore(state);
    fieldsWriter.closeDocStore(state);
  }
  @Override
  public void flush(Collection<DocConsumerPerThread> threads, SegmentWriteState state) throws IOException {
    Map<DocFieldConsumerPerThread, Collection<DocFieldConsumerPerField>> childThreadsAndFields = new HashMap<DocFieldConsumerPerThread, Collection<DocFieldConsumerPerField>>();
    for ( DocConsumerPerThread thread : threads) {
      DocFieldProcessorPerThread perThread = (DocFieldProcessorPerThread) thread;
      childThreadsAndFields.put(perThread.consumer, perThread.fields());
      perThread.trimFields(state);
    }
    fieldsWriter.flush(state);
    consumer.flush(childThreadsAndFields, state);
    final String fileName = state.segmentFileName(IndexFileNames.FIELD_INFOS_EXTENSION);
    fieldInfos.write(state.directory, fileName);
    state.flushedFiles.add(fileName);
  }
  @Override
  public void abort() {
    fieldsWriter.abort();
    consumer.abort();
  }
  @Override
  public boolean freeRAM() {
    return consumer.freeRAM();
  }
  @Override
  public DocConsumerPerThread addThread(DocumentsWriterThreadState threadState) throws IOException {
    return new DocFieldProcessorPerThread(threadState, this);
  }
}
