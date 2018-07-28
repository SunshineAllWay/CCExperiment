package org.apache.lucene.benchmark.stats;
public class MemUsage {
  public long maxFree, minFree, avgFree;
  public long maxTotal, minTotal, avgTotal;
  @Override
  public String toString() {
    return toScaledString(1, "B");
  }
  public String toScaledString(int div, String unit) {
    StringBuffer sb = new StringBuffer();
      sb.append("free=").append(minFree / div);
      sb.append("/").append(avgFree / div);
      sb.append("/").append(maxFree / div).append(" ").append(unit);
      sb.append(", total=").append(minTotal / div);
      sb.append("/").append(avgTotal / div);
      sb.append("/").append(maxTotal / div).append(" ").append(unit);
    return sb.toString();
  }
}
