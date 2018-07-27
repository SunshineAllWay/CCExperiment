package org.apache.lucene.index;
import org.apache.lucene.document.Fieldable;
final class DocFieldProcessorPerField {
  final DocFieldConsumerPerField consumer;
  final FieldInfo fieldInfo;
  DocFieldProcessorPerField next;
  int lastGen = -1;
  int fieldCount;
  Fieldable[] fields = new Fieldable[1];
  public DocFieldProcessorPerField(final DocFieldProcessorPerThread perThread, final FieldInfo fieldInfo) {
    this.consumer = perThread.consumer.addField(fieldInfo);
    this.fieldInfo = fieldInfo;
  }
  public void abort() {
    consumer.abort();
  }
}
