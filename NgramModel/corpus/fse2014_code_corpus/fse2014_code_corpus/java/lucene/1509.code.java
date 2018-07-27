package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
abstract class DocFieldConsumer {
  FieldInfos fieldInfos;
  abstract void flush(Map<DocFieldConsumerPerThread,Collection<DocFieldConsumerPerField>> threadsAndFields, SegmentWriteState state) throws IOException;
  abstract void closeDocStore(SegmentWriteState state) throws IOException;
  abstract void abort();
  abstract DocFieldConsumerPerThread addThread(DocFieldProcessorPerThread docFieldProcessorPerThread) throws IOException;
  abstract boolean freeRAM();
  void setFieldInfos(FieldInfos fieldInfos) {
    this.fieldInfos = fieldInfos;
  }
}
