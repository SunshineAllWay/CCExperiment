package org.apache.lucene.util;
import java.io.File;
import java.io.IOException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MergeScheduler;
import org.apache.lucene.index.ConcurrentMergeScheduler;
import org.apache.lucene.index.CheckIndex;
import org.apache.lucene.store.Directory;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Random;
public class _TestUtil {
  public static File getTempDir(String desc) {
    return new File(LuceneTestCaseJ4.TEMP_DIR, desc + "." + new Random().nextLong());
  }
  public static void rmDir(File dir) throws IOException {
    if (dir.exists()) {
      File[] files = dir.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (!files[i].delete()) {
          throw new IOException("could not delete " + files[i]);
        }
      }
      dir.delete();
    }
  }
  public static void rmDir(String dir) throws IOException {
    rmDir(new File(dir));
  }
  public static void syncConcurrentMerges(IndexWriter writer) {
    syncConcurrentMerges(writer.getConfig().getMergeScheduler());
  }
  public static void syncConcurrentMerges(MergeScheduler ms) {
    if (ms instanceof ConcurrentMergeScheduler)
      ((ConcurrentMergeScheduler) ms).sync();
  }
  public static boolean checkIndex(Directory dir) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
    CheckIndex checker = new CheckIndex(dir);
    checker.setInfoStream(new PrintStream(bos));
    CheckIndex.Status indexStatus = checker.checkIndex();
    if (indexStatus == null || indexStatus.clean == false) {
      System.out.println("CheckIndex failed");
      System.out.println(bos.toString());
      throw new RuntimeException("CheckIndex failed");
    } else
      return true;
  }
  @Deprecated
  public static String arrayToString(int[] array) {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    for(int i=0;i<array.length;i++) {
      if (i > 0) {
        buf.append(" ");
      }
      buf.append(array[i]);
    }
    buf.append("]");
    return buf.toString();
  }
  @Deprecated
  public static String arrayToString(Object[] array) {
    StringBuilder buf = new StringBuilder();
    buf.append("[");
    for(int i=0;i<array.length;i++) {
      if (i > 0) {
        buf.append(" ");
      }
      buf.append(array[i]);
    }
    buf.append("]");
    return buf.toString();
  }
  public static int getRandomSocketPort() {
    return 1024 + new Random().nextInt(64512);
  }
}
