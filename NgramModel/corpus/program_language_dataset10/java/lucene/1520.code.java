package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
final class DocInverterPerThread extends DocFieldConsumerPerThread {
  final DocInverter docInverter;
  final InvertedDocConsumerPerThread consumer;
  final InvertedDocEndConsumerPerThread endConsumer;
  final SingleTokenAttributeSource singleToken = new SingleTokenAttributeSource();
  static class SingleTokenAttributeSource extends AttributeSource {
    final TermAttribute termAttribute;
    final OffsetAttribute offsetAttribute;
    private SingleTokenAttributeSource() {
      termAttribute = addAttribute(TermAttribute.class);
      offsetAttribute = addAttribute(OffsetAttribute.class);
    }
    public void reinit(String stringValue, int startOffset,  int endOffset) {
      termAttribute.setTermBuffer(stringValue);
      offsetAttribute.setOffset(startOffset, endOffset);
    }
  }
  final DocumentsWriter.DocState docState;
  final FieldInvertState fieldState = new FieldInvertState();
  final ReusableStringReader stringReader = new ReusableStringReader();
  public DocInverterPerThread(DocFieldProcessorPerThread docFieldProcessorPerThread, DocInverter docInverter) {
    this.docInverter = docInverter;
    docState = docFieldProcessorPerThread.docState;
    consumer = docInverter.consumer.addThread(this);
    endConsumer = docInverter.endConsumer.addThread(this);
  }
  @Override
  public void startDocument() throws IOException {
    consumer.startDocument();
    endConsumer.startDocument();
  }
  @Override
  public DocumentsWriter.DocWriter finishDocument() throws IOException {
    endConsumer.finishDocument();
    return consumer.finishDocument();
  }
  @Override
  void abort() {
    try {
      consumer.abort();
    } finally {
      endConsumer.abort();
    }
  }
  @Override
  public DocFieldConsumerPerField addField(FieldInfo fi) {
    return new DocInverterPerField(this, fi);
  }
}
