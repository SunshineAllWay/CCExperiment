package org.apache.lucene.index;
import java.io.IOException;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.document.Fieldable;
final class StoredFieldsWriterPerThread {
  final FieldsWriter localFieldsWriter;
  final StoredFieldsWriter storedFieldsWriter;
  final DocumentsWriter.DocState docState;
  StoredFieldsWriter.PerDoc doc;
  public StoredFieldsWriterPerThread(DocumentsWriter.DocState docState, StoredFieldsWriter storedFieldsWriter) throws IOException {
    this.storedFieldsWriter = storedFieldsWriter;
    this.docState = docState;
    localFieldsWriter = new FieldsWriter((IndexOutput) null, (IndexOutput) null, storedFieldsWriter.fieldInfos);
  }
  public void startDocument() {
    if (doc != null) {
      doc.reset();
      doc.docID = docState.docID;
    }
  }
  public void addField(Fieldable field, FieldInfo fieldInfo) throws IOException {
    if (doc == null) {
      doc = storedFieldsWriter.getPerDoc();
      doc.docID = docState.docID;
      localFieldsWriter.setFieldsStream(doc.fdt);
      assert doc.numStoredFields == 0: "doc.numStoredFields=" + doc.numStoredFields;
      assert 0 == doc.fdt.length();
      assert 0 == doc.fdt.getFilePointer();
    }
    localFieldsWriter.writeField(fieldInfo, field);
    assert docState.testPoint("StoredFieldsWriterPerThread.processFields.writeField");
    doc.numStoredFields++;
  }
  public DocumentsWriter.DocWriter finishDocument() {
    try {
      return doc;
    } finally {
      doc = null;
    }
  }
  public void abort() {
    if (doc != null) {
      doc.abort();
      doc = null;
    }
  }
}
