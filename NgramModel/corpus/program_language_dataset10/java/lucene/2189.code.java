package org.apache.solr.common.util;
import java.io.*;
import java.nio.channels.FileChannel;
public class FileUtils {
  public static File resolvePath(File base, String path) {
    File r = new File(path);
    return r.isAbsolute() ? r : new File(base, path);
  }
  public static void copyFile(File src , File destination) throws IOException {
    FileChannel in = null;
    FileChannel out = null;
    try {
      in = new FileInputStream(src).getChannel();
      out = new FileOutputStream(destination).getChannel();
      in.transferTo(0, in.size(), out);
    } finally {
      try { if (in != null) in.close(); } catch (IOException e) {}
      try { if (out != null) out.close(); } catch (IOException e) {}
    }
  }
  public static void sync(File fullFile) throws IOException  {
    if (fullFile == null || !fullFile.exists())
      throw new FileNotFoundException("File does not exist " + fullFile);
    boolean success = false;
    int retryCount = 0;
    IOException exc = null;
    while(!success && retryCount < 5) {
      retryCount++;
      RandomAccessFile file = null;
      try {
        try {
          file = new RandomAccessFile(fullFile, "rw");
          file.getFD().sync();
          success = true;
        } finally {
          if (file != null)
            file.close();
        }
      } catch (IOException ioe) {
        if (exc == null)
          exc = ioe;
        try {
          Thread.sleep(5);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
        }
      }
    }
    if (!success)
      throw exc;
  }
}
