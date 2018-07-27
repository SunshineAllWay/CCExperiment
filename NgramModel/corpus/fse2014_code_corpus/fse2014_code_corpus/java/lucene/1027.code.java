package org.apache.lucene.store.instantiated;
import java.io.Serializable;
import java.util.Comparator;
import org.apache.lucene.index.Term;
public class InstantiatedTerm
    implements Serializable {
  private static final long serialVersionUID = 1l;
  public static final Comparator<InstantiatedTerm> comparator = new Comparator<InstantiatedTerm>() {
    public int compare(InstantiatedTerm instantiatedTerm, InstantiatedTerm instantiatedTerm1) {
      return instantiatedTerm.getTerm().compareTo(instantiatedTerm1.getTerm());
    }
  };
  public static final Comparator termComparator = new Comparator() {
    public int compare(Object o, Object o1) {
      return ((InstantiatedTerm)o).getTerm().compareTo((Term)o1);
    }
  };
  private Term term;
  private int termIndex;
  public Term getTerm() {
    return term;
  }
  InstantiatedTerm(String field, String text) {
    this.term = new Term(field, text);
  }
  private InstantiatedTermDocumentInformation[] associatedDocuments;
  public InstantiatedTermDocumentInformation[] getAssociatedDocuments() {
    return associatedDocuments;
  }
  void setAssociatedDocuments(InstantiatedTermDocumentInformation[] associatedDocuments) {
    this.associatedDocuments = associatedDocuments;
  }
  public int seekCeilingDocumentInformationIndex(int target) {
    return seekCeilingDocumentInformationIndex(target, 0, getAssociatedDocuments().length);
  }
  public int seekCeilingDocumentInformationIndex(int target, int startOffset) {
    return seekCeilingDocumentInformationIndex(target, startOffset, getAssociatedDocuments().length);
  }
  public int seekCeilingDocumentInformationIndex(int target, int startOffset, int endPosition) {
    int pos = binarySearchAssociatedDocuments(target, startOffset, endPosition - startOffset);
    if (pos < 0) {
      pos = -1 - pos;
    }
    if (getAssociatedDocuments().length <= pos) {
      return -1;
    } else {
      return pos;
    }
  }
  public int binarySearchAssociatedDocuments(int target) {
    return binarySearchAssociatedDocuments(target, 0);
  }
  public int binarySearchAssociatedDocuments(int target, int offset) {
    return binarySearchAssociatedDocuments(target, offset, associatedDocuments.length - offset);
  }
  public int binarySearchAssociatedDocuments(int target, int offset, int length) {
    if (length == 0) {
      return -1 - offset;
    }
    int min = offset, max = offset + length - 1;
    int minVal = getAssociatedDocuments()[min].getDocument().getDocumentNumber();
    int maxVal = getAssociatedDocuments()[max].getDocument().getDocumentNumber();
    int nPreviousSteps = 0;
    for (; ;) {
      if (target <= minVal) return target == minVal ? min : -1 - min;
      if (target >= maxVal) return target == maxVal ? max : -2 - max;
      assert min != max;
      int pivot;
      if (nPreviousSteps > 2) {
        pivot = (min + max) >> 1;
      } else {
        pivot = min + (int) ((target - (float) minVal) / (maxVal - (float) minVal) * (max - min));
        nPreviousSteps++;
      }
      int pivotVal = getAssociatedDocuments()[pivot].getDocument().getDocumentNumber();
      if (target > pivotVal) {
        min = pivot + 1;
        max--;
      } else if (target == pivotVal) {
        return pivot;
      } else {
        min++;
        max = pivot - 1;
      }
      maxVal = getAssociatedDocuments()[max].getDocument().getDocumentNumber();
      minVal = getAssociatedDocuments()[min].getDocument().getDocumentNumber();
    }
  }
  public InstantiatedTermDocumentInformation getAssociatedDocument(int documentNumber) {
    int pos = binarySearchAssociatedDocuments(documentNumber);
    return pos < 0 ? null : getAssociatedDocuments()[pos];
  }
  public final String field() {
    return term.field();
  }
  public final String text() {
    return term.text();
  }
  @Override
  public String toString() {
    return term.toString();
  }
  public int getTermIndex() {
    return termIndex;
  }
  public void setTermIndex(int termIndex) {
    this.termIndex = termIndex;
  }
}
