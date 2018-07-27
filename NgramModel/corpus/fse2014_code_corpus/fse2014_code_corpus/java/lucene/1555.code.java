package org.apache.lucene.index;
import java.util.Collection;
import java.util.Map;
import java.io.IOException;
abstract class InvertedDocEndConsumer {
  abstract InvertedDocEndConsumerPerThread addThread(DocInverterPerThread docInverterPerThread);
  abstract void flush(Map<InvertedDocEndConsumerPerThread,Collection<InvertedDocEndConsumerPerField>> threadsAndFields, SegmentWriteState state) throws IOException;
  abstract void closeDocStore(SegmentWriteState state) throws IOException;
  abstract void abort();
  abstract void setFieldInfos(FieldInfos fieldInfos);
}
