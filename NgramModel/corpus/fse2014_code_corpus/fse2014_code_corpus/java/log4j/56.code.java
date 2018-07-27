package org.apache.log4j;
import org.apache.log4j.spi.AppenderAttachable;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.HierarchyEventListener;
import org.apache.log4j.helpers.NullEnumeration;
import org.apache.log4j.helpers.AppenderAttachableImpl;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Vector;
public class Category implements AppenderAttachable {
  protected String   name;
  volatile protected Level level;
  volatile protected Category parent;
  private static final String FQCN = Category.class.getName();
  protected ResourceBundle resourceBundle;
  protected LoggerRepository repository;
  AppenderAttachableImpl aai;
  protected boolean additive = true;
  protected
  Category(String name) {
    this.name = name;
  }
  synchronized
  public
  void addAppender(Appender newAppender) {
    if(aai == null) {
      aai = new AppenderAttachableImpl();
    }
    aai.addAppender(newAppender);
    repository.fireAddAppenderEvent(this, newAppender);
  }
  public
  void assertLog(boolean assertion, String msg) {
    if(!assertion)
      this.error(msg);
  }
  public
  void callAppenders(LoggingEvent event) {
    int writes = 0;
    for(Category c = this; c != null; c=c.parent) {
      synchronized(c) {
	if(c.aai != null) {
	  writes += c.aai.appendLoopOnAppenders(event);
	}
	if(!c.additive) {
	  break;
	}
      }
    }
    if(writes == 0) {
      repository.emitNoAppenderWarning(this);
    }
  }
  synchronized
  void closeNestedAppenders() {
    Enumeration enumeration = this.getAllAppenders();
    if(enumeration != null) {
      while(enumeration.hasMoreElements()) {
	Appender a = (Appender) enumeration.nextElement();
	if(a instanceof AppenderAttachable) {
	  a.close();
	}
      }
    }
  }
  public
  void debug(Object message) {
    if(repository.isDisabled(Level.DEBUG_INT))
      return;
    if(Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(FQCN, Level.DEBUG, message, null);
    }
  }
  public
  void debug(Object message, Throwable t) {
    if(repository.isDisabled(Level.DEBUG_INT))
      return;
    if(Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.DEBUG, message, t);
  }
  public
  void error(Object message) {
    if(repository.isDisabled(Level.ERROR_INT))
      return;
    if(Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.ERROR, message, null);
  }
  public
  void error(Object message, Throwable t) {
    if(repository.isDisabled(Level.ERROR_INT))
      return;
    if(Level.ERROR.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.ERROR, message, t);
  }
  public
  static
  Logger exists(String name) {
    return LogManager.exists(name);
  }
  public
  void fatal(Object message) {
    if(repository.isDisabled(Level.FATAL_INT))
      return;
    if(Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.FATAL, message, null);
  }
  public
  void fatal(Object message, Throwable t) {
    if(repository.isDisabled(Level.FATAL_INT))
      return;
    if(Level.FATAL.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.FATAL, message, t);
  }
  protected
  void forcedLog(String fqcn, Priority level, Object message, Throwable t) {
    callAppenders(new LoggingEvent(fqcn, this, level, message, t));
  }
  public
  boolean getAdditivity() {
    return additive;
  }
  synchronized
  public
  Enumeration getAllAppenders() {
    if(aai == null)
      return NullEnumeration.getInstance();
    else
      return aai.getAllAppenders();
  }
  synchronized
  public
  Appender getAppender(String name) {
     if(aai == null || name == null)
      return null;
     return aai.getAppender(name);
  }
  public
  Level getEffectiveLevel() {
    for(Category c = this; c != null; c=c.parent) {
      if(c.level != null)
	return c.level;
    }
    return null; 
  }
  public
  Priority getChainedPriority() {
    for(Category c = this; c != null; c=c.parent) {
      if(c.level != null)
	return c.level;
    }
    return null; 
  }
  public
  static
  Enumeration getCurrentCategories() {
    return LogManager.getCurrentLoggers();
  }
  public
  static
  LoggerRepository getDefaultHierarchy() {
    return LogManager.getLoggerRepository();
  }
  public
  LoggerRepository  getHierarchy() {
    return repository;
  }
  public
  LoggerRepository  getLoggerRepository() {
    return repository;
  }
  public
  static
  Category getInstance(String name) {
    return LogManager.getLogger(name);
  }
  public
  static
  Category getInstance(Class clazz) {
    return LogManager.getLogger(clazz);
  }
  public
  final
  String getName() {
    return name;
  }
  final
  public
  Category getParent() {
    return this.parent;
  }
  final
  public
  Level getLevel() {
    return this.level;
  }
  final
  public
  Level getPriority() {
    return this.level;
  }
  final
  public
  static
  Category getRoot() {
    return LogManager.getRootLogger();
  }
  public
  ResourceBundle getResourceBundle() {
    for(Category c = this; c != null; c=c.parent) {
      if(c.resourceBundle != null)
	return c.resourceBundle;
    }
    return null;
  }
  protected
  String getResourceBundleString(String key) {
    ResourceBundle rb = getResourceBundle();
    if(rb == null) {
      return null;
    }
    else {
      try {
	return rb.getString(key);
      }
      catch(MissingResourceException mre) {
	error("No resource is associated with key \""+key+"\".");
	return null;
      }
    }
  }
  public
  void info(Object message) {
    if(repository.isDisabled(Level.INFO_INT))
      return;
    if(Level.INFO.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.INFO, message, null);
  }
  public
  void info(Object message, Throwable t) {
    if(repository.isDisabled(Level.INFO_INT))
      return;
    if(Level.INFO.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.INFO, message, t);
  }
  public
  boolean isAttached(Appender appender) {
    if(appender == null || aai == null)
      return false;
    else {
      return aai.isAttached(appender);
    }
  }
  public
  boolean isDebugEnabled() {
    if(repository.isDisabled( Level.DEBUG_INT))
      return false;
    return Level.DEBUG.isGreaterOrEqual(this.getEffectiveLevel());
  }
  public
  boolean isEnabledFor(Priority level) {
    if(repository.isDisabled(level.level))
      return false;
    return level.isGreaterOrEqual(this.getEffectiveLevel());
  }
  public
  boolean isInfoEnabled() {
    if(repository.isDisabled(Level.INFO_INT))
      return false;
    return Level.INFO.isGreaterOrEqual(this.getEffectiveLevel());
  }
  public
  void l7dlog(Priority priority, String key, Throwable t) {
    if(repository.isDisabled(priority.level)) {
      return;
    }
    if(priority.isGreaterOrEqual(this.getEffectiveLevel())) {
      String msg = getResourceBundleString(key);
      if(msg == null) {
	msg = key;
      }
      forcedLog(FQCN, priority, msg, t);
    }
  }
  public
  void l7dlog(Priority priority, String key,  Object[] params, Throwable t) {
    if(repository.isDisabled(priority.level)) {
      return;
    }
    if(priority.isGreaterOrEqual(this.getEffectiveLevel())) {
      String pattern = getResourceBundleString(key);
      String msg;
      if(pattern == null)
	msg = key;
      else
	msg = java.text.MessageFormat.format(pattern, params);
      forcedLog(FQCN, priority, msg, t);
    }
  }
  public
  void log(Priority priority, Object message, Throwable t) {
    if(repository.isDisabled(priority.level)) {
      return;
    }
    if(priority.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, priority, message, t);
  }
  public
  void log(Priority priority, Object message) {
    if(repository.isDisabled(priority.level)) {
      return;
    }
    if(priority.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, priority, message, null);
  }
  public
  void log(String callerFQCN, Priority level, Object message, Throwable t) {
    if(repository.isDisabled(level.level)) {
      return;
    }
    if(level.isGreaterOrEqual(this.getEffectiveLevel())) {
      forcedLog(callerFQCN, level, message, t);
    }
  }
   private void fireRemoveAppenderEvent(final Appender appender) {
       if (appender != null) {
         if (repository instanceof Hierarchy) {
           ((Hierarchy) repository).fireRemoveAppenderEvent(this, appender);
         } else if (repository instanceof HierarchyEventListener) {
             ((HierarchyEventListener) repository).removeAppenderEvent(this, appender);
         }
       }
   }
  synchronized
  public
  void removeAllAppenders() {
    if(aai != null) {
      Vector appenders = new Vector();
      for (Enumeration iter = aai.getAllAppenders(); iter != null && iter.hasMoreElements();) {
          appenders.add(iter.nextElement());
      }
      aai.removeAllAppenders();
      for(Enumeration iter = appenders.elements(); iter.hasMoreElements();) {
          fireRemoveAppenderEvent((Appender) iter.nextElement());
      }
      aai = null;
    }
  }
  synchronized
  public
  void removeAppender(Appender appender) {
    if(appender == null || aai == null)
      return;
    boolean wasAttached = aai.isAttached(appender);
    aai.removeAppender(appender);
    if (wasAttached) {
        fireRemoveAppenderEvent(appender);
    }
  }
  synchronized
  public
  void removeAppender(String name) {
    if(name == null || aai == null) return;
    Appender appender = aai.getAppender(name);
    aai.removeAppender(name);
    if (appender != null) {
        fireRemoveAppenderEvent(appender);
    }
  }
  public
  void setAdditivity(boolean additive) {
    this.additive = additive;
  }
  final
  void setHierarchy(LoggerRepository repository) {
    this.repository = repository;
  }
  public
  void setLevel(Level level) {
    this.level = level;
  }
  public
  void setPriority(Priority priority) {
    this.level = (Level) priority;
  }
  public
  void setResourceBundle(ResourceBundle bundle) {
    resourceBundle = bundle;
  }
  public
  static
  void shutdown() {
    LogManager.shutdown();
  }
  public
  void warn(Object message) {
    if(repository.isDisabled( Level.WARN_INT))
      return;
    if(Level.WARN.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.WARN, message, null);
  }
  public
  void warn(Object message, Throwable t) {
    if(repository.isDisabled(Level.WARN_INT))
      return;
    if(Level.WARN.isGreaterOrEqual(this.getEffectiveLevel()))
      forcedLog(FQCN, Level.WARN, message, t);
  }
}
