package org.apache.lucene.index;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.BitVector;
import org.apache.lucene.util.CloseableThreadLocal;
import org.apache.lucene.search.FieldCache; 
public class SegmentReader extends IndexReader implements Cloneable {
  protected boolean readOnly;
  private SegmentInfo si;
  private int readBufferSize;
  CloseableThreadLocal<FieldsReader> fieldsReaderLocal = new FieldsReaderLocal();
  CloseableThreadLocal<TermVectorsReader> termVectorsLocal = new CloseableThreadLocal<TermVectorsReader>();
  BitVector deletedDocs = null;
  AtomicInteger deletedDocsRef = null;
  private boolean deletedDocsDirty = false;
  private boolean normsDirty = false;
  private int pendingDeleteCount;
  private boolean rollbackHasChanges = false;
  private boolean rollbackDeletedDocsDirty = false;
  private boolean rollbackNormsDirty = false;
  private int rollbackPendingDeleteCount;
  private IndexInput singleNormStream;
  private AtomicInteger singleNormRef;
  CoreReaders core;
  static final class CoreReaders {
    private final AtomicInteger ref = new AtomicInteger(1);
    final String segment;
    final FieldInfos fieldInfos;
    final IndexInput freqStream;
    final IndexInput proxStream;
    final TermInfosReader tisNoIndex;
    final Directory dir;
    final Directory cfsDir;
    final int readBufferSize;
    final int termsIndexDivisor;
    private final SegmentReader origInstance;
    TermInfosReader tis;
    FieldsReader fieldsReaderOrig;
    TermVectorsReader termVectorsReaderOrig;
    CompoundFileReader cfsReader;
    CompoundFileReader storeCFSReader;
    CoreReaders(SegmentReader origInstance, Directory dir, SegmentInfo si, int readBufferSize, int termsIndexDivisor) throws IOException {
      segment = si.name;
      this.readBufferSize = readBufferSize;
      this.dir = dir;
      boolean success = false;
      try {
        Directory dir0 = dir;
        if (si.getUseCompoundFile()) {
          cfsReader = new CompoundFileReader(dir, IndexFileNames.segmentFileName(segment, IndexFileNames.COMPOUND_FILE_EXTENSION), readBufferSize);
          dir0 = cfsReader;
        }
        cfsDir = dir0;
        fieldInfos = new FieldInfos(cfsDir, IndexFileNames.segmentFileName(segment, IndexFileNames.FIELD_INFOS_EXTENSION));
        this.termsIndexDivisor = termsIndexDivisor;
        TermInfosReader reader = new TermInfosReader(cfsDir, segment, fieldInfos, readBufferSize, termsIndexDivisor);
        if (termsIndexDivisor == -1) {
          tisNoIndex = reader;
        } else {
          tis = reader;
          tisNoIndex = null;
        }
        freqStream = cfsDir.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.FREQ_EXTENSION), readBufferSize);
        if (fieldInfos.hasProx()) {
          proxStream = cfsDir.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.PROX_EXTENSION), readBufferSize);
        } else {
          proxStream = null;
        }
        success = true;
      } finally {
        if (!success) {
          decRef();
        }
      }
      this.origInstance = origInstance;
    }
    synchronized TermVectorsReader getTermVectorsReaderOrig() {
      return termVectorsReaderOrig;
    }
    synchronized FieldsReader getFieldsReaderOrig() {
      return fieldsReaderOrig;
    }
    synchronized void incRef() {
      ref.incrementAndGet();
    }
    synchronized Directory getCFSReader() {
      return cfsReader;
    }
    synchronized TermInfosReader getTermsReader() {
      if (tis != null) {
        return tis;
      } else {
        return tisNoIndex;
      }
    }      
    synchronized boolean termsIndexIsLoaded() {
      return tis != null;
    }      
    synchronized void loadTermsIndex(SegmentInfo si, int termsIndexDivisor) throws IOException {
      if (tis == null) {
        Directory dir0;
        if (si.getUseCompoundFile()) {
          if (cfsReader == null) {
            cfsReader = new CompoundFileReader(dir, IndexFileNames.segmentFileName(segment, IndexFileNames.COMPOUND_FILE_EXTENSION), readBufferSize);
          }
          dir0 = cfsReader;
        } else {
          dir0 = dir;
        }
        tis = new TermInfosReader(dir0, segment, fieldInfos, readBufferSize, termsIndexDivisor);
      }
    }
    synchronized void decRef() throws IOException {
      if (ref.decrementAndGet() == 0) {
        if (tis != null) {
          tis.close();
          tis = null;
        }
        if (tisNoIndex != null) {
          tisNoIndex.close();
        }
        if (freqStream != null) {
          freqStream.close();
        }
        if (proxStream != null) {
          proxStream.close();
        }
        if (termVectorsReaderOrig != null) {
          termVectorsReaderOrig.close();
        }
        if (fieldsReaderOrig != null) {
          fieldsReaderOrig.close();
        }
        if (cfsReader != null) {
          cfsReader.close();
        }
        if (storeCFSReader != null) {
          storeCFSReader.close();
        }
        if (origInstance != null) {
          FieldCache.DEFAULT.purge(origInstance);
        }
      }
    }
    synchronized void openDocStores(SegmentInfo si) throws IOException {
      assert si.name.equals(segment);
      if (fieldsReaderOrig == null) {
        final Directory storeDir;
        if (si.getDocStoreOffset() != -1) {
          if (si.getDocStoreIsCompoundFile()) {
            assert storeCFSReader == null;
            storeCFSReader = new CompoundFileReader(dir,
                IndexFileNames.segmentFileName(si.getDocStoreSegment(), IndexFileNames.COMPOUND_FILE_STORE_EXTENSION),
                                                    readBufferSize);
            storeDir = storeCFSReader;
            assert storeDir != null;
          } else {
            storeDir = dir;
            assert storeDir != null;
          }
        } else if (si.getUseCompoundFile()) {
          if (cfsReader == null) {
            cfsReader = new CompoundFileReader(dir, IndexFileNames.segmentFileName(segment, IndexFileNames.COMPOUND_FILE_EXTENSION), readBufferSize);
          }
          storeDir = cfsReader;
          assert storeDir != null;
        } else {
          storeDir = dir;
          assert storeDir != null;
        }
        final String storesSegment;
        if (si.getDocStoreOffset() != -1) {
          storesSegment = si.getDocStoreSegment();
        } else {
          storesSegment = segment;
        }
        fieldsReaderOrig = new FieldsReader(storeDir, storesSegment, fieldInfos, readBufferSize,
                                            si.getDocStoreOffset(), si.docCount);
        if (si.getDocStoreOffset() == -1 && fieldsReaderOrig.size() != si.docCount) {
          throw new CorruptIndexException("doc counts differ for segment " + segment + ": fieldsReader shows " + fieldsReaderOrig.size() + " but segmentInfo shows " + si.docCount);
        }
        if (fieldInfos.hasVectors()) { 
          termVectorsReaderOrig = new TermVectorsReader(storeDir, storesSegment, fieldInfos, readBufferSize, si.getDocStoreOffset(), si.docCount);
        }
      }
    }
  }
  private class FieldsReaderLocal extends CloseableThreadLocal<FieldsReader> {
    @Override
    protected FieldsReader initialValue() {
      return (FieldsReader) core.getFieldsReaderOrig().clone();
    }
  }
  final class Norm implements Cloneable {
    private int refCount = 1;
    private Norm origNorm;
    private IndexInput in;
    private long normSeek;
    private AtomicInteger bytesRef;
    private byte[] bytes;
    private boolean dirty;
    private int number;
    private boolean rollbackDirty;
    public Norm(IndexInput in, int number, long normSeek) {
      this.in = in;
      this.number = number;
      this.normSeek = normSeek;
    }
    public synchronized void incRef() {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      refCount++;
    }
    private void closeInput() throws IOException {
      if (in != null) {
        if (in != singleNormStream) {
          in.close();
        } else {
          if (singleNormRef.decrementAndGet() == 0) {
            singleNormStream.close();
            singleNormStream = null;
          }
        }
        in = null;
      }
    }
    public synchronized void decRef() throws IOException {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      if (--refCount == 0) {
        if (origNorm != null) {
          origNorm.decRef();
          origNorm = null;
        } else {
          closeInput();
        }
        if (bytes != null) {
          assert bytesRef != null;
          bytesRef.decrementAndGet();
          bytes = null;
          bytesRef = null;
        } else {
          assert bytesRef == null;
        }
      }
    }
    public synchronized void bytes(byte[] bytesOut, int offset, int len) throws IOException {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      if (bytes != null) {
        assert len <= maxDoc();
        System.arraycopy(bytes, 0, bytesOut, offset, len);
      } else {
        if (origNorm != null) {
          origNorm.bytes(bytesOut, offset, len);
        } else {
          synchronized(in) {
            in.seek(normSeek);
            in.readBytes(bytesOut, offset, len, false);
          }
        }
      }
    }
    public synchronized byte[] bytes() throws IOException {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      if (bytes == null) {                     
        assert bytesRef == null;
        if (origNorm != null) {
          bytes = origNorm.bytes();
          bytesRef = origNorm.bytesRef;
          bytesRef.incrementAndGet();
          origNorm.decRef();
          origNorm = null;
        } else {
          final int count = maxDoc();
          bytes = new byte[count];
          assert in != null;
          synchronized(in) {
            in.seek(normSeek);
            in.readBytes(bytes, 0, count, false);
          }
          bytesRef = new AtomicInteger(1);
          closeInput();
        }
      }
      return bytes;
    }
    AtomicInteger bytesRef() {
      return bytesRef;
    }
    public synchronized byte[] copyOnWrite() throws IOException {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      bytes();
      assert bytes != null;
      assert bytesRef != null;
      if (bytesRef.get() > 1) {
        assert refCount == 1;
        final AtomicInteger oldRef = bytesRef;
        bytes = cloneNormBytes(bytes);
        bytesRef = new AtomicInteger(1);
        oldRef.decrementAndGet();
      }
      dirty = true;
      return bytes;
    }
    @Override
    public synchronized Object clone() {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0);
      Norm clone;
      try {
        clone = (Norm) super.clone();
      } catch (CloneNotSupportedException cnse) {
        throw new RuntimeException("unexpected CloneNotSupportedException", cnse);
      }
      clone.refCount = 1;
      if (bytes != null) {
        assert bytesRef != null;
        assert origNorm == null;
        clone.bytesRef.incrementAndGet();
      } else {
        assert bytesRef == null;
        if (origNorm == null) {
          clone.origNorm = this;
        }
        clone.origNorm.incRef();
      }
      clone.in = null;
      return clone;
    }
    public void reWrite(SegmentInfo si) throws IOException {
      assert refCount > 0 && (origNorm == null || origNorm.refCount > 0): "refCount=" + refCount + " origNorm=" + origNorm;
      si.advanceNormGen(this.number);
      IndexOutput out = directory().createOutput(si.getNormFileName(this.number));
      try {
        out.writeBytes(bytes, maxDoc());
      } finally {
        out.close();
      }
      this.dirty = false;
    }
  }
  Map<String,Norm> norms = new HashMap<String,Norm>();
  public static SegmentReader get(boolean readOnly, SegmentInfo si, int termInfosIndexDivisor) throws CorruptIndexException, IOException {
    return get(readOnly, si.dir, si, BufferedIndexInput.BUFFER_SIZE, true, termInfosIndexDivisor);
  }
  public static SegmentReader get(boolean readOnly,
                                  Directory dir,
                                  SegmentInfo si,
                                  int readBufferSize,
                                  boolean doOpenStores,
                                  int termInfosIndexDivisor)
    throws CorruptIndexException, IOException {
    SegmentReader instance = readOnly ? new ReadOnlySegmentReader() : new SegmentReader();
    instance.readOnly = readOnly;
    instance.si = si;
    instance.readBufferSize = readBufferSize;
    boolean success = false;
    try {
      instance.core = new CoreReaders(instance, dir, si, readBufferSize, termInfosIndexDivisor);
      if (doOpenStores) {
        instance.core.openDocStores(si);
      }
      instance.loadDeletedDocs();
      instance.openNorms(instance.core.cfsDir, readBufferSize);
      success = true;
    } finally {
      if (!success) {
        instance.doClose();
      }
    }
    return instance;
  }
  void openDocStores() throws IOException {
    core.openDocStores(si);
  }
  private boolean checkDeletedCounts() throws IOException {
    final int recomputedCount = deletedDocs.getRecomputedCount();
    assert deletedDocs.count() == recomputedCount : "deleted count=" + deletedDocs.count() + " vs recomputed count=" + recomputedCount;
    assert si.getDelCount() == recomputedCount : 
    "delete count mismatch: info=" + si.getDelCount() + " vs BitVector=" + recomputedCount;
    assert si.getDelCount() <= maxDoc() : 
    "delete count mismatch: " + recomputedCount + ") exceeds max doc (" + maxDoc() + ") for segment " + si.name;
    return true;
  }
  private void loadDeletedDocs() throws IOException {
    if (hasDeletions(si)) {
      deletedDocs = new BitVector(directory(), si.getDelFileName());
      deletedDocsRef = new AtomicInteger(1);
      assert checkDeletedCounts();
      if (deletedDocs.size() != si.docCount) {
        throw new CorruptIndexException("document count mismatch: deleted docs count " + deletedDocs.size() + " vs segment doc count " + si.docCount + " segment=" + si.name);
      }
    } else
      assert si.getDelCount() == 0;
  }
  protected byte[] cloneNormBytes(byte[] bytes) {
    byte[] cloneBytes = new byte[bytes.length];
    System.arraycopy(bytes, 0, cloneBytes, 0, bytes.length);
    return cloneBytes;
  }
  protected BitVector cloneDeletedDocs(BitVector bv) {
    return (BitVector)bv.clone();
  }
  @Override
  public final synchronized Object clone() {
    try {
      return clone(readOnly); 
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  @Override
  public final synchronized IndexReader clone(boolean openReadOnly) throws CorruptIndexException, IOException {
    return reopenSegment(si, true, openReadOnly);
  }
  synchronized SegmentReader reopenSegment(SegmentInfo si, boolean doClone, boolean openReadOnly) throws CorruptIndexException, IOException {
    boolean deletionsUpToDate = (this.si.hasDeletions() == si.hasDeletions()) 
                                  && (!si.hasDeletions() || this.si.getDelFileName().equals(si.getDelFileName()));
    boolean normsUpToDate = true;
    boolean[] fieldNormsChanged = new boolean[core.fieldInfos.size()];
    final int fieldCount = core.fieldInfos.size();
    for (int i = 0; i < fieldCount; i++) {
      if (!this.si.getNormFileName(i).equals(si.getNormFileName(i))) {
        normsUpToDate = false;
        fieldNormsChanged[i] = true;
      }
    }
    if (normsUpToDate && deletionsUpToDate && !doClone && openReadOnly && readOnly) {
      return this;
    }    
    assert !doClone || (normsUpToDate && deletionsUpToDate);
    SegmentReader clone = openReadOnly ? new ReadOnlySegmentReader() : new SegmentReader();
    boolean success = false;
    try {
      core.incRef();
      clone.core = core;
      clone.readOnly = openReadOnly;
      clone.si = si;
      clone.readBufferSize = readBufferSize;
      clone.pendingDeleteCount = pendingDeleteCount;
      if (!openReadOnly && hasChanges) {
        clone.deletedDocsDirty = deletedDocsDirty;
        clone.normsDirty = normsDirty;
        clone.hasChanges = hasChanges;
        hasChanges = false;
      }
      if (doClone) {
        if (deletedDocs != null) {
          deletedDocsRef.incrementAndGet();
          clone.deletedDocs = deletedDocs;
          clone.deletedDocsRef = deletedDocsRef;
        }
      } else {
        if (!deletionsUpToDate) {
          assert clone.deletedDocs == null;
          clone.loadDeletedDocs();
        } else if (deletedDocs != null) {
          deletedDocsRef.incrementAndGet();
          clone.deletedDocs = deletedDocs;
          clone.deletedDocsRef = deletedDocsRef;
        }
      }
      clone.norms = new HashMap<String,Norm>();
      for (int i = 0; i < fieldNormsChanged.length; i++) {
        if (doClone || !fieldNormsChanged[i]) {
          final String curField = core.fieldInfos.fieldInfo(i).name;
          Norm norm = this.norms.get(curField);
          if (norm != null)
            clone.norms.put(curField, (Norm) norm.clone());
        }
      }
      clone.openNorms(si.getUseCompoundFile() ? core.getCFSReader() : directory(), readBufferSize);
      success = true;
    } finally {
      if (!success) {
        clone.decRef();
      }
    }
    return clone;
  }
  @Override
  protected void doCommit(Map<String,String> commitUserData) throws IOException {
    if (hasChanges) {
      if (deletedDocsDirty) {               
        si.advanceDelGen();
        deletedDocs.write(directory(), si.getDelFileName());
        si.setDelCount(si.getDelCount()+pendingDeleteCount);
        pendingDeleteCount = 0;
        assert deletedDocs.count() == si.getDelCount(): "delete count mismatch during commit: info=" + si.getDelCount() + " vs BitVector=" + deletedDocs.count();
      } else {
        assert pendingDeleteCount == 0;
      }
      if (normsDirty) {               
        si.setNumFields(core.fieldInfos.size());
        for (final Norm norm : norms.values()) {
          if (norm.dirty) {
            norm.reWrite(si);
          }
        }
      }
      deletedDocsDirty = false;
      normsDirty = false;
      hasChanges = false;
    }
  }
  FieldsReader getFieldsReader() {
    return fieldsReaderLocal.get();
  }
  @Override
  protected void doClose() throws IOException {
    termVectorsLocal.close();
    fieldsReaderLocal.close();
    if (deletedDocs != null) {
      deletedDocsRef.decrementAndGet();
      deletedDocs = null;
    }
    for (final Norm norm : norms.values()) {
      norm.decRef();
    }
    if (core != null) {
      core.decRef();
    }
  }
  static boolean hasDeletions(SegmentInfo si) throws IOException {
    return si.hasDeletions();
  }
  @Override
  public boolean hasDeletions() {
    return deletedDocs != null;
  }
  static boolean usesCompoundFile(SegmentInfo si) throws IOException {
    return si.getUseCompoundFile();
  }
  static boolean hasSeparateNorms(SegmentInfo si) throws IOException {
    return si.hasSeparateNorms();
  }
  @Override
  protected void doDelete(int docNum) {
    if (deletedDocs == null) {
      deletedDocs = new BitVector(maxDoc());
      deletedDocsRef = new AtomicInteger(1);
    }
    if (deletedDocsRef.get() > 1) {
      AtomicInteger oldRef = deletedDocsRef;
      deletedDocs = cloneDeletedDocs(deletedDocs);
      deletedDocsRef = new AtomicInteger(1);
      oldRef.decrementAndGet();
    }
    deletedDocsDirty = true;
    if (!deletedDocs.getAndSet(docNum))
      pendingDeleteCount++;
  }
  @Override
  protected void doUndeleteAll() {
    deletedDocsDirty = false;
    if (deletedDocs != null) {
      assert deletedDocsRef != null;
      deletedDocsRef.decrementAndGet();
      deletedDocs = null;
      deletedDocsRef = null;
      pendingDeleteCount = 0;
      si.clearDelGen();
      si.setDelCount(0);
    } else {
      assert deletedDocsRef == null;
      assert pendingDeleteCount == 0;
    }
  }
  List<String> files() throws IOException {
    return new ArrayList<String>(si.files());
  }
  @Override
  public TermEnum terms() {
    ensureOpen();
    return core.getTermsReader().terms();
  }
  @Override
  public TermEnum terms(Term t) throws IOException {
    ensureOpen();
    return core.getTermsReader().terms(t);
  }
  FieldInfos fieldInfos() {
    return core.fieldInfos;
  }
  @Override
  public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    ensureOpen();
    return getFieldsReader().doc(n, fieldSelector);
  }
  @Override
  public synchronized boolean isDeleted(int n) {
    return (deletedDocs != null && deletedDocs.get(n));
  }
  @Override
  public TermDocs termDocs(Term term) throws IOException {
    if (term == null) {
      return new AllTermDocs(this);
    } else {
      return super.termDocs(term);
    }
  }
  @Override
  public TermDocs termDocs() throws IOException {
    ensureOpen();
    return new SegmentTermDocs(this);
  }
  @Override
  public TermPositions termPositions() throws IOException {
    ensureOpen();
    return new SegmentTermPositions(this);
  }
  @Override
  public int docFreq(Term t) throws IOException {
    ensureOpen();
    TermInfo ti = core.getTermsReader().get(t);
    if (ti != null)
      return ti.docFreq;
    else
      return 0;
  }
  @Override
  public int numDocs() {
    int n = maxDoc();
    if (deletedDocs != null)
      n -= deletedDocs.count();
    return n;
  }
  @Override
  public int maxDoc() {
    return si.docCount;
  }
  @Override
  public Collection<String> getFieldNames(IndexReader.FieldOption fieldOption) {
    ensureOpen();
    Set<String> fieldSet = new HashSet<String>();
    for (int i = 0; i < core.fieldInfos.size(); i++) {
      FieldInfo fi = core.fieldInfos.fieldInfo(i);
      if (fieldOption == IndexReader.FieldOption.ALL) {
        fieldSet.add(fi.name);
      }
      else if (!fi.isIndexed && fieldOption == IndexReader.FieldOption.UNINDEXED) {
        fieldSet.add(fi.name);
      }
      else if (fi.omitTermFreqAndPositions && fieldOption == IndexReader.FieldOption.OMIT_TERM_FREQ_AND_POSITIONS) {
        fieldSet.add(fi.name);
      }
      else if (fi.storePayloads && fieldOption == IndexReader.FieldOption.STORES_PAYLOADS) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fieldOption == IndexReader.FieldOption.INDEXED) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fi.storeTermVector == false && fieldOption == IndexReader.FieldOption.INDEXED_NO_TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.storeTermVector == true &&
               fi.storePositionWithTermVector == false &&
               fi.storeOffsetWithTermVector == false &&
               fieldOption == IndexReader.FieldOption.TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.isIndexed && fi.storeTermVector && fieldOption == IndexReader.FieldOption.INDEXED_WITH_TERMVECTOR) {
        fieldSet.add(fi.name);
      }
      else if (fi.storePositionWithTermVector && fi.storeOffsetWithTermVector == false && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION) {
        fieldSet.add(fi.name);
      }
      else if (fi.storeOffsetWithTermVector && fi.storePositionWithTermVector == false && fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_OFFSET) {
        fieldSet.add(fi.name);
      }
      else if ((fi.storeOffsetWithTermVector && fi.storePositionWithTermVector) &&
                fieldOption == IndexReader.FieldOption.TERMVECTOR_WITH_POSITION_OFFSET) {
        fieldSet.add(fi.name);
      }
    }
    return fieldSet;
  }
  @Override
  public synchronized boolean hasNorms(String field) {
    ensureOpen();
    return norms.containsKey(field);
  }
  protected synchronized byte[] getNorms(String field) throws IOException {
    Norm norm = norms.get(field);
    if (norm == null) return null;  
    return norm.bytes();
  }
  @Override
  public synchronized byte[] norms(String field) throws IOException {
    ensureOpen();
    byte[] bytes = getNorms(field);
    return bytes;
  }
  @Override
  protected void doSetNorm(int doc, String field, byte value)
          throws IOException {
    Norm norm = norms.get(field);
    if (norm == null)                             
      return;
    normsDirty = true;
    norm.copyOnWrite()[doc] = value;                    
  }
  @Override
  public synchronized void norms(String field, byte[] bytes, int offset)
    throws IOException {
    ensureOpen();
    Norm norm = norms.get(field);
    if (norm == null) {
      Arrays.fill(bytes, offset, bytes.length, Similarity.getDefault().encodeNormValue(1.0f));
      return;
    }
    norm.bytes(bytes, offset, maxDoc());
  }
  private void openNorms(Directory cfsDir, int readBufferSize) throws IOException {
    long nextNormSeek = SegmentMerger.NORMS_HEADER.length; 
    int maxDoc = maxDoc();
    for (int i = 0; i < core.fieldInfos.size(); i++) {
      FieldInfo fi = core.fieldInfos.fieldInfo(i);
      if (norms.containsKey(fi.name)) {
        continue;
      }
      if (fi.isIndexed && !fi.omitNorms) {
        Directory d = directory();
        String fileName = si.getNormFileName(fi.number);
        if (!si.hasSeparateNorms(fi.number)) {
          d = cfsDir;
        }
        boolean singleNormFile = IndexFileNames.matchesExtension(fileName, IndexFileNames.NORMS_EXTENSION);
        IndexInput normInput = null;
        long normSeek;
        if (singleNormFile) {
          normSeek = nextNormSeek;
          if (singleNormStream == null) {
            singleNormStream = d.openInput(fileName, readBufferSize);
            singleNormRef = new AtomicInteger(1);
          } else {
            singleNormRef.incrementAndGet();
          }
          normInput = singleNormStream;
        } else {
          normSeek = 0;
          normInput = d.openInput(fileName);
        }
        norms.put(fi.name, new Norm(normInput, fi.number, normSeek));
        nextNormSeek += maxDoc; 
      }
    }
  }
  boolean termsIndexLoaded() {
    return core.termsIndexIsLoaded();
  }
  void loadTermsIndex(int termsIndexDivisor) throws IOException {
    core.loadTermsIndex(si, termsIndexDivisor);
  }
  boolean normsClosed() {
    if (singleNormStream != null) {
      return false;
    }
    for (final Norm norm : norms.values()) {
      if (norm.refCount > 0) {
        return false;
      }
    }
    return true;
  }
  boolean normsClosed(String field) {
    return norms.get(field).refCount == 0;
  }
  TermVectorsReader getTermVectorsReader() {
    TermVectorsReader tvReader = termVectorsLocal.get();
    if (tvReader == null) {
      TermVectorsReader orig = core.getTermVectorsReaderOrig();
      if (orig == null) {
        return null;
      } else {
        try {
          tvReader = (TermVectorsReader) orig.clone();
        } catch (CloneNotSupportedException cnse) {
          return null;
        }
      }
      termVectorsLocal.set(tvReader);
    }
    return tvReader;
  }
  TermVectorsReader getTermVectorsReaderOrig() {
    return core.getTermVectorsReaderOrig();
  }
  @Override
  public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException {
    ensureOpen();
    FieldInfo fi = core.fieldInfos.fieldInfo(field);
    if (fi == null || !fi.storeTermVector) 
      return null;
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null)
      return null;
    return termVectorsReader.get(docNumber, field);
  }
  @Override
  public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
    ensureOpen();
    FieldInfo fi = core.fieldInfos.fieldInfo(field);
    if (fi == null || !fi.storeTermVector)
      return;
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null) {
      return;
    }
    termVectorsReader.get(docNumber, field, mapper);
  }
  @Override
  public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
    ensureOpen();
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null)
      return;
    termVectorsReader.get(docNumber, mapper);
  }
  @Override
  public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
    ensureOpen();
    TermVectorsReader termVectorsReader = getTermVectorsReader();
    if (termVectorsReader == null)
      return null;
    return termVectorsReader.get(docNumber);
  }
  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder();
    if (hasChanges) {
      buffer.append('*');
    }
    buffer.append(si.toString(core.dir, pendingDeleteCount));
    return buffer.toString();
  }
  public String getSegmentName() {
    return core.segment;
  }
  SegmentInfo getSegmentInfo() {
    return si;
  }
  void setSegmentInfo(SegmentInfo info) {
    si = info;
  }
  void startCommit() {
    rollbackHasChanges = hasChanges;
    rollbackDeletedDocsDirty = deletedDocsDirty;
    rollbackNormsDirty = normsDirty;
    rollbackPendingDeleteCount = pendingDeleteCount;
    for (Norm norm : norms.values()) {
      norm.rollbackDirty = norm.dirty;
    }
  }
  void rollbackCommit() {
    hasChanges = rollbackHasChanges;
    deletedDocsDirty = rollbackDeletedDocsDirty;
    normsDirty = rollbackNormsDirty;
    pendingDeleteCount = rollbackPendingDeleteCount;
    for (Norm norm : norms.values()) {
      norm.dirty = norm.rollbackDirty;
    }
  }
  @Override
  public Directory directory() {
    return core.dir;
  }
  @Override
  public final Object getFieldCacheKey() {
    return core.freqStream;
  }
  @Override
  public long getUniqueTermCount() {
    return core.getTermsReader().size();
  }
  @Deprecated
  static SegmentReader getOnlySegmentReader(Directory dir) throws IOException {
    return getOnlySegmentReader(IndexReader.open(dir,false));
  }
  static SegmentReader getOnlySegmentReader(IndexReader reader) {
    if (reader instanceof SegmentReader)
      return (SegmentReader) reader;
    if (reader instanceof DirectoryReader) {
      IndexReader[] subReaders = reader.getSequentialSubReaders();
      if (subReaders.length != 1)
        throw new IllegalArgumentException(reader + " has " + subReaders.length + " segments instead of exactly one");
      return (SegmentReader) subReaders[0];
    }
    throw new IllegalArgumentException(reader + " is not a SegmentReader or a single-segment DirectoryReader");
  }
  @Override
  public int getTermInfosIndexDivisor() {
    return core.termsIndexDivisor;
  }
}
