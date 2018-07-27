package org.apache.lucene.index;
import java.io.IOException;
final class DocumentsWriterThreadState {
  boolean isIdle = true;                          
  int numThreads = 1;                             
  boolean doFlushAfter;                           
  final DocConsumerPerThread consumer;
  final DocumentsWriter.DocState docState;
  final DocumentsWriter docWriter;
  public DocumentsWriterThreadState(DocumentsWriter docWriter) throws IOException {
    this.docWriter = docWriter;
    docState = new DocumentsWriter.DocState();
    docState.maxFieldLength = docWriter.maxFieldLength;
    docState.infoStream = docWriter.infoStream;
    docState.similarity = docWriter.similarity;
    docState.docWriter = docWriter;
    consumer = docWriter.consumer.addThread(this);
  }
  void doAfterFlush() {
    numThreads = 0;
    doFlushAfter = false;
  }
}
