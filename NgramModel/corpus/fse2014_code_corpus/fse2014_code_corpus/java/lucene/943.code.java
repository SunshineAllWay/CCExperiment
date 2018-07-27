package org.apache.lucene.benchmark;
import java.io.File;
import junit.framework.TestCase;
public class BenchmarkTestCase extends TestCase {
  private static final File workDir;
  static {
    workDir = new File(System.getProperty("benchmark.work.dir", "test/benchmark")).getAbsoluteFile();
    workDir.mkdirs();
  }
  public File getWorkDir() {
    return workDir;
  }
}
