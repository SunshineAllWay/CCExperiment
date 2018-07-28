package org.apache.log4j.pattern;
import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.apache.log4j.NDC;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.Loader;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RendererSupport;
import org.apache.log4j.spi.ThrowableInformation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
public class LogEvent implements java.io.Serializable {
  private static long startTime = System.currentTimeMillis();
  transient public final String fqnOfCategoryClass;
  transient private Category logger;
  final public String categoryName;
  transient public Priority level;
  private String ndc;
  private Hashtable mdcCopy;
  private boolean ndcLookupRequired = true;
  private boolean mdcCopyLookupRequired = true;
  transient private Object message;
  private String renderedMessage;
  private String threadName;
  private ThrowableInformation throwableInfo;
  public final long timeStamp;
  private LocationInfo locationInfo;
  static final long serialVersionUID = -868428216207166145L;
  static final Integer[] PARAM_ARRAY = new Integer[1];
  static final String TO_LEVEL = "toLevel";
  static final Class[] TO_LEVEL_PARAMS = new Class[] {int.class};
  static final Hashtable methodCache = new Hashtable(3); 
  public LogEvent(String fqnOfCategoryClass, Category logger,
		      Priority level, Object message, Throwable throwable) {
    this.fqnOfCategoryClass = fqnOfCategoryClass;
    this.logger = logger;
    this.categoryName = logger.getName();
    this.level = level;
    this.message = message;
    if(throwable != null) {
      this.throwableInfo = new ThrowableInformation(throwable);
    }
    timeStamp = System.currentTimeMillis();
  }
  public LogEvent(String fqnOfCategoryClass, Category logger,
		      long timeStamp, Priority level, Object message,
		      Throwable throwable) {
    this.fqnOfCategoryClass = fqnOfCategoryClass;
    this.logger = logger;
    this.categoryName = logger.getName();
    this.level = level;
    this.message = message;
    if(throwable != null) {
      this.throwableInfo = new ThrowableInformation(throwable);
    }
    this.timeStamp = timeStamp;
  }
    public LogEvent(final String fqnOfCategoryClass,
                        final Logger logger,
                        final long timeStamp,
                        final Level level,
                        final Object message,
                        final String threadName,
                        final ThrowableInformation throwable,
                        final String ndc,
                        final LocationInfo info,
                        final java.util.Map properties) {
      super();
      this.fqnOfCategoryClass = fqnOfCategoryClass;
      this.logger = logger;
      if (logger != null) {
          categoryName = logger.getName();
      } else {
          categoryName = null;
      }
      this.level = level;
      this.message = message;
      if(throwable != null) {
        this.throwableInfo = throwable;
      }
      this.timeStamp = timeStamp;
      this.threadName = threadName;
      ndcLookupRequired = false;
      this.ndc = ndc;
      this.locationInfo = info;
      mdcCopyLookupRequired = false;
      if (properties != null) {
        mdcCopy = new java.util.Hashtable(properties);
      }
    }
  public LocationInfo getLocationInformation() {
    if(locationInfo == null) {
      locationInfo = new LocationInfo(new Throwable(), fqnOfCategoryClass);
    }
    return locationInfo;
  }
  public Level getLevel() {
    return (Level) level;
  }
  public String getLoggerName() {
    return categoryName;
  }
  public
  Object getMessage() {
    if(message != null) {
      return message;
    } else {
      return getRenderedMessage();
    }
  }
  public
  String getNDC() {
    if(ndcLookupRequired) {
      ndcLookupRequired = false;
      ndc = NDC.get();
    }
    return ndc;
  }
  public
  Object getMDC(String key) {
    Object r;
    if(mdcCopy != null) {
      r = mdcCopy.get(key);
      if(r != null) {
        return r;
      }
    }
    return MDC.get(key);
  }
  public
  void getMDCCopy() {
    if(mdcCopyLookupRequired) {
      mdcCopyLookupRequired = false;
      Hashtable t = (Hashtable) MDC.getContext();
      if(t != null) {
	mdcCopy = (Hashtable) t.clone();
      }
    }
  }
  public
  String getRenderedMessage() {
     if(renderedMessage == null && message != null) {
       if(message instanceof String)
	 renderedMessage = (String) message;
       else {
	 LoggerRepository repository = logger.getLoggerRepository();
	 if(repository instanceof RendererSupport) {
	   RendererSupport rs = (RendererSupport) repository;
	   renderedMessage= rs.getRendererMap().findAndRender(message);
	 } else {
	   renderedMessage = message.toString();
	 }
       }
     }
     return renderedMessage;
  }
  public static long getStartTime() {
    return startTime;
  }
  public
  String getThreadName() {
    if(threadName == null)
      threadName = (Thread.currentThread()).getName();
    return threadName;
  }
  public
  ThrowableInformation getThrowableInformation() {
    return throwableInfo;
  }
  public
  String[] getThrowableStrRep() {
    if(throwableInfo ==  null)
      return null;
    else
      return throwableInfo.getThrowableStrRep();
  }
  private
  void readLevel(ObjectInputStream ois)
                      throws java.io.IOException, ClassNotFoundException {
    int p = ois.readInt();
    try {
      String className = (String) ois.readObject();
      if(className == null) {
	level = Level.toLevel(p);
      } else {
	Method m = (Method) methodCache.get(className);
	if(m == null) {
	  Class clazz = Loader.loadClass(className);
	  m = clazz.getDeclaredMethod(TO_LEVEL, TO_LEVEL_PARAMS);
	  methodCache.put(className, m);
	}
	PARAM_ARRAY[0] = new Integer(p);
	level = (Level) m.invoke(null,  PARAM_ARRAY);
      }
    } catch(Exception e) {
	LogLog.warn("Level deserialization failed, reverting to default.", e);
	level = Level.toLevel(p);
    }
  }
  private void readObject(ObjectInputStream ois)
                        throws java.io.IOException, ClassNotFoundException {
    ois.defaultReadObject();
    readLevel(ois);
    if(locationInfo == null)
      locationInfo = new LocationInfo(null, null);
  }
  private
  void writeObject(ObjectOutputStream oos) throws java.io.IOException {
    this.getThreadName();
    this.getRenderedMessage();
    this.getNDC();
    this.getMDCCopy();
    this.getThrowableStrRep();
    oos.defaultWriteObject();
    writeLevel(oos);
  }
  private
  void writeLevel(ObjectOutputStream oos) throws java.io.IOException {
    oos.writeInt(level.toInt());
    Class clazz = level.getClass();
    if(clazz == Level.class) {
      oos.writeObject(null);
    } else {
      oos.writeObject(clazz.getName());
    }
  }
  public final void setProperty(final String propName,
                          final String propValue) {
        if (mdcCopy == null) {
            getMDCCopy();
        }
        if (mdcCopy == null) {
            mdcCopy = new Hashtable();
        }
        mdcCopy.put(propName, propValue);      
  }
    public final String getProperty(final String key) {
        Object value = getMDC(key);
        String retval = null;
        if (value != null) {
            retval = value.toString();
        }
        return retval;
    }
    public final boolean locationInformationExists() {
      return (locationInfo != null);
    }
    public final long getTimeStamp() {
      return timeStamp;
    }
    public Set getPropertyKeySet() {
      return getProperties().keySet();
    }
    public Map getProperties() {
      getMDCCopy();
      Map properties;
      if (mdcCopy == null) {
         properties = new HashMap();
      } else {
         properties = mdcCopy;
      }
      return Collections.unmodifiableMap(properties);
    }
    public String getFQNOfLoggerClass() {
      return fqnOfCategoryClass;
    }
}
