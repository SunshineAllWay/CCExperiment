package org.apache.lucene.store;
import java.io.IOException;
import java.util.Set;
public class FileSwitchDirectory extends Directory {
  private final Directory secondaryDir;
  private final Directory primaryDir;
  private final Set<String> primaryExtensions;
  private boolean doClose;
  public FileSwitchDirectory(Set<String> primaryExtensions, Directory primaryDir, Directory secondaryDir, boolean doClose) {
    this.primaryExtensions = primaryExtensions;
    this.primaryDir = primaryDir;
    this.secondaryDir = secondaryDir;
    this.doClose = doClose;
    this.lockFactory = primaryDir.getLockFactory();
  }
  public Directory getPrimaryDir() {
    return primaryDir;
  }
  public Directory getSecondaryDir() {
    return secondaryDir;
  }
  @Override
  public void close() throws IOException {
    if (doClose) {
      try {
        secondaryDir.close();
      } finally { 
        primaryDir.close();
      }
      doClose = false;
    }
  }
  @Override
  public String[] listAll() throws IOException {
    String[] primaryFiles = primaryDir.listAll();
    String[] secondaryFiles = secondaryDir.listAll();
    String[] files = new String[primaryFiles.length + secondaryFiles.length];
    System.arraycopy(primaryFiles, 0, files, 0, primaryFiles.length);
    System.arraycopy(secondaryFiles, 0, files, primaryFiles.length, secondaryFiles.length);
    return files;
  }
  public static String getExtension(String name) {
    int i = name.lastIndexOf('.');
    if (i == -1) {
      return "";
    }
    return name.substring(i+1, name.length());
  }
  private Directory getDirectory(String name) {
    String ext = getExtension(name);
    if (primaryExtensions.contains(ext)) {
      return primaryDir;
    } else {
      return secondaryDir;
    }
  }
  @Override
  public boolean fileExists(String name) throws IOException {
    return getDirectory(name).fileExists(name);
  }
  @Override
  public long fileModified(String name) throws IOException {
    return getDirectory(name).fileModified(name);
  }
  @Override
  public void touchFile(String name) throws IOException {
    getDirectory(name).touchFile(name);
  }
  @Override
  public void deleteFile(String name) throws IOException {
    getDirectory(name).deleteFile(name);
  }
  @Override
  public long fileLength(String name) throws IOException {
    return getDirectory(name).fileLength(name);
  }
  @Override
  public IndexOutput createOutput(String name) throws IOException {
    return getDirectory(name).createOutput(name);
  }
  @Override
  public void sync(String name) throws IOException {
    getDirectory(name).sync(name);
  }
  @Override
  public IndexInput openInput(String name) throws IOException {
    return getDirectory(name).openInput(name);
  }
}
