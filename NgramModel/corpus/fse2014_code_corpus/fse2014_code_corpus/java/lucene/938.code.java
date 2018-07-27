package org.apache.lucene.benchmark.stats;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Collection;
import java.util.Iterator;
public class TestRunData {
  private String id;
  private long start = 0L, end = 0L;
  private LinkedHashMap<String,Vector<TimeData>> data = new LinkedHashMap<String,Vector<TimeData>>();
  public TestRunData() {}
  public TestRunData(String id) {
    this.id = id;
  }
    public LinkedHashMap<String,Vector<TimeData>> getData()
    {
        return data;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }
    public long getEnd()
    {
        return end;
    }
    public long getStart()
    {
        return start;
    }
  public void startRun() {
    start = System.currentTimeMillis();
  }
  public void endRun() {
    end = System.currentTimeMillis();
  }
  public void addData(TimeData td) {
    td.recordMemUsage();
    Vector<TimeData> v = data.get(td.name);
    if (v == null) {
      v = new Vector<TimeData>();
      data.put(td.name, v);
    }
    v.add((TimeData)td.clone());
  }
  public Collection<String> getLabels() {
    return data.keySet();
  }
  public TimeData getTotals(String label) {
    Vector<TimeData> v = data.get(label);
      if (v == null)
      {
          return null;
      }
    TimeData res = new TimeData("TOTAL " + label);
    for (int i = 0; i < v.size(); i++) {
      TimeData td = v.get(i);
      res.count += td.count;
      res.elapsed += td.elapsed;
    }
    return res;
  }
  public Vector<TimeData> getTotals() {
    Collection<String> labels = getLabels();
    Vector<TimeData> v = new Vector<TimeData>();
    Iterator<String> it = labels.iterator();
    while (it.hasNext()) {
      TimeData td = getTotals(it.next());
      v.add(td);
    }
    return v;
  }
  public MemUsage getMemUsage(String label) {
    Vector<TimeData> v = data.get(label);
      if (v == null)
      {
          return null;
      }
    MemUsage res = new MemUsage();
    res.minFree = Long.MAX_VALUE;
    res.minTotal = Long.MAX_VALUE;
    long avgFree = 0L, avgTotal = 0L;
    for (int i = 0; i < v.size(); i++) {
      TimeData td = v.get(i);
        if (res.maxFree < td.freeMem)
        {
            res.maxFree = td.freeMem;
        }
        if (res.maxTotal < td.totalMem)
        {
            res.maxTotal = td.totalMem;
        }
        if (res.minFree > td.freeMem)
        {
            res.minFree = td.freeMem;
        }
        if (res.minTotal > td.totalMem)
        {
            res.minTotal = td.totalMem;
        }
      avgFree += td.freeMem;
      avgTotal += td.totalMem;
    }
    res.avgFree = avgFree / v.size();
    res.avgTotal = avgTotal / v.size();
    return res;
  }
  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    for (final String label : getLabels()) {
        sb.append(id).append("-").append(label).append(" ").append(getTotals(label).toString(false)).append(" ");
        sb.append(getMemUsage(label).toScaledString(1024 * 1024, "MB")).append("\n");
    }
    return sb.toString();
  }
}
