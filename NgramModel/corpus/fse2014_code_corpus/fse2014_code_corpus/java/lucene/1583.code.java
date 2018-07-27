package org.apache.lucene.index;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.index.MergePolicy.MergeAbortedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
final class SegmentMerger {
  static final byte[] NORMS_HEADER = new byte[]{'N','R','M',-1}; 
  private Directory directory;
  private String segment;
  private int termIndexInterval = IndexWriterConfig.DEFAULT_TERM_INDEX_INTERVAL;
  private List<IndexReader> readers = new ArrayList<IndexReader>();
  private FieldInfos fieldInfos;
  private int mergedDocs;
  private final CheckAbort checkAbort;
  private boolean mergeDocStores;
  private final static int MAX_RAW_MERGE_DOCS = 4192;
  SegmentMerger(Directory dir, String name) {
    directory = dir;
    segment = name;
    checkAbort = new CheckAbort(null, null) {
      @Override
      public void work(double units) throws MergeAbortedException {
      }
    };
  }
  SegmentMerger(IndexWriter writer, String name, MergePolicy.OneMerge merge) {
    directory = writer.getDirectory();
    segment = name;
    if (merge != null) {
      checkAbort = new CheckAbort(merge, directory);
    } else {
      checkAbort = new CheckAbort(null, null) {
        @Override
        public void work(double units) throws MergeAbortedException {
        }
      };
    }
    termIndexInterval = writer.getConfig().getTermIndexInterval();
  }
  boolean hasProx() {
    return fieldInfos.hasProx();
  }
  final void add(IndexReader reader) {
    readers.add(reader);
  }
  final IndexReader segmentReader(int i) {
    return readers.get(i);
  }
  final int merge() throws CorruptIndexException, IOException {
    return merge(true);
  }
  final int merge(boolean mergeDocStores) throws CorruptIndexException, IOException {
    this.mergeDocStores = mergeDocStores;
    mergedDocs = mergeFields();
    mergeTerms();
    mergeNorms();
    if (mergeDocStores && fieldInfos.hasVectors())
      mergeVectors();
    return mergedDocs;
  }
  final void closeReaders() throws IOException {
    for (final IndexReader reader : readers) {
      reader.close();
    }
  }
  final List<String> createCompoundFile(String fileName)
          throws IOException {
    CompoundFileWriter cfsWriter =
      new CompoundFileWriter(directory, fileName, checkAbort);
    List<String> files =
      new ArrayList<String>(IndexFileNames.COMPOUND_EXTENSIONS.length + 1);    
    for (String ext : IndexFileNames.COMPOUND_EXTENSIONS) {
      if (ext.equals(IndexFileNames.PROX_EXTENSION) && !hasProx())
        continue;
      if (mergeDocStores || (!ext.equals(IndexFileNames.FIELDS_EXTENSION) &&
                            !ext.equals(IndexFileNames.FIELDS_INDEX_EXTENSION)))
        files.add(IndexFileNames.segmentFileName(segment, ext));
    }
    int numFIs = fieldInfos.size();
    for (int i = 0; i < numFIs; i++) {
      FieldInfo fi = fieldInfos.fieldInfo(i);
      if (fi.isIndexed && !fi.omitNorms) {
        files.add(IndexFileNames.segmentFileName(segment, IndexFileNames.NORMS_EXTENSION));
        break;
      }
    }
    if (fieldInfos.hasVectors() && mergeDocStores) {
      for (String ext : IndexFileNames.VECTOR_EXTENSIONS) {
        files.add(IndexFileNames.segmentFileName(segment, ext));
      }
    }
    for (String file : files) {
      cfsWriter.addFile(file);
    }
    cfsWriter.close();
    return files;
  }
  private void addIndexed(IndexReader reader, FieldInfos fInfos,
      Collection<String> names, boolean storeTermVectors,
      boolean storePositionWithTermVector, boolean storeOffsetWithTermVector,
      boolean storePayloads, boolean omitTFAndPositions)
      throws IOException {
    for (String field : names) {
      fInfos.add(field, true, storeTermVectors,
          storePositionWithTermVector, storeOffsetWithTermVector, !reader
              .hasNorms(field), storePayloads, omitTFAndPositions);
    }
  }
  private SegmentReader[] matchingSegmentReaders;
  private int[] rawDocLengths;
  private int[] rawDocLengths2;
  private void setMatchingSegmentReaders() {
    int numReaders = readers.size();
    matchingSegmentReaders = new SegmentReader[numReaders];
    for (int i = 0; i < numReaders; i++) {
      IndexReader reader = readers.get(i);
      if (reader instanceof SegmentReader) {
        SegmentReader segmentReader = (SegmentReader) reader;
        boolean same = true;
        FieldInfos segmentFieldInfos = segmentReader.fieldInfos();
        int numFieldInfos = segmentFieldInfos.size();
        for (int j = 0; same && j < numFieldInfos; j++) {
          same = fieldInfos.fieldName(j).equals(segmentFieldInfos.fieldName(j));
        }
        if (same) {
          matchingSegmentReaders[i] = segmentReader;
        }
      }
    }
    rawDocLengths = new int[MAX_RAW_MERGE_DOCS];
    rawDocLengths2 = new int[MAX_RAW_MERGE_DOCS];
  }
  private final int mergeFields() throws CorruptIndexException, IOException {
    if (!mergeDocStores) {
      final SegmentReader sr = (SegmentReader) readers.get(readers.size()-1);
      fieldInfos = (FieldInfos) sr.core.fieldInfos.clone();
    } else {
      fieldInfos = new FieldInfos();		  
    }
    for (IndexReader reader : readers) {
      if (reader instanceof SegmentReader) {
        SegmentReader segmentReader = (SegmentReader) reader;
        FieldInfos readerFieldInfos = segmentReader.fieldInfos();
        int numReaderFieldInfos = readerFieldInfos.size();
        for (int j = 0; j < numReaderFieldInfos; j++) {
          FieldInfo fi = readerFieldInfos.fieldInfo(j);
          fieldInfos.add(fi.name, fi.isIndexed, fi.storeTermVector,
              fi.storePositionWithTermVector, fi.storeOffsetWithTermVector,
              !reader.hasNorms(fi.name), fi.storePayloads,
              fi.omitTermFreqAndPositions);
        }
      } else {
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.TERMVECTOR_WITH_POSITION_OFFSET), true, true, true, false, false);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.TERMVECTOR_WITH_POSITION), true, true, false, false, false);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.TERMVECTOR_WITH_OFFSET), true, false, true, false, false);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.TERMVECTOR), true, false, false, false, false);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.OMIT_TERM_FREQ_AND_POSITIONS), false, false, false, false, true);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.STORES_PAYLOADS), false, false, false, true, false);
        addIndexed(reader, fieldInfos, reader.getFieldNames(FieldOption.INDEXED), false, false, false, false, false);
        fieldInfos.add(reader.getFieldNames(FieldOption.UNINDEXED), false);
      }
    }
    fieldInfos.write(directory, segment + ".fnm");
    int docCount = 0;
    setMatchingSegmentReaders();
    if (mergeDocStores) {
      final FieldsWriter fieldsWriter = new FieldsWriter(directory, segment, fieldInfos);
      try {
        int idx = 0;
        for (IndexReader reader : readers) {
          final SegmentReader matchingSegmentReader = matchingSegmentReaders[idx++];
          FieldsReader matchingFieldsReader = null;
          if (matchingSegmentReader != null) {
            final FieldsReader fieldsReader = matchingSegmentReader.getFieldsReader();
            if (fieldsReader != null && fieldsReader.canReadRawDocs()) {            
              matchingFieldsReader = fieldsReader;
            }
          }
          if (reader.hasDeletions()) {
            docCount += copyFieldsWithDeletions(fieldsWriter,
                                                reader, matchingFieldsReader);
          } else {
            docCount += copyFieldsNoDeletions(fieldsWriter,
                                              reader, matchingFieldsReader);
          }
        }
      } finally {
        fieldsWriter.close();
      }
      final String fileName = IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_INDEX_EXTENSION);
      final long fdxFileLength = directory.fileLength(fileName);
      if (4+((long) docCount)*8 != fdxFileLength)
        throw new RuntimeException("mergeFields produced an invalid result: docCount is " + docCount + " but fdx file size is " + fdxFileLength + " file=" + fileName + " file exists?=" + directory.fileExists(fileName) + "; now aborting this merge to prevent index corruption");
    } else
      for (final IndexReader reader : readers) {
        docCount += reader.numDocs();
      }
    return docCount;
  }
  private int copyFieldsWithDeletions(final FieldsWriter fieldsWriter, final IndexReader reader,
                                      final FieldsReader matchingFieldsReader)
    throws IOException, MergeAbortedException, CorruptIndexException {
    int docCount = 0;
    final int maxDoc = reader.maxDoc();
    if (matchingFieldsReader != null) {
      for (int j = 0; j < maxDoc;) {
        if (reader.isDeleted(j)) {
          ++j;
          continue;
        }
        int start = j, numDocs = 0;
        do {
          j++;
          numDocs++;
          if (j >= maxDoc) break;
          if (reader.isDeleted(j)) {
            j++;
            break;
          }
        } while(numDocs < MAX_RAW_MERGE_DOCS);
        IndexInput stream = matchingFieldsReader.rawDocs(rawDocLengths, start, numDocs);
        fieldsWriter.addRawDocuments(stream, rawDocLengths, numDocs);
        docCount += numDocs;
        checkAbort.work(300 * numDocs);
      }
    } else {
      for (int j = 0; j < maxDoc; j++) {
        if (reader.isDeleted(j)) {
          continue;
        }
        Document doc = reader.document(j);
        fieldsWriter.addDocument(doc);
        docCount++;
        checkAbort.work(300);
      }
    }
    return docCount;
  }
  private int copyFieldsNoDeletions(final FieldsWriter fieldsWriter, final IndexReader reader,
                                    final FieldsReader matchingFieldsReader)
    throws IOException, MergeAbortedException, CorruptIndexException {
    final int maxDoc = reader.maxDoc();
    int docCount = 0;
    if (matchingFieldsReader != null) {
      while (docCount < maxDoc) {
        int len = Math.min(MAX_RAW_MERGE_DOCS, maxDoc - docCount);
        IndexInput stream = matchingFieldsReader.rawDocs(rawDocLengths, docCount, len);
        fieldsWriter.addRawDocuments(stream, rawDocLengths, len);
        docCount += len;
        checkAbort.work(300 * len);
      }
    } else {
      for (; docCount < maxDoc; docCount++) {
        Document doc = reader.document(docCount);
        fieldsWriter.addDocument(doc);
        checkAbort.work(300);
      }
    }
    return docCount;
  }
  private final void mergeVectors() throws IOException {
    TermVectorsWriter termVectorsWriter = 
      new TermVectorsWriter(directory, segment, fieldInfos);
    try {
      int idx = 0;
      for (final IndexReader reader : readers) {
        final SegmentReader matchingSegmentReader = matchingSegmentReaders[idx++];
        TermVectorsReader matchingVectorsReader = null;
        if (matchingSegmentReader != null) {
          TermVectorsReader vectorsReader = matchingSegmentReader.getTermVectorsReaderOrig();
          if (vectorsReader != null && vectorsReader.canReadRawDocs()) {
            matchingVectorsReader = vectorsReader;
          }
        }
        if (reader.hasDeletions()) {
          copyVectorsWithDeletions(termVectorsWriter, matchingVectorsReader, reader);
        } else {
          copyVectorsNoDeletions(termVectorsWriter, matchingVectorsReader, reader);
        }
      }
    } finally {
      termVectorsWriter.close();
    }
    final String fileName = IndexFileNames.segmentFileName(segment, IndexFileNames.VECTORS_INDEX_EXTENSION);
    final long tvxSize = directory.fileLength(fileName);
    if (4+((long) mergedDocs)*16 != tvxSize)
      throw new RuntimeException("mergeVectors produced an invalid result: mergedDocs is " + mergedDocs + " but tvx size is " + tvxSize + " file=" + fileName + " file exists?=" + directory.fileExists(fileName) + "; now aborting this merge to prevent index corruption");
  }
  private void copyVectorsWithDeletions(final TermVectorsWriter termVectorsWriter,
                                        final TermVectorsReader matchingVectorsReader,
                                        final IndexReader reader)
    throws IOException, MergeAbortedException {
    final int maxDoc = reader.maxDoc();
    if (matchingVectorsReader != null) {
      for (int docNum = 0; docNum < maxDoc;) {
        if (reader.isDeleted(docNum)) {
          ++docNum;
          continue;
        }
        int start = docNum, numDocs = 0;
        do {
          docNum++;
          numDocs++;
          if (docNum >= maxDoc) break;
          if (reader.isDeleted(docNum)) {
            docNum++;
            break;
          }
        } while(numDocs < MAX_RAW_MERGE_DOCS);
        matchingVectorsReader.rawDocs(rawDocLengths, rawDocLengths2, start, numDocs);
        termVectorsWriter.addRawDocuments(matchingVectorsReader, rawDocLengths, rawDocLengths2, numDocs);
        checkAbort.work(300 * numDocs);
      }
    } else {
      for (int docNum = 0; docNum < maxDoc; docNum++) {
        if (reader.isDeleted(docNum)) {
          continue;
        }
        TermFreqVector[] vectors = reader.getTermFreqVectors(docNum);
        termVectorsWriter.addAllDocVectors(vectors);
        checkAbort.work(300);
      }
    }
  }
  private void copyVectorsNoDeletions(final TermVectorsWriter termVectorsWriter,
                                      final TermVectorsReader matchingVectorsReader,
                                      final IndexReader reader)
      throws IOException, MergeAbortedException {
    final int maxDoc = reader.maxDoc();
    if (matchingVectorsReader != null) {
      int docCount = 0;
      while (docCount < maxDoc) {
        int len = Math.min(MAX_RAW_MERGE_DOCS, maxDoc - docCount);
        matchingVectorsReader.rawDocs(rawDocLengths, rawDocLengths2, docCount, len);
        termVectorsWriter.addRawDocuments(matchingVectorsReader, rawDocLengths, rawDocLengths2, len);
        docCount += len;
        checkAbort.work(300 * len);
      }
    } else {
      for (int docNum = 0; docNum < maxDoc; docNum++) {
        TermFreqVector[] vectors = reader.getTermFreqVectors(docNum);
        termVectorsWriter.addAllDocVectors(vectors);
        checkAbort.work(300);
      }
    }
  }
  private SegmentMergeQueue queue = null;
  private final void mergeTerms() throws CorruptIndexException, IOException {
    SegmentWriteState state = new SegmentWriteState(null, directory, segment, null, mergedDocs, 0, termIndexInterval);
    final FormatPostingsFieldsConsumer consumer = new FormatPostingsFieldsWriter(state, fieldInfos);
    try {
      queue = new SegmentMergeQueue(readers.size());
      mergeTermInfos(consumer);
    } finally {
      consumer.finish();
      if (queue != null) queue.close();
    }
  }
  boolean omitTermFreqAndPositions;
  private final void mergeTermInfos(final FormatPostingsFieldsConsumer consumer) throws CorruptIndexException, IOException {
    int base = 0;
    final int readerCount = readers.size();
    for (int i = 0; i < readerCount; i++) {
      IndexReader reader = readers.get(i);
      TermEnum termEnum = reader.terms();
      SegmentMergeInfo smi = new SegmentMergeInfo(base, termEnum, reader);
      int[] docMap  = smi.getDocMap();
      if (docMap != null) {
        if (docMaps == null) {
          docMaps = new int[readerCount][];
          delCounts = new int[readerCount];
        }
        docMaps[i] = docMap;
        delCounts[i] = smi.reader.maxDoc() - smi.reader.numDocs();
      }
      base += reader.numDocs();
      assert reader.numDocs() == reader.maxDoc() - smi.delCount;
      if (smi.next())
        queue.add(smi);				  
      else
        smi.close();
    }
    SegmentMergeInfo[] match = new SegmentMergeInfo[readers.size()];
    String currentField = null;
    FormatPostingsTermsConsumer termsConsumer = null;
    while (queue.size() > 0) {
      int matchSize = 0;			  
      match[matchSize++] = queue.pop();
      Term term = match[0].term;
      SegmentMergeInfo top = queue.top();
      while (top != null && term.compareTo(top.term) == 0) {
        match[matchSize++] =  queue.pop();
        top =  queue.top();
      }
      if (currentField != term.field) {
        currentField = term.field;
        if (termsConsumer != null)
          termsConsumer.finish();
        final FieldInfo fieldInfo = fieldInfos.fieldInfo(currentField);
        termsConsumer = consumer.addField(fieldInfo);
        omitTermFreqAndPositions = fieldInfo.omitTermFreqAndPositions;
      }
      int df = appendPostings(termsConsumer, match, matchSize);		  
      checkAbort.work(df/3.0);
      while (matchSize > 0) {
        SegmentMergeInfo smi = match[--matchSize];
        if (smi.next())
          queue.add(smi);			  
        else
          smi.close();				  
      }
    }
  }
  private byte[] payloadBuffer;
  private int[][] docMaps;
  int[][] getDocMaps() {
    return docMaps;
  }
  private int[] delCounts;
  int[] getDelCounts() {
    return delCounts;
  }
  private final int appendPostings(final FormatPostingsTermsConsumer termsConsumer, SegmentMergeInfo[] smis, int n)
        throws CorruptIndexException, IOException {
    final FormatPostingsDocsConsumer docConsumer = termsConsumer.addTerm(smis[0].term.text);
    int df = 0;
    for (int i = 0; i < n; i++) {
      SegmentMergeInfo smi = smis[i];
      TermPositions postings = smi.getPositions();
      assert postings != null;
      int base = smi.base;
      int[] docMap = smi.getDocMap();
      postings.seek(smi.termEnum);
      while (postings.next()) {
        df++;
        int doc = postings.doc();
        if (docMap != null)
          doc = docMap[doc];                      
        doc += base;                              
        final int freq = postings.freq();
        final FormatPostingsPositionsConsumer posConsumer = docConsumer.addDoc(doc, freq);
        if (!omitTermFreqAndPositions) {
          for (int j = 0; j < freq; j++) {
            final int position = postings.nextPosition();
            final int payloadLength = postings.getPayloadLength();
            if (payloadLength > 0) {
              if (payloadBuffer == null || payloadBuffer.length < payloadLength)
                payloadBuffer = new byte[payloadLength];
              postings.getPayload(payloadBuffer, 0);
            }
            posConsumer.addPosition(position, payloadBuffer, 0, payloadLength);
          }
          posConsumer.finish();
        }
      }
    }
    docConsumer.finish();
    return df;
  }
  private void mergeNorms() throws IOException {
    byte[] normBuffer = null;
    IndexOutput output = null;
    try {
      int numFieldInfos = fieldInfos.size();
      for (int i = 0; i < numFieldInfos; i++) {
        FieldInfo fi = fieldInfos.fieldInfo(i);
        if (fi.isIndexed && !fi.omitNorms) {
          if (output == null) { 
            output = directory.createOutput(IndexFileNames.segmentFileName(segment, IndexFileNames.NORMS_EXTENSION));
            output.writeBytes(NORMS_HEADER,NORMS_HEADER.length);
          }
          for ( IndexReader reader : readers) {
            int maxDoc = reader.maxDoc();
            if (normBuffer == null || normBuffer.length < maxDoc) {
              normBuffer = new byte[maxDoc];
            }
            reader.norms(fi.name, normBuffer, 0);
            if (!reader.hasDeletions()) {
              output.writeBytes(normBuffer, maxDoc);
            } else {
              for (int k = 0; k < maxDoc; k++) {
                if (!reader.isDeleted(k)) {
                  output.writeByte(normBuffer[k]);
                }
              }
            }
            checkAbort.work(maxDoc);
          }
        }
      }
    } finally {
      if (output != null) { 
        output.close();
      }
    }
  }
  static class CheckAbort {
    private double workCount;
    private MergePolicy.OneMerge merge;
    private Directory dir;
    public CheckAbort(MergePolicy.OneMerge merge, Directory dir) {
      this.merge = merge;
      this.dir = dir;
    }
    public void work(double units) throws MergePolicy.MergeAbortedException {
      workCount += units;
      if (workCount >= 10000.0) {
        merge.checkAborted(dir);
        workCount = 0;
      }
    }
  }
}
