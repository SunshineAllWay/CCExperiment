package org.apache.lucene.index;
import java.io.IOException;
final class DocFieldConsumersPerThread extends DocFieldConsumerPerThread {
  final DocFieldConsumerPerThread one;
  final DocFieldConsumerPerThread two;
  final DocFieldConsumers parent;
  final DocumentsWriter.DocState docState;
  public DocFieldConsumersPerThread(DocFieldProcessorPerThread docFieldProcessorPerThread,
                                    DocFieldConsumers parent, DocFieldConsumerPerThread one, DocFieldConsumerPerThread two) {
    this.parent = parent;
    this.one = one;
    this.two = two;
    docState = docFieldProcessorPerThread.docState;
  }
  @Override
  public void startDocument() throws IOException {
    one.startDocument();
    two.startDocument();
  }
  @Override
  public void abort() {
    try {
      one.abort();
    } finally {
      two.abort();
    }
  }
  @Override
  public DocumentsWriter.DocWriter finishDocument() throws IOException {
    final DocumentsWriter.DocWriter oneDoc = one.finishDocument();
    final DocumentsWriter.DocWriter twoDoc = two.finishDocument();
    if (oneDoc == null)
      return twoDoc;
    else if (twoDoc == null)
      return oneDoc;
    else {
      DocFieldConsumers.PerDoc both = parent.getPerDoc();
      both.docID = docState.docID;
      assert oneDoc.docID == docState.docID;
      assert twoDoc.docID == docState.docID;
      both.writerOne = oneDoc;
      both.writerTwo = twoDoc;
      return both;
    }
  }
  @Override
  public DocFieldConsumerPerField addField(FieldInfo fi) {
    return new DocFieldConsumersPerField(this, one.addField(fi), two.addField(fi));
  }
}
