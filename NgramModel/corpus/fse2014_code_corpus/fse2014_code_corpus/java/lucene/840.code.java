package org.apache.lucene.benchmark.byTask;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import org.apache.lucene.benchmark.byTask.utils.Algorithm;
import org.apache.lucene.benchmark.byTask.utils.Config;
public class Benchmark {
  private PerfRunData runData;
  private Algorithm algorithm;
  private boolean executed;
  public Benchmark (Reader algReader) throws Exception {
    try {
      runData = new PerfRunData(new Config(algReader));
    } catch (Exception e) {
      e.printStackTrace();
      throw new Exception("Error: cannot init PerfRunData!",e);
    }
    try {
      algorithm = new Algorithm(runData);
    } catch (Exception e) {
      throw new Exception("Error: cannot understand algorithm!",e);
    }
  }
  public synchronized void  execute() throws Exception {
    if (executed) {
      throw new IllegalStateException("Benchmark was already executed");
    }
    executed = true;
    runData.setStartTimeMillis();
    algorithm.execute();
  }
  public static void main(String[] args) {
    if (args.length < 1) {
      System.err.println("Usage: java Benchmark <algorithm file>");
      System.exit(1);
    }
    File algFile = new File(args[0]);
    if (!algFile.exists() || !algFile.isFile() || !algFile.canRead()) {
      System.err.println("cannot find/read algorithm file: "+algFile.getAbsolutePath()); 
      System.exit(1);
    }
    System.out.println("Running algorithm from: "+algFile.getAbsolutePath());
    Benchmark benchmark = null;
    try {
      benchmark = new Benchmark(new FileReader(algFile));
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("------------> algorithm:");
    System.out.println(benchmark.getAlgorithm().toString());
    try {
      benchmark.execute();
    } catch (Exception e) {
      System.err.println("Error: cannot execute the algorithm! "+e.getMessage());
      e.printStackTrace();
    }
    System.out.println("####################");
    System.out.println("###  D O N E !!! ###");
    System.out.println("####################");
  }
  public Algorithm getAlgorithm() {
    return algorithm;
  }
  public PerfRunData getRunData() {
    return runData;
  }
}
