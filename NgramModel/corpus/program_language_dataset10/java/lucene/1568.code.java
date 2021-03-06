package org.apache.lucene.index;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.DirectoryReader.MultiTermDocs;
import org.apache.lucene.index.DirectoryReader.MultiTermEnum;
import org.apache.lucene.index.DirectoryReader.MultiTermPositions;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.FieldCache; 
public class MultiReader extends IndexReader implements Cloneable {
  protected IndexReader[] subReaders;
  private int[] starts;                           
  private boolean[] decrefOnClose;                
  private Map<String,byte[]> normsCache = new HashMap<String,byte[]>();
  private int maxDoc = 0;
  private int numDocs = -1;
  private boolean hasDeletions = false;
  public MultiReader(IndexReader... subReaders) {
    initialize(subReaders, true);
  }
  public MultiReader(IndexReader[] subReaders, boolean closeSubReaders) {
    initialize(subReaders, closeSubReaders);
  }
  private void initialize(IndexReader[] subReaders, boolean closeSubReaders) {
    this.subReaders =  subReaders.clone();
    starts = new int[subReaders.length + 1];    
    decrefOnClose = new boolean[subReaders.length];
    for (int i = 0; i < subReaders.length; i++) {
      starts[i] = maxDoc;
      maxDoc += subReaders[i].maxDoc();      
      if (!closeSubReaders) {
        subReaders[i].incRef();
        decrefOnClose[i] = true;
      } else {
        decrefOnClose[i] = false;
      }
      if (subReaders[i].hasDeletions())
        hasDeletions = true;
    }
    starts[subReaders.length] = maxDoc;
  }
  @Override
  public synchronized IndexReader reopen() throws CorruptIndexException, IOException {
    return doReopen(false);
  }
  @Override
  public synchronized Object clone() {
    try {
      return doReopen(true);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  protected IndexReader doReopen(boolean doClone) throws CorruptIndexException, IOException {
    ensureOpen();
    boolean reopened = false;
    IndexReader[] newSubReaders = new IndexReader[subReaders.length];
    boolean success = false;
    try {
      for (int i = 0; i < subReaders.length; i++) {
        if (doClone)
          newSubReaders[i] = (IndexReader) subReaders[i].clone();
        else
          newSubReaders[i] = subReaders[i].reopen();
        if (newSubReaders[i] != subReaders[i]) {
          reopened = true;
        }
      }
      success = true;
    } finally {
      if (!success && reopened) {
        for (int i = 0; i < newSubReaders.length; i++) {
          if (newSubReaders[i] != subReaders[i]) {
            try {
              newSubReaders[i].close();
            } catch (IOException ignore) {
            }
          }
        }
      }
    }
    if (reopened) {
      boolean[] newDecrefOnClose = new boolean[subReaders.length];
      for (int i = 0; i < subReaders.length; i++) {
        if (newSubReaders[i] == subReaders[i]) {
          newSubReaders[i].incRef();
          newDecrefOnClose[i] = true;
        }
      }
      MultiReader mr = new MultiReader(newSubReaders);
      mr.decrefOnClose = newDecrefOnClose;
      return mr;
    } else {
      return this;
    }
  }
  @Override
  public TermFreqVector[] getTermFreqVectors(int n) throws IOException {
    ensureOpen();
    int i = readerIndex(n);        
    return subReaders[i].getTermFreqVectors(n - starts[i]); 
  }
  @Override
  public TermFreqVector getTermFreqVector(int n, String field)
      throws IOException {
    ensureOpen();
    int i = readerIndex(n);        
    return subReaders[i].getTermFreqVector(n - starts[i], field);
  }
  @Override
  public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
    ensureOpen();
    int i = readerIndex(docNumber);        
    subReaders[i].getTermFreqVector(docNumber - starts[i], field, mapper);
  }
  @Override
  public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
    ensureOpen();
    int i = readerIndex(docNumber);        
    subReaders[i].getTermFreqVector(docNumber - starts[i], mapper);
  }
  @Override
  public boolean isOptimized() {
    return false;
  }
  @Override
  public int numDocs() {
    if (numDocs == -1) {        
      int n = 0;                
      for (int i = 0; i < subReaders.length; i++)
        n += subReaders[i].numDocs();      
      numDocs = n;
    }
    return numDocs;
  }
  @Override
  public int maxDoc() {
    return maxDoc;
  }
  @Override
  public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    ensureOpen();
    int i = readerIndex(n);                          
    return subReaders[i].document(n - starts[i], fieldSelector);    
  }
  @Override
  public boolean isDeleted(int n) {
    int i = readerIndex(n);                           
    return subReaders[i].isDeleted(n - starts[i]);    
  }
  @Override
  public boolean hasDeletions() {
    return hasDeletions;
  }
  @Override
  protected void doDelete(int n) throws CorruptIndexException, IOException {
    numDocs = -1;                             
    int i = readerIndex(n);                   
    subReaders[i].deleteDocument(n - starts[i]);      
    hasDeletions = true;
  }
  @Override
  protected void doUndeleteAll() throws CorruptIndexException, IOException {
    for (int i = 0; i < subReaders.length; i++)
      subReaders[i].undeleteAll();
    hasDeletions = false;
    numDocs = -1;                                 
  }
  private int readerIndex(int n) {    
    return DirectoryReader.readerIndex(n, this.starts, this.subReaders.length);
  }
  @Override
  public boolean hasNorms(String field) throws IOException {
    ensureOpen();
    for (int i = 0; i < subReaders.length; i++) {
      if (subReaders[i].hasNorms(field)) return true;
    }
    return false;
  }
  @Override
  public synchronized byte[] norms(String field) throws IOException {
    ensureOpen();
    byte[] bytes = normsCache.get(field);
    if (bytes != null)
      return bytes;          
    if (!hasNorms(field))
      return null;
    bytes = new byte[maxDoc()];
    for (int i = 0; i < subReaders.length; i++)
      subReaders[i].norms(field, bytes, starts[i]);
    normsCache.put(field, bytes);      
    return bytes;
  }
  @Override
  public synchronized void norms(String field, byte[] result, int offset)
    throws IOException {
    ensureOpen();
    byte[] bytes = normsCache.get(field);
    for (int i = 0; i < subReaders.length; i++)      
      subReaders[i].norms(field, result, offset + starts[i]);
    if (bytes==null && !hasNorms(field)) {
      Arrays.fill(result, offset, result.length, Similarity.getDefault().encodeNormValue(1.0f));
    } else if (bytes != null) {                         
      System.arraycopy(bytes, 0, result, offset, maxDoc());
    } else {
      for (int i = 0; i < subReaders.length; i++) {     
        subReaders[i].norms(field, result, offset + starts[i]);
      }
    }
  }
  @Override
  protected void doSetNorm(int n, String field, byte value)
    throws CorruptIndexException, IOException {
    synchronized (normsCache) {
      normsCache.remove(field);                         
    }
    int i = readerIndex(n);                           
    subReaders[i].setNorm(n-starts[i], field, value); 
  }
  @Override
  public TermEnum terms() throws IOException {
    ensureOpen();
    if (subReaders.length == 1) {
      return subReaders[0].terms();
    } else {
      return new MultiTermEnum(this, subReaders, starts, null);
    }
  }
  @Override
  public TermEnum terms(Term term) throws IOException {
    ensureOpen();
    if (subReaders.length == 1) {
      return subReaders[0].terms(term);
    } else {
      return new MultiTermEnum(this, subReaders, starts, term);
    }
  }
  @Override
  public int docFreq(Term t) throws IOException {
    ensureOpen();
    int total = 0;          
    for (int i = 0; i < subReaders.length; i++)
      total += subReaders[i].docFreq(t);
    return total;
  }
  @Override
  public TermDocs termDocs() throws IOException {
    ensureOpen();
    if (subReaders.length == 1) {
      return subReaders[0].termDocs();
    } else {
      return new MultiTermDocs(this, subReaders, starts);
    }
  }
  @Override
  public TermDocs termDocs(Term term) throws IOException {
    ensureOpen();
    if (subReaders.length == 1) {
      return subReaders[0].termDocs(term);
    } else {
      return super.termDocs(term);
    }
  }
  @Override
  public TermPositions termPositions() throws IOException {
    ensureOpen();
    if (subReaders.length == 1) {
      return subReaders[0].termPositions();
    } else {
      return new MultiTermPositions(this, subReaders, starts);
    }
  }
  @Override
  protected void doCommit(Map<String,String> commitUserData) throws IOException {
    for (int i = 0; i < subReaders.length; i++)
      subReaders[i].commit(commitUserData);
  }
  @Override
  protected synchronized void doClose() throws IOException {
    for (int i = 0; i < subReaders.length; i++) {
      if (decrefOnClose[i]) {
        subReaders[i].decRef();
      } else {
        subReaders[i].close();
      }
    }
    FieldCache.DEFAULT.purge(this);
  }
  @Override
  public Collection<String> getFieldNames (IndexReader.FieldOption fieldNames) {
    ensureOpen();
    return DirectoryReader.getFieldNames(fieldNames, this.subReaders);
  }  
  @Override
  public boolean isCurrent() throws CorruptIndexException, IOException {
    for (int i = 0; i < subReaders.length; i++) {
      if (!subReaders[i].isCurrent()) {
        return false;
      }
    }
    return true;
  }
  @Override
  public long getVersion() {
    throw new UnsupportedOperationException("MultiReader does not support this method.");
  }
  @Override
  public IndexReader[] getSequentialSubReaders() {
    return subReaders;
  }
}
