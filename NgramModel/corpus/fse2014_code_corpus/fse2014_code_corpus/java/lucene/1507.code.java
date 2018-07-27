package org.apache.lucene.index;
import java.io.IOException;
import java.util.Collection;
abstract class DocConsumer {
  abstract DocConsumerPerThread addThread(DocumentsWriterThreadState perThread) throws IOException;
  abstract void flush(final Collection<DocConsumerPerThread> threads, final SegmentWriteState state) throws IOException;
  abstract void closeDocStore(final SegmentWriteState state) throws IOException;
  abstract void abort();
  abstract boolean freeRAM();
}
