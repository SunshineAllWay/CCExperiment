package org.apache.lucene.index;
final class NormsWriterPerThread extends InvertedDocEndConsumerPerThread {
  final NormsWriter normsWriter;
  final DocumentsWriter.DocState docState;
  public NormsWriterPerThread(DocInverterPerThread docInverterPerThread, NormsWriter normsWriter) {
    this.normsWriter = normsWriter;
    docState = docInverterPerThread.docState;
  }
  @Override
  InvertedDocEndConsumerPerField addField(DocInverterPerField docInverterPerField, final FieldInfo fieldInfo) {
    return new NormsWriterPerField(docInverterPerField, this, fieldInfo);
  }
  @Override
  void abort() {}
  @Override
  void startDocument() {}
  @Override
  void finishDocument() {}
  boolean freeRAM() {
    return false;
  }
}
