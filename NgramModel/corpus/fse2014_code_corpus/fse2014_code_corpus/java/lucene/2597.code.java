package org.apache.solr.util;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Deprecated
public class CommonParams implements org.apache.solr.common.params.CommonParams {
  public static Logger log = LoggerFactory.getLogger(CommonParams.class);
  public String fl = null;
  public String df = null;
  public String debugQuery = null;
  public String explainOther = null;
  public boolean highlight = false;
  public String highlightFields = null;
  public int maxSnippets = 1;
  public String highlightFormatterClass = null;
  public CommonParams() {
  }
  public CommonParams(NamedList args) {
    this();
    setValues(args);
  }
  public void setValues(NamedList args) {
    Object tmp;
    tmp = args.get(FL);
    if (null != tmp) {
      if (tmp instanceof String) {
        fl = tmp.toString();
      } else {
        log.error("init param is not a str: " + FL);
      }
    }
    tmp = args.get(DF);
    if (null != tmp) {
      if (tmp instanceof String) {
        df = tmp.toString();
      } else {
        log.error("init param is not a str: " + DF);
      }
    }
    tmp = args.get(DEBUG_QUERY);
    if (null != tmp) {
      if (tmp instanceof String) {
        debugQuery = tmp.toString();
      } else {
        log.error("init param is not a str: " + DEBUG_QUERY);
      }
    }
    tmp = args.get(EXPLAIN_OTHER);
    if (null != tmp) {
      if (tmp instanceof String) {
        explainOther = tmp.toString();
      } else {
        log.error("init param is not a str: " + EXPLAIN_OTHER);
      }
    }
  }
}
