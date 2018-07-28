package org.apache.lucene.benchmark.stats;
public class TimeData {
  public String name;
  public long count = 0;
  public long elapsed = 0L;
  private long delta = 0L;
  public long freeMem = 0L;
  public long totalMem = 0L;
  public TimeData() {}
  public TimeData(String name) {
    this.name = name;
  }
  public void start() {
    delta = System.currentTimeMillis();
  }
  public void stop() {
    count++;
    elapsed += (System.currentTimeMillis() - delta);
  }
  public void recordMemUsage() {
    freeMem = Runtime.getRuntime().freeMemory();
    totalMem = Runtime.getRuntime().totalMemory();
  }
  public void reset() {
    count = 0;
    elapsed = 0L;
    delta = elapsed;
  }
  @Override
  protected Object clone() {
    TimeData td = new TimeData(name);
    td.name = name;
    td.elapsed = elapsed;
    td.count = count;
    td.delta = delta;
    td.freeMem = freeMem;
    td.totalMem = totalMem;
    return td;
  }
  public double getRate() {
    double rps = count * 1000.0 / (elapsed > 0 ? elapsed : 1); 
    return rps;
  }
  public static String getLabels() {
    return "# count\telapsed\trec/s\tfreeMem\ttotalMem";
  }
  @Override
  public String toString() { return toString(true); }
  public String toString(boolean withMem) {
    StringBuffer sb = new StringBuffer();
    sb.append(count + "\t" + elapsed + "\t" + getRate());
    if (withMem) sb.append("\t" + freeMem + "\t" + totalMem);
    return sb.toString();
  }
}
