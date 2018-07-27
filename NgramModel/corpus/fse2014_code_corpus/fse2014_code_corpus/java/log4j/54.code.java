package org.apache.log4j;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
public class AsyncAppender extends AppenderSkeleton
  implements AppenderAttachable {
  public static final int DEFAULT_BUFFER_SIZE = 128;
  private final List buffer = new ArrayList();
  private final Map discardMap = new HashMap();
  private int bufferSize = DEFAULT_BUFFER_SIZE;
  AppenderAttachableImpl aai;
  private final AppenderAttachableImpl appenders;
  private final Thread dispatcher;
  private boolean locationInfo = false;
  private boolean blocking = true;
  public AsyncAppender() {
    appenders = new AppenderAttachableImpl();
    aai = appenders;
    dispatcher =
      new Thread(new Dispatcher(this, buffer, discardMap, appenders));
    dispatcher.setDaemon(true);
    dispatcher.setName("AsyncAppender-Dispatcher-" + dispatcher.getName());
    dispatcher.start();
  }
  public void addAppender(final Appender newAppender) {
    synchronized (appenders) {
      appenders.addAppender(newAppender);
    }
  }
  public void append(final LoggingEvent event) {
    if ((dispatcher == null) || !dispatcher.isAlive() || (bufferSize <= 0)) {
      synchronized (appenders) {
        appenders.appendLoopOnAppenders(event);
      }
      return;
    }
    event.getNDC();
    event.getThreadName();
    event.getMDCCopy();
    if (locationInfo) {
      event.getLocationInformation();
    }
    event.getRenderedMessage();
    event.getThrowableStrRep();
    synchronized (buffer) {
      while (true) {
        int previousSize = buffer.size();
        if (previousSize < bufferSize) {
          buffer.add(event);
          if (previousSize == 0) {
            buffer.notifyAll();
          }
          break;
        }
        boolean discard = true;
        if (blocking
                && !Thread.interrupted()
                && Thread.currentThread() != dispatcher) {
          try {
            buffer.wait();
            discard = false;
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        if (discard) {
          String loggerName = event.getLoggerName();
          DiscardSummary summary = (DiscardSummary) discardMap.get(loggerName);
          if (summary == null) {
            summary = new DiscardSummary(event);
            discardMap.put(loggerName, summary);
          } else {
            summary.add(event);
          }
          break;
        }
      }
    }
  }
  public void close() {
    synchronized (buffer) {
      closed = true;
      buffer.notifyAll();
    }
    try {
      dispatcher.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      org.apache.log4j.helpers.LogLog.error(
        "Got an InterruptedException while waiting for the "
        + "dispatcher to finish.", e);
    }
    synchronized (appenders) {
      Enumeration iter = appenders.getAllAppenders();
      if (iter != null) {
        while (iter.hasMoreElements()) {
          Object next = iter.nextElement();
          if (next instanceof Appender) {
            ((Appender) next).close();
          }
        }
      }
    }
  }
  public Enumeration getAllAppenders() {
    synchronized (appenders) {
      return appenders.getAllAppenders();
    }
  }
  public Appender getAppender(final String name) {
    synchronized (appenders) {
      return appenders.getAppender(name);
    }
  }
  public boolean getLocationInfo() {
    return locationInfo;
  }
  public boolean isAttached(final Appender appender) {
    synchronized (appenders) {
      return appenders.isAttached(appender);
    }
  }
  public boolean requiresLayout() {
    return false;
  }
  public void removeAllAppenders() {
    synchronized (appenders) {
      appenders.removeAllAppenders();
    }
  }
  public void removeAppender(final Appender appender) {
    synchronized (appenders) {
      appenders.removeAppender(appender);
    }
  }
  public void removeAppender(final String name) {
    synchronized (appenders) {
      appenders.removeAppender(name);
    }
  }
  public void setLocationInfo(final boolean flag) {
    locationInfo = flag;
  }
  public void setBufferSize(final int size) {
    if (size < 0) {
      throw new java.lang.NegativeArraySizeException("size");
    }
    synchronized (buffer) {
      bufferSize = (size < 1) ? 1 : size;
      buffer.notifyAll();
    }
  }
  public int getBufferSize() {
    return bufferSize;
  }
  public void setBlocking(final boolean value) {
    synchronized (buffer) {
      blocking = value;
      buffer.notifyAll();
    }
  }
  public boolean getBlocking() {
    return blocking;
  }
  private static final class DiscardSummary {
    private LoggingEvent maxEvent;
    private int count;
    public DiscardSummary(final LoggingEvent event) {
      maxEvent = event;
      count = 1;
    }
    public void add(final LoggingEvent event) {
      if (event.getLevel().toInt() > maxEvent.getLevel().toInt()) {
        maxEvent = event;
      }
      count++;
    }
    public LoggingEvent createEvent() {
      String msg =
        MessageFormat.format(
          "Discarded {0} messages due to full event buffer including: {1}",
          new Object[] { new Integer(count), maxEvent.getMessage() });
      return new LoggingEvent(
              "org.apache.log4j.AsyncAppender.DONT_REPORT_LOCATION",
              Logger.getLogger(maxEvent.getLoggerName()),
              maxEvent.getLevel(),
              msg,
              null);
    }
  }
  private static class Dispatcher implements Runnable {
    private final AsyncAppender parent;
    private final List buffer;
    private final Map discardMap;
    private final AppenderAttachableImpl appenders;
    public Dispatcher(
      final AsyncAppender parent, final List buffer, final Map discardMap,
      final AppenderAttachableImpl appenders) {
      this.parent = parent;
      this.buffer = buffer;
      this.appenders = appenders;
      this.discardMap = discardMap;
    }
    public void run() {
      boolean isActive = true;
      try {
        while (isActive) {
          LoggingEvent[] events = null;
          synchronized (buffer) {
            int bufferSize = buffer.size();
            isActive = !parent.closed;
            while ((bufferSize == 0) && isActive) {
              buffer.wait();
              bufferSize = buffer.size();
              isActive = !parent.closed;
            }
            if (bufferSize > 0) {
              events = new LoggingEvent[bufferSize + discardMap.size()];
              buffer.toArray(events);
              int index = bufferSize;
              for (
                Iterator iter = discardMap.values().iterator();
                  iter.hasNext();) {
                events[index++] = ((DiscardSummary) iter.next()).createEvent();
              }
              buffer.clear();
              discardMap.clear();
              buffer.notifyAll();
            }
          }
          if (events != null) {
            for (int i = 0; i < events.length; i++) {
              synchronized (appenders) {
                appenders.appendLoopOnAppenders(events[i]);
              }
            }
          }
        }
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
      }
    }
  }
}
