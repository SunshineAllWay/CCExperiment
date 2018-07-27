package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class SetPropTask extends PerfTask {
  public SetPropTask(PerfRunData runData) {
    super(runData);
  }
  private String name;
  private String value;
  @Override
  public int doLogic() throws Exception {
    if (name==null || value==null) {
      throw new Exception(getName()+" - undefined name or value: name="+name+" value="+value);
    }
    getRunData().getConfig().set(name,value);
    return 0;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    int k = params.indexOf(",");
    name = params.substring(0,k).trim();
    value = params.substring(k+1).trim();
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
