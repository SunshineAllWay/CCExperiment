package org.apache.lucene.benchmark.byTask.feeds;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.lucene.benchmark.byTask.utils.Config;
public abstract class ContentSource {
  private static final int BZIP = 0;
  private static final int OTHER = 1;
  private static final Map<String,Integer> extensionToType = new HashMap<String,Integer>();
  static {
    extensionToType.put(".bz2", Integer.valueOf(BZIP));
    extensionToType.put(".bzip", Integer.valueOf(BZIP));
  }
  protected static final int BUFFER_SIZE = 1 << 16; 
  private long bytesCount;
  private long totalBytesCount;
  private int docsCount;
  private int totalDocsCount;
  private Config config;
  protected boolean forever;
  protected int logStep;
  protected boolean verbose;
  protected String encoding;
  private CompressorStreamFactory csFactory = new CompressorStreamFactory();
  protected final synchronized void addBytes(long numBytes) {
    bytesCount += numBytes;
    totalBytesCount += numBytes;
  }
  protected final synchronized void addDoc() {
    ++docsCount;
    ++totalDocsCount;
  }
  protected final void collectFiles(File dir, ArrayList<File> files) {
    if (!dir.canRead()) {
      return;
    }
    File[] dirFiles = dir.listFiles();
    Arrays.sort(dirFiles);
    for (int i = 0; i < dirFiles.length; i++) {
      File file = dirFiles[i];
      if (file.isDirectory()) {
        collectFiles(file, files);
      } else if (file.canRead()) {
        files.add(file);
      }
    }
  }
  protected InputStream getInputStream(File file) throws IOException {
    InputStream is = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
    String fileName = file.getName();
    int idx = fileName.lastIndexOf('.');
    int type = OTHER;
    if (idx != -1) {
      Integer typeInt = extensionToType.get(fileName.substring(idx));
      if (typeInt != null) {
        type = typeInt.intValue();
      }
    }
    switch (type) {
      case BZIP:
        try {
          is = csFactory.createCompressorInputStream("bzip2", is);
        } catch (CompressorException e) {
          IOException ioe = new IOException(e.getMessage());
          ioe.initCause(e);
          throw ioe;
        }
        break;
      default: 
    }
    return is;
  }
  protected final boolean shouldLog() {
    return verbose && logStep > 0 && docsCount % logStep == 0;
  }
  public abstract void close() throws IOException;
  public final long getBytesCount() { return bytesCount; }
  public final int getDocsCount() { return docsCount; }
  public final Config getConfig() { return config; }
  public abstract DocData getNextDocData(DocData docData) throws NoMoreDataException, IOException;
  public final long getTotalBytesCount() { return totalBytesCount; }
  public final int getTotalDocsCount() { return totalDocsCount; }
  public void resetInputs() throws IOException {
    bytesCount = 0;
    docsCount = 0;
  }
  public void setConfig(Config config) {
    this.config = config;
    forever = config.get("content.source.forever", true);
    logStep = config.get("content.source.log.step", 0);
    verbose = config.get("content.source.verbose", false);
    encoding = config.get("content.source.encoding", null);
  }
}
