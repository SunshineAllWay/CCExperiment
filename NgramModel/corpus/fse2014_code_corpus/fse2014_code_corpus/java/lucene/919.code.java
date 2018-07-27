package org.apache.lucene.benchmark.byTask.utils;
import java.io.File;
import java.io.IOException;
public class FileUtils {
  public static boolean fullyDelete(File dir) throws IOException {
    if (dir == null || !dir.exists()) return false;
    File contents[] = dir.listFiles();
    if (contents != null) {
      for (int i = 0; i < contents.length; i++) {
        if (contents[i].isFile()) {
          if (!contents[i].delete()) {
            return false;
          }
        } else {
          if (!fullyDelete(contents[i])) {
            return false;
          }
        }
      }
    }
    return dir.delete();
  }
}
