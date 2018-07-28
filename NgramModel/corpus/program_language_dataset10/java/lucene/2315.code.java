package org.apache.solr.core;
@Deprecated
public class SolrException extends org.apache.solr.common.SolrException {
  public SolrException(ErrorCode code, String msg, boolean alreadyLogged) {
    super(code, msg, alreadyLogged);
  }
  public SolrException(ErrorCode code, String msg, Throwable th, boolean alreadyLogged) {
    super(code, msg, th, alreadyLogged);
  }
  public SolrException(ErrorCode code, String msg, Throwable th) {
    super(code, msg, th);
  }
  public SolrException(ErrorCode code, Throwable th) {
    super(code, th);
  }
  public SolrException(ErrorCode code, String msg) {
    super(code, msg);
  }
}
