package org.apache.lucene.index;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.Lock;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.util.Constants;
import org.apache.lucene.util.ThreadInterruptedException;
import org.apache.lucene.util.Version;
import java.io.IOException;
import java.io.Closeable;
import java.io.PrintStream;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
public class IndexWriter implements Closeable {
  public static long WRITE_LOCK_TIMEOUT = IndexWriterConfig.WRITE_LOCK_TIMEOUT;
  private long writeLockTimeout;
  public static final String WRITE_LOCK_NAME = "write.lock";
  public final static int DISABLE_AUTO_FLUSH = IndexWriterConfig.DISABLE_AUTO_FLUSH;
  public final static int DEFAULT_MAX_BUFFERED_DOCS = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DOCS;
  public final static double DEFAULT_RAM_BUFFER_SIZE_MB = IndexWriterConfig.DEFAULT_RAM_BUFFER_SIZE_MB;
  public final static int DEFAULT_MAX_BUFFERED_DELETE_TERMS = IndexWriterConfig.DEFAULT_MAX_BUFFERED_DELETE_TERMS;
  public final static int DEFAULT_MAX_FIELD_LENGTH = 10000;
  public final static int DEFAULT_TERM_INDEX_INTERVAL = IndexWriterConfig.DEFAULT_TERM_INDEX_INTERVAL;
  public final static int MAX_TERM_LENGTH = DocumentsWriter.MAX_TERM_LENGTH;
  private final static int MERGE_READ_BUFFER_SIZE = 4096;
  private static Object MESSAGE_ID_LOCK = new Object();
  private static int MESSAGE_ID = 0;
  private int messageID = -1;
  volatile private boolean hitOOM;
  private final Directory directory;  
  private final Analyzer analyzer;    
  private Similarity similarity = Similarity.getDefault(); 
  private volatile long changeCount; 
  private long lastCommitChangeCount; 
  private SegmentInfos rollbackSegmentInfos;      
  private HashMap<SegmentInfo,Integer> rollbackSegments;
  volatile SegmentInfos pendingCommit;            
  volatile long pendingCommitChangeCount;
  private SegmentInfos localRollbackSegmentInfos;      
  private int localFlushedDocCount;               
  private SegmentInfos segmentInfos = new SegmentInfos();       
  private DocumentsWriter docWriter;
  private IndexFileDeleter deleter;
  private Set<SegmentInfo> segmentsToOptimize = new HashSet<SegmentInfo>();           
  private Lock writeLock;
  private int termIndexInterval;
  private boolean closed;
  private boolean closing;
  private HashSet<SegmentInfo> mergingSegments = new HashSet<SegmentInfo>();
  private MergePolicy mergePolicy = new LogByteSizeMergePolicy(this);
  private MergeScheduler mergeScheduler;
  private LinkedList<MergePolicy.OneMerge> pendingMerges = new LinkedList<MergePolicy.OneMerge>();
  private Set<MergePolicy.OneMerge> runningMerges = new HashSet<MergePolicy.OneMerge>();
  private List<MergePolicy.OneMerge> mergeExceptions = new ArrayList<MergePolicy.OneMerge>();
  private long mergeGen;
  private boolean stopMerges;
  private int flushCount;
  private int flushDeletesCount;
  private int readCount;                          
  private Thread writeThread;                     
  final ReaderPool readerPool = new ReaderPool();
  private int upgradeCount;
  private volatile boolean poolReaders;
  private final IndexWriterConfig config;
  public IndexReader getReader() throws IOException {
    return getReader(IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
  }
  public IndexReader getReader(int termInfosIndexDivisor) throws IOException {
    ensureOpen();
    if (infoStream != null) {
      message("flush at getReader");
    }
    poolReaders = true;
    flush(true, true, false);
    synchronized(this) {
      applyDeletes();
      final IndexReader r = new ReadOnlyDirectoryReader(this, segmentInfos, termInfosIndexDivisor);
      if (infoStream != null) {
        message("return reader version=" + r.getVersion() + " reader=" + r);
      }
      return r;
    }
  }
  class ReaderPool {
    private final Map<SegmentInfo,SegmentReader> readerMap = new HashMap<SegmentInfo,SegmentReader>();
    synchronized void clear(SegmentInfos infos) throws IOException {
      if (infos == null) {
        for (Map.Entry<SegmentInfo,SegmentReader> ent: readerMap.entrySet()) {
          ent.getValue().hasChanges = false;
        }
      } else {
        for (final SegmentInfo info: infos) {
          if (readerMap.containsKey(info)) {
            readerMap.get(info).hasChanges = false;
          }
        }     
      }
    }
    public synchronized boolean infoIsLive(SegmentInfo info) {
      int idx = segmentInfos.indexOf(info);
      assert idx != -1;
      assert segmentInfos.get(idx) == info;
      return true;
    }
    public synchronized SegmentInfo mapToLive(SegmentInfo info) {
      int idx = segmentInfos.indexOf(info);
      if (idx != -1) {
        info = segmentInfos.get(idx);
      }
      return info;
    }
    public synchronized void release(SegmentReader sr) throws IOException {
      release(sr, false);
    }
    public synchronized void release(SegmentReader sr, boolean drop) throws IOException {
      final boolean pooled = readerMap.containsKey(sr.getSegmentInfo());
      assert !pooled | readerMap.get(sr.getSegmentInfo()) == sr;
      sr.decRef();
      if (pooled && (drop || (!poolReaders && sr.getRefCount() == 1))) {
        readerMap.remove(sr.getSegmentInfo());
        assert !sr.hasChanges || Thread.holdsLock(IndexWriter.this);
        boolean success = false;
        try {
          sr.close();
          success = true;
        } finally {
          if (!success && sr.hasChanges) {
            sr.hasChanges = false;
            try {
              sr.close();
            } catch (Throwable ignore) {
            }
          }
        }
      }
    }
    synchronized void close() throws IOException {
      Iterator<Map.Entry<SegmentInfo,SegmentReader>> iter = readerMap.entrySet().iterator();
      while (iter.hasNext()) {
        Map.Entry<SegmentInfo,SegmentReader> ent = iter.next();
        SegmentReader sr = ent.getValue();
        if (sr.hasChanges) {
          assert infoIsLive(sr.getSegmentInfo());
          sr.startCommit();
          boolean success = false;
          try {
            sr.doCommit(null);
            success = true;
          } finally {
            if (!success) {
              sr.rollbackCommit();
            }
          }
        }
        iter.remove();
        sr.decRef();
      }
    }
    synchronized void commit() throws IOException {
      for (Map.Entry<SegmentInfo,SegmentReader> ent : readerMap.entrySet()) {
        SegmentReader sr = ent.getValue();
        if (sr.hasChanges) {
          assert infoIsLive(sr.getSegmentInfo());
          sr.startCommit();
          boolean success = false;
          try {
            sr.doCommit(null);
            success = true;
          } finally {
            if (!success) {
              sr.rollbackCommit();
            }
          }
        }
      }
    }
    public synchronized SegmentReader getReadOnlyClone(SegmentInfo info, boolean doOpenStores, int termInfosIndexDivisor) throws IOException {
      SegmentReader sr = get(info, doOpenStores, BufferedIndexInput.BUFFER_SIZE, termInfosIndexDivisor);
      try {
        return (SegmentReader) sr.clone(true);
      } finally {
        sr.decRef();
      }
    }
    public synchronized SegmentReader get(SegmentInfo info, boolean doOpenStores) throws IOException {
      return get(info, doOpenStores, BufferedIndexInput.BUFFER_SIZE, IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    }
    public synchronized SegmentReader get(SegmentInfo info, boolean doOpenStores, int readBufferSize, int termsIndexDivisor) throws IOException {
      if (poolReaders) {
        readBufferSize = BufferedIndexInput.BUFFER_SIZE;
      }
      SegmentReader sr = readerMap.get(info);
      if (sr == null) {
        sr = SegmentReader.get(false, info.dir, info, readBufferSize, doOpenStores, termsIndexDivisor);
        if (info.dir == directory) {
          readerMap.put(info, sr);
        }
      } else {
        if (doOpenStores) {
          sr.openDocStores();
        }
        if (termsIndexDivisor != -1 && !sr.termsIndexLoaded()) {
          sr.loadTermsIndex(termsIndexDivisor);
        }
      }
      if (info.dir == directory) {
        sr.incRef();
      }
      return sr;
    }
    public synchronized SegmentReader getIfExists(SegmentInfo info) throws IOException {
      SegmentReader sr = readerMap.get(info);
      if (sr != null) {
        sr.incRef();
      }
      return sr;
    }
  }
  public int numDeletedDocs(SegmentInfo info) throws IOException {
    SegmentReader reader = readerPool.getIfExists(info);
    try {
      if (reader != null) {
        return reader.numDeletedDocs();
      } else {
        return info.getDelCount();
      }
    } finally {
      if (reader != null) {
        readerPool.release(reader);
      }
    }
  }
  synchronized void acquireWrite() {
    assert writeThread != Thread.currentThread();
    while(writeThread != null || readCount > 0)
      doWait();
    ensureOpen();
    writeThread = Thread.currentThread();
  }
  synchronized void releaseWrite() {
    assert Thread.currentThread() == writeThread;
    writeThread = null;
    notifyAll();
  }
  synchronized void acquireRead() {
    final Thread current = Thread.currentThread();
    while(writeThread != null && writeThread != current)
      doWait();
    readCount++;
  }
  synchronized void upgradeReadToWrite() {
    assert readCount > 0;
    upgradeCount++;
    while(readCount > upgradeCount || writeThread != null) {
      doWait();
    }
    writeThread = Thread.currentThread();
    readCount--;
    upgradeCount--;
  }
  synchronized void releaseRead() {
    readCount--;
    assert readCount >= 0;
    notifyAll();
  }
  protected final void ensureOpen(boolean includePendingClose) throws AlreadyClosedException {
    if (closed || (includePendingClose && closing)) {
      throw new AlreadyClosedException("this IndexWriter is closed");
    }
  }
  protected final void ensureOpen() throws AlreadyClosedException {
    ensureOpen(true);
  }
  public void message(String message) {
    if (infoStream != null)
      infoStream.println("IW " + messageID + " [" + Thread.currentThread().getName() + "]: " + message);
  }
  private synchronized void setMessageID(PrintStream infoStream) {
    if (infoStream != null && messageID == -1) {
      synchronized(MESSAGE_ID_LOCK) {
        messageID = MESSAGE_ID++;
      }
    }
    this.infoStream = infoStream;
  }
  private LogMergePolicy getLogMergePolicy() {
    if (mergePolicy instanceof LogMergePolicy)
      return (LogMergePolicy) mergePolicy;
    else
      throw new IllegalArgumentException("this method can only be called when the merge policy is the default LogMergePolicy");
  }
  public boolean getUseCompoundFile() {
    return getLogMergePolicy().getUseCompoundFile();
  }
  public void setUseCompoundFile(boolean value) {
    getLogMergePolicy().setUseCompoundFile(value);
    getLogMergePolicy().setUseCompoundDocStore(value);
  }
  public void setSimilarity(Similarity similarity) {
    ensureOpen();
    this.similarity = similarity;
    docWriter.setSimilarity(similarity);
    config.setSimilarity(similarity);
  }
  public Similarity getSimilarity() {
    ensureOpen();
    return similarity;
  }
  public void setTermIndexInterval(int interval) {
    ensureOpen();
    this.termIndexInterval = interval;
    config.setTermIndexInterval(interval);
  }
  public int getTermIndexInterval() {
    ensureOpen(false);
    return termIndexInterval;
  }
  public IndexWriter(Directory d, Analyzer a, boolean create, MaxFieldLength mfl)
       throws CorruptIndexException, LockObtainFailedException, IOException {
    this(d, new IndexWriterConfig(Version.LUCENE_31, a).setOpenMode(
        create ? OpenMode.CREATE : OpenMode.APPEND).setMaxFieldLength(
        mfl.getLimit()));
  }
  public IndexWriter(Directory d, Analyzer a, MaxFieldLength mfl)
    throws CorruptIndexException, LockObtainFailedException, IOException {
    this(d, new IndexWriterConfig(Version.LUCENE_31, a)
        .setMaxFieldLength(mfl.getLimit()));
  }
  public IndexWriter(Directory d, Analyzer a, IndexDeletionPolicy deletionPolicy, MaxFieldLength mfl)
    throws CorruptIndexException, LockObtainFailedException, IOException {
    this(d, new IndexWriterConfig(Version.LUCENE_31, a).setMaxFieldLength(
        mfl.getLimit()).setIndexDeletionPolicy(deletionPolicy));
  }
  public IndexWriter(Directory d, Analyzer a, boolean create, IndexDeletionPolicy deletionPolicy, MaxFieldLength mfl)
       throws CorruptIndexException, LockObtainFailedException, IOException {
    this(d, new IndexWriterConfig(Version.LUCENE_31, a).setOpenMode(
        create ? OpenMode.CREATE : OpenMode.APPEND).setMaxFieldLength(
        mfl.getLimit()).setIndexDeletionPolicy(deletionPolicy));
  }
  public IndexWriter(Directory d, Analyzer a, IndexDeletionPolicy deletionPolicy, MaxFieldLength mfl, IndexCommit commit)
       throws CorruptIndexException, LockObtainFailedException, IOException {
    this(d, new IndexWriterConfig(Version.LUCENE_31, a)
        .setOpenMode(OpenMode.APPEND).setMaxFieldLength(mfl.getLimit())
        .setIndexDeletionPolicy(deletionPolicy).setIndexCommit(commit));
  }
  public IndexWriter(Directory d, IndexWriterConfig conf)
      throws CorruptIndexException, LockObtainFailedException, IOException {
    config = (IndexWriterConfig) conf.clone();
    directory = d;
    analyzer = conf.getAnalyzer();
    setMessageID(defaultInfoStream);
    maxFieldLength = conf.getMaxFieldLength();
    termIndexInterval = conf.getTermIndexInterval();
    writeLockTimeout = conf.getWriteLockTimeout();
    similarity = conf.getSimilarity();
    mergeScheduler = conf.getMergeScheduler();
    mergedSegmentWarmer = conf.getMergedSegmentWarmer();
    OpenMode mode = conf.getOpenMode();
    boolean create;
    if (mode == OpenMode.CREATE) {
      create = true;
    } else if (mode == OpenMode.APPEND) {
      create = false;
    } else {
      create = !IndexReader.indexExists(directory);
    }
    if (create) {
      directory.clearLock(WRITE_LOCK_NAME);
    }
    writeLock = directory.makeLock(WRITE_LOCK_NAME);
    if (!writeLock.obtain(writeLockTimeout)) 
      throw new LockObtainFailedException("Index locked for write: " + writeLock);
    try {
      if (create) {
        boolean doCommit;
        try {
          segmentInfos.read(directory);
          segmentInfos.clear();
          doCommit = false;
        } catch (IOException e) {
          doCommit = true;
        }
        if (doCommit) {
          segmentInfos.commit(directory);
          synced.addAll(segmentInfos.files(directory, true));
        } else {
          changeCount++;
        }
      } else {
        segmentInfos.read(directory);
        IndexCommit commit = conf.getIndexCommit();
        if (commit != null) {
          if (commit.getDirectory() != directory)
            throw new IllegalArgumentException("IndexCommit's directory doesn't match my directory");
          SegmentInfos oldInfos = new SegmentInfos();
          oldInfos.read(directory, commit.getSegmentsFileName());
          segmentInfos.replace(oldInfos);
          changeCount++;
          if (infoStream != null)
            message("init: loaded commit \"" + commit.getSegmentsFileName() + "\"");
        }
        synced.addAll(segmentInfos.files(directory, true));
      }
      setRollbackSegmentInfos(segmentInfos);
      docWriter = new DocumentsWriter(directory, this, conf.getIndexingChain(), conf.getMaxThreadStates());
      docWriter.setInfoStream(infoStream);
      docWriter.setMaxFieldLength(maxFieldLength);
      deleter = new IndexFileDeleter(directory,
                                     conf.getIndexDeletionPolicy(),
                                     segmentInfos, infoStream, docWriter);
      if (deleter.startingCommitDeleted)
        changeCount++;
      docWriter.setMaxBufferedDeleteTerms(conf.getMaxBufferedDeleteTerms());
      docWriter.setRAMBufferSizeMB(conf.getRAMBufferSizeMB());
      docWriter.setMaxBufferedDocs(conf.getMaxBufferedDocs());
      pushMaxBufferedDocs();
      if (infoStream != null) {
        messageState();
      }
    } catch (IOException e) {
      writeLock.release();
      writeLock = null;
      throw e;
    }
  }
  private synchronized void setRollbackSegmentInfos(SegmentInfos infos) {
    rollbackSegmentInfos = (SegmentInfos) infos.clone();
    assert !rollbackSegmentInfos.hasExternalSegments(directory);
    rollbackSegments = new HashMap<SegmentInfo,Integer>();
    final int size = rollbackSegmentInfos.size();
    for(int i=0;i<size;i++)
      rollbackSegments.put(rollbackSegmentInfos.info(i), Integer.valueOf(i));
  }
  public IndexWriterConfig getConfig() {
    return config;
  }
  public void setMergePolicy(MergePolicy mp) {
    ensureOpen();
    if (mp == null)
      throw new NullPointerException("MergePolicy must be non-null");
    if (mergePolicy != mp)
      mergePolicy.close();
    mergePolicy = mp;
    pushMaxBufferedDocs();
    if (infoStream != null)
      message("setMergePolicy " + mp);
  }
  public MergePolicy getMergePolicy() {
    ensureOpen();
    return mergePolicy;
  }
  synchronized public void setMergeScheduler(MergeScheduler mergeScheduler) throws CorruptIndexException, IOException {
    ensureOpen();
    if (mergeScheduler == null)
      throw new NullPointerException("MergeScheduler must be non-null");
    if (this.mergeScheduler != mergeScheduler) {
      finishMerges(true);
      this.mergeScheduler.close();
    }
    this.mergeScheduler = mergeScheduler;
    if (infoStream != null)
      message("setMergeScheduler " + mergeScheduler);
    config.setMergeScheduler(mergeScheduler);
  }
  public MergeScheduler getMergeScheduler() {
    ensureOpen();
    return mergeScheduler;
  }
  public void setMaxMergeDocs(int maxMergeDocs) {
    getLogMergePolicy().setMaxMergeDocs(maxMergeDocs);
  }
  public int getMaxMergeDocs() {
    return getLogMergePolicy().getMaxMergeDocs();
  }
  public void setMaxFieldLength(int maxFieldLength) {
    ensureOpen();
    this.maxFieldLength = maxFieldLength;
    docWriter.setMaxFieldLength(maxFieldLength);
    if (infoStream != null)
      message("setMaxFieldLength " + maxFieldLength);
    config.setMaxFieldLength(maxFieldLength);
  }
  public int getMaxFieldLength() {
    ensureOpen();
    return maxFieldLength;
  }
  public void setMaxBufferedDocs(int maxBufferedDocs) {
    ensureOpen();
    if (maxBufferedDocs != DISABLE_AUTO_FLUSH && maxBufferedDocs < 2)
      throw new IllegalArgumentException(
          "maxBufferedDocs must at least be 2 when enabled");
    if (maxBufferedDocs == DISABLE_AUTO_FLUSH
        && getRAMBufferSizeMB() == DISABLE_AUTO_FLUSH)
      throw new IllegalArgumentException(
          "at least one of ramBufferSize and maxBufferedDocs must be enabled");
    docWriter.setMaxBufferedDocs(maxBufferedDocs);
    pushMaxBufferedDocs();
    if (infoStream != null)
      message("setMaxBufferedDocs " + maxBufferedDocs);
    config.setMaxBufferedDocs(maxBufferedDocs);
  }
  private void pushMaxBufferedDocs() {
    if (docWriter.getMaxBufferedDocs() != DISABLE_AUTO_FLUSH) {
      final MergePolicy mp = mergePolicy;
      if (mp instanceof LogDocMergePolicy) {
        LogDocMergePolicy lmp = (LogDocMergePolicy) mp;
        final int maxBufferedDocs = docWriter.getMaxBufferedDocs();
        if (lmp.getMinMergeDocs() != maxBufferedDocs) {
          if (infoStream != null)
            message("now push maxBufferedDocs " + maxBufferedDocs + " to LogDocMergePolicy");
          lmp.setMinMergeDocs(maxBufferedDocs);
        }
      }
    }
  }
  public int getMaxBufferedDocs() {
    ensureOpen();
    return docWriter.getMaxBufferedDocs();
  }
  public void setRAMBufferSizeMB(double mb) {
    if (mb > 2048.0) {
      throw new IllegalArgumentException("ramBufferSize " + mb + " is too large; should be comfortably less than 2048");
    }
    if (mb != DISABLE_AUTO_FLUSH && mb <= 0.0)
      throw new IllegalArgumentException(
          "ramBufferSize should be > 0.0 MB when enabled");
    if (mb == DISABLE_AUTO_FLUSH && getMaxBufferedDocs() == DISABLE_AUTO_FLUSH)
      throw new IllegalArgumentException(
          "at least one of ramBufferSize and maxBufferedDocs must be enabled");
    docWriter.setRAMBufferSizeMB(mb);
    if (infoStream != null)
      message("setRAMBufferSizeMB " + mb);
    config.setRAMBufferSizeMB(mb);
  }
  public double getRAMBufferSizeMB() {
    return docWriter.getRAMBufferSizeMB();
  }
  public void setMaxBufferedDeleteTerms(int maxBufferedDeleteTerms) {
    ensureOpen();
    if (maxBufferedDeleteTerms != DISABLE_AUTO_FLUSH
        && maxBufferedDeleteTerms < 1)
      throw new IllegalArgumentException(
          "maxBufferedDeleteTerms must at least be 1 when enabled");
    docWriter.setMaxBufferedDeleteTerms(maxBufferedDeleteTerms);
    if (infoStream != null)
      message("setMaxBufferedDeleteTerms " + maxBufferedDeleteTerms);
    config.setMaxBufferedDeleteTerms(maxBufferedDeleteTerms);
  }
  public int getMaxBufferedDeleteTerms() {
    ensureOpen();
    return docWriter.getMaxBufferedDeleteTerms();
  }
  public void setMergeFactor(int mergeFactor) {
    getLogMergePolicy().setMergeFactor(mergeFactor);
  }
  public int getMergeFactor() {
    return getLogMergePolicy().getMergeFactor();
  }
  public static void setDefaultInfoStream(PrintStream infoStream) {
    IndexWriter.defaultInfoStream = infoStream;
  }
  public static PrintStream getDefaultInfoStream() {
    return IndexWriter.defaultInfoStream;
  }
  public void setInfoStream(PrintStream infoStream) {
    ensureOpen();
    setMessageID(infoStream);
    docWriter.setInfoStream(infoStream);
    deleter.setInfoStream(infoStream);
    if (infoStream != null)
      messageState();
  }
  private void messageState() {
    message("\ndir=" + directory + "\n" +
            "mergePolicy=" + mergePolicy + "\n" + 
            "index=" + segString() + "\n" +
            "version=" + Constants.LUCENE_VERSION + "\n" +
            config.toString());
  }
  public PrintStream getInfoStream() {
    ensureOpen();
    return infoStream;
  }
  public boolean verbose() {
    return infoStream != null;
  }
  public void setWriteLockTimeout(long writeLockTimeout) {
    ensureOpen();
    this.writeLockTimeout = writeLockTimeout;
    config.setWriteLockTimeout(writeLockTimeout);
  }
  public long getWriteLockTimeout() {
    ensureOpen();
    return writeLockTimeout;
  }
  public static void setDefaultWriteLockTimeout(long writeLockTimeout) {
    IndexWriterConfig.setDefaultWriteLockTimeout(writeLockTimeout);
  }
  public static long getDefaultWriteLockTimeout() {
    return IndexWriterConfig.getDefaultWriteLockTimeout();
  }
  public void close() throws CorruptIndexException, IOException {
    close(true);
  }
  public void close(boolean waitForMerges) throws CorruptIndexException, IOException {
    if (shouldClose()) {
      if (hitOOM)
        rollbackInternal();
      else
        closeInternal(waitForMerges);
    }
  }
  synchronized private boolean shouldClose() {
    while(true) {
      if (!closed) {
        if (!closing) {
          closing = true;
          return true;
        } else {
          doWait();
        }
      } else
        return false;
    }
  }
  private void closeInternal(boolean waitForMerges) throws CorruptIndexException, IOException {
    docWriter.pauseAllThreads();
    try {
      if (infoStream != null)
        message("now flush at close");
      docWriter.close();
      if (!hitOOM) {
        flush(waitForMerges, true, true);
      }
      if (waitForMerges)
        mergeScheduler.merge(this);
      mergePolicy.close();
      finishMerges(waitForMerges);
      stopMerges = true;
      mergeScheduler.close();
      if (infoStream != null)
        message("now call final commit()");
      if (!hitOOM) {
        commit(0);
      }
      if (infoStream != null)
        message("at close: " + segString());
      synchronized(this) {
        readerPool.close();
        docWriter = null;
        deleter.close();
      }
      if (writeLock != null) {
        writeLock.release();                          
        writeLock = null;
      }
      synchronized(this) {
        closed = true;
      }
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "closeInternal");
    } finally {
      synchronized(this) {
        closing = false;
        notifyAll();
        if (!closed) {
          if (docWriter != null)
            docWriter.resumeAllThreads();
          if (infoStream != null)
            message("hit exception while closing");
        }
      }
    }
  }
  private synchronized boolean flushDocStores() throws IOException {
    boolean useCompoundDocStore = false;
    String docStoreSegment;
    boolean success = false;
    try {
      docStoreSegment = docWriter.closeDocStore();
      success = true;
    } finally {
      if (!success && infoStream != null) {
        message("hit exception closing doc store segment");
      }
    }
    useCompoundDocStore = mergePolicy.useCompoundDocStore(segmentInfos);
    if (useCompoundDocStore && docStoreSegment != null && docWriter.closedFiles().size() != 0) {
      if (infoStream != null) {
        message("create compound file " + IndexFileNames.segmentFileName(docStoreSegment, IndexFileNames.COMPOUND_FILE_STORE_EXTENSION));
      }
      success = false;
      final int numSegments = segmentInfos.size();
      final String compoundFileName = IndexFileNames.segmentFileName(docStoreSegment, IndexFileNames.COMPOUND_FILE_STORE_EXTENSION);
      try {
        CompoundFileWriter cfsWriter = new CompoundFileWriter(directory, compoundFileName);
        for (final String file :  docWriter.closedFiles() ) {
          cfsWriter.addFile(file);
        }
        cfsWriter.close();
        success = true;
      } finally {
        if (!success) {
          if (infoStream != null)
            message("hit exception building compound file doc store for segment " + docStoreSegment);
          deleter.deleteFile(compoundFileName);
          docWriter.abort();
        }
      }
      for(int i=0;i<numSegments;i++) {
        SegmentInfo si = segmentInfos.info(i);
        if (si.getDocStoreOffset() != -1 &&
            si.getDocStoreSegment().equals(docStoreSegment))
          si.setDocStoreIsCompoundFile(true);
      }
      checkpoint();
      deleter.deleteNewFiles(docWriter.closedFiles());
    }
    return useCompoundDocStore;
  }
  public Directory getDirectory() {     
    ensureOpen(false);
    return directory;
  }
  public Analyzer getAnalyzer() {
    ensureOpen();
    return analyzer;
  }
  public synchronized int maxDoc() {
    int count;
    if (docWriter != null)
      count = docWriter.getNumDocsInRAM();
    else
      count = 0;
    for (int i = 0; i < segmentInfos.size(); i++)
      count += segmentInfos.info(i).docCount;
    return count;
  }
  public synchronized int numDocs() throws IOException {
    int count;
    if (docWriter != null)
      count = docWriter.getNumDocsInRAM();
    else
      count = 0;
    for (int i = 0; i < segmentInfos.size(); i++) {
      final SegmentInfo info = segmentInfos.info(i);
      count += info.docCount - info.getDelCount();
    }
    return count;
  }
  public synchronized boolean hasDeletions() throws IOException {
    ensureOpen();
    if (docWriter.hasDeletes())
      return true;
    for (int i = 0; i < segmentInfos.size(); i++)
      if (segmentInfos.info(i).hasDeletions())
        return true;
    return false;
  }
  private int maxFieldLength;
  public void addDocument(Document doc) throws CorruptIndexException, IOException {
    addDocument(doc, analyzer);
  }
  public void addDocument(Document doc, Analyzer analyzer) throws CorruptIndexException, IOException {
    ensureOpen();
    boolean doFlush = false;
    boolean success = false;
    try {
      try {
        doFlush = docWriter.addDocument(doc, analyzer);
        success = true;
      } finally {
        if (!success) {
          if (infoStream != null)
            message("hit exception adding document");
          synchronized (this) {
            if (docWriter != null) {
              final Collection<String> files = docWriter.abortedFiles();
              if (files != null)
                deleter.deleteNewFiles(files);
            }
          }
        }
      }
      if (doFlush)
        flush(true, false, false);
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "addDocument");
    }
  }
  public void deleteDocuments(Term term) throws CorruptIndexException, IOException {
    ensureOpen();
    try {
      boolean doFlush = docWriter.bufferDeleteTerm(term);
      if (doFlush)
        flush(true, false, false);
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "deleteDocuments(Term)");
    }
  }
  public void deleteDocuments(Term... terms) throws CorruptIndexException, IOException {
    ensureOpen();
    try {
      boolean doFlush = docWriter.bufferDeleteTerms(terms);
      if (doFlush)
        flush(true, false, false);
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "deleteDocuments(Term..)");
    }
  }
  public void deleteDocuments(Query query) throws CorruptIndexException, IOException {
    ensureOpen();
    boolean doFlush = docWriter.bufferDeleteQuery(query);
    if (doFlush)
      flush(true, false, false);
  }
  public void deleteDocuments(Query... queries) throws CorruptIndexException, IOException {
    ensureOpen();
    boolean doFlush = docWriter.bufferDeleteQueries(queries);
    if (doFlush)
      flush(true, false, false);
  }
  public void updateDocument(Term term, Document doc) throws CorruptIndexException, IOException {
    ensureOpen();
    updateDocument(term, doc, getAnalyzer());
  }
  public void updateDocument(Term term, Document doc, Analyzer analyzer)
      throws CorruptIndexException, IOException {
    ensureOpen();
    try {
      boolean doFlush = false;
      boolean success = false;
      try {
        doFlush = docWriter.updateDocument(term, doc, analyzer);
        success = true;
      } finally {
        if (!success) {
          if (infoStream != null)
            message("hit exception updating document");
          synchronized (this) {
            final Collection<String> files = docWriter.abortedFiles();
            if (files != null)
              deleter.deleteNewFiles(files);
          }
        }
      }
      if (doFlush)
        flush(true, false, false);
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "updateDocument");
    }
  }
  final synchronized int getSegmentCount(){
    return segmentInfos.size();
  }
  final synchronized int getNumBufferedDocuments(){
    return docWriter.getNumDocsInRAM();
  }
  final synchronized int getDocCount(int i) {
    if (i >= 0 && i < segmentInfos.size()) {
      return segmentInfos.info(i).docCount;
    } else {
      return -1;
    }
  }
  final synchronized int getFlushCount() {
    return flushCount;
  }
  final synchronized int getFlushDeletesCount() {
    return flushDeletesCount;
  }
  final String newSegmentName() {
    synchronized(segmentInfos) {
      changeCount++;
      return "_" + Integer.toString(segmentInfos.counter++, Character.MAX_RADIX);
    }
  }
  private PrintStream infoStream = null;
  private static PrintStream defaultInfoStream = null;
  public void optimize() throws CorruptIndexException, IOException {
    optimize(true);
  }
  public void optimize(int maxNumSegments) throws CorruptIndexException, IOException {
    optimize(maxNumSegments, true);
  }
  public void optimize(boolean doWait) throws CorruptIndexException, IOException {
    optimize(1, doWait);
  }
  public void optimize(int maxNumSegments, boolean doWait) throws CorruptIndexException, IOException {
    ensureOpen();
    if (maxNumSegments < 1)
      throw new IllegalArgumentException("maxNumSegments must be >= 1; got " + maxNumSegments);
    if (infoStream != null)
      message("optimize: index now " + segString());
    flush(true, false, true);
    synchronized(this) {
      resetMergeExceptions();
      segmentsToOptimize = new HashSet<SegmentInfo>();
      final int numSegments = segmentInfos.size();
      for(int i=0;i<numSegments;i++)
        segmentsToOptimize.add(segmentInfos.info(i));
      for(final MergePolicy.OneMerge merge  : pendingMerges) {
        merge.optimize = true;
        merge.maxNumSegmentsOptimize = maxNumSegments;
      }
      for ( final MergePolicy.OneMerge merge: runningMerges ) {
        merge.optimize = true;
        merge.maxNumSegmentsOptimize = maxNumSegments;
      }
    }
    maybeMerge(maxNumSegments, true);
    if (doWait) {
      synchronized(this) {
        while(true) {
          if (hitOOM) {
            throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot complete optimize");
          }
          if (mergeExceptions.size() > 0) {
            final int size = mergeExceptions.size();
            for(int i=0;i<size;i++) {
              final MergePolicy.OneMerge merge = mergeExceptions.get(i);
              if (merge.optimize) {
                IOException err = new IOException("background merge hit exception: " + merge.segString(directory));
                final Throwable t = merge.getException();
                if (t != null)
                  err.initCause(t);
                throw err;
              }
            }
          }
          if (optimizeMergesPending())
            doWait();
          else
            break;
        }
      }
      ensureOpen();
    }
  }
  private synchronized boolean optimizeMergesPending() {
    for (final MergePolicy.OneMerge merge : pendingMerges) {
      if (merge.optimize)
        return true;
    }
    for (final MergePolicy.OneMerge merge : runningMerges) {
      if (merge.optimize)
        return true;
    }
    return false;
  }
  public void expungeDeletes(boolean doWait)
    throws CorruptIndexException, IOException {
    ensureOpen();
    if (infoStream != null)
      message("expungeDeletes: index now " + segString());
    MergePolicy.MergeSpecification spec;
    synchronized(this) {
      spec = mergePolicy.findMergesToExpungeDeletes(segmentInfos);
      if (spec != null) {
        final int numMerges = spec.merges.size();
        for(int i=0;i<numMerges;i++)
          registerMerge(spec.merges.get(i));
      }
    }
    mergeScheduler.merge(this);
    if (spec != null && doWait) {
      final int numMerges = spec.merges.size();
      synchronized(this) {
        boolean running = true;
        while(running) {
          if (hitOOM) {
            throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot complete expungeDeletes");
          }
          running = false;
          for(int i=0;i<numMerges;i++) {
            final MergePolicy.OneMerge merge = spec.merges.get(i);
            if (pendingMerges.contains(merge) || runningMerges.contains(merge))
              running = true;
            Throwable t = merge.getException();
            if (t != null) {
              IOException ioe = new IOException("background merge hit exception: " + merge.segString(directory));
              ioe.initCause(t);
              throw ioe;
            }
          }
          if (running)
            doWait();
        }
      }
    }
  }
  public void expungeDeletes() throws CorruptIndexException, IOException {
    expungeDeletes(true);
  }
  public final void maybeMerge() throws CorruptIndexException, IOException {
    maybeMerge(false);
  }
  private final void maybeMerge(boolean optimize) throws CorruptIndexException, IOException {
    maybeMerge(1, optimize);
  }
  private final void maybeMerge(int maxNumSegmentsOptimize, boolean optimize) throws CorruptIndexException, IOException {
    updatePendingMerges(maxNumSegmentsOptimize, optimize);
    mergeScheduler.merge(this);
  }
  private synchronized void updatePendingMerges(int maxNumSegmentsOptimize, boolean optimize)
    throws CorruptIndexException, IOException {
    assert !optimize || maxNumSegmentsOptimize > 0;
    if (stopMerges)
      return;
    if (hitOOM) {
      return;
    }
    final MergePolicy.MergeSpecification spec;
    if (optimize) {
      spec = mergePolicy.findMergesForOptimize(segmentInfos, maxNumSegmentsOptimize, segmentsToOptimize);
      if (spec != null) {
        final int numMerges = spec.merges.size();
        for(int i=0;i<numMerges;i++) {
          final MergePolicy.OneMerge merge = ( spec.merges.get(i));
          merge.optimize = true;
          merge.maxNumSegmentsOptimize = maxNumSegmentsOptimize;
        }
      }
    } else
      spec = mergePolicy.findMerges(segmentInfos);
    if (spec != null) {
      final int numMerges = spec.merges.size();
      for(int i=0;i<numMerges;i++)
        registerMerge(spec.merges.get(i));
    }
  }
  synchronized MergePolicy.OneMerge getNextMerge() {
    if (pendingMerges.size() == 0)
      return null;
    else {
      MergePolicy.OneMerge merge = pendingMerges.removeFirst();
      runningMerges.add(merge);
      return merge;
    }
  }
  private synchronized MergePolicy.OneMerge getNextExternalMerge() {
    if (pendingMerges.size() == 0)
      return null;
    else {
      Iterator<MergePolicy.OneMerge> it = pendingMerges.iterator();
      while(it.hasNext()) {
        MergePolicy.OneMerge merge = it.next();
        if (merge.isExternal) {
          it.remove();
          runningMerges.add(merge);
          return merge;
        }
      }
      return null;
    }
  }
  private synchronized void startTransaction(boolean haveReadLock) throws IOException {
    boolean success = false;
    try {
      if (infoStream != null)
        message("now start transaction");
      assert docWriter.getNumBufferedDeleteTerms() == 0 :
      "calling startTransaction with buffered delete terms not supported: numBufferedDeleteTerms=" + docWriter.getNumBufferedDeleteTerms();
      assert docWriter.getNumDocsInRAM() == 0 :
      "calling startTransaction with buffered documents not supported: numDocsInRAM=" + docWriter.getNumDocsInRAM();
      ensureOpen();
      synchronized(this) {
        while(stopMerges)
          doWait();
      }
      success = true;
    } finally {
      if (!success && haveReadLock)
        releaseRead();
    }
    if (haveReadLock) {
      upgradeReadToWrite();
    } else {
      acquireWrite();
    }
    success = false;
    try {
      localRollbackSegmentInfos = (SegmentInfos) segmentInfos.clone();
      assert !hasExternalSegments();
      localFlushedDocCount = docWriter.getFlushedDocCount();
      deleter.incRef(segmentInfos, false);
      success = true;
    } finally {
      if (!success)
        finishAddIndexes();
    }
  }
  private synchronized void rollbackTransaction() throws IOException {
    if (infoStream != null)
      message("now rollback transaction");
    if (docWriter != null) {
      docWriter.setFlushedDocCount(localFlushedDocCount);
    }
    finishMerges(false);
    segmentInfos.clear();
    segmentInfos.addAll(localRollbackSegmentInfos);
    localRollbackSegmentInfos = null;
    finishAddIndexes();
    deleter.checkpoint(segmentInfos, false);
    deleter.decRef(segmentInfos);
    deleter.refresh();
    notifyAll();
    assert !hasExternalSegments();
  }
  private synchronized void commitTransaction() throws IOException {
    if (infoStream != null)
      message("now commit transaction");
    checkpoint();
    deleter.decRef(localRollbackSegmentInfos);
    localRollbackSegmentInfos = null;
    assert !hasExternalSegments();
    finishAddIndexes();
  }
  public void rollback() throws IOException {
    ensureOpen();
    if (shouldClose())
      rollbackInternal();
  }
  private void rollbackInternal() throws IOException {
    boolean success = false;
    docWriter.pauseAllThreads();
    try {
      finishMerges(false);
      mergePolicy.close();
      mergeScheduler.close();
      synchronized(this) {
        if (pendingCommit != null) {
          pendingCommit.rollbackCommit(directory);
          deleter.decRef(pendingCommit);
          pendingCommit = null;
          notifyAll();
        }
        segmentInfos.clear();
        segmentInfos.addAll(rollbackSegmentInfos);
        assert !hasExternalSegments();
        docWriter.abort();
        assert testPoint("rollback before checkpoint");
        deleter.checkpoint(segmentInfos, false);
        deleter.refresh();
      }
      readerPool.clear(null);
      lastCommitChangeCount = changeCount;
      success = true;
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "rollbackInternal");
    } finally {
      synchronized(this) {
        if (!success) {
          docWriter.resumeAllThreads();
          closing = false;
          notifyAll();
          if (infoStream != null)
            message("hit exception during rollback");
        }
      }
    }
    closeInternal(false);
  }
  public synchronized void deleteAll() throws IOException {
    docWriter.pauseAllThreads();
    try {
      finishMerges(false);
      docWriter.abort();
      docWriter.setFlushedDocCount(0);
      segmentInfos.clear();
      deleter.checkpoint(segmentInfos, false);
      deleter.refresh();
      readerPool.clear(null);      
      ++changeCount;
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "deleteAll");
    } finally {
      docWriter.resumeAllThreads();
      if (infoStream != null) {
        message("hit exception during deleteAll");
      }
    }
  }
  private synchronized void finishMerges(boolean waitForMerges) throws IOException {
    if (!waitForMerges) {
      stopMerges = true;
      for (final MergePolicy.OneMerge merge : pendingMerges) {
        if (infoStream != null)
          message("now abort pending merge " + merge.segString(directory));
        merge.abort();
        mergeFinish(merge);
      }
      pendingMerges.clear();
      for (final MergePolicy.OneMerge merge : runningMerges) {
        if (infoStream != null)
          message("now abort running merge " + merge.segString(directory));
        merge.abort();
      }
      acquireRead();
      releaseRead();
      while(runningMerges.size() > 0) {
        if (infoStream != null)
          message("now wait for " + runningMerges.size() + " running merge to abort");
        doWait();
      }
      stopMerges = false;
      notifyAll();
      assert 0 == mergingSegments.size();
      if (infoStream != null)
        message("all running merges have aborted");
    } else {
      waitForMerges();
    }
  }
  public synchronized void waitForMerges() {
    acquireRead();
    releaseRead();
    while(pendingMerges.size() > 0 || runningMerges.size() > 0) {
      doWait();
    }
    assert 0 == mergingSegments.size();
  }
  private synchronized void checkpoint() throws IOException {
    changeCount++;
    deleter.checkpoint(segmentInfos, false);
  }
  private void finishAddIndexes() {
    releaseWrite();
  }
  private void blockAddIndexes() {
    acquireRead();
    boolean success = false;
    try {
      ensureOpen(false);
      success = true;
    } finally {
      if (!success)
        releaseRead();
    }
  }
  private void resumeAddIndexes() {
    releaseRead();
  }
  private synchronized void resetMergeExceptions() {
    mergeExceptions = new ArrayList<MergePolicy.OneMerge>();
    mergeGen++;
  }
  private void noDupDirs(Directory... dirs) {
    HashSet<Directory> dups = new HashSet<Directory>();
    for(int i=0;i<dirs.length;i++) {
      if (dups.contains(dirs[i]))
        throw new IllegalArgumentException("Directory " + dirs[i] + " appears more than once");
      if (dirs[i] == directory)
        throw new IllegalArgumentException("Cannot add directory to itself");
      dups.add(dirs[i]);
    }
  }
  public void addIndexesNoOptimize(Directory... dirs)
      throws CorruptIndexException, IOException {
    ensureOpen();
    noDupDirs(dirs);
    docWriter.pauseAllThreads();
    try {
      if (infoStream != null)
        message("flush at addIndexesNoOptimize");
      flush(true, false, true);
      boolean success = false;
      startTransaction(false);
      try {
        int docCount = 0;
        synchronized(this) {
          ensureOpen();
          for (int i = 0; i < dirs.length; i++) {
            if (directory == dirs[i]) {
              throw new IllegalArgumentException("Cannot add this index to itself");
            }
            SegmentInfos sis = new SegmentInfos(); 
            sis.read(dirs[i]);
            for (int j = 0; j < sis.size(); j++) {
              SegmentInfo info = sis.info(j);
              assert !segmentInfos.contains(info): "dup info dir=" + info.dir + " name=" + info.name;
              docCount += info.docCount;
              segmentInfos.add(info); 
            }
          }
        }
        docWriter.updateFlushedDocCount(docCount);
        maybeMerge();
        ensureOpen();
        resolveExternalSegments();
        ensureOpen();
        success = true;
      } finally {
        if (success) {
          commitTransaction();
        } else {
          rollbackTransaction();
        }
      }
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "addIndexesNoOptimize");
    } finally {
      if (docWriter != null) {
        docWriter.resumeAllThreads();
      }
    }
  }
  private boolean hasExternalSegments() {
    return segmentInfos.hasExternalSegments(directory);
  }
  private void resolveExternalSegments() throws CorruptIndexException, IOException {
    boolean any = false;
    boolean done = false;
    while(!done) {
      SegmentInfo info = null;
      MergePolicy.OneMerge merge = null;
      synchronized(this) {
        if (stopMerges)
          throw new MergePolicy.MergeAbortedException("rollback() was called or addIndexes* hit an unhandled exception");
        final int numSegments = segmentInfos.size();
        done = true;
        for(int i=0;i<numSegments;i++) {
          info = segmentInfos.info(i);
          if (info.dir != directory) {
            done = false;
            final MergePolicy.OneMerge newMerge = new MergePolicy.OneMerge(segmentInfos.range(i, 1+i), mergePolicy instanceof LogMergePolicy && getUseCompoundFile());
            if (registerMerge(newMerge)) {
              merge = newMerge;
              pendingMerges.remove(merge);
              runningMerges.add(merge);
              break;
            }
          }
        }
        if (!done && merge == null)
          merge = getNextExternalMerge();
        if (!done && merge == null)
          doWait();
      }
      if (merge != null) {
        any = true;
        merge(merge);
      }
    }
    if (any)
      mergeScheduler.merge(this);
  }
  public void addIndexes(IndexReader... readers)
    throws CorruptIndexException, IOException {
    ensureOpen();
    docWriter.pauseAllThreads();
    acquireRead();
    try {
      SegmentInfo info = null;
      String mergedName = null;
      SegmentMerger merger = null;
      boolean success = false;
      try {
        flush(true, false, true);
        optimize();					  
        success = true;
      } finally {
        if (!success)
          releaseRead();
      }
      startTransaction(true);
      try {
        mergedName = newSegmentName();
        merger = new SegmentMerger(this, mergedName, null);
        SegmentReader sReader = null;
        synchronized(this) {
          if (segmentInfos.size() == 1) { 
            sReader = readerPool.get(segmentInfos.info(0), true, BufferedIndexInput.BUFFER_SIZE, -1);
          }
        }
        success = false;
        try {
          if (sReader != null)
            merger.add(sReader);
          for (int i = 0; i < readers.length; i++)      
            merger.add(readers[i]);
          int docCount = merger.merge();                
          synchronized(this) {
            segmentInfos.clear();                      
            info = new SegmentInfo(mergedName, docCount, directory, false, true,
                                   -1, null, false, merger.hasProx());
            setDiagnostics(info, "addIndexes(IndexReader...)");
            segmentInfos.add(info);
          }
          docWriter.updateFlushedDocCount(docCount);
          success = true;
        } finally {
          if (sReader != null) {
            readerPool.release(sReader);
          }
        }
      } finally {
        if (!success) {
          if (infoStream != null)
            message("hit exception in addIndexes during merge");
          rollbackTransaction();
        } else {
          commitTransaction();
        }
      }
      if (mergePolicy instanceof LogMergePolicy && getUseCompoundFile()) {
        List<String> files = null;
        synchronized(this) {
          if (segmentInfos.contains(info)) {
            files = info.files();
            deleter.incRef(files);
          }
        }
        if (files != null) {
          success = false;
          startTransaction(false);
          try {
            merger.createCompoundFile(mergedName + ".cfs");
            synchronized(this) {
              info.setUseCompoundFile(true);
            }
            success = true;
          } finally {
            deleter.decRef(files);
            if (!success) {
              if (infoStream != null)
                message("hit exception building compound file in addIndexes during merge");
              rollbackTransaction();
            } else {
              commitTransaction();
            }
          }
        }
      }
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "addIndexes(IndexReader...)");
    } finally {
      if (docWriter != null) {
        docWriter.resumeAllThreads();
      }
    }
  }
  protected void doAfterFlush() throws IOException {}
  protected void doBeforeFlush() throws IOException {}
  public final void prepareCommit() throws CorruptIndexException, IOException {
    ensureOpen();
    prepareCommit(null);
  }
  public final void prepareCommit(Map<String,String> commitUserData) throws CorruptIndexException, IOException {
    if (hitOOM) {
      throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot commit");
    }
    if (pendingCommit != null)
      throw new IllegalStateException("prepareCommit was already called with no corresponding call to commit");
    if (infoStream != null)
      message("prepareCommit: flush");
    flush(true, true, true);
    startCommit(0, commitUserData);
  }
  private final Object commitLock = new Object();
  private void commit(long sizeInBytes) throws IOException {
    synchronized(commitLock) {
      startCommit(sizeInBytes, null);
      finishCommit();
    }
  }
  public final void commit() throws CorruptIndexException, IOException {
    commit(null);
  }
  public final void commit(Map<String,String> commitUserData) throws CorruptIndexException, IOException {
    ensureOpen();
    if (infoStream != null) {
      message("commit: start");
    }
    synchronized(commitLock) {
      if (infoStream != null) {
        message("commit: enter lock");
      }
      if (pendingCommit == null) {
        if (infoStream != null) {
          message("commit: now prepare");
        }
        prepareCommit(commitUserData);
      } else if (infoStream != null) {
        message("commit: already prepared");
      }
      finishCommit();
    }
  }
  private synchronized final void finishCommit() throws CorruptIndexException, IOException {
    if (pendingCommit != null) {
      try {
        if (infoStream != null)
    	  message("commit: pendingCommit != null");
        pendingCommit.finishCommit(directory);
        if (infoStream != null)
          message("commit: wrote segments file \"" + pendingCommit.getCurrentSegmentFileName() + "\"");
        lastCommitChangeCount = pendingCommitChangeCount;
        segmentInfos.updateGeneration(pendingCommit);
        segmentInfos.setUserData(pendingCommit.getUserData());
        setRollbackSegmentInfos(pendingCommit);
        deleter.checkpoint(pendingCommit, true);
      } finally {
        deleter.decRef(pendingCommit);
        pendingCommit = null;
        notifyAll();
      }
    } else if (infoStream != null)
        message("commit: pendingCommit == null; skip");
    if (infoStream != null)
      message("commit: done");
  }
  protected final void flush(boolean triggerMerge, boolean flushDocStores, boolean flushDeletes) throws CorruptIndexException, IOException {
    ensureOpen(false);
    if (doFlush(flushDocStores, flushDeletes) && triggerMerge)
      maybeMerge();
  }
  private synchronized final boolean doFlush(boolean flushDocStores, boolean flushDeletes) throws CorruptIndexException, IOException {
    try {
      return doFlushInternal(flushDocStores, flushDeletes);
    } finally {
      docWriter.clearFlushPending();
    }
  }
  private synchronized final boolean doFlushInternal(boolean flushDocStores, boolean flushDeletes) throws CorruptIndexException, IOException {
    if (hitOOM) {
      throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot flush");
    }
    ensureOpen(false);
    assert testPoint("startDoFlush");
    doBeforeFlush();
    flushCount++;
    flushDeletes |= docWriter.doApplyDeletes();
    if (docWriter.pauseAllThreads()) {
      docWriter.resumeAllThreads();
      return false;
    }
    try {
      SegmentInfo newSegment = null;
      final int numDocs = docWriter.getNumDocsInRAM();
      boolean flushDocs = numDocs > 0;
      String docStoreSegment = docWriter.getDocStoreSegment();
      assert docStoreSegment != null || numDocs == 0: "dss=" + docStoreSegment + " numDocs=" + numDocs;
      if (docStoreSegment == null)
        flushDocStores = false;
      int docStoreOffset = docWriter.getDocStoreOffset();
      boolean docStoreIsCompoundFile = false;
      if (infoStream != null) {
        message("  flush: segment=" + docWriter.getSegment() +
                " docStoreSegment=" + docWriter.getDocStoreSegment() +
                " docStoreOffset=" + docStoreOffset +
                " flushDocs=" + flushDocs +
                " flushDeletes=" + flushDeletes +
                " flushDocStores=" + flushDocStores +
                " numDocs=" + numDocs +
                " numBufDelTerms=" + docWriter.getNumBufferedDeleteTerms());
        message("  index before flush " + segString());
      }
      if (flushDocStores && (!flushDocs || !docWriter.getSegment().equals(docWriter.getDocStoreSegment()))) {
        if (infoStream != null)
          message("  flush shared docStore segment " + docStoreSegment);
        docStoreIsCompoundFile = flushDocStores();
        flushDocStores = false;
      }
      String segment = docWriter.getSegment();
      assert segment != null || !flushDocs;
      if (flushDocs) {
        boolean success = false;
        final int flushedDocCount;
        try {
          flushedDocCount = docWriter.flush(flushDocStores);
          success = true;
        } finally {
          if (!success) {
            if (infoStream != null)
              message("hit exception flushing segment " + segment);
            deleter.refresh(segment);
          }
        }
        if (0 == docStoreOffset && flushDocStores) {
          assert docStoreSegment != null;
          assert docStoreSegment.equals(segment);
          docStoreOffset = -1;
          docStoreIsCompoundFile = false;
          docStoreSegment = null;
        }
        newSegment = new SegmentInfo(segment,
                                     flushedDocCount,
                                     directory, false, true,
                                     docStoreOffset, docStoreSegment,
                                     docStoreIsCompoundFile,    
                                     docWriter.hasProx());
        setDiagnostics(newSegment, "flush");
      }
      docWriter.pushDeletes();
      if (flushDocs) {
        segmentInfos.add(newSegment);
        checkpoint();
      }
      if (flushDocs && mergePolicy.useCompoundFile(segmentInfos, newSegment)) {
        boolean success = false;
        try {
          docWriter.createCompoundFile(segment);
          success = true;
        } finally {
          if (!success) {
            if (infoStream != null)
              message("hit exception creating compound file for newly flushed segment " + segment);
            deleter.deleteFile(IndexFileNames.segmentFileName(segment, IndexFileNames.COMPOUND_FILE_EXTENSION));
          }
        }
        newSegment.setUseCompoundFile(true);
        checkpoint();
      }
      if (flushDeletes) {
        applyDeletes();
      }
      if (flushDocs)
        checkpoint();
      doAfterFlush();
      return flushDocs;
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "doFlush");
      return false;
    } finally {
      docWriter.resumeAllThreads();
    }
  }
  public final long ramSizeInBytes() {
    ensureOpen();
    return docWriter.getRAMUsed();
  }
  public final synchronized int numRamDocs() {
    ensureOpen();
    return docWriter.getNumDocsInRAM();
  }
  private int ensureContiguousMerge(MergePolicy.OneMerge merge) {
    int first = segmentInfos.indexOf(merge.segments.info(0));
    if (first == -1)
      throw new MergePolicy.MergeException("could not find segment " + merge.segments.info(0).name + " in current index " + segString(), directory);
    final int numSegments = segmentInfos.size();
    final int numSegmentsToMerge = merge.segments.size();
    for(int i=0;i<numSegmentsToMerge;i++) {
      final SegmentInfo info = merge.segments.info(i);
      if (first + i >= numSegments || !segmentInfos.info(first+i).equals(info)) {
        if (segmentInfos.indexOf(info) == -1)
          throw new MergePolicy.MergeException("MergePolicy selected a segment (" + info.name + ") that is not in the current index " + segString(), directory);
        else
          throw new MergePolicy.MergeException("MergePolicy selected non-contiguous segments to merge (" + merge.segString(directory) + " vs " + segString() + "), which IndexWriter (currently) cannot handle",
                                               directory);
      }
    }
    return first;
  }
  synchronized private void commitMergedDeletes(MergePolicy.OneMerge merge, SegmentReader mergeReader) throws IOException {
    assert testPoint("startCommitMergeDeletes");
    final SegmentInfos sourceSegments = merge.segments;
    if (infoStream != null)
      message("commitMergeDeletes " + merge.segString(directory));
    int docUpto = 0;
    int delCount = 0;
    for(int i=0; i < sourceSegments.size(); i++) {
      SegmentInfo info = sourceSegments.info(i);
      int docCount = info.docCount;
      SegmentReader previousReader = merge.readersClone[i];
      SegmentReader currentReader = merge.readers[i];
      if (previousReader.hasDeletions()) {
        if (currentReader.numDeletedDocs() > previousReader.numDeletedDocs()) {
          for(int j=0;j<docCount;j++) {
            if (previousReader.isDeleted(j))
              assert currentReader.isDeleted(j);
            else {
              if (currentReader.isDeleted(j)) {
                mergeReader.doDelete(docUpto);
                delCount++;
              }
              docUpto++;
            }
          }
        } else {
          docUpto += docCount - previousReader.numDeletedDocs();
        }
      } else if (currentReader.hasDeletions()) {
        for(int j=0; j<docCount; j++) {
          if (currentReader.isDeleted(j)) {
            mergeReader.doDelete(docUpto);
            delCount++;
          }
          docUpto++;
        }
      } else
        docUpto += info.docCount;
    }
    assert mergeReader.numDeletedDocs() == delCount;
    mergeReader.hasChanges = delCount >= 0;
  }
  synchronized private boolean commitMerge(MergePolicy.OneMerge merge, SegmentMerger merger, int mergedDocCount, SegmentReader mergedReader) throws IOException {
    assert testPoint("startCommitMerge");
    if (hitOOM) {
      throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot complete merge");
    }
    if (infoStream != null)
      message("commitMerge: " + merge.segString(directory) + " index=" + segString());
    assert merge.registerDone;
    if (merge.isAborted()) {
      if (infoStream != null)
        message("commitMerge: skipping merge " + merge.segString(directory) + ": it was aborted");
      deleter.refresh(merge.info.name);
      return false;
    }
    final int start = ensureContiguousMerge(merge);
    commitMergedDeletes(merge, mergedReader);
    docWriter.remapDeletes(segmentInfos, merger.getDocMaps(), merger.getDelCounts(), merge, mergedDocCount);
    final String mergeDocStoreSegment = merge.info.getDocStoreSegment(); 
    if (mergeDocStoreSegment != null && !merge.info.getDocStoreIsCompoundFile()) {
      final int size = segmentInfos.size();
      for(int i=0;i<size;i++) {
        final SegmentInfo info = segmentInfos.info(i);
        final String docStoreSegment = info.getDocStoreSegment();
        if (docStoreSegment != null &&
            docStoreSegment.equals(mergeDocStoreSegment) && 
            info.getDocStoreIsCompoundFile()) {
          merge.info.setDocStoreIsCompoundFile(true);
          break;
        }
      }
    }
    merge.info.setHasProx(merger.hasProx());
    segmentInfos.subList(start, start + merge.segments.size()).clear();
    assert !segmentInfos.contains(merge.info);
    segmentInfos.add(start, merge.info);
    checkpoint();
    readerPool.clear(merge.segments);
    if (merge.optimize)
      segmentsToOptimize.add(merge.info);
    return true;
  }
  private synchronized void decrefMergeSegments(MergePolicy.OneMerge merge) throws IOException {
    assert merge.increfDone;
    merge.increfDone = false;
  }
  final private void handleMergeException(Throwable t, MergePolicy.OneMerge merge) throws IOException {
    if (infoStream != null) {
      message("handleMergeException: merge=" + merge.segString(directory) + " exc=" + t);
    }
    merge.setException(t);
    addMergeException(merge);
    if (t instanceof MergePolicy.MergeAbortedException) {
      if (merge.isExternal)
        throw (MergePolicy.MergeAbortedException) t;
    } else if (t instanceof IOException)
      throw (IOException) t;
    else if (t instanceof RuntimeException)
      throw (RuntimeException) t;
    else if (t instanceof Error)
      throw (Error) t;
    else
      throw new RuntimeException(t);
  }
  final void merge(MergePolicy.OneMerge merge)
    throws CorruptIndexException, IOException {
    boolean success = false;
    final long t0 = System.currentTimeMillis();
    try {
      try {
        try {
          mergeInit(merge);
          if (infoStream != null)
            message("now merge\n  merge=" + merge.segString(directory) + "\n  merge=" + merge + "\n  index=" + segString());
          mergeMiddle(merge);
          mergeSuccess(merge);
          success = true;
        } catch (Throwable t) {
          handleMergeException(t, merge);
        }
      } finally {
        synchronized(this) {
          mergeFinish(merge);
          if (!success) {
            if (infoStream != null)
              message("hit exception during merge");
            if (merge.info != null && !segmentInfos.contains(merge.info))
              deleter.refresh(merge.info.name);
          }
          if (success && !merge.isAborted() && !closed && !closing)
            updatePendingMerges(merge.maxNumSegmentsOptimize, merge.optimize);
        }
      }
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "merge");
    }
    if (infoStream != null) {
      message("merge time " + (System.currentTimeMillis()-t0) + " msec for " + merge.info.docCount + " docs");
    }
  }
  void mergeSuccess(MergePolicy.OneMerge merge) {
  }
  final synchronized boolean registerMerge(MergePolicy.OneMerge merge) throws MergePolicy.MergeAbortedException {
    if (merge.registerDone)
      return true;
    if (stopMerges) {
      merge.abort();
      throw new MergePolicy.MergeAbortedException("merge is aborted: " + merge.segString(directory));
    }
    final int count = merge.segments.size();
    boolean isExternal = false;
    for(int i=0;i<count;i++) {
      final SegmentInfo info = merge.segments.info(i);
      if (mergingSegments.contains(info))
        return false;
      if (segmentInfos.indexOf(info) == -1)
        return false;
      if (info.dir != directory)
        isExternal = true;
    }
    ensureContiguousMerge(merge);
    pendingMerges.add(merge);
    if (infoStream != null)
      message("add merge to pendingMerges: " + merge.segString(directory) + " [total " + pendingMerges.size() + " pending]");
    merge.mergeGen = mergeGen;
    merge.isExternal = isExternal;
    for(int i=0;i<count;i++)
      mergingSegments.add(merge.segments.info(i));
    merge.registerDone = true;
    return true;
  }
  final synchronized void mergeInit(MergePolicy.OneMerge merge) throws IOException {
    boolean success = false;
    try {
      _mergeInit(merge);
      success = true;
    } finally {
      if (!success) {
        mergeFinish(merge);
      }
    }
  }
  final synchronized private void _mergeInit(MergePolicy.OneMerge merge) throws IOException {
    assert testPoint("startMergeInit");
    assert merge.registerDone;
    assert !merge.optimize || merge.maxNumSegmentsOptimize > 0;
    if (hitOOM) {
      throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot merge");
    }
    if (merge.info != null)
      return;
    if (merge.isAborted())
      return;
    applyDeletes();
    final SegmentInfos sourceSegments = merge.segments;
    final int end = sourceSegments.size();
    Directory lastDir = directory;
    String lastDocStoreSegment = null;
    int next = -1;
    boolean mergeDocStores = false;
    boolean doFlushDocStore = false;
    final String currentDocStoreSegment = docWriter.getDocStoreSegment();
    for (int i = 0; i < end; i++) {
      SegmentInfo si = sourceSegments.info(i);
      if (si.hasDeletions())
        mergeDocStores = true;
      if (-1 == si.getDocStoreOffset())
        mergeDocStores = true;
      String docStoreSegment = si.getDocStoreSegment();
      if (docStoreSegment == null)
        mergeDocStores = true;
      else if (lastDocStoreSegment == null)
        lastDocStoreSegment = docStoreSegment;
      else if (!lastDocStoreSegment.equals(docStoreSegment))
        mergeDocStores = true;
      if (-1 == next)
        next = si.getDocStoreOffset() + si.docCount;
      else if (next != si.getDocStoreOffset())
        mergeDocStores = true;
      else
        next = si.getDocStoreOffset() + si.docCount;
      if (lastDir != si.dir)
        mergeDocStores = true;
      if (si.getDocStoreOffset() != -1 && currentDocStoreSegment != null && si.getDocStoreSegment().equals(currentDocStoreSegment)) {
        doFlushDocStore = true;
      }
    }
    final int docStoreOffset;
    final String docStoreSegment;
    final boolean docStoreIsCompoundFile;
    if (mergeDocStores) {
      docStoreOffset = -1;
      docStoreSegment = null;
      docStoreIsCompoundFile = false;
    } else {
      SegmentInfo si = sourceSegments.info(0);        
      docStoreOffset = si.getDocStoreOffset();
      docStoreSegment = si.getDocStoreSegment();
      docStoreIsCompoundFile = si.getDocStoreIsCompoundFile();
    }
    if (mergeDocStores && doFlushDocStore) {
      if (infoStream != null)
        message("now flush at merge");
      doFlush(true, false);
    }
    merge.increfDone = true;
    merge.mergeDocStores = mergeDocStores;
    merge.info = new SegmentInfo(newSegmentName(), 0,
                                 directory, false, true,
                                 docStoreOffset,
                                 docStoreSegment,
                                 docStoreIsCompoundFile,
                                 false);
    Map<String,String> details = new HashMap<String,String>();
    details.put("optimize", Boolean.toString(merge.optimize));
    details.put("mergeFactor", Integer.toString(end));
    details.put("mergeDocStores", Boolean.toString(mergeDocStores));
    setDiagnostics(merge.info, "merge", details);
    mergingSegments.add(merge.info);
  }
  private void setDiagnostics(SegmentInfo info, String source) {
    setDiagnostics(info, source, null);
  }
  private void setDiagnostics(SegmentInfo info, String source, Map<String,String> details) {
    Map<String,String> diagnostics = new HashMap<String,String>();
    diagnostics.put("source", source);
    diagnostics.put("lucene.version", Constants.LUCENE_VERSION);
    diagnostics.put("os", Constants.OS_NAME);
    diagnostics.put("os.arch", Constants.OS_ARCH);
    diagnostics.put("os.version", Constants.OS_VERSION);
    diagnostics.put("java.version", Constants.JAVA_VERSION);
    diagnostics.put("java.vendor", Constants.JAVA_VENDOR);
    if (details != null) {
      diagnostics.putAll(details);
    }
    info.setDiagnostics(diagnostics);
  }
  final synchronized void mergeFinish(MergePolicy.OneMerge merge) throws IOException {
    notifyAll();
    if (merge.increfDone)
      decrefMergeSegments(merge);
    if (merge.registerDone) {
      final SegmentInfos sourceSegments = merge.segments;
      final int end = sourceSegments.size();
      for(int i=0;i<end;i++)
        mergingSegments.remove(sourceSegments.info(i));
      mergingSegments.remove(merge.info);
      merge.registerDone = false;
    }
    runningMerges.remove(merge);
  }
  final private int mergeMiddle(MergePolicy.OneMerge merge) 
    throws CorruptIndexException, IOException {
    merge.checkAborted(directory);
    final String mergedName = merge.info.name;
    SegmentMerger merger = null;
    int mergedDocCount = 0;
    SegmentInfos sourceSegments = merge.segments;
    final int numSegments = sourceSegments.size();
    if (infoStream != null)
      message("merging " + merge.segString(directory));
    merger = new SegmentMerger(this, mergedName, merge);
    merge.readers = new SegmentReader[numSegments];
    merge.readersClone = new SegmentReader[numSegments];
    boolean mergeDocStores = false;
    final Set<String> dss = new HashSet<String>();
    boolean success = false;
    try {
      int totDocCount = 0;
      for (int i = 0; i < numSegments; i++) {
        final SegmentInfo info = sourceSegments.info(i);
        SegmentReader reader = merge.readers[i] = readerPool.get(info, merge.mergeDocStores,
                                                                 MERGE_READ_BUFFER_SIZE,
                                                                 -1);
        SegmentReader clone = merge.readersClone[i] = (SegmentReader) reader.clone(true);
        merger.add(clone);
        if (clone.hasDeletions()) {
          mergeDocStores = true;
        }
        if (info.getDocStoreOffset() != -1) {
          dss.add(info.getDocStoreSegment());
        }
        totDocCount += clone.numDocs();
      }
      if (infoStream != null) {
        message("merge: total "+totDocCount+" docs");
      }
      merge.checkAborted(directory);
      if (mergeDocStores && !merge.mergeDocStores) {
        merge.mergeDocStores = true;
        synchronized(this) {
          if (dss.contains(docWriter.getDocStoreSegment())) {
            if (infoStream != null)
              message("now flush at mergeMiddle");
            doFlush(true, false);
          }
        }
        for(int i=0;i<numSegments;i++) {
          merge.readersClone[i].openDocStores();
        }
        synchronized(this) {
          merge.info.setDocStore(-1, null, false);
        }
      }
      mergedDocCount = merge.info.docCount = merger.merge(merge.mergeDocStores);
      assert mergedDocCount == totDocCount;
      final SegmentReader mergedReader = readerPool.get(merge.info, false, BufferedIndexInput.BUFFER_SIZE, -1);
      try {
        if (poolReaders && mergedSegmentWarmer != null) {
          mergedSegmentWarmer.warm(mergedReader);
        }
        if (!commitMerge(merge, merger, mergedDocCount, mergedReader))
          return 0;
      } finally {
        synchronized(this) {
          readerPool.release(mergedReader);
        }
      }
      success = true;
    } finally {
      synchronized(this) {
        if (!success) {
          for (int i=0;i<numSegments;i++) {
            if (merge.readers[i] != null) {
              try {
                readerPool.release(merge.readers[i], true);
              } catch (Throwable t) {
              }
            }
            if (merge.readersClone[i] != null) {
              try {
                merge.readersClone[i].close();
              } catch (Throwable t) {
              }
              assert merge.readersClone[i].getRefCount() == 0;
            }
          }
        } else {
          for (int i=0;i<numSegments;i++) {
            if (merge.readers[i] != null) {
              readerPool.release(merge.readers[i], true);
            }
            if (merge.readersClone[i] != null) {
              merge.readersClone[i].close();
              assert merge.readersClone[i].getRefCount() == 0;
            }
          }
        }
      }
    }
    synchronized(this) {
      deleter.checkpoint(segmentInfos, false);
    }
    decrefMergeSegments(merge);
    if (merge.useCompoundFile) {
      success = false;
      final String compoundFileName = IndexFileNames.segmentFileName(mergedName, IndexFileNames.COMPOUND_FILE_EXTENSION);
      try {
        merger.createCompoundFile(compoundFileName);
        success = true;
      } catch (IOException ioe) {
        synchronized(this) {
          if (merge.isAborted()) {
            success = true;
          } else
            handleMergeException(ioe, merge);
        }
      } catch (Throwable t) {
        handleMergeException(t, merge);
      } finally {
        if (!success) {
          if (infoStream != null)
            message("hit exception creating compound file during merge");
          synchronized(this) {
            deleter.deleteFile(compoundFileName);
          }
        }
      }
      if (merge.isAborted()) {
        if (infoStream != null)
          message("abort merge after building CFS");
        deleter.deleteFile(compoundFileName);
        return 0;
      }
      synchronized(this) {
        if (segmentInfos.indexOf(merge.info) == -1 || merge.isAborted()) {
          deleter.deleteFile(compoundFileName);
        } else {
          merge.info.setUseCompoundFile(true);
          checkpoint();
        }
      }
    }
    return mergedDocCount;
  }
  synchronized void addMergeException(MergePolicy.OneMerge merge) {
    assert merge.getException() != null;
    if (!mergeExceptions.contains(merge) && mergeGen == merge.mergeGen)
      mergeExceptions.add(merge);
  }
  private final synchronized boolean applyDeletes() throws CorruptIndexException, IOException {
    assert testPoint("startApplyDeletes");
    flushDeletesCount++;
    SegmentInfos rollback = (SegmentInfos) segmentInfos.clone();
    boolean success = false;
    boolean changed;
    try {
      changed = docWriter.applyDeletes(segmentInfos);
      success = true;
    } finally {
      if (!success) {
        if (infoStream != null)
          message("hit exception flushing deletes");
        final int size = rollback.size();
        for(int i=0;i<size;i++) {
          final String newDelFileName = segmentInfos.info(i).getDelFileName();
          final String delFileName = rollback.info(i).getDelFileName();
          if (newDelFileName != null && !newDelFileName.equals(delFileName))
            deleter.deleteFile(newDelFileName);
        }
        segmentInfos.clear();
        segmentInfos.addAll(rollback);
      }
    }
    if (changed)
      checkpoint();
    return changed;
  }
  final synchronized int getBufferedDeleteTermsSize() {
    return docWriter.getBufferedDeleteTerms().size();
  }
  final synchronized int getNumBufferedDeleteTerms() {
    return docWriter.getNumBufferedDeleteTerms();
  }
  SegmentInfo newestSegment() {
    return segmentInfos.info(segmentInfos.size()-1);
  }
  public synchronized String segString() {
    return segString(segmentInfos);
  }
  private synchronized String segString(SegmentInfos infos) {
    StringBuilder buffer = new StringBuilder();
    final int count = infos.size();
    for(int i = 0; i < count; i++) {
      if (i > 0) {
        buffer.append(' ');
      }
      final SegmentInfo info = infos.info(i);
      buffer.append(info.toString(directory, 0));
      if (info.dir != directory)
        buffer.append("**");
    }
    return buffer.toString();
  }
  private HashSet<String> synced = new HashSet<String>();
  private HashSet<String> syncing = new HashSet<String>();
  private boolean startSync(String fileName, Collection<String> pending) {
    synchronized(synced) {
      if (!synced.contains(fileName)) {
        if (!syncing.contains(fileName)) {
          syncing.add(fileName);
          return true;
        } else {
          pending.add(fileName);
          return false;
        }
      } else
        return false;
    }
  }
  private void finishSync(String fileName, boolean success) {
    synchronized(synced) {
      assert syncing.contains(fileName);
      syncing.remove(fileName);
      if (success)
        synced.add(fileName);
      synced.notifyAll();
    }
  }
  private boolean waitForAllSynced(Collection<String> syncing) throws IOException {
    synchronized(synced) {
      Iterator<String> it = syncing.iterator();
      while(it.hasNext()) {
        final String fileName = it.next();
        while(!synced.contains(fileName)) {
          if (!syncing.contains(fileName))
            return false;
          else
            try {
              synced.wait();
            } catch (InterruptedException ie) {
              throw new ThreadInterruptedException(ie);
            }
        }
      }
      return true;
    }
  }
  private synchronized void doWait() {
    try {
      wait(1000);
    } catch (InterruptedException ie) {
      throw new ThreadInterruptedException(ie);
    }
  }
  private void startCommit(long sizeInBytes, Map<String,String> commitUserData) throws IOException {
    assert testPoint("startStartCommit");
    if (hitOOM) {
      throw new IllegalStateException("this writer hit an OutOfMemoryError; cannot commit");
    }
    try {
      if (infoStream != null)
        message("startCommit(): start sizeInBytes=" + sizeInBytes);
      SegmentInfos toSync = null;
      final long myChangeCount;
      synchronized(this) {
        blockAddIndexes();
        assert !hasExternalSegments();
        try {
          assert lastCommitChangeCount <= changeCount;
          if (changeCount == lastCommitChangeCount) {
            if (infoStream != null)
              message("  skip startCommit(): no changes pending");
            return;
          }
          if (infoStream != null)
            message("startCommit index=" + segString(segmentInfos) + " changeCount=" + changeCount);
          readerPool.commit();
          toSync = (SegmentInfos) segmentInfos.clone();
          if (commitUserData != null)
            toSync.setUserData(commitUserData);
          deleter.incRef(toSync, false);
          myChangeCount = changeCount;
          Collection<String> files = toSync.files(directory, false);
          for(final String fileName: files) {
            assert directory.fileExists(fileName): "file " + fileName + " does not exist";
          }
        } finally {
          resumeAddIndexes();
        }
      }
      assert testPoint("midStartCommit");
      boolean setPending = false;
      try {
        while(true) {
          final Collection<String> pending = new ArrayList<String>();
          Iterator<String> it = toSync.files(directory, false).iterator();
          while(it.hasNext()) {
            final String fileName = it.next();
            if (startSync(fileName, pending)) {
              boolean success = false;
              try {
                assert directory.fileExists(fileName): "file '" + fileName + "' does not exist dir=" + directory;
                if (infoStream != null)
                  message("now sync " + fileName);
                directory.sync(fileName);
                success = true;
              } finally {
                finishSync(fileName, success);
              }
            }
          }
          if (waitForAllSynced(pending))
            break;
        }
        assert testPoint("midStartCommit2");
        synchronized(this) {
          while(true) {
            if (myChangeCount <= lastCommitChangeCount) {
              if (infoStream != null) {
                message("sync superseded by newer infos");
              }
              break;
            } else if (pendingCommit == null) {
              if (segmentInfos.getGeneration() > toSync.getGeneration())
                toSync.updateGeneration(segmentInfos);
              boolean success = false;
              try {
                try {
                  toSync.prepareCommit(directory);
                } finally {
                  segmentInfos.updateGeneration(toSync);
                }
                assert pendingCommit == null;
                setPending = true;
                pendingCommit = toSync;
                pendingCommitChangeCount = myChangeCount;
                success = true;
              } finally {
                if (!success && infoStream != null)
                  message("hit exception committing segments file");
              }
              break;
            } else {
              doWait();
            }
          }
        }
        if (infoStream != null)
          message("done all syncs");
        assert testPoint("midStartCommitSuccess");
      } finally {
        synchronized(this) {
          if (!setPending)
            deleter.decRef(toSync);
        }
      }
    } catch (OutOfMemoryError oom) {
      handleOOM(oom, "startCommit");
    }
    assert testPoint("finishStartCommit");
  }
  public static boolean isLocked(Directory directory) throws IOException {
    return directory.makeLock(WRITE_LOCK_NAME).isLocked();
  }
  public static void unlock(Directory directory) throws IOException {
    directory.makeLock(IndexWriter.WRITE_LOCK_NAME).release();
  }
  public static final class MaxFieldLength {
    private int limit;
    private String name;
    private MaxFieldLength(String name, int limit) {
      this.name = name;
      this.limit = limit;
    }
    public MaxFieldLength(int limit) {
      this("User-specified", limit);
    }
    public int getLimit() {
      return limit;
    }
    @Override
    public String toString()
    {
      return name + ":" + limit;
    }
    public static final MaxFieldLength UNLIMITED
        = new MaxFieldLength("UNLIMITED", Integer.MAX_VALUE);
    public static final MaxFieldLength LIMITED
        = new MaxFieldLength("LIMITED", DEFAULT_MAX_FIELD_LENGTH);
  }
  public static abstract class IndexReaderWarmer {
    public abstract void warm(IndexReader reader) throws IOException;
  }
  private IndexReaderWarmer mergedSegmentWarmer;
  public void setMergedSegmentWarmer(IndexReaderWarmer warmer) {
    mergedSegmentWarmer = warmer;
    config.setMergedSegmentWarmer(mergedSegmentWarmer);
  }
  public IndexReaderWarmer getMergedSegmentWarmer() {
    return mergedSegmentWarmer;
  }
  private void handleOOM(OutOfMemoryError oom, String location) {
    if (infoStream != null) {
      message("hit OutOfMemoryError inside " + location);
    }
    hitOOM = true;
    throw oom;
  }
  boolean testPoint(String name) {
    return true;
  }
  synchronized boolean nrtIsCurrent(SegmentInfos infos) {
    if (!infos.equals(segmentInfos)) {
      return false;
    } else {
      return !docWriter.anyChanges();
    }
  }
  synchronized boolean isClosed() {
    return closed;
  }
  public synchronized void deleteUnusedFiles() throws IOException {
    deleter.deletePendingFiles();
  }
}
