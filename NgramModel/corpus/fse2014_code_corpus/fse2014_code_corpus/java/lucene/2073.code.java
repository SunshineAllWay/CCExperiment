package org.apache.solr.handler.dataimport;
public class DataImportHandlerException extends RuntimeException {
  private int errCode;
  public boolean debugged = false;
  public static final int SEVERE = 500, WARN = 400, SKIP = 300, SKIP_ROW =301;
  public DataImportHandlerException(int err) {
    super();
    errCode = err;
  }
  public DataImportHandlerException(int err, String message) {
    super(message + (SolrWriter.getDocCount() == null ? "" : MSG + SolrWriter.getDocCount()));
    errCode = err;
  }
  public DataImportHandlerException(int err, String message, Throwable cause) {
    super(message + (SolrWriter.getDocCount() == null ? "" : MSG + SolrWriter.getDocCount()), cause);
    errCode = err;
  }
  public DataImportHandlerException(int err, Throwable cause) {
    super(cause);
    errCode = err;
  }
  public int getErrCode() {
    return errCode;
  }
  public static void wrapAndThrow(int err, Exception e) {
    if (e instanceof DataImportHandlerException) {
      throw (DataImportHandlerException) e;
    } else {
      throw new DataImportHandlerException(err, e);
    }
  }
  public static void wrapAndThrow(int err, Exception e, String msg) {
    if (e instanceof DataImportHandlerException) {
      throw (DataImportHandlerException) e;
    } else {
      throw new DataImportHandlerException(err, msg, e);
    }
  }
  public static final String MSG = " Processing Document # ";
}
