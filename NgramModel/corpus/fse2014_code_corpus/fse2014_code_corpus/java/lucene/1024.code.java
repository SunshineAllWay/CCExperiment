package org.apache.lucene.store.instantiated;
import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermPositionVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.util.BitVector;
public class InstantiatedIndex
    implements Serializable,Closeable {
  private static final long serialVersionUID = 1l;
  private long version = System.currentTimeMillis();
  private InstantiatedDocument[] documentsByNumber;
  private BitVector deletedDocuments;
  private Map<String, Map<String, InstantiatedTerm>> termsByFieldAndText;
  private InstantiatedTerm[] orderedTerms;
  private Map<String, byte[]> normsByFieldNameAndDocumentNumber;
  private FieldSettings fieldSettings;
  public InstantiatedIndex() {
    initialize();
  }
  void initialize() {
    termsByFieldAndText = new HashMap<String, Map<String, InstantiatedTerm>>();
    fieldSettings = new FieldSettings();
    orderedTerms = new InstantiatedTerm[0];
    documentsByNumber = new InstantiatedDocument[0];
    normsByFieldNameAndDocumentNumber = new HashMap<String, byte[]>();
  }
  public InstantiatedIndex(IndexReader sourceIndexReader) throws IOException {
    this(sourceIndexReader, null);
  }
  public InstantiatedIndex(IndexReader sourceIndexReader, Set<String> fields) throws IOException {
    if (!sourceIndexReader.isOptimized()) {
      System.out.println(("Source index is not optimized."));      
    }
    initialize();
    Collection<String> allFieldNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.ALL);
    Collection<String> indexedNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.INDEXED);
    for (String name : indexedNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.indexed = true;
    }
    Collection<String> indexedNoVecNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.INDEXED_NO_TERMVECTOR);
    for (String name : indexedNoVecNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storeTermVector = false;
      setting.indexed = true;
    }
    Collection<String> indexedVecNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR);
    for (String name : indexedVecNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storeTermVector = true;
      setting.indexed = true;
    }
    Collection<String> payloadNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.STORES_PAYLOADS);
    for (String name : payloadNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storePayloads = true;
    }
    Collection<String> termVecNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.TERMVECTOR);
    for (String name : termVecNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storeTermVector = true;
    }
    Collection<String> termVecOffsetNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_OFFSET);
    for (String name : termVecOffsetNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storeOffsetWithTermVector = true;
    }
    Collection<String> termVecPosNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_POSITION);
    for (String name : termVecPosNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storePositionWithTermVector = true;
    }
    Collection<String> termVecPosOffNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.TERMVECTOR_WITH_POSITION_OFFSET);
    for (String name : termVecPosOffNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.storeOffsetWithTermVector = true;
      setting.storePositionWithTermVector = true;
    }
    Collection<String> unindexedNames = sourceIndexReader.getFieldNames(IndexReader.FieldOption.UNINDEXED);
    for (String name : unindexedNames) {
      FieldSetting setting = fieldSettings.get(name, true);
      setting.indexed = false;
    }
    documentsByNumber = new InstantiatedDocument[sourceIndexReader.maxDoc()];
    if (sourceIndexReader.hasDeletions()) {
      deletedDocuments = new BitVector(sourceIndexReader.maxDoc());
    }
    for (int i = 0; i < sourceIndexReader.maxDoc(); i++) {
      if (sourceIndexReader.hasDeletions() && sourceIndexReader.isDeleted(i)) {
        deletedDocuments.set(i);
      } else {
        InstantiatedDocument document = new InstantiatedDocument();
        Document sourceDocument = sourceIndexReader.document(i);
        for (Fieldable field : sourceDocument.getFields()) {
          if (fields == null || fields.contains(field.name())) {
            document.getDocument().add(field);
          }
        }
        document.setDocumentNumber(i);
        documentsByNumber[i] = document;
        for (Fieldable field : document.getDocument().getFields()) {
          if (fields == null || fields.contains(field.name())) {
            if (field.isTermVectorStored()) {
              if (document.getVectorSpace() == null) {
                document.setVectorSpace(new HashMap<String, List<InstantiatedTermDocumentInformation>>());
              }
              document.getVectorSpace().put(field.name(), new ArrayList<InstantiatedTermDocumentInformation>());
            }
          }
        }
      }
    }
    for (String fieldName : allFieldNames) {
      if (fields == null || fields.contains(fieldName)) {
        getNormsByFieldNameAndDocumentNumber().put(fieldName, sourceIndexReader.norms(fieldName));
      }
    }
    for (String fieldName : allFieldNames) {
      if (fields == null || fields.contains(fieldName)) {
        getTermsByFieldAndText().put(fieldName, new HashMap<String, InstantiatedTerm>(5000));
      }
    }
    List<InstantiatedTerm> terms = new ArrayList<InstantiatedTerm>(5000 * getTermsByFieldAndText().size());
    TermEnum termEnum = sourceIndexReader.terms();
    while (termEnum.next()) {
      if (fields == null || fields.contains(termEnum.term().field())) { 
        InstantiatedTerm instantiatedTerm = new InstantiatedTerm(termEnum.term().field(), termEnum.term().text());
        getTermsByFieldAndText().get(termEnum.term().field()).put(termEnum.term().text(), instantiatedTerm);
        instantiatedTerm.setTermIndex(terms.size());
        terms.add(instantiatedTerm);
        instantiatedTerm.setAssociatedDocuments(new InstantiatedTermDocumentInformation[termEnum.docFreq()]);
      }
    }
    termEnum.close();
    orderedTerms = terms.toArray(new InstantiatedTerm[terms.size()]);
    for (InstantiatedTerm term : orderedTerms) {
      TermPositions termPositions = sourceIndexReader.termPositions(term.getTerm());
      int position = 0;
      while (termPositions.next()) {
        InstantiatedDocument document = documentsByNumber[termPositions.doc()];
        byte[][] payloads = new byte[termPositions.freq()][];
        int[] positions = new int[termPositions.freq()];
        for (int i = 0; i < termPositions.freq(); i++) {
          positions[i] = termPositions.nextPosition();
          if (termPositions.isPayloadAvailable()) {
            payloads[i] = new byte[termPositions.getPayloadLength()];
            termPositions.getPayload(payloads[i], 0);
          }
        }
        InstantiatedTermDocumentInformation termDocumentInformation = new InstantiatedTermDocumentInformation(term, document, positions, payloads);
        term.getAssociatedDocuments()[position++] = termDocumentInformation;
        if (document.getVectorSpace() != null
            && document.getVectorSpace().containsKey(term.field())) {
          document.getVectorSpace().get(term.field()).add(termDocumentInformation);
        }
      }
    }
    for (InstantiatedDocument document : getDocumentsByNumber()) {
      if (document == null) {
        continue; 
      }
      for (Fieldable field : document.getDocument().getFields()) {
        if (field.isTermVectorStored() && field.isStoreOffsetWithTermVector()) {
          TermPositionVector termPositionVector = (TermPositionVector) sourceIndexReader.getTermFreqVector(document.getDocumentNumber(), field.name());
          if (termPositionVector != null) {
            for (int i = 0; i < termPositionVector.getTerms().length; i++) {
              String token = termPositionVector.getTerms()[i];
              InstantiatedTerm term = findTerm(field.name(), token);
              InstantiatedTermDocumentInformation termDocumentInformation = term.getAssociatedDocument(document.getDocumentNumber());
              termDocumentInformation.setTermOffsets(termPositionVector.getOffsets(i));
            }
          }
        }
      }
    }
  }
  public InstantiatedIndexWriter indexWriterFactory(Analyzer analyzer, boolean create) throws IOException {
    return new InstantiatedIndexWriter(this, analyzer, create);
  }
  public InstantiatedIndexReader indexReaderFactory() throws IOException {
    return new InstantiatedIndexReader(this);
  }
  public void close() throws IOException {
  }
  InstantiatedTerm findTerm(Term term) {
    return findTerm(term.field(), term.text());
  }
  InstantiatedTerm findTerm(String field, String text) {
    Map<String, InstantiatedTerm> termsByField = termsByFieldAndText.get(field);
    if (termsByField == null) {
      return null;
    } else {
      return termsByField.get(text);
    }
  }
  public Map<String, Map<String, InstantiatedTerm>> getTermsByFieldAndText() {
    return termsByFieldAndText;
  }
  public InstantiatedTerm[] getOrderedTerms() {
    return orderedTerms;
  }
  public InstantiatedDocument[] getDocumentsByNumber() {
    return documentsByNumber;
  }
  public Map<String, byte[]> getNormsByFieldNameAndDocumentNumber() {
    return normsByFieldNameAndDocumentNumber;
  }
  void setNormsByFieldNameAndDocumentNumber(Map<String, byte[]> normsByFieldNameAndDocumentNumber) {
    this.normsByFieldNameAndDocumentNumber = normsByFieldNameAndDocumentNumber;
  }
  public BitVector getDeletedDocuments() {
    return deletedDocuments;
  }
  void setDeletedDocuments(BitVector deletedDocuments) {
    this.deletedDocuments = deletedDocuments;
  }
  void setOrderedTerms(InstantiatedTerm[] orderedTerms) {
    this.orderedTerms = orderedTerms;
  }
  void setDocumentsByNumber(InstantiatedDocument[] documentsByNumber) {
    this.documentsByNumber = documentsByNumber;
  }
  public long getVersion() {
    return version;
  }
  void setVersion(long version) {
    this.version = version;
  }
  FieldSettings getFieldSettings() {
    return fieldSettings;
  }
}
