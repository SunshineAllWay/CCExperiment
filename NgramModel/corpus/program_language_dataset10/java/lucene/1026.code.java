package org.apache.lucene.store.instantiated;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.util.StringHelper;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.BitVector;
public class InstantiatedIndexWriter implements Closeable {
  private PrintStream infoStream = null;
  private int maxFieldLength = IndexWriter.DEFAULT_MAX_FIELD_LENGTH;
  private final InstantiatedIndex index;
  private final Analyzer analyzer;
  private Similarity similarity = Similarity.getDefault(); 
  private transient Set<String> fieldNameBuffer;
  private Map<InstantiatedDocument, Map<FieldSetting, Map<String , TermDocumentInformationFactory>>> termDocumentInformationFactoryByDocument = new LinkedHashMap<InstantiatedDocument, Map<FieldSetting, Map<String , TermDocumentInformationFactory>>>(2000);
  private Set<InstantiatedDocument> unflushedDocuments = new HashSet<InstantiatedDocument>();
  public InstantiatedIndexWriter(InstantiatedIndex index) throws IOException {
    this(index, null);
  }
  public InstantiatedIndexWriter(InstantiatedIndex index, Analyzer analyzer) throws IOException {
    this(index, analyzer, false);
  }
  public InstantiatedIndexWriter(InstantiatedIndex index, Analyzer analyzer, boolean create) throws IOException {
    this.index = index;
    this.analyzer = analyzer;
    fieldNameBuffer = new HashSet<String>();
    if (create) {
      this.index.initialize();
    }
  }
  private int mergeFactor = 2500;
  public void setMergeFactor(int mergeFactor) {
    this.mergeFactor = mergeFactor;
  }
  public int getMergeFactor() {
    return mergeFactor;
  }
  public void setInfoStream(PrintStream infoStream) {
    this.infoStream = infoStream;
  }
  public void abort() throws IOException {
  }
  public void addIndexes(IndexReader[] readers) {
    throw new RuntimeException("Not implemented");
  }
  public PrintStream getInfoStream() {
    return infoStream;
  }
  public void close() throws IOException {
    commit();
  }
  public int docCount() {
    return index.getDocumentsByNumber().length  + unflushedDocuments.size();
  }
  public void commit() throws IOException {
    boolean orderedTermsDirty = false;
    Set<InstantiatedTerm> dirtyTerms = new HashSet<InstantiatedTerm>(1000);
    Map<String, FieldSetting> fieldSettingsByFieldName = new HashMap<String, FieldSetting>();
    for (String fieldName : fieldNameBuffer) {
      fieldSettingsByFieldName.put(fieldName, new FieldSetting(fieldName));
    }
    InstantiatedDocument[] documentsByNumber = new InstantiatedDocument[index.getDocumentsByNumber().length + termDocumentInformationFactoryByDocument.size()];
    System.arraycopy(index.getDocumentsByNumber(), 0, documentsByNumber, 0, index.getDocumentsByNumber().length);
    int documentNumber = index.getDocumentsByNumber().length;
    List<InstantiatedTerm> orderedTerms = new ArrayList<InstantiatedTerm>(index.getOrderedTerms().length + 5000);
    for (InstantiatedTerm instantiatedTerm : index.getOrderedTerms()) {
      orderedTerms.add(instantiatedTerm);
    }
    Map<String, byte[]> normsByFieldNameAndDocumentNumber = new HashMap<String, byte[]>(index.getTermsByFieldAndText().size());
    Set<String> fieldNames = new HashSet<String>(20);
    fieldNames.addAll(index.getNormsByFieldNameAndDocumentNumber().keySet());
    fieldNames.addAll(fieldNameBuffer);
    for (String field : index.getTermsByFieldAndText().keySet()) {
      byte[] norms = new byte[index.getDocumentsByNumber().length + termDocumentInformationFactoryByDocument.size()];
      byte[] oldNorms = index.getNormsByFieldNameAndDocumentNumber().get(field);
      if (oldNorms != null) {
        System.arraycopy(oldNorms, 0, norms, 0, oldNorms.length);
        Arrays.fill(norms, oldNorms.length, norms.length, similarity.encodeNormValue(1.0f));
      } else {
        Arrays.fill(norms, 0, norms.length, similarity.encodeNormValue(1.0f));
      }
      normsByFieldNameAndDocumentNumber.put(field, norms);
      fieldNames.remove(field);
    }
    for (String field : fieldNames) {
      byte[] norms = new byte[index.getDocumentsByNumber().length + termDocumentInformationFactoryByDocument.size()];
      Arrays.fill(norms, 0, norms.length, similarity.encodeNormValue(1.0f));
      normsByFieldNameAndDocumentNumber.put(field, norms);
    }
    fieldNames.clear();
    index.setNormsByFieldNameAndDocumentNumber(normsByFieldNameAndDocumentNumber);
    for (Map.Entry<InstantiatedDocument, Map<FieldSetting, Map<String , TermDocumentInformationFactory>>> eDocumentTermDocInfoByTermTextAndField : termDocumentInformationFactoryByDocument.entrySet()) {
      InstantiatedDocument document = eDocumentTermDocInfoByTermTextAndField.getKey();
      document.setDocumentNumber(documentNumber++);
      documentsByNumber[document.getDocumentNumber()] = document;
      int numFieldsWithTermVectorsInDocument = 0;
      int termsInDocument = 0;
      for (Map.Entry<FieldSetting, Map<String , TermDocumentInformationFactory>> eFieldTermDocInfoFactoriesByTermText : eDocumentTermDocInfoByTermTextAndField.getValue().entrySet()) {
        if (eFieldTermDocInfoFactoriesByTermText.getKey().storeTermVector) {
          numFieldsWithTermVectorsInDocument += eFieldTermDocInfoFactoriesByTermText.getValue().size();
        }
        termsInDocument += eFieldTermDocInfoFactoriesByTermText.getValue().size();
        if (eFieldTermDocInfoFactoriesByTermText.getKey().indexed && !eFieldTermDocInfoFactoriesByTermText.getKey().omitNorms) {
          float norm = eFieldTermDocInfoFactoriesByTermText.getKey().boost;
          norm *= document.getDocument().getBoost();
          norm *= similarity.lengthNorm(eFieldTermDocInfoFactoriesByTermText.getKey().fieldName, eFieldTermDocInfoFactoriesByTermText.getKey().fieldLength);
          normsByFieldNameAndDocumentNumber.get(eFieldTermDocInfoFactoriesByTermText.getKey().fieldName)[document.getDocumentNumber()] = similarity.encodeNormValue(norm);
        } else {
          System.currentTimeMillis();
        }
      }
      Map<InstantiatedTerm, InstantiatedTermDocumentInformation> informationByTermOfCurrentDocument = new HashMap<InstantiatedTerm, InstantiatedTermDocumentInformation>(termsInDocument);
      Map<String, FieldSetting> documentFieldSettingsByFieldName = new HashMap<String, FieldSetting>(eDocumentTermDocInfoByTermTextAndField.getValue().size());
      for (Map.Entry<FieldSetting, Map<String , TermDocumentInformationFactory>> eFieldSetting_TermDocInfoFactoriesByTermText : eDocumentTermDocInfoByTermTextAndField.getValue().entrySet()) {
        documentFieldSettingsByFieldName.put(eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName, eFieldSetting_TermDocInfoFactoriesByTermText.getKey());
        for (Map.Entry<String , TermDocumentInformationFactory> eTermText_TermDocInfoFactory : eFieldSetting_TermDocInfoFactoriesByTermText.getValue().entrySet()) {
          InstantiatedTerm term;
          Map<String, InstantiatedTerm> termsByText = index.getTermsByFieldAndText().get(eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName);
          if (termsByText == null) {
            termsByText = new HashMap<String, InstantiatedTerm>(1000);
            index.getTermsByFieldAndText().put(eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName, termsByText);
            term = new InstantiatedTerm(eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName, eTermText_TermDocInfoFactory.getKey());
            termsByText.put(eTermText_TermDocInfoFactory.getKey(), term);
            int pos = Collections.binarySearch(orderedTerms, term, InstantiatedTerm.comparator);
            pos = -1 - pos;
            orderedTerms.add(pos, term);
            orderedTermsDirty = true;
          } else {
            term = termsByText.get(eTermText_TermDocInfoFactory.getKey());
            if (term == null) {
              term = new InstantiatedTerm(eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName, eTermText_TermDocInfoFactory.getKey());
              termsByText.put(eTermText_TermDocInfoFactory.getKey(), term);
              int pos = Collections.binarySearch(orderedTerms, term, InstantiatedTerm.comparator);
              pos = -1 - pos;
              orderedTerms.add(pos, term);
              orderedTermsDirty = true;
            }
          }
          int[] positions = new int[eTermText_TermDocInfoFactory.getValue().termPositions.size()];
          for (int i = 0; i < positions.length; i++) {
            positions[i] = eTermText_TermDocInfoFactory.getValue().termPositions.get(i);
          }
          byte[][] payloads = new byte[eTermText_TermDocInfoFactory.getValue().payloads.size()][];
          for (int i = 0; i < payloads.length; i++) {
            payloads[i] = eTermText_TermDocInfoFactory.getValue().payloads.get(i);
          }
          InstantiatedTermDocumentInformation info = new InstantiatedTermDocumentInformation(term, document,  positions, payloads);
          InstantiatedTermDocumentInformation[] associatedDocuments;
          if (term.getAssociatedDocuments() != null) {
            associatedDocuments = new InstantiatedTermDocumentInformation[term.getAssociatedDocuments().length + 1];
            System.arraycopy(term.getAssociatedDocuments(), 0, associatedDocuments, 0, term.getAssociatedDocuments().length);
          } else {
            associatedDocuments = new InstantiatedTermDocumentInformation[1];
          }
          associatedDocuments[associatedDocuments.length - 1] = info;          
          term.setAssociatedDocuments(associatedDocuments);
          informationByTermOfCurrentDocument.put(term, info);
          dirtyTerms.add(term);
        }
        if (eFieldSetting_TermDocInfoFactoriesByTermText.getKey().storeOffsetWithTermVector) {
          for (Map.Entry<InstantiatedTerm, InstantiatedTermDocumentInformation> e : informationByTermOfCurrentDocument.entrySet()) {
            if (eFieldSetting_TermDocInfoFactoriesByTermText.getKey().fieldName.equals(e.getKey().field())) {
              TermDocumentInformationFactory factory = eFieldSetting_TermDocInfoFactoriesByTermText.getValue().get(e.getKey().text());
              e.getValue().setTermOffsets(factory.termOffsets.toArray(new TermVectorOffsetInfo[factory.termOffsets.size()]));
            }
          }
        }
      }
      Map<String, List<InstantiatedTermDocumentInformation>> termDocumentInformationsByField = new HashMap<String, List<InstantiatedTermDocumentInformation>>();
      for (Map.Entry<InstantiatedTerm, InstantiatedTermDocumentInformation> eTerm_TermDocumentInformation : informationByTermOfCurrentDocument.entrySet()) {
        List<InstantiatedTermDocumentInformation> termDocumentInformations = termDocumentInformationsByField.get(eTerm_TermDocumentInformation.getKey().field());
        if (termDocumentInformations == null) {
          termDocumentInformations = new ArrayList<InstantiatedTermDocumentInformation>();
          termDocumentInformationsByField.put(eTerm_TermDocumentInformation.getKey().field(), termDocumentInformations);
        }
        termDocumentInformations.add(eTerm_TermDocumentInformation.getValue());
      }
      for (Map.Entry<String, List<InstantiatedTermDocumentInformation>> eField_TermDocInfos : termDocumentInformationsByField.entrySet()) {
        Collections.sort(eField_TermDocInfos.getValue(), new Comparator<InstantiatedTermDocumentInformation>() {
          public int compare(InstantiatedTermDocumentInformation instantiatedTermDocumentInformation, InstantiatedTermDocumentInformation instantiatedTermDocumentInformation1) {
            return instantiatedTermDocumentInformation.getTerm().getTerm().compareTo(instantiatedTermDocumentInformation1.getTerm().getTerm());
          }
        });
        if (documentFieldSettingsByFieldName.get(eField_TermDocInfos.getKey()).storeTermVector) {
          if (document.getVectorSpace() == null) {
            document.setVectorSpace(new HashMap<String, List<InstantiatedTermDocumentInformation>>(documentFieldSettingsByFieldName.size()));
          }
          document.getVectorSpace().put(eField_TermDocInfos.getKey(), eField_TermDocInfos.getValue());
        }
      }
      fieldSettingsByFieldName.putAll(documentFieldSettingsByFieldName);
    }
    for (InstantiatedTerm term : dirtyTerms) {
      Arrays.sort(term.getAssociatedDocuments(), InstantiatedTermDocumentInformation.documentNumberComparator);
    }
    index.setDocumentsByNumber(documentsByNumber);
    index.setOrderedTerms(orderedTerms.toArray(new InstantiatedTerm[orderedTerms.size()]));
    for (FieldSetting fieldSetting : fieldSettingsByFieldName.values()) {
      index.getFieldSettings().merge(fieldSetting);
    }
    if (orderedTermsDirty) {
      for (int i = 0; i < index.getOrderedTerms().length; i++) {
        index.getOrderedTerms()[i].setTermIndex(i);
      }
    }
    IndexReader indexDeleter = index.indexReaderFactory();
    if (unflushedDeletions.size() > 0) {
      for (Term term : unflushedDeletions) {
        indexDeleter.deleteDocuments(term);
      }
      unflushedDeletions.clear();
    }
    unflushedDocuments.clear();
    termDocumentInformationFactoryByDocument.clear();
    fieldNameBuffer.clear();
    if (index.getDeletedDocuments() != null) {
      BitVector deletedDocuments = new BitVector(index.getDocumentsByNumber().length);
      for (int i = 0; i < index.getDeletedDocuments().size(); i++) {
        if (index.getDeletedDocuments().get(i)) {
          deletedDocuments.set(i);
        }
      }
      index.setDeletedDocuments(deletedDocuments);
    }
    index.setVersion(System.currentTimeMillis());
    indexDeleter.close();
  }
  public void addDocument(Document doc) throws IOException {
    addDocument(doc, getAnalyzer());
  }
  public void addDocument(Document doc, Analyzer analyzer) throws IOException {
    addDocument(new InstantiatedDocument(doc), analyzer);
  }
  protected void addDocument(InstantiatedDocument document, Analyzer analyzer) throws IOException {
    if (document.getDocumentNumber() != null) {
      throw new RuntimeException("Document number already set! Are you trying to add a document that already is bound to this or another index?");
    }
    Map<String , FieldSetting> fieldSettingsByFieldName = new HashMap<String, FieldSetting>();
    for (Fieldable field : document.getDocument().getFields()) {
      FieldSetting fieldSetting = fieldSettingsByFieldName.get(field.name());
      if (fieldSetting == null) {
        fieldSetting = new FieldSetting();
        fieldSetting.fieldName = StringHelper.intern(field.name());
        fieldSettingsByFieldName.put(fieldSetting.fieldName, fieldSetting);
        fieldNameBuffer.add(fieldSetting.fieldName);
      }
      fieldSetting.boost *= field.getBoost();
      if (field.getOmitNorms()) {
        fieldSetting.omitNorms = true;
      }
      if (field.isIndexed() ) {
        fieldSetting.indexed = true;
      }
      if (field.isTokenized()) {
        fieldSetting.tokenized = true;
      }
      if (field.isStored()) {
        fieldSetting.stored = true;
      }
      if (field.isBinary()) {
        fieldSetting.isBinary = true;
      }
      if (field.isTermVectorStored()) {
        fieldSetting.storeTermVector = true;
      }
      if (field.isStorePositionWithTermVector()) {
        fieldSetting.storePositionWithTermVector = true;
      }
      if (field.isStoreOffsetWithTermVector()) {
        fieldSetting.storeOffsetWithTermVector = true;
      }
    }
    Map<Fieldable, LinkedList<Token>> tokensByField = new LinkedHashMap<Fieldable, LinkedList<Token>>(20);
    for (Iterator<Fieldable> it = document.getDocument().getFields().iterator(); it.hasNext();) {
      Fieldable field = it.next();
      FieldSetting fieldSetting = fieldSettingsByFieldName.get(field.name());
      if (field.isIndexed()) {
        LinkedList<Token> tokens = new LinkedList<Token>();
        tokensByField.put(field, tokens);
        if (field.isTokenized()) {
          final TokenStream tokenStream;
          if (field.tokenStreamValue() != null) {
            tokenStream = field.tokenStreamValue();
          } else {
            tokenStream = analyzer.tokenStream(field.name(), new StringReader(field.stringValue()));
          }
          tokenStream.reset();
          while (tokenStream.incrementToken()) {
            final Token token = new Token();
            for (Iterator<AttributeImpl> atts = tokenStream.getAttributeImplsIterator(); atts.hasNext();) {
              final AttributeImpl att = atts.next();
              try {
                att.copyTo(token);
              } catch (Exception e) {
              }
            }
            tokens.add(token); 
            fieldSetting.fieldLength++;
            if (fieldSetting.fieldLength > maxFieldLength) {
              break;
            }
          }
          tokenStream.end();
          tokenStream.close();
        } else {
          String fieldVal = field.stringValue();
          Token token = new Token(0, fieldVal.length(), "untokenized");
          token.setTermBuffer(fieldVal);
          tokens.add(token);
          fieldSetting.fieldLength++;
        }
      }
      if (!field.isStored()) {
        it.remove();
      }
    }
    Map<FieldSetting, Map<String , TermDocumentInformationFactory>> termDocumentInformationFactoryByTermTextAndFieldSetting = new HashMap<FieldSetting, Map<String , TermDocumentInformationFactory>>();
    termDocumentInformationFactoryByDocument.put(document, termDocumentInformationFactoryByTermTextAndFieldSetting);
    for (Map.Entry<Fieldable, LinkedList<Token>> eField_Tokens : tokensByField.entrySet()) {
      FieldSetting fieldSetting = fieldSettingsByFieldName.get(eField_Tokens.getKey().name());
      Map<String, TermDocumentInformationFactory> termDocumentInformationFactoryByTermText = termDocumentInformationFactoryByTermTextAndFieldSetting.get(fieldSettingsByFieldName.get(eField_Tokens.getKey().name()));
      if (termDocumentInformationFactoryByTermText == null) {
        termDocumentInformationFactoryByTermText = new HashMap<String , TermDocumentInformationFactory>();
        termDocumentInformationFactoryByTermTextAndFieldSetting.put(fieldSettingsByFieldName.get(eField_Tokens.getKey().name()), termDocumentInformationFactoryByTermText);
      }
      int lastOffset = 0;
      if (fieldSetting.position > 0) {
        fieldSetting.position += analyzer.getPositionIncrementGap(fieldSetting.fieldName);
      }
      for (Token token : eField_Tokens.getValue()) {
        TermDocumentInformationFactory termDocumentInformationFactory = termDocumentInformationFactoryByTermText.get(token.term());
        if (termDocumentInformationFactory == null) {
          termDocumentInformationFactory = new TermDocumentInformationFactory();
          termDocumentInformationFactoryByTermText.put(token.term(), termDocumentInformationFactory);
        }
        fieldSetting.position += (token.getPositionIncrement() - 1);
        termDocumentInformationFactory.termPositions.add(fieldSetting.position++);
        if (token.getPayload() != null && token.getPayload().length() > 0) {
          termDocumentInformationFactory.payloads.add(token.getPayload().toByteArray());
          fieldSetting.storePayloads = true;
        } else {
          termDocumentInformationFactory.payloads.add(null);
        }
        if (eField_Tokens.getKey().isStoreOffsetWithTermVector()) {
          termDocumentInformationFactory.termOffsets.add(new TermVectorOffsetInfo(fieldSetting.offset + token.startOffset(), fieldSetting.offset + token.endOffset()));
          lastOffset = fieldSetting.offset + token.endOffset();
        }
      }
      if (eField_Tokens.getKey().isStoreOffsetWithTermVector()) {
        fieldSetting.offset = lastOffset + 1;
      }
    }
    unflushedDocuments.add(document);
    if (unflushedDocuments.size() >= getMergeFactor()) {
      commit();
    }
  }
  private Set<Term> unflushedDeletions = new HashSet<Term>();
  public void deleteDocuments(Term term) throws IOException {
    unflushedDeletions.add(term);
  }
  public void deleteDocuments(Term[] terms) throws IOException {
    for (Term term : terms) {
      deleteDocuments(term);
    }
  }
  public void updateDocument(Term term, Document doc) throws IOException {
    updateDocument(term, doc, getAnalyzer());
  }
  public void updateDocument(Term term, Document doc, Analyzer analyzer) throws IOException {
    deleteDocuments(term);
    addDocument(doc, analyzer);
  }
  public int getMaxFieldLength() {
    return maxFieldLength;
  }
  public void setMaxFieldLength(int maxFieldLength) {
    this.maxFieldLength = maxFieldLength;
  }
  public Similarity getSimilarity() {
    return similarity;
  }
  public void setSimilarity(Similarity similarity) {
    this.similarity = similarity;
  }
  public Analyzer getAnalyzer() {
    return analyzer;
  }
  private class TermDocumentInformationFactory {
    private LinkedList<byte[]> payloads = new LinkedList<byte[]>();
    private LinkedList<Integer> termPositions = new LinkedList<Integer>();
    private LinkedList<TermVectorOffsetInfo> termOffsets = new LinkedList<TermVectorOffsetInfo>();
  }
  static class FieldSetting extends org.apache.lucene.store.instantiated.FieldSetting {
    float boost = 1;
    int position = 0;
    int offset;
    int fieldLength = 0;
    boolean omitNorms = false;
    boolean isBinary = false;
    private FieldSetting() {
    }
    private FieldSetting(String fieldName) {
      super(fieldName);
    }
  }
}
