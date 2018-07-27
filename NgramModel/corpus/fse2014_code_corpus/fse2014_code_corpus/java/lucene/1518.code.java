package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
final class DocInverter extends DocFieldConsumer {
  final InvertedDocConsumer consumer;
  final InvertedDocEndConsumer endConsumer;
  public DocInverter(InvertedDocConsumer consumer, InvertedDocEndConsumer endConsumer) {
    this.consumer = consumer;
    this.endConsumer = endConsumer;
  }
  @Override
  void setFieldInfos(FieldInfos fieldInfos) {
    super.setFieldInfos(fieldInfos);
    consumer.setFieldInfos(fieldInfos);
    endConsumer.setFieldInfos(fieldInfos);
  }
  @Override
  void flush(Map<DocFieldConsumerPerThread, Collection<DocFieldConsumerPerField>> threadsAndFields, SegmentWriteState state) throws IOException {
    Map<InvertedDocConsumerPerThread,Collection<InvertedDocConsumerPerField>> childThreadsAndFields = new HashMap<InvertedDocConsumerPerThread,Collection<InvertedDocConsumerPerField>>();
    Map<InvertedDocEndConsumerPerThread,Collection<InvertedDocEndConsumerPerField>> endChildThreadsAndFields = new HashMap<InvertedDocEndConsumerPerThread,Collection<InvertedDocEndConsumerPerField>>();
    for (Map.Entry<DocFieldConsumerPerThread,Collection<DocFieldConsumerPerField>> entry : threadsAndFields.entrySet() ) {
      DocInverterPerThread perThread = (DocInverterPerThread) entry.getKey();
      Collection<InvertedDocConsumerPerField> childFields = new HashSet<InvertedDocConsumerPerField>();
      Collection<InvertedDocEndConsumerPerField> endChildFields = new HashSet<InvertedDocEndConsumerPerField>();
      for (final DocFieldConsumerPerField field: entry.getValue() ) {  
        DocInverterPerField perField = (DocInverterPerField) field;
        childFields.add(perField.consumer);
        endChildFields.add(perField.endConsumer);
      }
      childThreadsAndFields.put(perThread.consumer, childFields);
      endChildThreadsAndFields.put(perThread.endConsumer, endChildFields);
    }
    consumer.flush(childThreadsAndFields, state);
    endConsumer.flush(endChildThreadsAndFields, state);
  }
  @Override
  public void closeDocStore(SegmentWriteState state) throws IOException {
    consumer.closeDocStore(state);
    endConsumer.closeDocStore(state);
  }
  @Override
  void abort() {
    consumer.abort();
    endConsumer.abort();
  }
  @Override
  public boolean freeRAM() {
    return consumer.freeRAM();
  }
  @Override
  public DocFieldConsumerPerThread addThread(DocFieldProcessorPerThread docFieldProcessorPerThread) {
    return new DocInverterPerThread(docFieldProcessorPerThread, this);
  }
}
