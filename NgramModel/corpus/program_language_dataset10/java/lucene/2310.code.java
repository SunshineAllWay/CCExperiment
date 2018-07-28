package org.apache.solr.core;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.SolrIndexSearcher;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
class RunExecutableListener extends AbstractSolrEventListener {
  public RunExecutableListener(SolrCore core) {
    super(core);
  }
  protected String[] cmd;
  protected File dir;
  protected String[] envp;
  protected boolean wait=true;
  public void init(NamedList args) {
    super.init(args);
    List cmdlist = new ArrayList();
    cmdlist.add(args.get("exe"));
    List lst = (List)args.get("args");
    if (lst != null) cmdlist.addAll(lst);
    cmd = (String[])cmdlist.toArray(new String[cmdlist.size()]);
    lst = (List)args.get("env");
    if (lst != null) {
      envp = (String[])lst.toArray(new String[lst.size()]);
    }
    String str = (String)args.get("dir");
    if (str==null || str.equals("") || str.equals(".") || str.equals("./")) {
      dir = null;
    } else {
      dir = new File(str);
    }
    if ("false".equals(args.get("wait")) || Boolean.FALSE.equals(args.get("wait"))) wait=false;
  }
  protected int exec(String callback) {
    int ret = 0;
    try {
      boolean doLog = log.isDebugEnabled();
      if (doLog) {
        log.debug("About to exec " + cmd[0]);
      }
      Process proc = Runtime.getRuntime().exec(cmd, envp ,dir);
      if (wait) {
        try {
          ret = proc.waitFor();
        } catch (InterruptedException e) {
          SolrException.log(log,e);
          ret = INVALID_PROCESS_RETURN_CODE;
        }
      }
      if (wait && doLog) {
        log.debug("Executable " + cmd[0] + " returned " + ret);
      }
    } catch (IOException e) {
      SolrException.log(log,e);
      ret = INVALID_PROCESS_RETURN_CODE;
    }
    return ret;
  }
  public void postCommit() {
    exec("postCommit");
  }
  public void newSearcher(SolrIndexSearcher newSearcher, SolrIndexSearcher currentSearcher) {
    exec("newSearcher");
  }
  private static int INVALID_PROCESS_RETURN_CODE = -1;
}
