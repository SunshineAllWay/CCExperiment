package org.apache.log4j.lf5;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
public abstract class LogRecord implements java.io.Serializable {
  protected static long _seqCount = 0;
  protected LogLevel _level;
  protected String _message;
  protected long _sequenceNumber;
  protected long _millis;
  protected String _category;
  protected String _thread;
  protected String _thrownStackTrace;
  protected Throwable _thrown;
  protected String _ndc;
  protected String _location;
  public LogRecord() {
    super();
    _millis = System.currentTimeMillis();
    _category = "Debug";
    _message = "";
    _level = LogLevel.INFO;
    _sequenceNumber = getNextId();
    _thread = Thread.currentThread().toString();
    _ndc = "";
    _location = "";
  }
  public LogLevel getLevel() {
    return (_level);
  }
  public void setLevel(LogLevel level) {
    _level = level;
  }
  public abstract boolean isSevereLevel();
  public boolean hasThrown() {
    Throwable thrown = getThrown();
    if (thrown == null) {
      return false;
    }
    String thrownString = thrown.toString();
    return thrownString != null && thrownString.trim().length() != 0;
  }
  public boolean isFatal() {
    return isSevereLevel() || hasThrown();
  }
  public String getCategory() {
    return (_category);
  }
  public void setCategory(String category) {
    _category = category;
  }
  public String getMessage() {
    return (_message);
  }
  public void setMessage(String message) {
    _message = message;
  }
  public long getSequenceNumber() {
    return (_sequenceNumber);
  }
  public void setSequenceNumber(long number) {
    _sequenceNumber = number;
  }
  public long getMillis() {
    return _millis;
  }
  public void setMillis(long millis) {
    _millis = millis;
  }
  public String getThreadDescription() {
    return (_thread);
  }
  public void setThreadDescription(String threadDescription) {
    _thread = threadDescription;
  }
  public String getThrownStackTrace() {
    return (_thrownStackTrace);
  }
  public void setThrownStackTrace(String trace) {
    _thrownStackTrace = trace;
  }
  public Throwable getThrown() {
    return (_thrown);
  }
  public void setThrown(Throwable thrown) {
    if (thrown == null) {
      return;
    }
    _thrown = thrown;
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    thrown.printStackTrace(out);
    out.flush();
    _thrownStackTrace = sw.toString();
    try {
      out.close();
      sw.close();
    } catch (IOException e) {
    }
    out = null;
    sw = null;
  }
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("LogRecord: [" + _level + ", " + _message + "]");
    return (buf.toString());
  }
  public String getNDC() {
    return _ndc;
  }
  public void setNDC(String ndc) {
    _ndc = ndc;
  }
  public String getLocation() {
    return _location;
  }
  public void setLocation(String location) {
    _location = location;
  }
  public static synchronized void resetSequenceNumber() {
    _seqCount = 0;
  }
  protected static synchronized long getNextId() {
    _seqCount++;
    return _seqCount;
  }
}
