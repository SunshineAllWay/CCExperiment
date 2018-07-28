package org.apache.lucene.store.instantiated;
import org.apache.lucene.document.Document;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
public class InstantiatedDocument
    implements Serializable {
  private static final long serialVersionUID = 1l;
  private Document document;
  public InstantiatedDocument() {
    this.document = new Document();
  }
  public InstantiatedDocument(Document document) {
    this.document = document;
  }
  private Integer documentNumber;
  private Map<String , List<InstantiatedTermDocumentInformation>> vectorSpace;
  public Integer getDocumentNumber() {
    return documentNumber;
  }
  void setDocumentNumber(Integer documentNumber) {
    this.documentNumber = documentNumber;
  }
  public Map< String, List<InstantiatedTermDocumentInformation>> getVectorSpace() {
    return vectorSpace;
  }
  public void setVectorSpace(Map< String, List<InstantiatedTermDocumentInformation>> vectorSpace) {
    this.vectorSpace = vectorSpace;
  }
  public Document getDocument() {
    return document;
  }
  @Override
  public String toString() {
    return document.toString();
  }
}
