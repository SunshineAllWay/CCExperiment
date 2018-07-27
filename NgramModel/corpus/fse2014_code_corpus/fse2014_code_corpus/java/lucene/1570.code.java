package org.apache.lucene.index;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.search.Similarity;
final class NormsWriterPerField extends InvertedDocEndConsumerPerField implements Comparable<NormsWriterPerField> {
  final NormsWriterPerThread perThread;
  final FieldInfo fieldInfo;
  final DocumentsWriter.DocState docState;
  int[] docIDs = new int[1];
  byte[] norms = new byte[1];
  int upto;
  final FieldInvertState fieldState;
  public void reset() {
    docIDs = ArrayUtil.shrink(docIDs, upto);
    norms = ArrayUtil.shrink(norms, upto);
    upto = 0;
  }
  public NormsWriterPerField(final DocInverterPerField docInverterPerField, final NormsWriterPerThread perThread, final FieldInfo fieldInfo) {
    this.perThread = perThread;
    this.fieldInfo = fieldInfo;
    docState = perThread.docState;
    fieldState = docInverterPerField.fieldState;
  }
  @Override
  void abort() {
    upto = 0;
  }
  public int compareTo(NormsWriterPerField other) {
    return fieldInfo.name.compareTo(other.fieldInfo.name);
  }
  @Override
  void finish() {
    if (fieldInfo.isIndexed && !fieldInfo.omitNorms) {
      if (docIDs.length <= upto) {
        assert docIDs.length == upto;
        docIDs = ArrayUtil.grow(docIDs, 1+upto);
      }
      if (norms.length <= upto) {
        assert norms.length == upto;
        norms = ArrayUtil.grow(norms, 1+upto);
      }
      final float norm = docState.similarity.computeNorm(fieldInfo.name, fieldState);
      norms[upto] = Similarity.getDefault().encodeNormValue(norm);
      docIDs[upto] = docState.docID;
      upto++;
    }
  }
}
