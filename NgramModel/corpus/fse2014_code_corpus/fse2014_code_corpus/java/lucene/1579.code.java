package org.apache.lucene.index;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.util.BitVector;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
public final class SegmentInfo {
  static final int NO = -1;          
  static final int YES = 1;          
  static final int CHECK_DIR = 0;    
  static final int WITHOUT_GEN = 0;  
  public String name;				  
  public int docCount;				  
  public Directory dir;				  
  private boolean preLockless;                    
  private long delGen;                            
  private long[] normGen;                         
  private byte isCompoundFile;                    
  private boolean hasSingleNormFile;              
  private List<String> files;                             
  long sizeInBytes = -1;                          
  private int docStoreOffset;                     
  private String docStoreSegment;                 
  private boolean docStoreIsCompoundFile;         
  private int delCount;                           
  private boolean hasProx;                        
  private Map<String,String> diagnostics;
  public SegmentInfo(String name, int docCount, Directory dir) {
    this.name = name;
    this.docCount = docCount;
    this.dir = dir;
    delGen = NO;
    isCompoundFile = CHECK_DIR;
    preLockless = true;
    hasSingleNormFile = false;
    docStoreOffset = -1;
    docStoreSegment = name;
    docStoreIsCompoundFile = false;
    delCount = 0;
    hasProx = true;
  }
  public SegmentInfo(String name, int docCount, Directory dir, boolean isCompoundFile, boolean hasSingleNormFile) { 
    this(name, docCount, dir, isCompoundFile, hasSingleNormFile, -1, null, false, true);
  }
  public SegmentInfo(String name, int docCount, Directory dir, boolean isCompoundFile, boolean hasSingleNormFile,
                     int docStoreOffset, String docStoreSegment, boolean docStoreIsCompoundFile, boolean hasProx) { 
    this(name, docCount, dir);
    this.isCompoundFile = (byte) (isCompoundFile ? YES : NO);
    this.hasSingleNormFile = hasSingleNormFile;
    preLockless = false;
    this.docStoreOffset = docStoreOffset;
    this.docStoreSegment = docStoreSegment;
    this.docStoreIsCompoundFile = docStoreIsCompoundFile;
    this.hasProx = hasProx;
    delCount = 0;
    assert docStoreOffset == -1 || docStoreSegment != null: "dso=" + docStoreOffset + " dss=" + docStoreSegment + " docCount=" + docCount;
  }
  void reset(SegmentInfo src) {
    clearFiles();
    name = src.name;
    docCount = src.docCount;
    dir = src.dir;
    preLockless = src.preLockless;
    delGen = src.delGen;
    docStoreOffset = src.docStoreOffset;
    docStoreIsCompoundFile = src.docStoreIsCompoundFile;
    if (src.normGen == null) {
      normGen = null;
    } else {
      normGen = new long[src.normGen.length];
      System.arraycopy(src.normGen, 0, normGen, 0, src.normGen.length);
    }
    isCompoundFile = src.isCompoundFile;
    hasSingleNormFile = src.hasSingleNormFile;
    delCount = src.delCount;
  }
  void setDiagnostics(Map<String, String> diagnostics) {
    this.diagnostics = diagnostics;
  }
  public Map<String, String> getDiagnostics() {
    return diagnostics;
  }
  SegmentInfo(Directory dir, int format, IndexInput input) throws IOException {
    this.dir = dir;
    name = input.readString();
    docCount = input.readInt();
    if (format <= SegmentInfos.FORMAT_LOCKLESS) {
      delGen = input.readLong();
      if (format <= SegmentInfos.FORMAT_SHARED_DOC_STORE) {
        docStoreOffset = input.readInt();
        if (docStoreOffset != -1) {
          docStoreSegment = input.readString();
          docStoreIsCompoundFile = (1 == input.readByte());
        } else {
          docStoreSegment = name;
          docStoreIsCompoundFile = false;
        }
      } else {
        docStoreOffset = -1;
        docStoreSegment = name;
        docStoreIsCompoundFile = false;
      }
      if (format <= SegmentInfos.FORMAT_SINGLE_NORM_FILE) {
        hasSingleNormFile = (1 == input.readByte());
      } else {
        hasSingleNormFile = false;
      }
      int numNormGen = input.readInt();
      if (numNormGen == NO) {
        normGen = null;
      } else {
        normGen = new long[numNormGen];
        for(int j=0;j<numNormGen;j++) {
          normGen[j] = input.readLong();
        }
      }
      isCompoundFile = input.readByte();
      preLockless = (isCompoundFile == CHECK_DIR);
      if (format <= SegmentInfos.FORMAT_DEL_COUNT) {
        delCount = input.readInt();
        assert delCount <= docCount;
      } else
        delCount = -1;
      if (format <= SegmentInfos.FORMAT_HAS_PROX)
        hasProx = input.readByte() == 1;
      else
        hasProx = true;
      if (format <= SegmentInfos.FORMAT_DIAGNOSTICS) {
        diagnostics = input.readStringStringMap();
      } else {
        diagnostics = Collections.<String,String>emptyMap();
      }
    } else {
      delGen = CHECK_DIR;
      normGen = null;
      isCompoundFile = CHECK_DIR;
      preLockless = true;
      hasSingleNormFile = false;
      docStoreOffset = -1;
      docStoreIsCompoundFile = false;
      docStoreSegment = null;
      delCount = -1;
      hasProx = true;
      diagnostics = Collections.<String,String>emptyMap();
    }
  }
  void setNumFields(int numFields) {
    if (normGen == null) {
      normGen = new long[numFields];
      if (preLockless) {
      } else {
        for(int i=0;i<numFields;i++) {
          normGen[i] = NO;
        }
      }
    }
  }
  public long sizeInBytes() throws IOException {
    if (sizeInBytes == -1) {
      List<String> files = files();
      final int size = files.size();
      sizeInBytes = 0;
      for(int i=0;i<size;i++) {
        final String fileName = files.get(i);
        if (docStoreOffset == -1 || !IndexFileNames.isDocStoreFile(fileName))
          sizeInBytes += dir.fileLength(fileName);
      }
    }
    return sizeInBytes;
  }
  public boolean hasDeletions()
    throws IOException {
    if (delGen == NO) {
      return false;
    } else if (delGen >= YES) {
      return true;
    } else {
      return dir.fileExists(getDelFileName());
    }
  }
  void advanceDelGen() {
    if (delGen == NO) {
      delGen = YES;
    } else {
      delGen++;
    }
    clearFiles();
  }
  void clearDelGen() {
    delGen = NO;
    clearFiles();
  }
  @Override
  public Object clone () {
    SegmentInfo si = new SegmentInfo(name, docCount, dir);
    si.isCompoundFile = isCompoundFile;
    si.delGen = delGen;
    si.delCount = delCount;
    si.hasProx = hasProx;
    si.preLockless = preLockless;
    si.hasSingleNormFile = hasSingleNormFile;
    si.diagnostics = new HashMap<String, String>(diagnostics);
    if (normGen != null) {
      si.normGen = normGen.clone();
    }
    si.docStoreOffset = docStoreOffset;
    si.docStoreSegment = docStoreSegment;
    si.docStoreIsCompoundFile = docStoreIsCompoundFile;
    return si;
  }
  public String getDelFileName() {
    if (delGen == NO) {
      return null;
    } else {
      return IndexFileNames.fileNameFromGeneration(name, IndexFileNames.DELETES_EXTENSION, delGen); 
    }
  }
  public boolean hasSeparateNorms(int fieldNumber)
    throws IOException {
    if ((normGen == null && preLockless) || (normGen != null && normGen[fieldNumber] == CHECK_DIR)) {
      String fileName = name + ".s" + fieldNumber;
      return dir.fileExists(fileName);
    } else if (normGen == null || normGen[fieldNumber] == NO) {
      return false;
    } else {
      return true;
    }
  }
  public boolean hasSeparateNorms()
    throws IOException {
    if (normGen == null) {
      if (!preLockless) {
        return false;
      } else {
        String[] result = dir.listAll();
        if (result == null)
          throw new IOException("cannot read directory " + dir + ": listAll() returned null");
        final IndexFileNameFilter filter = IndexFileNameFilter.getFilter();
        String pattern;
        pattern = name + ".s";
        int patternLength = pattern.length();
        for(int i = 0; i < result.length; i++){
          String fileName = result[i];
          if (filter.accept(null, fileName) && fileName.startsWith(pattern) && Character.isDigit(fileName.charAt(patternLength)))
              return true;
        }
        return false;
      }
    } else {
      for(int i=0;i<normGen.length;i++) {
        if (normGen[i] >= YES) {
          return true;
        }
      }
      for(int i=0;i<normGen.length;i++) {
        if (normGen[i] == CHECK_DIR) {
          if (hasSeparateNorms(i)) {
            return true;
          }
        }
      }
    }
    return false;
  }
  void advanceNormGen(int fieldIndex) {
    if (normGen[fieldIndex] == NO) {
      normGen[fieldIndex] = YES;
    } else {
      normGen[fieldIndex]++;
    }
    clearFiles();
  }
  public String getNormFileName(int number) throws IOException {
    long gen;
    if (normGen == null) {
      gen = CHECK_DIR;
    } else {
      gen = normGen[number];
    }
    if (hasSeparateNorms(number)) {
      return IndexFileNames.fileNameFromGeneration(name, "s" + number, gen);
    }
    if (hasSingleNormFile) {
      return IndexFileNames.fileNameFromGeneration(name, IndexFileNames.NORMS_EXTENSION, WITHOUT_GEN);
    }
    return IndexFileNames.fileNameFromGeneration(name, "f" + number, WITHOUT_GEN);
  }
  void setUseCompoundFile(boolean isCompoundFile) {
    if (isCompoundFile) {
      this.isCompoundFile = YES;
    } else {
      this.isCompoundFile = NO;
    }
    clearFiles();
  }
  public boolean getUseCompoundFile() throws IOException {
    if (isCompoundFile == NO) {
      return false;
    } else if (isCompoundFile == YES) {
      return true;
    } else {
      return dir.fileExists(IndexFileNames.segmentFileName(name, IndexFileNames.COMPOUND_FILE_EXTENSION));
    }
  }
  public int getDelCount() throws IOException {
    if (delCount == -1) {
      if (hasDeletions()) {
        final String delFileName = getDelFileName();
        delCount = new BitVector(dir, delFileName).count();
      } else
        delCount = 0;
    }
    assert delCount <= docCount;
    return delCount;
  }
  void setDelCount(int delCount) {
    this.delCount = delCount;
    assert delCount <= docCount;
  }
  public int getDocStoreOffset() {
    return docStoreOffset;
  }
  public boolean getDocStoreIsCompoundFile() {
    return docStoreIsCompoundFile;
  }
  void setDocStoreIsCompoundFile(boolean v) {
    docStoreIsCompoundFile = v;
    clearFiles();
  }
  public String getDocStoreSegment() {
    return docStoreSegment;
  }
  void setDocStoreOffset(int offset) {
    docStoreOffset = offset;
    clearFiles();
  }
  void setDocStore(int offset, String segment, boolean isCompoundFile) {        
    docStoreOffset = offset;
    docStoreSegment = segment;
    docStoreIsCompoundFile = isCompoundFile;
  }
  void write(IndexOutput output)
    throws IOException {
    assert delCount <= docCount: "delCount=" + delCount + " docCount=" + docCount + " segment=" + name;
    output.writeString(name);
    output.writeInt(docCount);
    output.writeLong(delGen);
    output.writeInt(docStoreOffset);
    if (docStoreOffset != -1) {
      output.writeString(docStoreSegment);
      output.writeByte((byte) (docStoreIsCompoundFile ? 1:0));
    }
    output.writeByte((byte) (hasSingleNormFile ? 1:0));
    if (normGen == null) {
      output.writeInt(NO);
    } else {
      output.writeInt(normGen.length);
      for(int j = 0; j < normGen.length; j++) {
        output.writeLong(normGen[j]);
      }
    }
    output.writeByte(isCompoundFile);
    output.writeInt(delCount);
    output.writeByte((byte) (hasProx ? 1:0));
    output.writeStringStringMap(diagnostics);
  }
  void setHasProx(boolean hasProx) {
    this.hasProx = hasProx;
    clearFiles();
  }
  public boolean getHasProx() {
    return hasProx;
  }
  private void addIfExists(List<String> files, String fileName) throws IOException {
    if (dir.fileExists(fileName))
      files.add(fileName);
  }
  public List<String> files() throws IOException {
    if (files != null) {
      return files;
    }
    files = new ArrayList<String>();
    boolean useCompoundFile = getUseCompoundFile();
    if (useCompoundFile) {
      files.add(IndexFileNames.segmentFileName(name, IndexFileNames.COMPOUND_FILE_EXTENSION));
    } else {
      for (String ext : IndexFileNames.NON_STORE_INDEX_EXTENSIONS)
        addIfExists(files, IndexFileNames.segmentFileName(name, ext));
    }
    if (docStoreOffset != -1) {
      assert docStoreSegment != null;
      if (docStoreIsCompoundFile) {
        files.add(IndexFileNames.segmentFileName(docStoreSegment, IndexFileNames.COMPOUND_FILE_STORE_EXTENSION));
      } else {
        for (String ext : IndexFileNames.STORE_INDEX_EXTENSIONS)
          addIfExists(files, IndexFileNames.segmentFileName(docStoreSegment, ext));
      }
    } else if (!useCompoundFile) {
      for (String ext : IndexFileNames.STORE_INDEX_EXTENSIONS)
        addIfExists(files, IndexFileNames.segmentFileName(name, ext));
    }
    String delFileName = IndexFileNames.fileNameFromGeneration(name, IndexFileNames.DELETES_EXTENSION, delGen);
    if (delFileName != null && (delGen >= YES || dir.fileExists(delFileName))) {
      files.add(delFileName);
    }
    if (normGen != null) {
      for(int i=0;i<normGen.length;i++) {
        long gen = normGen[i];
        if (gen >= YES) {
          files.add(IndexFileNames.fileNameFromGeneration(name, IndexFileNames.SEPARATE_NORMS_EXTENSION + i, gen));
        } else if (NO == gen) {
          if (!hasSingleNormFile && !useCompoundFile) {
            String fileName = IndexFileNames.segmentFileName(name, IndexFileNames.PLAIN_NORMS_EXTENSION + i);
            if (dir.fileExists(fileName)) {
              files.add(fileName);
            }
          }
        } else if (CHECK_DIR == gen) {
          String fileName = null;
          if (useCompoundFile) {
            fileName = IndexFileNames.segmentFileName(name, IndexFileNames.SEPARATE_NORMS_EXTENSION + i);
          } else if (!hasSingleNormFile) {
            fileName = IndexFileNames.segmentFileName(name, IndexFileNames.PLAIN_NORMS_EXTENSION + i);
          }
          if (fileName != null && dir.fileExists(fileName)) {
            files.add(fileName);
          }
        }
      }
    } else if (preLockless || (!hasSingleNormFile && !useCompoundFile)) {
      String prefix;
      if (useCompoundFile)
        prefix = IndexFileNames.segmentFileName(name, IndexFileNames.SEPARATE_NORMS_EXTENSION);
      else
        prefix = IndexFileNames.segmentFileName(name, IndexFileNames.PLAIN_NORMS_EXTENSION);
      int prefixLength = prefix.length();
      String[] allFiles = dir.listAll();
      final IndexFileNameFilter filter = IndexFileNameFilter.getFilter();
      for(int i=0;i<allFiles.length;i++) {
        String fileName = allFiles[i];
        if (filter.accept(null, fileName) && fileName.length() > prefixLength && Character.isDigit(fileName.charAt(prefixLength)) && fileName.startsWith(prefix)) {
          files.add(fileName);
        }
      }
    }
    return files;
  }
  private void clearFiles() {
    files = null;
    sizeInBytes = -1;
  }
  @Override
  public String toString() {
    return toString(dir, 0);
  }
  public String toString(Directory dir, int pendingDelCount) {
    StringBuilder s = new StringBuilder();
    s.append(name).append(':');
    char cfs;
    try {
      if (getUseCompoundFile()) {
        cfs = 'c';
      } else {
        cfs = 'C';
      }
    } catch (IOException ioe) {
      cfs = '?';
    }
    s.append(cfs);
    if (this.dir != dir) {
      s.append('x');
    }
    s.append(docCount);
    int delCount;
    try {
      delCount = getDelCount();
    } catch (IOException ioe) {
      delCount = -1;
    }
    if (delCount != -1) {
      delCount += pendingDelCount;
    }
    if (delCount != 0) {
      s.append('/');
      if (delCount == -1) {
        s.append('?');
      } else {
        s.append(delCount);
      }
    }
    if (docStoreOffset != -1) {
      s.append("->").append(docStoreSegment);
    }
    return s.toString();
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj instanceof SegmentInfo) {
      final SegmentInfo other = (SegmentInfo) obj;
      return other.dir == dir && other.name.equals(name);
    } else {
      return false;
    }
  }
  @Override
  public int hashCode() {
    return dir.hashCode() + name.hashCode();
  }
}
