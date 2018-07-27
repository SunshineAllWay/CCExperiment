package org.apache.lucene.index;
import java.util.Collection;
import java.util.Map;
import java.io.IOException;
abstract class InvertedDocConsumer {
  abstract InvertedDocConsumerPerThread addThread(DocInverterPerThread docInverterPerThread);
  abstract void abort();
  abstract void flush(Map<InvertedDocConsumerPerThread,Collection<InvertedDocConsumerPerField>> threadsAndFields, SegmentWriteState state) throws IOException;
  abstract void closeDocStore(SegmentWriteState state) throws IOException;
  abstract boolean freeRAM();
  FieldInfos fieldInfos;
  void setFieldInfos(FieldInfos fieldInfos) {
    this.fieldInfos = fieldInfos;
  }
}
