package org.apache.lucene.benchmark.byTask.tasks;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Properties;
import org.apache.lucene.benchmark.BenchmarkTestCase;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class CreateIndexTaskTest extends BenchmarkTestCase {
  private PerfRunData createPerfRunData(String infoStreamValue) throws Exception {
    Properties props = new Properties();
    props.setProperty("print.props", "false"); 
    props.setProperty("directory", "RAMDirectory");
    props.setProperty("writer.info.stream", infoStreamValue);
    Config config = new Config(props);
    return new PerfRunData(config);
  }
  public void testInfoStream_SystemOutErr() throws Exception {
    PrintStream curOut = System.out;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));
    try {
      PerfRunData runData = createPerfRunData("SystemOut");
      CreateIndexTask cit = new CreateIndexTask(runData);
      cit.doLogic();
      new CloseIndexTask(runData).doLogic();
      assertTrue(baos.size() > 0);
    } finally {
      System.setOut(curOut);
    }
    PrintStream curErr = System.err;
    baos.reset();
    System.setErr(new PrintStream(baos));
    try {
      PerfRunData runData = createPerfRunData("SystemErr");
      CreateIndexTask cit = new CreateIndexTask(runData);
      cit.doLogic();
      new CloseIndexTask(runData).doLogic();
      assertTrue(baos.size() > 0);
    } finally {
      System.setErr(curErr);
    }
  }
  public void testInfoStream_File() throws Exception {
    File outFile = new File(getWorkDir(), "infoStreamTest");
    PerfRunData runData = createPerfRunData(outFile.getAbsolutePath());
    new CreateIndexTask(runData).doLogic();
    new CloseIndexTask(runData).doLogic();
    assertTrue(outFile.length() > 0);
  }
}
