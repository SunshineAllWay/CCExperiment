package org.apache.log4j;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.LoggingEvent;
class Dispatcher extends Thread {
  private org.apache.log4j.helpers.BoundedFIFO bf;
  private AppenderAttachableImpl aai;
  private boolean interrupted = false;
  AsyncAppender container;
  Dispatcher(org.apache.log4j.helpers.BoundedFIFO bf, AsyncAppender container) {
    this.bf = bf;
    this.container = container;
    this.aai = container.aai;
    this.setDaemon(true);
    this.setPriority(Thread.MIN_PRIORITY);
    this.setName("Dispatcher-" + getName());
  }
  void close() {
    synchronized (bf) {
      interrupted = true;
      if (bf.length() == 0) {
        bf.notify();
      }
    }
  }
  public void run() {
    LoggingEvent event;
    while (true) {
      synchronized (bf) {
        if (bf.length() == 0) {
          if (interrupted) {
            break;
          }
          try {
            bf.wait();
          } catch (InterruptedException e) {
            break;
          }
        }
        event = bf.get();
        if (bf.wasFull()) {
          bf.notify();
        }
      }
      synchronized (container.aai) {
        if ((aai != null) && (event != null)) {
          aai.appendLoopOnAppenders(event);
        }
      }
    }
    aai.removeAllAppenders();
  }
}
