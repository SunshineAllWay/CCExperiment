package org.apache.lucene.store.instantiated;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermVectorOffsetInfo;
import java.io.Serializable;
public class InstantiatedTermPositionVector
    extends InstantiatedTermFreqVector
    implements TermPositionVector, Serializable {
  private static final long serialVersionUID = 1l;
  public InstantiatedTermPositionVector(InstantiatedDocument document, String field) {
    super(document, field);
  }
  public int[] getTermPositions(int index) {
    return getTermDocumentInformations().get(index).getTermPositions();
  }
  public TermVectorOffsetInfo[] getOffsets(int index) {
    return getTermDocumentInformations().get(index).getTermOffsets();
  }
}
