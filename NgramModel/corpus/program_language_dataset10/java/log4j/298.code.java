package org.apache.log4j;
import java.util.Vector;
import org.apache.log4j.spi.LoggingEvent;
public class VectorAppender extends AppenderSkeleton {
  public Vector vector;
  public VectorAppender() {
    vector = new Vector();
  }
  public void activateOptions() {
  }
  public void append(LoggingEvent event) {
    try {
      Thread.sleep(100);
    } catch(Exception e) {
    }
    vector.addElement(event);
   }
  public Vector getVector() {
    return vector;
  }
  public synchronized void close() {
    if(this.closed)
      return;
    this.closed = true;
  }
  public boolean isClosed() {
    return closed;
  }
  public boolean requiresLayout() {
    return false;
  }
}
