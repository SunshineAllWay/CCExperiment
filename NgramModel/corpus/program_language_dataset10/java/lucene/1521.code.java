package org.apache.lucene.index;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMFile;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.Constants;
import org.apache.lucene.util.ThreadInterruptedException;
import org.apache.lucene.util.RamUsageEstimator;
final class DocumentsWriter {
  IndexWriter writer;
  Directory directory;
  String segment;                         
  private String docStoreSegment;         
  private int docStoreOffset;                     
  private int nextDocID;                          
  private int numDocsInRAM;                       
  int numDocsInStore;                     
  private DocumentsWriterThreadState[] threadStates = new DocumentsWriterThreadState[0];
  private final HashMap<Thread,DocumentsWriterThreadState> threadBindings = new HashMap<Thread,DocumentsWriterThreadState>();
  private int pauseThreads;               
  boolean flushPending;                   
  boolean bufferIsFull;                   
  private boolean aborting;               
  private DocFieldProcessor docFieldProcessor;
  PrintStream infoStream;
  int maxFieldLength = IndexWriterConfig.UNLIMITED_FIELD_LENGTH;
  Similarity similarity;
  private final int maxThreadStates;
  List<String> newFiles;
  static class DocState {
    DocumentsWriter docWriter;
    Analyzer analyzer;
    int maxFieldLength;
    PrintStream infoStream;
    Similarity similarity;
    int docID;
    Document doc;
    String maxTermPrefix;
    public boolean testPoint(String name) {
      return docWriter.writer.testPoint(name);
    }
  }
  abstract static class DocWriter {
    DocWriter next;
    int docID;
    abstract void finish() throws IOException;
    abstract void abort();
    abstract long sizeInBytes();
    void setNext(DocWriter next) {
      this.next = next;
    }
  }
  PerDocBuffer newPerDocBuffer() {
    return new PerDocBuffer();
  }
  class PerDocBuffer extends RAMFile {
    protected byte[] newBuffer(int size) {
      assert size == PER_DOC_BLOCK_SIZE;
      return perDocAllocator.getByteBlock(false);
    }
    synchronized void recycle() {
      if (buffers.size() > 0) {
        setLength(0);
        final int blockCount = buffers.size();
        final byte[][] blocks = buffers.toArray( new byte[blockCount][] );
        perDocAllocator.recycleByteBlocks(blocks, 0, blockCount);
        buffers.clear();
        sizeInBytes = 0;
        assert numBuffers() == 0;
      }
    }
  }
  abstract static class IndexingChain {
    abstract DocConsumer getChain(DocumentsWriter documentsWriter);
  }
  static final IndexingChain defaultIndexingChain = new IndexingChain() {
    @Override
    DocConsumer getChain(DocumentsWriter documentsWriter) {
      final TermsHashConsumer termVectorsWriter = new TermVectorsTermsWriter(documentsWriter);
      final TermsHashConsumer freqProxWriter = new FreqProxTermsWriter();
      final InvertedDocConsumer  termsHash = new TermsHash(documentsWriter, true, freqProxWriter,
                                                           new TermsHash(documentsWriter, false, termVectorsWriter, null));
      final NormsWriter normsWriter = new NormsWriter();
      final DocInverter docInverter = new DocInverter(termsHash, normsWriter);
      return new DocFieldProcessor(documentsWriter, docInverter);
    }
  };
  final DocConsumer consumer;
  private BufferedDeletes deletesInRAM = new BufferedDeletes(false);
  private BufferedDeletes deletesFlushed = new BufferedDeletes(true);
  private int maxBufferedDeleteTerms = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DELETE_TERMS;
  private long ramBufferSize = (long) (IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB*1024*1024);
  private long waitQueuePauseBytes = (long) (ramBufferSize*0.1);
  private long waitQueueResumeBytes = (long) (ramBufferSize*0.05);
  private long freeTrigger = (long) (IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB*1024*1024*1.05);
  private long freeLevel = (long) (IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB*1024*1024*0.95);
  private int maxBufferedDocs = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DOCS;
  private int flushedDocCount;                      
  synchronized void updateFlushedDocCount(int n) {
    flushedDocCount += n;
  }
  synchronized int getFlushedDocCount() {
    return flushedDocCount;
  }
  synchronized void setFlushedDocCount(int n) {
    flushedDocCount = n;
  }
  private boolean closed;
  DocumentsWriter(Directory directory, IndexWriter writer, IndexingChain indexingChain, int maxThreadStates) throws IOException {
    this.directory = directory;
    this.writer = writer;
    this.similarity = writer.getConfig().getSimilarity();
    this.maxThreadStates = maxThreadStates;
    flushedDocCount = writer.maxDoc();
    consumer = indexingChain.getChain(this);
    if (consumer instanceof DocFieldProcessor) {
      docFieldProcessor = (DocFieldProcessor) consumer;
    }
  }
  boolean hasProx() {
    return (docFieldProcessor != null) ? docFieldProcessor.fieldInfos.hasProx()
                                       : true;
  }
  synchronized void setInfoStream(PrintStream infoStream) {
    this.infoStream = infoStream;
    for(int i=0;i<threadStates.length;i++)
      threadStates[i].docState.infoStream = infoStream;
  }
  synchronized void setMaxFieldLength(int maxFieldLength) {
    this.maxFieldLength = maxFieldLength;
    for(int i=0;i<threadStates.length;i++)
      threadStates[i].docState.maxFieldLength = maxFieldLength;
  }
  synchronized void setSimilarity(Similarity similarity) {
    this.similarity = similarity;
    for(int i=0;i<threadStates.length;i++)
      threadStates[i].docState.similarity = similarity;
  }
  synchronized void setRAMBufferSizeMB(double mb) {
    if (mb == IndexWriterConfig.DISABLE_AUTO_FLUSH) {
      ramBufferSize = IndexWriterConfig.DISABLE_AUTO_FLUSH;
      waitQueuePauseBytes = 4*1024*1024;
      waitQueueResumeBytes = 2*1024*1024;
    } else {
      ramBufferSize = (long) (mb*1024*1024);
      waitQueuePauseBytes = (long) (ramBufferSize*0.1);
      waitQueueResumeBytes = (long) (ramBufferSize*0.05);
      freeTrigger = (long) (1.05 * ramBufferSize);
      freeLevel = (long) (0.95 * ramBufferSize);
    }
  }
  synchronized double getRAMBufferSizeMB() {
    if (ramBufferSize == IndexWriterConfig.DISABLE_AUTO_FLUSH) {
      return ramBufferSize;
    } else {
      return ramBufferSize/1024./1024.;
    }
  }
  void setMaxBufferedDocs(int count) {
    maxBufferedDocs = count;
  }
  int getMaxBufferedDocs() {
    return maxBufferedDocs;
  }
  String getSegment() {
    return segment;
  }
  int getNumDocsInRAM() {
    return numDocsInRAM;
  }
  synchronized String getDocStoreSegment() {
    return docStoreSegment;
  }
  int getDocStoreOffset() {
    return docStoreOffset;
  }
  synchronized String closeDocStore() throws IOException {
    assert allThreadsIdle();
    if (infoStream != null)
      message("closeDocStore: " + openFiles.size() + " files to flush to segment " + docStoreSegment + " numDocs=" + numDocsInStore);
    boolean success = false;
    try {
      initFlushState(true);
      closedFiles.clear();
      consumer.closeDocStore(flushState);
      assert 0 == openFiles.size();
      String s = docStoreSegment;
      docStoreSegment = null;
      docStoreOffset = 0;
      numDocsInStore = 0;
      success = true;
      return s;
    } finally {
      if (!success) {
        abort();
      }
    }
  }
  private Collection<String> abortedFiles;               
  private SegmentWriteState flushState;
  Collection<String> abortedFiles() {
    return abortedFiles;
  }
  void message(String message) {
    if (infoStream != null)
      writer.message("DW: " + message);
  }
  final List<String> openFiles = new ArrayList<String>();
  final List<String> closedFiles = new ArrayList<String>();
  @SuppressWarnings("unchecked")
  synchronized List<String> openFiles() {
    return (List<String>) ((ArrayList<String>) openFiles).clone();
  }
  @SuppressWarnings("unchecked")
  synchronized List<String> closedFiles() {
    return (List<String>) ((ArrayList<String>) closedFiles).clone();
  }
  synchronized void addOpenFile(String name) {
    assert !openFiles.contains(name);
    openFiles.add(name);
  }
  synchronized void removeOpenFile(String name) {
    assert openFiles.contains(name);
    openFiles.remove(name);
    closedFiles.add(name);
  }
  synchronized void setAborting() {
    aborting = true;
  }
  synchronized void abort() throws IOException {
    try {
      if (infoStream != null) {
        message("docWriter: now abort");
      }
      waitQueue.abort();
      pauseAllThreads();
      try {
        assert 0 == waitQueue.numWaiting;
        waitQueue.waitingBytes = 0;
        try {
          abortedFiles = openFiles();
        } catch (Throwable t) {
          abortedFiles = null;
        }
        deletesInRAM.clear();
        openFiles.clear();
        for(int i=0;i<threadStates.length;i++)
          try {
            threadStates[i].consumer.abort();
          } catch (Throwable t) {
          }
        try {
          consumer.abort();
        } catch (Throwable t) {
        }
        docStoreSegment = null;
        numDocsInStore = 0;
        docStoreOffset = 0;
        doAfterFlush();
      } finally {
        resumeAllThreads();
      }
    } finally {
      aborting = false;
      notifyAll();
      if (infoStream != null) {
        message("docWriter: done abort");
      }
    }
  }
  private void doAfterFlush() throws IOException {
    assert allThreadsIdle();
    threadBindings.clear();
    waitQueue.reset();
    segment = null;
    numDocsInRAM = 0;
    nextDocID = 0;
    bufferIsFull = false;
    flushPending = false;
    for(int i=0;i<threadStates.length;i++)
      threadStates[i].doAfterFlush();
    numBytesUsed = 0;
  }
  synchronized boolean pauseAllThreads() {
    pauseThreads++;
    while(!allThreadsIdle()) {
      try {
        wait();
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
    }
    return aborting;
  }
  synchronized void resumeAllThreads() {
    pauseThreads--;
    assert pauseThreads >= 0;
    if (0 == pauseThreads)
      notifyAll();
  }
  private synchronized boolean allThreadsIdle() {
    for(int i=0;i<threadStates.length;i++)
      if (!threadStates[i].isIdle)
        return false;
    return true;
  }
  synchronized boolean anyChanges() {
    return numDocsInRAM != 0 ||
      deletesInRAM.numTerms != 0 ||
      deletesInRAM.docIDs.size() != 0 ||
      deletesInRAM.queries.size() != 0;
  }
  synchronized private void initFlushState(boolean onlyDocStore) {
    initSegmentName(onlyDocStore);
    flushState = new SegmentWriteState(this, directory, segment, docStoreSegment, numDocsInRAM, numDocsInStore, writer.getConfig().getTermIndexInterval());
  }
  synchronized int flush(boolean closeDocStore) throws IOException {
    assert allThreadsIdle();
    assert numDocsInRAM > 0;
    assert nextDocID == numDocsInRAM;
    assert waitQueue.numWaiting == 0;
    assert waitQueue.waitingBytes == 0;
    initFlushState(false);
    docStoreOffset = numDocsInStore;
    if (infoStream != null)
      message("flush postings as segment " + flushState.segmentName + " numDocs=" + numDocsInRAM);
    boolean success = false;
    try {
      if (closeDocStore) {
        assert flushState.docStoreSegmentName != null;
        assert flushState.docStoreSegmentName.equals(flushState.segmentName);
        closeDocStore();
        flushState.numDocsInStore = 0;
      }
      Collection<DocConsumerPerThread> threads = new HashSet<DocConsumerPerThread>();
      for(int i=0;i<threadStates.length;i++)
        threads.add(threadStates[i].consumer);
      consumer.flush(threads, flushState);
      if (infoStream != null) {
        SegmentInfo si = new SegmentInfo(flushState.segmentName, flushState.numDocs, directory);
        final long newSegmentSize = si.sizeInBytes();
        String message = "  oldRAMSize=" + numBytesUsed +
          " newFlushedSize=" + newSegmentSize +
          " docs/MB=" + nf.format(numDocsInRAM/(newSegmentSize/1024./1024.)) +
          " new/old=" + nf.format(100.0*newSegmentSize/numBytesUsed) + "%";
        message(message);
      }
      flushedDocCount += flushState.numDocs;
      doAfterFlush();
      success = true;
    } finally {
      if (!success) {
        abort();
      }
    }
    assert waitQueue.waitingBytes == 0;
    return flushState.numDocs;
  }
  void createCompoundFile(String segment) throws IOException {
    CompoundFileWriter cfsWriter = new CompoundFileWriter(directory, 
        IndexFileNames.segmentFileName(segment, IndexFileNames.COMPOUND_FILE_EXTENSION));
    for (final String flushedFile : flushState.flushedFiles)
      cfsWriter.addFile(flushedFile);
    cfsWriter.close();
  }
  synchronized boolean setFlushPending() {
    if (flushPending)
      return false;
    else {
      flushPending = true;
      return true;
    }
  }
  synchronized void clearFlushPending() {
    flushPending = false;
  }
  synchronized void pushDeletes() {
    deletesFlushed.update(deletesInRAM);
  }
  synchronized void close() {
    closed = true;
    notifyAll();
  }
  synchronized void initSegmentName(boolean onlyDocStore) {
    if (segment == null && (!onlyDocStore || docStoreSegment == null)) {
      segment = writer.newSegmentName();
      assert numDocsInRAM == 0;
    }
    if (docStoreSegment == null) {
      docStoreSegment = segment;
      assert numDocsInStore == 0;
    }
  }
  synchronized DocumentsWriterThreadState getThreadState(Document doc, Term delTerm) throws IOException {
    DocumentsWriterThreadState state = threadBindings.get(Thread.currentThread());
    if (state == null) {
      DocumentsWriterThreadState minThreadState = null;
      for(int i=0;i<threadStates.length;i++) {
        DocumentsWriterThreadState ts = threadStates[i];
        if (minThreadState == null || ts.numThreads < minThreadState.numThreads)
          minThreadState = ts;
      }
      if (minThreadState != null && (minThreadState.numThreads == 0 || threadStates.length >= maxThreadStates)) {
        state = minThreadState;
        state.numThreads++;
      } else {
        DocumentsWriterThreadState[] newArray = new DocumentsWriterThreadState[1+threadStates.length];
        if (threadStates.length > 0)
          System.arraycopy(threadStates, 0, newArray, 0, threadStates.length);
        state = newArray[threadStates.length] = new DocumentsWriterThreadState(this);
        threadStates = newArray;
      }
      threadBindings.put(Thread.currentThread(), state);
    }
    waitReady(state);
    initSegmentName(false);
    state.isIdle = false;
    boolean success = false;
    try {
      state.docState.docID = nextDocID;
      assert writer.testPoint("DocumentsWriter.ThreadState.init start");
      if (delTerm != null) {
        addDeleteTerm(delTerm, state.docState.docID);
        state.doFlushAfter = timeToFlushDeletes();
      }
      assert writer.testPoint("DocumentsWriter.ThreadState.init after delTerm");
      nextDocID++;
      numDocsInRAM++;
      if (!flushPending &&
          maxBufferedDocs != IndexWriterConfig.DISABLE_AUTO_FLUSH
          && numDocsInRAM >= maxBufferedDocs) {
        flushPending = true;
        state.doFlushAfter = true;
      }
      success = true;
    } finally {
      if (!success) {
        state.isIdle = true;
        notifyAll();
        if (state.doFlushAfter) {
          state.doFlushAfter = false;
          flushPending = false;
        }
      }
    }
    return state;
  }
  boolean addDocument(Document doc, Analyzer analyzer)
    throws CorruptIndexException, IOException {
    return updateDocument(doc, analyzer, null);
  }
  boolean updateDocument(Term t, Document doc, Analyzer analyzer)
    throws CorruptIndexException, IOException {
    return updateDocument(doc, analyzer, t);
  }
  boolean updateDocument(Document doc, Analyzer analyzer, Term delTerm)
    throws CorruptIndexException, IOException {
    final DocumentsWriterThreadState state = getThreadState(doc, delTerm);
    final DocState docState = state.docState;
    docState.doc = doc;
    docState.analyzer = analyzer;
    boolean success = false;
    try {
      final DocWriter perDoc = state.consumer.processDocument();
      finishDocument(state, perDoc);
      success = true;
    } finally {
      if (!success) {
        synchronized(this) {
          if (aborting) {
            state.isIdle = true;
            notifyAll();
            abort();
          } else {
            skipDocWriter.docID = docState.docID;
            boolean success2 = false;
            try {
              waitQueue.add(skipDocWriter);
              success2 = true;
            } finally {
              if (!success2) {
                state.isIdle = true;
                notifyAll();
                abort();
                return false;
              }
            }
            state.isIdle = true;
            notifyAll();
            if (state.doFlushAfter) {
              state.doFlushAfter = false;
              flushPending = false;
              notifyAll();
            }
            addDeleteDocID(state.docState.docID);
          }
        }
      }
    }
    return state.doFlushAfter || timeToFlushDeletes();
  }
  synchronized int getNumBufferedDeleteTerms() {
    return deletesInRAM.numTerms;
  }
  synchronized Map<Term,BufferedDeletes.Num> getBufferedDeleteTerms() {
    return deletesInRAM.terms;
  }
  synchronized void remapDeletes(SegmentInfos infos, int[][] docMaps, int[] delCounts, MergePolicy.OneMerge merge, int mergeDocCount) {
    if (docMaps == null)
      return;
    MergeDocIDRemapper mapper = new MergeDocIDRemapper(infos, docMaps, delCounts, merge, mergeDocCount);
    deletesInRAM.remap(mapper, infos, docMaps, delCounts, merge, mergeDocCount);
    deletesFlushed.remap(mapper, infos, docMaps, delCounts, merge, mergeDocCount);
    flushedDocCount -= mapper.docShift;
  }
  synchronized private void waitReady(DocumentsWriterThreadState state) {
    while (!closed && ((state != null && !state.isIdle) || pauseThreads != 0 || flushPending || aborting)) {
      try {
        wait();
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
    }
    if (closed)
      throw new AlreadyClosedException("this IndexWriter is closed");
  }
  synchronized boolean bufferDeleteTerms(Term[] terms) throws IOException {
    waitReady(null);
    for (int i = 0; i < terms.length; i++)
      addDeleteTerm(terms[i], numDocsInRAM);
    return timeToFlushDeletes();
  }
  synchronized boolean bufferDeleteTerm(Term term) throws IOException {
    waitReady(null);
    addDeleteTerm(term, numDocsInRAM);
    return timeToFlushDeletes();
  }
  synchronized boolean bufferDeleteQueries(Query[] queries) throws IOException {
    waitReady(null);
    for (int i = 0; i < queries.length; i++)
      addDeleteQuery(queries[i], numDocsInRAM);
    return timeToFlushDeletes();
  }
  synchronized boolean bufferDeleteQuery(Query query) throws IOException {
    waitReady(null);
    addDeleteQuery(query, numDocsInRAM);
    return timeToFlushDeletes();
  }
  synchronized boolean deletesFull() {
    return (ramBufferSize != IndexWriterConfig.DISABLE_AUTO_FLUSH &&
            (deletesInRAM.bytesUsed + deletesFlushed.bytesUsed + numBytesUsed) >= ramBufferSize) ||
      (maxBufferedDeleteTerms != IndexWriterConfig.DISABLE_AUTO_FLUSH &&
       ((deletesInRAM.size() + deletesFlushed.size()) >= maxBufferedDeleteTerms));
  }
  synchronized boolean doApplyDeletes() {
    return (ramBufferSize != IndexWriterConfig.DISABLE_AUTO_FLUSH &&
            (deletesInRAM.bytesUsed + deletesFlushed.bytesUsed) >= ramBufferSize/2) ||
      (maxBufferedDeleteTerms != IndexWriterConfig.DISABLE_AUTO_FLUSH &&
       ((deletesInRAM.size() + deletesFlushed.size()) >= maxBufferedDeleteTerms));
  }
  synchronized private boolean timeToFlushDeletes() {
    return (bufferIsFull || deletesFull()) && setFlushPending();
  }
  void setMaxBufferedDeleteTerms(int maxBufferedDeleteTerms) {
    this.maxBufferedDeleteTerms = maxBufferedDeleteTerms;
  }
  int getMaxBufferedDeleteTerms() {
    return maxBufferedDeleteTerms;
  }
  synchronized boolean hasDeletes() {
    return deletesFlushed.any();
  }
  synchronized boolean applyDeletes(SegmentInfos infos) throws IOException {
    if (!hasDeletes())
      return false;
    final long t0 = System.currentTimeMillis();
    if (infoStream != null)
      message("apply " + deletesFlushed.numTerms + " buffered deleted terms and " +
              deletesFlushed.docIDs.size() + " deleted docIDs and " +
              deletesFlushed.queries.size() + " deleted queries on " +
              + infos.size() + " segments.");
    final int infosEnd = infos.size();
    int docStart = 0;
    boolean any = false;
    for (int i = 0; i < infosEnd; i++) {
      assert infos.info(i).dir == directory;
      SegmentReader reader = writer.readerPool.get(infos.info(i), false);
      try {
        any |= applyDeletes(reader, docStart);
        docStart += reader.maxDoc();
      } finally {
        writer.readerPool.release(reader);
      }
    }
    deletesFlushed.clear();
    if (infoStream != null) {
      message("apply deletes took " + (System.currentTimeMillis()-t0) + " msec");
    }
    return any;
  }
  private Term lastDeleteTerm;
  private boolean checkDeleteTerm(Term term) {
    if (term != null) {
      assert lastDeleteTerm == null || term.compareTo(lastDeleteTerm) > 0: "lastTerm=" + lastDeleteTerm + " vs term=" + term;
    }
    lastDeleteTerm = term;
    return true;
  }
  private final synchronized boolean applyDeletes(IndexReader reader, int docIDStart)
    throws CorruptIndexException, IOException {
    final int docEnd = docIDStart + reader.maxDoc();
    boolean any = false;
    assert checkDeleteTerm(null);
    if (deletesFlushed.terms.size() > 0) {
      TermDocs docs = reader.termDocs();
      try {
        for (Entry<Term, BufferedDeletes.Num> entry: deletesFlushed.terms.entrySet()) {
          Term term = entry.getKey();
          assert checkDeleteTerm(term);
          docs.seek(term);
          int limit = entry.getValue().getNum();
          while (docs.next()) {
            int docID = docs.doc();
            if (docIDStart+docID >= limit)
              break;
            reader.deleteDocument(docID);
            any = true;
          }
        }
      } finally {
        docs.close();
      }
    }
    for (Integer docIdInt : deletesFlushed.docIDs) {
      int docID = docIdInt.intValue();
      if (docID >= docIDStart && docID < docEnd) {
        reader.deleteDocument(docID-docIDStart);
        any = true;
      }
    }
    if (deletesFlushed.queries.size() > 0) {
      IndexSearcher searcher = new IndexSearcher(reader);
      try {
        for (Entry<Query, Integer> entry : deletesFlushed.queries.entrySet()) {
          Query query = entry.getKey();
          int limit = entry.getValue().intValue();
          Weight weight = query.weight(searcher);
          Scorer scorer = weight.scorer(reader, true, false);
          if (scorer != null) {
            while(true)  {
              int doc = scorer.nextDoc();
              if (((long) docIDStart) + doc >= limit)
                break;
              reader.deleteDocument(doc);
              any = true;
            }
          }
        }
      } finally {
        searcher.close();
      }
    }
    return any;
  }
  synchronized private void addDeleteTerm(Term term, int docCount) {
    BufferedDeletes.Num num = deletesInRAM.terms.get(term);
    final int docIDUpto = flushedDocCount + docCount;
    if (num == null)
      deletesInRAM.terms.put(term, new BufferedDeletes.Num(docIDUpto));
    else
      num.setNum(docIDUpto);
    deletesInRAM.numTerms++;
    deletesInRAM.addBytesUsed(BYTES_PER_DEL_TERM + term.text.length()*CHAR_NUM_BYTE);
  }
  synchronized private void addDeleteDocID(int docID) {
    deletesInRAM.docIDs.add(Integer.valueOf(flushedDocCount+docID));
    deletesInRAM.addBytesUsed(BYTES_PER_DEL_DOCID);
  }
  synchronized private void addDeleteQuery(Query query, int docID) {
    deletesInRAM.queries.put(query, Integer.valueOf(flushedDocCount + docID));
    deletesInRAM.addBytesUsed(BYTES_PER_DEL_QUERY);
  }
  synchronized boolean doBalanceRAM() {
    return ramBufferSize != IndexWriterConfig.DISABLE_AUTO_FLUSH && !bufferIsFull && (numBytesUsed+deletesInRAM.bytesUsed+deletesFlushed.bytesUsed >= ramBufferSize || numBytesAlloc >= freeTrigger);
  }
  private void finishDocument(DocumentsWriterThreadState perThread, DocWriter docWriter) throws IOException {
    if (doBalanceRAM())
      balanceRAM();
    synchronized(this) {
      assert docWriter == null || docWriter.docID == perThread.docState.docID;
      if (aborting) {
        if (docWriter != null)
          try {
            docWriter.abort();
          } catch (Throwable t) {
          }
        perThread.isIdle = true;
        notifyAll();
        return;
      }
      final boolean doPause;
      if (docWriter != null)
        doPause = waitQueue.add(docWriter);
      else {
        skipDocWriter.docID = perThread.docState.docID;
        doPause = waitQueue.add(skipDocWriter);
      }
      if (doPause)
        waitForWaitQueue();
      if (bufferIsFull && !flushPending) {
        flushPending = true;
        perThread.doFlushAfter = true;
      }
      perThread.isIdle = true;
      notifyAll();
    }
  }
  synchronized void waitForWaitQueue() {
    do {
      try {
        wait();
      } catch (InterruptedException ie) {
        throw new ThreadInterruptedException(ie);
      }
    } while (!waitQueue.doResume());
  }
  private static class SkipDocWriter extends DocWriter {
    @Override
    void finish() {
    }
    @Override
    void abort() {
    }
    @Override
    long sizeInBytes() {
      return 0;
    }
  }
  final SkipDocWriter skipDocWriter = new SkipDocWriter();
  long getRAMUsed() {
    return numBytesUsed + deletesInRAM.bytesUsed + deletesFlushed.bytesUsed;
  }
  long numBytesAlloc;
  long numBytesUsed;
  NumberFormat nf = NumberFormat.getInstance();
  final static int OBJECT_HEADER_BYTES = 8;
  final static int POINTER_NUM_BYTE = Constants.JRE_IS_64BIT ? 8 : 4;
  final static int INT_NUM_BYTE = 4;
  final static int CHAR_NUM_BYTE = 2;
  final static int BYTES_PER_DEL_TERM = 8*POINTER_NUM_BYTE + 5*OBJECT_HEADER_BYTES + 6*INT_NUM_BYTE;
  final static int BYTES_PER_DEL_DOCID = 2*POINTER_NUM_BYTE + OBJECT_HEADER_BYTES + INT_NUM_BYTE;
  final static int BYTES_PER_DEL_QUERY = 5*POINTER_NUM_BYTE + 2*OBJECT_HEADER_BYTES + 2*INT_NUM_BYTE + 24;
  final static int BYTE_BLOCK_SHIFT = 15;
  final static int BYTE_BLOCK_SIZE = 1 << BYTE_BLOCK_SHIFT;
  final static int BYTE_BLOCK_MASK = BYTE_BLOCK_SIZE - 1;
  final static int BYTE_BLOCK_NOT_MASK = ~BYTE_BLOCK_MASK;
  private class ByteBlockAllocator extends ByteBlockPool.Allocator {
    final int blockSize;
    ByteBlockAllocator(int blockSize) {
      this.blockSize = blockSize;
    }
    ArrayList<byte[]> freeByteBlocks = new ArrayList<byte[]>();
    @Override
    byte[] getByteBlock(boolean trackAllocations) {
      synchronized(DocumentsWriter.this) {
        final int size = freeByteBlocks.size();
        final byte[] b;
        if (0 == size) {
          numBytesAlloc += blockSize;
          b = new byte[blockSize];
        } else
          b = freeByteBlocks.remove(size-1);
        if (trackAllocations)
          numBytesUsed += blockSize;
        assert numBytesUsed <= numBytesAlloc;
        return b;
      }
    }
    @Override
    void recycleByteBlocks(byte[][] blocks, int start, int end) {
      synchronized(DocumentsWriter.this) {
        for(int i=start;i<end;i++)
          freeByteBlocks.add(blocks[i]);
      }
    }
  }
  final static int INT_BLOCK_SHIFT = 13;
  final static int INT_BLOCK_SIZE = 1 << INT_BLOCK_SHIFT;
  final static int INT_BLOCK_MASK = INT_BLOCK_SIZE - 1;
  private ArrayList<int[]> freeIntBlocks = new ArrayList<int[]>();
  synchronized int[] getIntBlock(boolean trackAllocations) {
    final int size = freeIntBlocks.size();
    final int[] b;
    if (0 == size) {
      numBytesAlloc += INT_BLOCK_SIZE*INT_NUM_BYTE;
      b = new int[INT_BLOCK_SIZE];
    } else
      b = freeIntBlocks.remove(size-1);
    if (trackAllocations)
      numBytesUsed += INT_BLOCK_SIZE*INT_NUM_BYTE;
    assert numBytesUsed <= numBytesAlloc;
    return b;
  }
  synchronized void bytesAllocated(long numBytes) {
    numBytesAlloc += numBytes;
    assert numBytesUsed <= numBytesAlloc;
  }
  synchronized void bytesUsed(long numBytes) {
    numBytesUsed += numBytes;
    assert numBytesUsed <= numBytesAlloc;
  }
  synchronized void recycleIntBlocks(int[][] blocks, int start, int end) {
    for(int i=start;i<end;i++)
      freeIntBlocks.add(blocks[i]);
  }
  ByteBlockAllocator byteBlockAllocator = new ByteBlockAllocator(BYTE_BLOCK_SIZE);
  final static int PER_DOC_BLOCK_SIZE = 1024;
  final ByteBlockAllocator perDocAllocator = new ByteBlockAllocator(PER_DOC_BLOCK_SIZE);
  final static int CHAR_BLOCK_SHIFT = 14;
  final static int CHAR_BLOCK_SIZE = 1 << CHAR_BLOCK_SHIFT;
  final static int CHAR_BLOCK_MASK = CHAR_BLOCK_SIZE - 1;
  final static int MAX_TERM_LENGTH = CHAR_BLOCK_SIZE-1;
  private ArrayList<char[]> freeCharBlocks = new ArrayList<char[]>();
  synchronized char[] getCharBlock() {
    final int size = freeCharBlocks.size();
    final char[] c;
    if (0 == size) {
      numBytesAlloc += CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
      c = new char[CHAR_BLOCK_SIZE];
    } else
      c = freeCharBlocks.remove(size-1);
    numBytesUsed += CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
    assert numBytesUsed <= numBytesAlloc;
    return c;
  }
  synchronized void recycleCharBlocks(char[][] blocks, int numBlocks) {
    for(int i=0;i<numBlocks;i++)
      freeCharBlocks.add(blocks[i]);
  }
  String toMB(long v) {
    return nf.format(v/1024./1024.);
  }
  void balanceRAM() {
    final long flushTrigger = ramBufferSize;
    final long deletesRAMUsed = deletesInRAM.bytesUsed+deletesFlushed.bytesUsed;
    if (numBytesAlloc+deletesRAMUsed > freeTrigger) {
      if (infoStream != null)
        message("  RAM: now balance allocations: usedMB=" + toMB(numBytesUsed) +
                " vs trigger=" + toMB(flushTrigger) +
                " allocMB=" + toMB(numBytesAlloc) +
                " deletesMB=" + toMB(deletesRAMUsed) +
                " vs trigger=" + toMB(freeTrigger) +
                " byteBlockFree=" + toMB(byteBlockAllocator.freeByteBlocks.size()*BYTE_BLOCK_SIZE) +
                " perDocFree=" + toMB(perDocAllocator.freeByteBlocks.size()*PER_DOC_BLOCK_SIZE) +
                " charBlockFree=" + toMB(freeCharBlocks.size()*CHAR_BLOCK_SIZE*CHAR_NUM_BYTE));
      final long startBytesAlloc = numBytesAlloc + deletesRAMUsed;
      int iter = 0;
      boolean any = true;
      while(numBytesAlloc+deletesRAMUsed > freeLevel) {
        synchronized(this) {
          if (0 == perDocAllocator.freeByteBlocks.size() 
              && 0 == byteBlockAllocator.freeByteBlocks.size() 
              && 0 == freeCharBlocks.size() 
              && 0 == freeIntBlocks.size() 
              && !any) {
            bufferIsFull = numBytesUsed+deletesRAMUsed > flushTrigger;
            if (infoStream != null) {
              if (numBytesUsed > flushTrigger)
                message("    nothing to free; now set bufferIsFull");
              else
                message("    nothing to free");
            }
            assert numBytesUsed <= numBytesAlloc;
            break;
          }
          if ((0 == iter % 5) && byteBlockAllocator.freeByteBlocks.size() > 0) {
            byteBlockAllocator.freeByteBlocks.remove(byteBlockAllocator.freeByteBlocks.size()-1);
            numBytesAlloc -= BYTE_BLOCK_SIZE;
          }
          if ((1 == iter % 5) && freeCharBlocks.size() > 0) {
            freeCharBlocks.remove(freeCharBlocks.size()-1);
            numBytesAlloc -= CHAR_BLOCK_SIZE * CHAR_NUM_BYTE;
          }
          if ((2 == iter % 5) && freeIntBlocks.size() > 0) {
            freeIntBlocks.remove(freeIntBlocks.size()-1);
            numBytesAlloc -= INT_BLOCK_SIZE * INT_NUM_BYTE;
          }
          if ((3 == iter % 5) && perDocAllocator.freeByteBlocks.size() > 0) {
            for (int i = 0; i < 32; ++i) {
              perDocAllocator.freeByteBlocks.remove(perDocAllocator.freeByteBlocks.size() - 1);
              numBytesAlloc -= PER_DOC_BLOCK_SIZE;
              if (perDocAllocator.freeByteBlocks.size() == 0) {
                break;
              }
            }
          }
        }
        if ((4 == iter % 5) && any)
          any = consumer.freeRAM();
        iter++;
      }
      if (infoStream != null)
        message("    after free: freedMB=" + nf.format((startBytesAlloc-numBytesAlloc-deletesRAMUsed)/1024./1024.) + " usedMB=" + nf.format((numBytesUsed+deletesRAMUsed)/1024./1024.) + " allocMB=" + nf.format(numBytesAlloc/1024./1024.));
    } else {
      synchronized(this) {
        if (numBytesUsed+deletesRAMUsed > flushTrigger) {
          if (infoStream != null)
            message("  RAM: now flush @ usedMB=" + nf.format(numBytesUsed/1024./1024.) +
                    " allocMB=" + nf.format(numBytesAlloc/1024./1024.) +
                    " deletesMB=" + nf.format(deletesRAMUsed/1024./1024.) +
                    " triggerMB=" + nf.format(flushTrigger/1024./1024.));
          bufferIsFull = true;
        }
      }
    }
  }
  final WaitQueue waitQueue = new WaitQueue();
  private class WaitQueue {
    DocWriter[] waiting;
    int nextWriteDocID;
    int nextWriteLoc;
    int numWaiting;
    long waitingBytes;
    public WaitQueue() {
      waiting = new DocWriter[10];
    }
    synchronized void reset() {
      assert numWaiting == 0;
      assert waitingBytes == 0;
      nextWriteDocID = 0;
    }
    synchronized boolean doResume() {
      return waitingBytes <= waitQueueResumeBytes;
    }
    synchronized boolean doPause() {
      return waitingBytes > waitQueuePauseBytes;
    }
    synchronized void abort() {
      int count = 0;
      for(int i=0;i<waiting.length;i++) {
        final DocWriter doc = waiting[i];
        if (doc != null) {
          doc.abort();
          waiting[i] = null;
          count++;
        }
      }
      waitingBytes = 0;
      assert count == numWaiting;
      numWaiting = 0;
    }
    private void writeDocument(DocWriter doc) throws IOException {
      assert doc == skipDocWriter || nextWriteDocID == doc.docID;
      boolean success = false;
      try {
        doc.finish();
        nextWriteDocID++;
        numDocsInStore++;
        nextWriteLoc++;
        assert nextWriteLoc <= waiting.length;
        if (nextWriteLoc == waiting.length)
          nextWriteLoc = 0;
        success = true;
      } finally {
        if (!success)
          setAborting();
      }
    }
    synchronized public boolean add(DocWriter doc) throws IOException {
      assert doc.docID >= nextWriteDocID;
      if (doc.docID == nextWriteDocID) {
        writeDocument(doc);
        while(true) {
          doc = waiting[nextWriteLoc];
          if (doc != null) {
            numWaiting--;
            waiting[nextWriteLoc] = null;
            waitingBytes -= doc.sizeInBytes();
            writeDocument(doc);
          } else
            break;
        }
      } else {
        int gap = doc.docID - nextWriteDocID;
        if (gap >= waiting.length) {
          DocWriter[] newArray = new DocWriter[ArrayUtil.oversize(gap, RamUsageEstimator.NUM_BYTES_OBJECT_REF)];
          assert nextWriteLoc >= 0;
          System.arraycopy(waiting, nextWriteLoc, newArray, 0, waiting.length-nextWriteLoc);
          System.arraycopy(waiting, 0, newArray, waiting.length-nextWriteLoc, nextWriteLoc);
          nextWriteLoc = 0;
          waiting = newArray;
          gap = doc.docID - nextWriteDocID;
        }
        int loc = nextWriteLoc + gap;
        if (loc >= waiting.length)
          loc -= waiting.length;
        assert loc < waiting.length;
        assert waiting[loc] == null;
        waiting[loc] = doc;
        numWaiting++;
        waitingBytes += doc.sizeInBytes();
      }
      return doPause();
    }
  }
}
